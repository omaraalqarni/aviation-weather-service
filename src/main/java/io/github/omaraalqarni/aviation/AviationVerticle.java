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
          var organized = converter(res);
          LOGGER.info("HERE");
          ctx.response().setStatusCode(200).end(organized.encodePrettily());
        }
        );
      flights.onFailure(res -> {
          ctx.response().end(String.format("Error: %s",res.toString()));
      });

//      ctx.response().end("Hello Aviation Verticle");

  }

  private JsonObject converter(JsonObject res) {
    JsonObject template = new JsonObject();

    template.put("success", true); // hardcoded for now
    template.put("source", "api"); //or db
// add to res lastly
    JsonArray resultArray = new JsonArray();

    JsonObject sortedFlights = filterFlightsByDay(res.getJsonArray("data"));

    JsonObject dataObj = new JsonObject();
    dataObj.put("pagination", res.getJsonObject("pagination"));
    dataObj.put("data", sortedFlights);
    resultArray.add(dataObj);

    template.put("result",resultArray);
    template.put("errors","");

    return template;
  }

  private JsonObject filterFlightsByDay(JsonArray data){
    LocalDate todayDate = LocalDate.now();
    LocalDate yesterdayDate = LocalDate.now().minusDays(1);

    JsonObject days = new JsonObject();
    JsonArray today = new JsonArray();
    JsonObject todayObj = new JsonObject();
    JsonObject yesterdayObj = new JsonObject();
    JsonArray yesterday = new JsonArray();

    for(int i = 0; i < data.size(); i++){
        JsonObject flight =data.getJsonObject(i);
        flight.put("weather", ""); //hardcoded
        var flightDate = flight.getString("flight_date");
        if (flightDate.equals(todayDate.toString())){

          today.add(flight);
        }
        else if (yesterday.size()<10 && flightDate.equals(yesterdayDate.toString())){
          yesterday.add(flight);
      }
    }

      days.put("today", today);
      days.put("yesterday", yesterday);
    return days;
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
