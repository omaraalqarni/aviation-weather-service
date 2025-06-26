package io.github.omaraalqarni.api;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WeatherApi {
  private final WebClient webClient;
  private final Logger logger = LoggerFactory.getLogger(WeatherApi.class);

  public WeatherApi(Vertx vertx) {
    this.webClient = WebClient.create(vertx);
  }

  public Future<JsonObject> fetchWeatherDataOpenWeatherAPI(double lat, double lon) {
    Promise<JsonObject> promise = Promise.promise();

    String apiKey = System.getenv("WEATHER_API");
    if (apiKey == null) {
      throw new RuntimeException("OPEN WEATHER_API environment variable is not set");
    }
    webClient.get(443, "api.openweathermap.org", "/data/3.0/onecall")
      .ssl(true)
      .addQueryParam("appid", apiKey)
      .addQueryParam("lat", String.valueOf(lat))
      .addQueryParam("lon", String.valueOf(lon))
      .send(ar -> {
      if (ar.succeeded()) {
        logger.info("Successfully fetched data from OpenWeatherApi");
        promise.complete(ar.result().bodyAsJsonObject());
      } else {
        promise.fail(ar.cause().getMessage());
      }
    });
    return promise.future();
  }

  public Future<JsonObject> fetchWeatherDataWeatherAPI(double lat, double lon) {
    Promise<JsonObject> promise = Promise.promise();
    String q = String.format("%.4f,%.4f", lat,lon);

    String apiKey = System.getenv("WEATHER_API_2");
    if (apiKey == null) {
      throw new RuntimeException("WEATHER API environment variable is not set");
    }
    webClient.get(443, "api.weatherapi.com", "/v1/current.json")
      .ssl(true)
      .addQueryParam("key", apiKey)
      .addQueryParam("q", q)
      .send(ar -> {
      if (ar.succeeded()) {
        logger.info("Successfully fetched data from WeatherApi");
        promise.complete(ar.result().bodyAsJsonObject());
      } else {
        promise.fail(ar.cause().getMessage());
      }
    });
    return promise.future();
  }

}
