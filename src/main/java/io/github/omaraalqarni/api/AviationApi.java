package io.github.omaraalqarni.api;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.List;

public class AviationApi {
  private final WebClient webClient;
  private static final Logger LOGGER = LogManager.getLogger(AviationApi.class);


  public AviationApi(Vertx vertx) {
    this.webClient = WebClient.create(vertx);
  }



  public Future<JsonObject> fetchByBatch(String flightStatus, String offsetStr, String limitStr, String arr_icao) {
    int offset = offsetStr != null ? Integer.parseInt(offsetStr) : 0;
    int totalLimit = limitStr != null ? Integer.parseInt(limitStr) : 100;

    int batchSize;
    if (totalLimit <= 10) {
      batchSize = totalLimit;
    } else {
      batchSize = 10;
    }

    int totalBatches = (int) Math.ceil((double) totalLimit / batchSize);

    List<Future> batchFutures = new ArrayList<>();
    LOGGER.info("Started batch fetching");

    for (int i = 0; i < totalBatches; i++) {
      LOGGER.info("Started fetching batch #{}", i+1);
      final int batchIndex = i;
      int batchOffset = i * batchSize + offset;
      int currentLimit = Math.min(batchSize, totalLimit - (i * batchSize));

      Future<JsonObject> batch = fetchFlights(flightStatus, String.valueOf(batchOffset), String.valueOf(currentLimit), arr_icao)
        .recover(err -> {
          LOGGER.info("Failed to fetch batch #{}", batchIndex+1);
          JsonObject errorObj = new JsonObject()
            .put("error_source", "fetchFlights")
            .put("error_code", 500)
            .put("batch_index", batchIndex);

          String msg = err.getMessage();
          try {
            errorObj.put("error_message", new JsonObject(msg));
          } catch (Exception e) {
            errorObj.put("error_message", new JsonObject().put("message", msg));
          }

          return Future.succeededFuture(errorObj);
        });

      batchFutures.add(batch);
    }
    return CompositeFuture.all(batchFutures).map(AviationApi::mergeFlights);
  }


  public Future<JsonObject> fetchFlights(String flightStatus, String offset, String limit, String arr_icao) {
    String apiKey = System.getenv("AVIATION_API");
    if (apiKey == null) {
      throw new RuntimeException("API_KEY environment variable is not set");
    }
    Promise<JsonObject> promise = Promise.promise();
    var request = webClient
      .get(443, "api.aviationstack.com", "/v1/flights")
      .ssl(true)
//      .addQueryParam("flight_date", String.valueOf(LocalDate.now().minusDays(1)))
      .timeout(5000)
      .addQueryParam("access_key", apiKey);

    if (offset != null){
      request.addQueryParam("offset", offset);
    }
    if (limit != null){
      request.addQueryParam("limit", limit);
    }
    if (flightStatus != null) {
      request.addQueryParam("flight_status", flightStatus);
    }
    if (arr_icao != null){
      request.addQueryParam("arr_icao", arr_icao);
    }

    request.send(asyncRes -> {
      if (asyncRes.succeeded()) {
        if (asyncRes.result().statusCode() == 200) {
          promise.complete(asyncRes.result().bodyAsJsonObject());
        } else {
          promise.fail(asyncRes.result().bodyAsJsonObject().encodePrettily());
          LOGGER.info(asyncRes.result().body());
        }
      } else {
        promise.fail(asyncRes.cause().getMessage());
      }
    });
    return promise.future();
  }


  private static JsonObject mergeFlights(CompositeFuture cf) {
    JsonArray mergedFlights = new JsonArray();
    JsonArray errors = new JsonArray();



    for (int i = 0; i < cf.size(); i++) {
      JsonObject result = cf.resultAt(i);
      if (result.containsKey("data")) {
        result.getJsonArray("data").forEach(mergedFlights::add);
      } else {
        errors.add(result);
      }
    }
    JsonObject pagination = new JsonObject()
      .put("limit",mergedFlights.size())
      .put("offset", 0)
      .put("count",mergedFlights.size())
      .put("total",0);


    return new JsonObject()
      .put("success", errors.isEmpty())
      .put("source", "api")
      .put("pagination", pagination)
      .put("data", mergedFlights)
      .put("errors", errors);
  }
}

