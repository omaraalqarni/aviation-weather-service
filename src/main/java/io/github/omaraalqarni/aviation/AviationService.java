package io.github.omaraalqarni.aviation;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface AviationService {
  Future<JsonObject> getFlights(String flightStatus, int limit, int offset, String ArrICAO);
}
