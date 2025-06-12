package io.github.omaraalqarni.aviation;

import io.github.omaraalqarni.aviation.impl.AviationServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class AviationVerticle extends AbstractVerticle {
  private final Logger LOGGER = LoggerFactory.getLogger(AviationVerticle.class);

  private static Router subRouter;
  private AviationService aviationService;

  @Override
  public void start() {
    subRouter = Router.router(vertx);
    LOGGER.info("AviationVerticle Started");


    subRouter.get("/flights").handler(this::getAllFlights);
  }

  public void getAllFlights(RoutingContext ctx){
//    TODO: get the params for limit and offset
      String JsonResponse =
    LOGGER.info("Flights endpoint here");
    ctx.response().end("Hello Aviation Verticle");

  }

  @Override
  public void stop()  {

  }
  public static Router getSubRouter() {
    return subRouter;
  }
}
