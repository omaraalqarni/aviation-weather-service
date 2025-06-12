package io.github.omaraalqarni;

import io.github.omaraalqarni.aviation.AviationService;
import io.github.omaraalqarni.aviation.AviationVerticle;
import io.github.omaraalqarni.aviation.impl.AviationServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {
  private final Logger LOGGGER = LoggerFactory.getLogger(MainVerticle.class);
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
    vertx.deployVerticle(new AviationVerticle());
  }
  @Override
  public void start()  {
    LOGGGER.info("Started");
    Router router = Router.router(vertx);
    router.mountSubRouter("/api/v1/",AviationVerticle.getSubRouter());
    vertx.createHttpServer(new HttpServerOptions().setLogActivity(true)).requestHandler(router).listen(8888);

  }
}
