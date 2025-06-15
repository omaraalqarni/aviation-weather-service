package io.github.omaraalqarni.aviation;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

public class AviationApi {
  private final WebClient webClient;
  private static final Logger LOGGER = LoggerFactory.getLogger(AviationApi.class);

  public AviationApi(Vertx vertx) {
    this.webClient = WebClient.create(vertx);
  }

  public Future<JsonObject> fetchFlights(String flightStatus, String offset, String limit){
    String apiKey = "65be09ab80cff2eb0430dc0cab1d3382";
    Promise<JsonObject> promise = Promise.promise();
    var request = webClient
      .get(443, "api.aviationstack.com", "/v1/flights")
      .ssl(true)
      .addQueryParam("access_key",apiKey);
//      .timeout(3000)
    if (!flightStatus.isEmpty()) {
      request.addQueryParam("flight_status", flightStatus);
    }
    if (!limit.isEmpty()) {
      request.addQueryParam("limit", limit);
    }
    if (!offset.isEmpty()) {
      request.addQueryParam("offset", offset);
    }

      request.send( asyncRes -> {
        if (asyncRes.succeeded()){
          if (asyncRes.result().statusCode() == 200){
            LOGGER.info("It is 200");
            LOGGER.info(asyncRes.result().body());
          promise.complete(asyncRes.result().bodyAsJsonObject());
          LOGGER.info("Successfully fetched data from AviationStack");
          JsonObject res = asyncRes.result().bodyAsJsonObject();
          LOGGER.info(res);
          }
          else {
            LOGGER.info(asyncRes.result().body());
          }
        }else{
          LOGGER.info(String.format("Failed to Fetch from AviationStack API, reason: \n%s",asyncRes.cause().getMessage()));
          promise.fail(asyncRes.cause());
        }
      });
    return promise.future();
  }

}

