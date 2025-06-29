package io.github.omaraalqarni.verticle;

import io.github.omaraalqarni.api.WeatherApi;
import io.github.omaraalqarni.common.EventBusAddresses;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(WeatherVerticle.class);
  private final WeatherApi weatherApi;

  public WeatherVerticle(WeatherApi weatherApi) {
    this.weatherApi = weatherApi;
  }

  @Override
  public void start() {

    vertx.eventBus().consumer(EventBusAddresses.GET_WEATHER_DATA_API, message -> {
//      logger.info("Consumed GET_WEATHER_DATA");
      JsonObject body = (JsonObject) message.body();
      double lat = body.getDouble("lat");
      double lon = body.getDouble("lon");
      logger.info("Started fetching from weather API");


      CircuitBreaker.create("weather-circuit-breaker", vertx,
        new CircuitBreakerOptions().setMaxFailures(1).setTimeout(3000))
        .fallback(promise -> {

        return null;
      }).execute(promise -> {

      });



      weatherApi.fetchWeatherDataWeatherAPI(lat, lon)
        .onSuccess(message::reply)
        .onFailure(error -> {
          logger.error("Failed to fetch weather data. Error: {}", error.getMessage());
          message.reply(new JsonObject().put("error", error.getMessage()));
        });
    });

  }


  @Override
  public void stop() throws Exception {
    super.stop();
  }
}
