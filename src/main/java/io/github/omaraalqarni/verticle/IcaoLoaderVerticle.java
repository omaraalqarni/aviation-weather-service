package io.github.omaraalqarni.verticle;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IcaoLoaderVerticle extends AbstractVerticle {

  public static JsonObject icaoObj;
  private static final Logger LOGGER = LoggerFactory.getLogger(IcaoLoaderVerticle.class);


  @Override
  public void start(Promise<Void> startPromise) {

    String icaoPath = "src/main/resources/airports_by_icao.json";

    ConfigStoreOptions icaoStore = new ConfigStoreOptions()
      .setType("file")
      .setFormat("json")
      .setConfig(new JsonObject().put("path", icaoPath));

    ConfigRetrieverOptions icaoData = new ConfigRetrieverOptions()
      .addStore(icaoStore);
    ConfigRetriever icaoRetriever = ConfigRetriever.create(vertx, icaoData);


    icaoRetriever.getConfig()
      .onSuccess(config -> {
        icaoObj = config;
        LOGGER.info("Loaded ICAO coordinates from file.");
//        LOGGER.info(icaoObj.getJsonObject("OMDB").encodePrettily());
        startPromise.complete();
      })
      .onFailure(err -> {
        System.err.println("Failed to load ICAO coordinates: " + err.getMessage());
        startPromise.fail(err);
      });

  }
}
