package io.github.omaraalqarni.service.impl;

import io.github.omaraalqarni.common.EventBusAddresses;
import io.github.omaraalqarni.service.AviationService;
import io.github.omaraalqarni.verticle.AviationVerticle;
import io.github.omaraalqarni.verticle.WeatherLoaderVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class AviationServiceImpl implements AviationService {
  private final Logger LOGGER = LoggerFactory.getLogger(AviationVerticle.class);
  private final EventBus eventBus;
  public static Map<String, Future<JsonObject>> weatherCache = new HashMap<>();

  public AviationServiceImpl(EventBus eventBus) {
    this.eventBus = eventBus;
  }


  @Override
  public Future<JsonObject> processAllFlights(JsonArray flights) {
    Set<String> icaoCodes = extractICAOCodes(flights);
    return fetchLatLonBulk(icaoCodes)
      .compose(latLongData -> {
        return attachWeather(flights, latLongData)
          .map(enrichedFlights -> {
            LOGGER.info("Enriched flights with weather data successfully");
            JsonObject groupedFlights = filterFlightsByDay(enrichedFlights);
            return groupedFlights;
          });
      });
  }


  private Future<JsonObject> fetchLatLonBulk(Set<String> iataCodes) {
    return eventBus
      .request(EventBusAddresses.GET_LAT_LON_BULK, new JsonArray(new ArrayList<>(iataCodes)))
      .map(reply -> JsonObject.mapFrom(reply.body()))
      .recover(err -> {
        LOGGER.info("Failed to fetch Lat Lon");
        // Handle fallback if necessary
        JsonObject error = new JsonObject()
          .put("latlon_lookup_failed", true)
          .put("error", new JsonObject()
            .put("error_source", "DB")
            .put("error_code", 500)
            .put("error_message", err.getMessage()));
        return Future.succeededFuture(error);
      });
  }

  private Set<String> extractICAOCodes(JsonArray flights) {
    Set<String> icaoSet = flights.stream()
      .map(obj -> ((JsonObject) obj).getJsonObject("arrival").getString("icao"))
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
    LOGGER.info(icaoSet);
    return icaoSet;
  }


  private Future<JsonArray> attachWeather(JsonArray flights, JsonObject latLongData) {
    List<Future> futures = new ArrayList<>();

    LOGGER.info("Enriching flights with weather...");
    boolean latlonLookupFailed = latLongData.containsKey("latlon_lookup_failed");
    for (int i = 0; i < flights.size(); i++) {
      JsonObject flight = flights.getJsonObject(i).copy();
      JsonObject arrival = flight.getJsonObject("arrival");
      String icao = arrival.getString("icao");


      // Json file lookup failed entirely or icao not found
      if (icao == null || icao.isBlank()) {
        arrival.put("weather", createWeatherError(400, "Missing ICAO code"));
        futures.add(Future.succeededFuture(flight));
        continue;
      }

      if (latlonLookupFailed) {
        arrival.put("weather", createWeatherError(500, latLongData.getJsonObject("error").getString("error_message")));
        futures.add(Future.succeededFuture(flight));
        continue;
      }

      if (!latLongData.containsKey(icao)) {
        arrival.put("weather", createWeatherError(404, "ICAO code not found in database"));
        futures.add(Future.succeededFuture(flight));
        continue;
      }

      JsonObject latLon = latLongData.getJsonObject(icao);
      Future<JsonObject> weatherFuture = weatherCache.computeIfAbsent(icao, k -> {
        JsonObject fromFile = WeatherLoaderVerticle.weatherObj.getJsonObject(icao);

        if (fromFile != null && fromFile.containsKey("weather")) {
          LOGGER.info(String.format("Using weather from local file cache for ICAO %s", icao));
          return Future.succeededFuture(new JsonObject()
            .put("success", true)
            .put("source", "json")
            .put("result", fromFile.getJsonObject("weather"))
            .put("errors", new JsonArray()));
        }
          return eventBus.<JsonObject>request(EventBusAddresses.GET_WEATHER_DATA_API, latLon)
            .map(resp -> new JsonObject()
              .put("success", true)
              .put("source", "api")
              .put("result", resp.body())
              .put("errors", new JsonArray()));
//            .recover(apiErr -> {
//                LOGGER.info("Weather data not available in API, retrieving from DB ");
//                return eventBus.<JsonObject>request(EventBusAddresses.GET_WEATHER_DATA_DB, latLon)
//                  .map(dbResp -> new JsonObject()
//                    .put("success", true)
//                    .put("source", "db")
//                    .put("result", dbResp.body())
//                    .put("errors", new JsonArray().add(
//                      new JsonObject()
//                        .put("error_source", "api.openweathermap.org/data/2.5/weather")
//                        .put("error_code", 500)
//                        .put("error_message", apiErr.getMessage())
//                    ))
//                  );
//              }
//            );
        }
      );

      Future<JsonObject> enrichedFlight = weatherFuture.map(weather -> {
        arrival.put("weather", weather);
        return flight;
      });
      futures.add(enrichedFlight);
    }
    return CompositeFuture.all(futures).map(compositeFuture -> {
      JsonArray enrichedFlights = new JsonArray();
      compositeFuture.list().forEach(enrichedFlights::add);
      return enrichedFlights;
    });
  }



  private JsonObject createWeatherError(int code, String message) {
    return new JsonObject()
      .put("success", false)
      .put("source", "api")
      .put("result", new JsonObject())
      .put("errors", new JsonArray().add(
        new JsonObject()
          .put("error", "Failed to convert ICAO to coordinates")
          .put("error_source", "DB")
          .put("error_code", code)
          .put("error_message", message)
      ));
  }


  public JsonObject parseResponse(JsonObject res, JsonObject groupedData) {
    JsonObject template = new JsonObject();
    template.put("success", true);
    template.put("source", "api");

    JsonObject dataObj = new JsonObject();
    dataObj.put("pagination", res.getJsonObject("pagination"));
    dataObj.put("data", groupedData);

    template.put("result", new JsonArray().add(dataObj));
    template.put("errors", "");

    return template;
  }

  public JsonObject filterFlightsByDay(JsonArray data) {
    LocalDate todayDate = LocalDate.now();
    LocalDate yesterdayDate = todayDate.minusDays(1);
    LocalDate tomorrowDate = todayDate.plusDays(1);

    JsonArray tomorrow = new JsonArray();
    JsonArray today = new JsonArray();
    JsonArray yesterday = new JsonArray();

    for (int i = 0; i < data.size(); i++) {
      JsonObject flight = data.getJsonObject(i);
      String date = flight.getString("flight_date");
      if (date == null) {
        continue;
      }
      if (date.equals(todayDate.toString())) {
        today.add(flight);
      } else if (date.equals(tomorrowDate.toString())) {
        tomorrow.add(flight);
      } else if (date.equals(yesterdayDate.toString()) && yesterday.size() < 10) {
        yesterday.add(flight);
      }
    }

    JsonObject grouped = new JsonObject();
    grouped.put("tomorrow", tomorrow);
    grouped.put("today", today);
    grouped.put("yesterday", yesterday);
    return grouped;
  }

  public void saveWeatherDataToDb(JsonObject data) {

    data.getJsonArray("yesterday", new JsonArray())
      .forEach(flightData -> doSaveWeatherData((JsonObject) flightData));
    data.getJsonArray("today", new JsonArray())
      .forEach(flightData -> doSaveWeatherData((JsonObject) flightData));
    data.getJsonArray("tomorrow", new JsonArray())
      .forEach(flightData -> doSaveWeatherData((JsonObject) flightData));
  }


  private void doSaveWeatherData(JsonObject flight) {
    JsonObject weatherData = flight
      .getJsonObject("arrival", new JsonObject())
      .getJsonObject("weather");
    Double lat = weatherData.getDouble("lat");
    Double lon = weatherData.getDouble("lon");
    JsonObject toBeSaved = new JsonObject()
      .put("lat", lat)
      .put("lon", lon)
      .put("weather_data", weatherData);
    eventBus.<JsonObject>request(EventBusAddresses.SAVE_WEATHER_DATA, toBeSaved);
  }
}
