package io.github.omaraalqarni.aviation.impl;

import io.github.omaraalqarni.aviation.AviationApi;
import io.github.omaraalqarni.aviation.AviationService;
import io.github.omaraalqarni.aviation.AviationVerticle;
import io.github.omaraalqarni.aviation.models.FlightResponse;
import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class AviationServiceImpl implements AviationService {
  private final AviationApi aviationApi;
  private final Logger LOGGER = LoggerFactory.getLogger(AviationVerticle.class);

  public AviationServiceImpl(AviationApi aviationApi) {
    this.aviationApi = aviationApi;
  }


  public String parseFlights(String flights) {
        FlightResponse flightResponse = Json.decodeValue(flights, FlightResponse.class);
        LOGGER.info("Flight Response Parsed successfully");
        System.out.println(flightResponse);


    return null;
  }



}
