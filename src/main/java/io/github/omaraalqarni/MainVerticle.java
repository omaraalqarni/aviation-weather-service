package io.github.omaraalqarni;

import io.github.omaraalqarni.api.AviationApi;
import io.github.omaraalqarni.api.WeatherApi;
import io.github.omaraalqarni.common.PostgresConnector;
import io.github.omaraalqarni.verticle.AviationVerticle;
import io.github.omaraalqarni.verticle.DatabaseVerticle;
import io.github.omaraalqarni.verticle.WeatherVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgPool;

public class MainVerticle extends AbstractVerticle {
  private final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    PgPool client = PostgresConnector.getClient(vertx);
    AviationApi aviationApi = new AviationApi(vertx);
    WeatherApi weatherApi = new WeatherApi();

    AviationVerticle aviationVerticle = new AviationVerticle(aviationApi);
    DatabaseVerticle dbVerticle = new DatabaseVerticle(client);
    WeatherVerticle weatherVerticle = new WeatherVerticle(weatherApi);

    vertx.deployVerticle(new MainVerticle());

    vertx.deployVerticle(dbVerticle);
    vertx.deployVerticle(aviationVerticle);
    vertx.deployVerticle(weatherVerticle);


  }

  @Override
  public void start()  {
    LOGGER.info("Started");

    Router router = Router.router(vertx);
    router.route("/api/v1/*")
        .subRouter(AviationVerticle.getAviationRouter());
    vertx.createHttpServer(new HttpServerOptions().setLogActivity(true)).requestHandler(router).listen(8888);

  }
}
