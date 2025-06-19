package io.github.omaraalqarni.verticle;

import io.github.omaraalqarni.api.AviationApi;
import io.github.omaraalqarni.service.AviationService;
import io.github.omaraalqarni.service.impl.AviationServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;


public class AviationVerticle extends AbstractVerticle {
  private final Logger LOGGER = LoggerFactory.getLogger(AviationVerticle.class);

  private static Router aviationRouter;
  private AviationService aviationService;
  private final AviationApi aviationApi;

  public AviationVerticle(AviationApi aviationApi) {
    this.aviationApi = aviationApi;
  }

  @Override
  public void start() {
    this.aviationService = new AviationServiceImpl(vertx.eventBus());

    aviationRouter = Router.router(vertx);
    LOGGER.info("AviationVerticle Started");

    aviationRouter.get("/flights").handler(this::getAllFlights);

  }

  public void getAllFlights(RoutingContext ctx){
    LOGGER.info("Flights endpoint here");

    String flightStatus = ctx.queryParams().get("flight_status");
    String limit = ctx.queryParams().get("limit");
    String offset = ctx.queryParams().get("offset");


    LOGGER.info("Calling aviation api");
    aviationApi.fetchFlights(flightStatus, offset, limit)
        .compose(res -> {
          LOGGER.info("Calling aviation api");
          JsonArray rawFlights = res.getJsonArray("data");
          return aviationService.processAllFlights(rawFlights)
            .map(grouped -> aviationService.parseResponse(res, grouped));
        })
        .onSuccess(finalResponse -> {
           ctx.response()
            .setStatusCode(200)
            .putHeader("Content-Type", "application/json")
            .end(finalResponse.encodePrettily());
//         aviationService.saveWeatherData();
        })
      .onFailure(err -> {
        LOGGER.info("end aviation api");
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
