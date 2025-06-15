package io.github.omaraalqarni.aviation;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class AviationVerticle extends AbstractVerticle {
  private final Logger LOGGER = LoggerFactory.getLogger(AviationVerticle.class);

  private static Router subRouter;
  private AviationService aviationService;
  private final AviationApi aviationApi;

  public AviationVerticle(AviationApi aviationApi) {
    this.aviationApi = aviationApi;
  }

  @Override
  public void start() {
    subRouter = Router.router(vertx);
    LOGGER.info("AviationVerticle Started");


    subRouter.get("/flights").handler(this::getAllFlights);
  }

  public void getAllFlights(RoutingContext ctx){
    AtomicReference<JsonObject> jsonObject = new AtomicReference<>(new JsonObject());
      LOGGER.info("Flights endpoint here");
      // Validate required query params

      String flightStatus = ctx.queryParam("flight_status").getFirst();
      String limit = ctx.queryParam("limit").getFirst();
      String offset = ctx.queryParam("offset").getFirst();
      Future<JsonObject> flights = aviationApi.fetchFlights(flightStatus, limit, offset);
      flights.onSuccess(res -> {
//          converter(res);
          LOGGER.info("HERE");
          ctx.response().setStatusCode(200).end(res.encodePrettily());
        }
        );
      flights.onFailure(res -> {
          ctx.response().end(String.format("Error: %s",res.toString()));
      });

//      ctx.response().end("Hello Aviation Verticle");

  }

  private void converter(JsonObject res) {
    JsonObject template = new JsonObject();
    template.put("success", true);
    template.put("source", "api"); //or db
    template.put("result", res);
    filterByDay(JsonObject.mapFrom(res.getJsonArray("data")));

  }

  private JsonArray filterByDay(JsonObject data){
    for(int i = 0; i < data.size(); i++){

    }
    return null;
  }

  private void sortDays(JsonObject flight){
    LocalDate date = LocalDate.now();
    JsonArray days = new JsonArray();
    JsonArray today = new JsonArray();
    JsonArray yesterday = new JsonArray();
    days.add(today);
    days.add(yesterday);
    if (flight.getJsonObject("flight_date").toString().equals(date.toString())){

    }
  }

  //TODO: will be more reusable later
  void CustomError (RoutingContext ctx, String value){
    ctx.response().setStatusCode(404).end(String.format("Error, %s cannot be nullable", value));
  }

  @Override
  public void stop()  {

  }
  public static Router getSubRouter() {
    return subRouter;
  }
}
