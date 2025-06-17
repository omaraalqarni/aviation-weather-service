package io.github.omaraalqarni.service.impl;

import io.github.omaraalqarni.common.EventBusAddresses;
import io.github.omaraalqarni.service.AviationService;
import io.github.omaraalqarni.verticle.AviationVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AviationServiceImpl implements AviationService {
  private final Logger LOGGER = LoggerFactory.getLogger(AviationVerticle.class);
  private final EventBus eventBus;

  public AviationServiceImpl(EventBus eventBus) {
    this.eventBus = eventBus;
  }



  @Override
  public Future<JsonObject> processAllFlights(JsonArray flights) {
    Set<String> icaoCodes = extractICAOCodes(flights);
//    LOGGER.info(flights);
    return fetchLatLonBulk(icaoCodes).compose(icaoToCoords -> {
      LOGGER.info(String.format("icaotocoords: \n%s",icaoToCoords));
      return attachWeather(flights, icaoToCoords).map(this::filterFlightsByDay);
      }

    );
  }

  private Set<String> extractICAOCodes(JsonArray flights) {
    var f = flights.stream()
      .map(obj -> ((JsonObject) obj).getJsonObject("arrival").getString("icao"))
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
    LOGGER.info(f);
    return f;
  }

  private Future<JsonObject> fetchLatLonBulk(Set<String> iataCodes) {
    return eventBus
      .request(EventBusAddresses.GET_LAT_LONG_BULK, new JsonArray(new ArrayList<>(iataCodes)))
      .map(reply -> JsonObject.mapFrom(reply.body()))
      .recover(err -> {
        LOGGER.info("Reached recover in fetchLatLonBulk");
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

  private Future<JsonArray> attachWeather(JsonArray flights, JsonObject icaoToCoords) {
    List<Future> futures = new ArrayList<>();
    LOGGER.info("In attachWeather");
    boolean latlonLookupFailed = icaoToCoords.containsKey("latlon_lookup_failed");
    for (int i = 0; i < flights.size(); i++) {
      JsonObject flight = flights.getJsonObject(i).copy();
      JsonObject arrival = flight.getJsonObject("arrival");
      String icao = arrival.getString("icao");


      // DB lookup failed entirely or icao not found
      if (icao == null || icao.isBlank()) {
        arrival.put("weather", createWeatherError(400, "Missing ICAO code"));
        futures.add(Future.succeededFuture(flight));
        continue;
      }

      if (latlonLookupFailed) {
        arrival.put("weather", createWeatherError(500, icaoToCoords.getJsonObject("error").getString("error_message")));
        futures.add(Future.succeededFuture(flight));
        continue;
      }

      if (!icaoToCoords.containsKey(icao)) {
        arrival.put("weather", createWeatherError(404, "ICAO code not found in database"));
        futures.add(Future.succeededFuture(flight));
        continue;
      }

      JsonObject latLong = icaoToCoords.getJsonObject(icao);
      LOGGER.info(latLong);

      Future<JsonObject> future = eventBus.<JsonObject>request(EventBusAddresses.GET_WEATHER_DATA, latLong)
        .map(resp -> {
          LOGGER.info("Requested from GET_WEATHER_DATA");
          JsonObject weatherPayload = new JsonObject()
            .put("success", true)
            .put("source", "api")
            .put("result", resp.body())
            .put("errors", new JsonArray());

          arrival.put("weather", weatherPayload);
          return flight;
        })
        .recover(err -> {
          JsonObject weatherError = new JsonObject()
            .put("success", false)
            .put("source", "api")
            .put("result", new JsonObject())
            .put("errors", new JsonArray().add(new JsonObject()
              .put("error_source", "api.openweathermap.org/data/2.5/weather")
              .put("error_code", 500)
              .put("error_message", err.getMessage())
            ));

          arrival.put("weather", weatherError);
          return Future.succeededFuture(flight);
        });

      futures.add(future);
    }

    return CompositeFuture.all(futures).map(cf -> {
      JsonArray enrichedFlights = new JsonArray();
      cf.list().forEach(enrichedFlights::add);
      return enrichedFlights;
    });
  }

  private JsonObject createWeatherError(int code, String message) {
    return new JsonObject()
      .put("success", false)
      .put("source", "DB".toLowerCase())
      .put("result", new JsonObject())
      .put("errors", new JsonArray().add(
        new JsonObject()
          .put("error_source", "DB")
          .put("error_code", code)
          .put("error_message", message)
      ));
  }



  public JsonObject parseResponse(JsonObject res, JsonObject groupedData) {
    JsonObject template = new JsonObject();
    template.put("success", true); // hardcoded for now
    template.put("source", "api"); //or db



    JsonObject dataObj = new JsonObject();
    dataObj.put("pagination", res.getJsonObject("pagination"));
    dataObj.put("data", groupedData);


    template.put("result", new JsonArray().add(dataObj));
    template.put("errors","");

    return template;
  }

  public JsonObject filterFlightsByDay(JsonArray data) {
    LocalDate todayDate = LocalDate.now();
    LocalDate yesterdayDate = todayDate.minusDays(1);

    JsonArray today = new JsonArray();
    JsonArray yesterday = new JsonArray();

    for (int i = 0; i < data.size(); i++) {
      JsonObject flight = data.getJsonObject(i);
      String date = flight.getString("flight_date");
      if (date == null){
        continue;
      }

       if (date.equals(yesterdayDate.toString()) && yesterday.size() < 10) {
        yesterday.add(flight);
      } else{
        today.add(flight);
       }
    }

    JsonObject grouped = new JsonObject();
    grouped.put("today", today);
    grouped.put("yesterday", yesterday);
    return grouped;
  }



}
