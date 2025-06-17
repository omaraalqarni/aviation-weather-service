package io.github.omaraalqarni.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface DBService {
  Future<JsonObject> getAirportCoordinates(JsonArray icaoCodes);
}
