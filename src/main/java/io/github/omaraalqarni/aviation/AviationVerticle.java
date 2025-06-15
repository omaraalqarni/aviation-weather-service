package io.github.omaraalqarni.aviation;

import io.github.omaraalqarni.aviation.impl.AviationServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.atomic.AtomicReference;

public class AviationVerticle extends AbstractVerticle {
  private final Logger LOGGER = LoggerFactory.getLogger(AviationVerticle.class);

  private static Router aviationRouter;
  private final AviationService aviationService;
  private final AviationApi aviationApi;

  public AviationVerticle(AviationApi aviationApi) {
    this.aviationApi = aviationApi;
    this.aviationService = new AviationServiceImpl();
  }

  @Override
  public void start() {
    aviationRouter = Router.router(vertx);
    LOGGER.info("AviationVerticle Started");


    aviationRouter.get("/flights").handler(this::getAllFlights);
  }

  public void getAllFlights(RoutingContext ctx){
      LOGGER.info("Flights endpoint here");

      String flightStatus = ctx.queryParam("flight_status").getFirst();
      String limit = ctx.queryParam("limit").getFirst();
      String offset = ctx.queryParam("offset").getFirst();
      Future<JsonObject> flights = aviationApi.fetchFlights(flightStatus, limit, offset);
      flights.onSuccess(res -> {
          JsonObject parsedResponse = aviationService.parseResponse(res);
          LOGGER.info("HERE");
          ctx.response().setStatusCode(200).end(parsedResponse.encodePrettily());
        }
        );
      flights.onFailure(res -> {
          ctx.response().end(String.format("Error: %s",res.toString()));
      });


  }

  //TODO: will be more reusable later
  void CustomError (RoutingContext ctx, String value){
    ctx.response().setStatusCode(404).end(String.format("Error, %s cannot be nullable", value));
  }

  public static Router getAviationRouter() {
    return aviationRouter;
  }

  @Override
  public void stop()  {

  }
}
