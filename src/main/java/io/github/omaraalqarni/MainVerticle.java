package io.github.omaraalqarni;

import io.github.omaraalqarni.aviation.AviationApi;
import io.github.omaraalqarni.aviation.AviationService;
import io.github.omaraalqarni.aviation.AviationVerticle;
import io.github.omaraalqarni.aviation.impl.AviationServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {
  private final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start()  {

    Vertx vertx = Vertx.vertx();
    AviationApi aviationApi = new AviationApi(vertx);
    AviationService aviationService = new AviationServiceImpl();
    AviationVerticle aviationVerticle = new AviationVerticle(aviationApi);
    vertx.deployVerticle(new MainVerticle());
    vertx.deployVerticle(aviationVerticle);
    LOGGER.info("Started");

    Router router = Router.router(vertx);
    router.route("/api/v1/*")
        .subRouter(AviationVerticle.getAviationRouter());
    vertx.createHttpServer(new HttpServerOptions().setLogActivity(true)).requestHandler(router).listen(8888);

  }
}
