package io.github.omaraalqarni.verticle;

import io.github.omaraalqarni.common.EventBusAddresses;
import io.github.omaraalqarni.service.DBService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseVerticle extends AbstractVerticle {
  private final PgPool pgPool;
  private final Logger logger = LoggerFactory.getLogger(DatabaseVerticle.class);


  public DatabaseVerticle(PgPool pgPool) {
    this.pgPool = pgPool;
  }

  @Override
  public void start() {
    logger.info("DB Verticle Started");

    vertx.eventBus().<JsonArray>consumer(EventBusAddresses.GET_LAT_LON_BULK, message -> {
      logger.info("Consumed GET_LAT_LON");
      JsonArray icaoCodes = message.body();

      if (icaoCodes.isEmpty()) {
        logger.info("icao is null or empty");
        message.fail(400, "Missing 'icao_code' in database query request.");
        return;
      }

      JsonObject result = new JsonObject();

      for (int i = 0; i < icaoCodes.size(); i++) {
        String code = icaoCodes.getString(i);
        JsonObject coords = IcaoLoaderVerticle.icaoObj.getJsonObject(code);

        if (coords != null) {
          result.put(code, coords);
        } else {
          logger.warn("Missing ICAO in file: {}", code);
        }
      }
      message.reply(result);
    });

  }


  @Override
  public void stop() throws Exception {
    super.stop();
  }
}
