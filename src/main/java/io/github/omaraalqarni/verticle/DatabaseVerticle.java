package io.github.omaraalqarni.verticle;

import io.github.omaraalqarni.common.EventBusAddresses;
import io.github.omaraalqarni.service.DBService;
import io.github.omaraalqarni.service.impl.DBServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
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

    vertx.eventBus().<JsonArray>consumer(EventBusAddresses.GET_LAT_LONG_BULK, message -> {
      logger.info("Consumed GET_LAT_LONG");
      JsonArray icaoCodes = message.body();
      logger.info(String.format("ICAO Codes: \n%s",icaoCodes.encodePrettily()));

      if (icaoCodes.isEmpty()) {
        logger.info("icao is null or empty");
        message.fail(400, "Missing 'icao_code' in database query request.");
        return;
      }

      dbService.getAirportCoordinates(icaoCodes)
        .onSuccess(message::reply)
        .onFailure(message::reply);

    });

  }


  @Override
  public void stop() throws Exception {
    super.stop();
  }
}
