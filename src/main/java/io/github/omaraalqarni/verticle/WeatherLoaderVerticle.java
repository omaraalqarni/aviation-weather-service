package io.github.omaraalqarni.verticle;

import io.github.omaraalqarni.api.WeatherApi;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WeatherLoaderVerticle extends AbstractVerticle {

  public static JsonObject weatherObj;
  private static final Logger LOGGER = LoggerFactory.getLogger(WeatherLoaderVerticle.class);


  @Override
  public void start(Promise<Void> startPromise) {
    LOGGER.info("WeatherLoaderVerticle Started");
    String airportsWeatherPath = "src/main/resources/airports_weather.json";

    ConfigStoreOptions icaoStore = new ConfigStoreOptions()
      .setType("file")
      .setFormat("json")
      .setConfig(new JsonObject().put("path", airportsWeatherPath));

    ConfigRetrieverOptions icaoData = new ConfigRetrieverOptions()
      .addStore(icaoStore);
    ConfigRetriever icaoRetriever = ConfigRetriever.create(vertx, icaoData);


    icaoRetriever.getConfig()
      .onSuccess(config -> {
        weatherObj = config;
//        updateWeatherDb();
        weatherRefresh();
        LOGGER.info("Loaded weather data from file.");
        startPromise.complete();
      })
      .onFailure(err -> {
        System.err.println("Failed to load weather data coordinates: " + err.getMessage());
        startPromise.fail(err);
      });
  }

  void weatherRefresh() {
    LOGGER.info("Updating weather data");
    //TODO include date
    LocalDateTime expiryTime = LocalDateTime.now().minusHours(3);
    String dataTime = weatherObj.getJsonObject("OMDB").getString("time", "");

    if (dataTime.isEmpty()) {
      LOGGER.info("Weather time not set. updating...");
      updateWeatherDb();
      return;
    }

    try {
      LocalDateTime weatherDate = LocalDateTime.parse(dataTime);
      if (weatherDate.isBefore(expiryTime)) {
        updateWeatherDb();
      }
    } catch (Exception e) {
      LOGGER.info("Invalid time format. Triggering update.");
      updateWeatherDb();
    }
  }


  private void updateWeatherDb() {
    LOGGER.info("updating DB weather data");
    LocalDateTime currentTime = LocalDateTime.now();

    List<Future> futures = new ArrayList<>();

    for (String icao : weatherObj.fieldNames()) {
      LOGGER.info("Updating weather for icao: {}", icao);
      JsonObject currentIcaoObj = weatherObj.getJsonObject(icao);
      Double lat = currentIcaoObj.getDouble("lat");
      Double lon = currentIcaoObj.getDouble("lon");

      Future<Void> fetchFuture = new WeatherApi(vertx).fetchWeatherDataOpenWeatherAPI(lat, lon)
        .onSuccess(weatherFetched -> {
          LOGGER.info("Fetched weather for {}", icao);
          currentIcaoObj.put("weather", weatherFetched);
          currentIcaoObj.put("time", currentTime.toString());
        })
        .onFailure(err -> LOGGER.info("Failed to fetch weather for {}", icao))
        .mapEmpty(); // Convert to Future<Void> for CompositeFuture

      futures.add(fetchFuture);
    }

    CompositeFuture.all(futures).onComplete(ar -> {
      writeWeatherData();
      LOGGER.info("All weather updates complete and written to file.");
    });
  }


  private void writeWeatherData() {
    try {
      String updatedJson = weatherObj.encodePrettily();
      Files.write(Paths.get("src/main/resources/airports_weather.json"), updatedJson.getBytes());
    } catch (IOException e) {
      LOGGER.error("Failed to persist weather data", e);
    }
    LOGGER.info("Updated weather db");
  }


  @Override
  public void stop() {

  }
}
