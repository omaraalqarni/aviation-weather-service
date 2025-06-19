package io.github.omaraalqarni.api;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AviationApi {
  private final WebClient webClient;
  private static final Logger LOGGER = LogManager.getLogger(AviationApi.class);


  public AviationApi(Vertx vertx) {
    this.webClient = WebClient.create(vertx);
  }

  public Future<JsonObject> fetchFlights(String flightStatus, String offset, String limit){
    String apiKey = System.getenv("AVIATION_API");
    if (apiKey == null) {
      throw new RuntimeException("API_KEY environment variable is not set");
    }
    Promise<JsonObject> promise = Promise.promise();
    var request = webClient
      .get(443, "api.aviationstack.com", "/v1/flights")
      .ssl(true)
//      .timeout(3000)
//      .addQueryParam("flight_date", String.valueOf(LocalDate.now().minusDays(1)))
      .addQueryParam("access_key",apiKey);

    if (flightStatus != null) {
      request.addQueryParam("flight_status", flightStatus);
    }
    if (limit != null) {
      request.addQueryParam("limit", limit);
    }
    if (offset != null) {
      request.addQueryParam("offset", offset);
    }

      request.send( asyncRes -> {
        if (asyncRes.succeeded()){
          if (asyncRes.result().statusCode() == 200){
            promise.complete(asyncRes.result().bodyAsJsonObject());

            LOGGER.info("Successfully fetched data from AviationStack");

          }
          else {
            promise.fail(asyncRes.result().bodyAsJsonObject().encodePrettily());
            LOGGER.info(asyncRes.result().body());
          }
        }else{
          LOGGER.info("Failed to Fetch from AviationStack API, reason: \n{}", asyncRes.cause().getMessage());
          promise.fail(asyncRes.cause());
        }
      });
    return promise.future();
  }

}

