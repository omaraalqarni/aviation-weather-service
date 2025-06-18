package io.github.omaraalqarni.verticle;

import io.github.omaraalqarni.common.EventBusAddresses;
import io.github.omaraalqarni.service.DBService;
import io.github.omaraalqarni.service.impl.DBServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseVerticle extends AbstractVerticle {
  private DBService dbService;
  private final PgPool pgPool;
  private final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);


  public DatabaseVerticle(PgPool pgPool) {
    this.pgPool = pgPool;
  }

  @Override
  public void start() {
    this.dbService = new DBServiceImpl(pgPool);
    logger.info("DB Verticle Started");

    vertx.eventBus().<JsonArray>consumer(EventBusAddresses.GET_LAT_LON_BULK, message -> {
      logger.info("Consumed GET_LAT_LON");
      JsonArray icaoCodes = message.body();
      logger.info(String.format("ICAO Codes: \n%s", icaoCodes.encodePrettily()));

      if (icaoCodes.isEmpty()) {
        logger.info("icao is null or empty");
        message.fail(400, "Missing 'icao_code' in database query request.");
        return;
      }


      dbService.getAirportCoordinates(icaoCodes)
        .onSuccess(message::reply)
        .onFailure(message::reply);

    });
    vertx.eventBus().<JsonObject>consumer(EventBusAddresses.SAVE_WEATHER_DATA, message -> {
      double lat = message.body().getDouble("lat");
      double lon = message.body().getDouble("lon");
      JsonObject weatherData = message.body().getJsonObject("weather_data");

      dbService.saveWeatherData(lat, lon, weatherData)
        .onSuccess(res -> {
          logger.info("Weather data saved to DB");
          message.reply(res);
        })
        .onFailure(err -> {
          String errorMsg = "Failed to save weather: " + err.toString();
          logger.warn(errorMsg);
          message.fail(500, errorMsg);
        });

    });


    vertx.eventBus().<JsonObject>consumer(EventBusAddresses.GET_WEATHER_DATA_DB, message -> {
      double lat = message.body().getDouble("lat");
      double lon = message.body().getDouble("lon");
      dbService.getWeatherDataFromDb(lat, lon)
        .onSuccess(message::reply)
        .onFailure(err -> {
          JsonObject error = new JsonObject()
            .put("error_source", "db")
            .put("error_code", 404)
            .put("error_message", err.getMessage());
          message.fail(404, error.encode());
        });
    });


  }


  @Override
  public void stop() throws Exception {
    super.stop();
  }
}
