package io.github.omaraalqarni.aviation.impl;

import io.github.omaraalqarni.aviation.AviationApi;
import io.github.omaraalqarni.aviation.AviationService;
import io.github.omaraalqarni.aviation.models.FlightResponse;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class AviationServiceImpl implements AviationService {
  private final AviationApi aviationApi;

  public AviationServiceImpl(AviationApi aviationApi) {
    this.aviationApi = aviationApi;
  }

  @Override
  public Future<FlightResponse> ParseFlights(String rawData) {
    FlightResponse flightResponse = Json.decodeValue(rawData, FlightResponse.class);
    System.out.println(flightResponse);
    return Future.succeededFuture(flightResponse);
  }
}
