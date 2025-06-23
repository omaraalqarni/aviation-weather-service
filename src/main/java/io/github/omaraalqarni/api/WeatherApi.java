package io.github.omaraalqarni.api;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WeatherApi {
  private final WebClient webClient;
  private final Logger logger = LoggerFactory.getLogger(WeatherApi.class);

  public WeatherApi(Vertx vertx) {
    this.webClient = WebClient.create(vertx);
  }

  public Future<JsonObject> fetchWeatherData(double lat, double lon) {
    Promise<JsonObject> promise = Promise.promise();

    String apiKey = System.getenv("WEATHER_API");
    if (apiKey == null) {
      throw new RuntimeException("WEATHER_API environment variable is not set");
    }
    HttpRequest<Buffer> request = webClient.get(443, "api.openweathermap.org", "/data/3.0/onecall")
      .ssl(true)
      .addQueryParam("appid", apiKey)
      .addQueryParam("lat", String.valueOf(lat))
      .addQueryParam("lon", String.valueOf(lon));

    request.send(ar -> {
      if (ar.succeeded()) {
        logger.info("Successfully fetched data from WeatherAPI");
        promise.complete(ar.result().bodyAsJsonObject());
      } else {
        promise.fail(ar.cause().getMessage());
      }
    });
    return promise.future();
  }
}
