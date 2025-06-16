package io.github.omaraalqarni.verticle;

import io.github.omaraalqarni.api.AviationApi;
import io.github.omaraalqarni.service.AviationService;
import io.github.omaraalqarni.service.impl.AviationServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

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

    String flightStatus = ctx.queryParams().get("flight_status");
    String limit = ctx.queryParams().get("limit");
    String offset = ctx.queryParams().get("offset");

    Future<JsonObject> flights = aviationApi.fetchFlights(flightStatus, offset, limit);
    flights.onSuccess(res -> {
        JsonObject parsedResponse = aviationService.parseResponse(res);
        LOGGER.info("HERE");
        ctx.response().setStatusCode(200).end(parsedResponse.encodePrettily());
      }
    );
    flights.onFailure(err -> {
      var gg= err.getMessage();
      ctx.response().setStatusCode(500).end(String.format("Error: %s",gg));
    });





  }


  public static Router getAviationRouter() {
    return aviationRouter;
  }

  @Override
  public void stop()  {

  }
}
