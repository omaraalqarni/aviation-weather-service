package io.github.omaraalqarni.verticle;

import io.github.omaraalqarni.api.WeatherApi;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;

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
    LocalTime expiryTime = LocalTime.now().minusHours(5);
    String dataTime = weatherObj.getJsonObject("OMDB").getString("time", "");

    if (dataTime.isEmpty()) {
      LOGGER.info("Weather time not set. updating...");
      updateWeatherDb();
      return;
    }

    try {
      LocalTime weatherDate = LocalTime.parse(dataTime);
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

    LocalTime currentTime = LocalTime.now();

    for (String icao : weatherObj.fieldNames()) {
      LOGGER.info("Updating weather for icao: {}", icao);
      JsonObject currentIcaoObj = weatherObj.getJsonObject(icao);
      Double lat = currentIcaoObj.getDouble("lat");
      Double lon = currentIcaoObj.getDouble("lon");

      new WeatherApi(vertx).fetchWeatherData(lat, lon)
        .onSuccess(weatherFetched -> {
          LOGGER.info("Fetched weather for {}", icao);
          writeWeatherData();
          currentIcaoObj.put("weather", weatherFetched);
          currentIcaoObj.put("time", currentTime.toString());
        })
        .onFailure(err -> LOGGER.info("Failed to fetch weather for {}", icao));
    }
  }


  private void writeWeatherData() {
    try {
      String updatedJson = weatherObj.encodePrettily();
      Files.write(Paths.get("src/main/resources/airports_weather.json"), updatedJson.getBytes());
      LOGGER.info("Updated weather db");
    } catch (IOException e) {
      LOGGER.error("Failed to persist weather data", e);
    }
  }


  @Override
  public void stop() {

  }
}
