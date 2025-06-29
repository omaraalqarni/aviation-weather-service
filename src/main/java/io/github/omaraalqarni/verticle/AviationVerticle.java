
package io.github.omaraalqarni.verticle;

import io.github.omaraalqarni.api.AviationApi;
import io.github.omaraalqarni.service.AviationService;
import io.github.omaraalqarni.service.impl.AviationServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.List;


public class  AviationVerticle extends AbstractVerticle {
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

  public void getAllFlights(RoutingContext ctx) {
    LOGGER.info("Flights endpoint here");

    String flightStatus = ctx.queryParams().get("flight_status");
    String limitStr = ctx.queryParams().get("limit");
    String offsetStr = ctx.queryParams().get("offset");
    String arr_icao = ctx.queryParams().get("arr_icao");
    JsonArray errors = new JsonArray();

    List<String> validStatuses = List.of("scheduled", "active", "landed", "cancelled", "incident", "diverted", "");

    if (flightStatus != null && !validStatuses.contains(flightStatus.toLowerCase())) {
      errors.add(new JsonObject()
        .put("error_source", "flight_status")
        .put("error_code", 400)
        .put("error_message", "Invalid flight_status: " + flightStatus));
    }

    try {
      if (limitStr != null) {
        int limit = Integer.parseInt(limitStr);
        if (limit < 0 || limit > 100) {
          errors.add(new JsonObject().put("error_source", "limit").put("error_code", 400).put("error_message", "limit must be 0-100"));
        }
      }
    } catch (NumberFormatException e) {
      errors.add(new JsonObject().put("error_source", "limit").put("error_code", 400).put("error_message", "limit must be an integer"));
    }

    try {
      if (offsetStr != null) {
        int offset = Integer.parseInt(offsetStr);
        if (offset < 0 || offset > 100) {
          errors.add(new JsonObject().put("error_source", "offset").put("error_code", 400).put("error_message", "offset must be 0-100"));
        }
      }
    } catch (NumberFormatException e) {
      errors.add(new JsonObject().put("error_source", "offset").put("error_code", 400).put("error_message", "offset must be an integer"));
    }

    if (!errors.isEmpty()) {
      JsonObject response = new JsonObject()
        .put("success", false)
        .put("source", "api")
        .put("result", new JsonArray())
        .put("errors", errors);
      ctx.response().setStatusCode(400).putHeader("Content-Type", "application/json").end(response.encodePrettily());
      return;
    }

    LOGGER.info("Start fetching from Aviation API");
    aviationApi.fetchByBatch(flightStatus, offsetStr, limitStr, arr_icao)
      .compose(this::processingFlights)
      .onSuccess(resp -> {
        ctx.response()
          .setStatusCode(200)
          .putHeader("Content-Type", "application/json")
          .end(resp.encodePrettily());
      })
      .onFailure(err -> {
        JsonObject errPayload = new JsonObject()
          .put("success", false)
          .put("source", "api")
          .put("result", new JsonArray())
          .put("errors", new JsonArray().add(new JsonObject()
            .put("error_source", "unknown")
            .put("error_code", 500)
            .put("error_message", err.getMessage())));
        ctx.response().setStatusCode(500).putHeader("Content-Type", "application/json").end(errPayload.encodePrettily());
      });
  }

  public static Router getAviationRouter() {
    return aviationRouter;
  }

  @Override
  public void stop() {

  }

  private Future<JsonObject> processingFlights(JsonObject res) {
    JsonArray rawFlights = res.getJsonArray("data", new JsonArray());
    JsonArray errorList = res.getJsonArray("errors", new JsonArray());

    return aviationService.processAllFlights(rawFlights)
      .map(grouped -> {
        JsonObject response = aviationService.parseResponse(res, grouped);
        response.put("errors", errorList); // embed batch-level errors
        return response;
      });
  }

}
