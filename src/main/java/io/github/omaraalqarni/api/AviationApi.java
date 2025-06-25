package io.github.omaraalqarni.api;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
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


  public Future<JsonObject> fetchByBatch(String flightStatus, String offsetStr, String limitStr) {
    int offset = offsetStr != null ? Integer.parseInt(offsetStr) : 0;
    int totalLimit = limitStr != null ? Integer.parseInt(limitStr) : 100;

    int batchSize;
    if (totalLimit <= 10) {
      batchSize = totalLimit;
    } else if (totalLimit <= 50) {
      batchSize = 10;
    } else {
      batchSize = 10;
    }

    int totalBatches = (int) Math.ceil((double) totalLimit / batchSize);
    List<Future> batchFutures = new ArrayList<>();

    for (int i = 0; i < totalBatches; i++) {
      int batchOffset = offset + i * batchSize;
      int currentLimit = Math.min(batchSize, totalLimit - i * batchSize);
      batchFutures.add(fetchFlights(flightStatus, String.valueOf(batchOffset), String.valueOf(currentLimit)));
    }

    return CompositeFuture.all(batchFutures)
      .map(cf -> {
        JsonObject merged = new JsonObject().put("data", new io.vertx.core.json.JsonArray());
        for (int i = 0; i < cf.size(); i++) {
          JsonObject obj = cf.resultAt(i);
          if (obj.containsKey("data")) {
            obj.getJsonArray("data").forEach(entry ->
              merged.getJsonArray("data").add(entry)
            );
          }
        }
        return merged;
      });
  }

  private Future<JsonObject> fetchFlights(String flightStatus, String offset, String limit) {
    String apiKey = System.getenv("AVIATION_API");
    if (apiKey == null) {
      throw new RuntimeException("API_KEY environment variable is not set");
    }
    Promise<JsonObject> promise = Promise.promise();
    var request = webClient
      .get(443, "api.aviationstack.com", "/v1/flights")
      .ssl(true)
//      .addQueryParam("flight_date", String.valueOf(LocalDate.now().minusDays(1)))
//      .timeout(3000)
      .addQueryParam("access_key", apiKey)
      .addQueryParam("limit", limit)
      .addQueryParam("offset", offset);

    if (flightStatus != null) {
      request.addQueryParam("flight_status", flightStatus);
    }

    request.send(asyncRes -> {
      if (asyncRes.succeeded()) {
        if (asyncRes.result().statusCode() == 200) {
          LOGGER.info("Successfully fetched data from AviationStack");
          promise.complete(asyncRes.result().bodyAsJsonObject());
        } else {
          promise.fail(asyncRes.result().bodyAsJsonObject().encodePrettily());
          LOGGER.info(asyncRes.result().body());
        }
      } else {
        LOGGER.info("Failed to Fetch from AviationStack API, reason: \n{}", asyncRes.cause().getMessage());
        promise.fail(asyncRes.cause().getMessage());
      }
    });
    return promise.future();
  }
}

