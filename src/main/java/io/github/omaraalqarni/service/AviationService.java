package io.github.omaraalqarni.service;


import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Future;

import java.util.Set;

public interface AviationService {
  JsonObject parseResponse(JsonObject res, JsonObject groupedData);
  Future<JsonObject> processAllFlights(JsonArray flights);
//  JsonObject filterFlightsByDay(JsonArray data);


}
