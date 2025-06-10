package io.github.omaraalqarni.aviation;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class AviationServiceImpl implements AviationService{


  @Override
  public Future<JsonObject> getFlights(String flightStatus, int limit, int offset, String ArrICAO) {
    return null;
  }
}
