package io.github.omaraalqarni.verticle;

import io.github.omaraalqarni.api.WeatherApi;
import io.github.omaraalqarni.common.EventBusAddresses;
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

    vertx.eventBus().consumer(EventBusAddresses.GET_WEATHER_DATA, message -> {
      logger.info("Consumed GET_WEATHER_DATA");
      JsonObject body = (JsonObject) message.body();
      double lat = body.getDouble("lat");
      double lon = body.getDouble("lon");
      logger.info(String.format("Lat: %.4f Long: %.4f", lat,lon));

      weatherApi.fetchWeatherData(lat,lon)
        .onSuccess(message::reply)
        .onFailure(message::reply);
    });


  }


  @Override
  public void stop() throws Exception {
    super.stop();
  }
}
