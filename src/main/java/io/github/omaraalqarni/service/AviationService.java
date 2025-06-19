package io.github.omaraalqarni.service;


import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface AviationService {
  JsonObject parseResponse(JsonObject res, JsonObject groupedData);
  Future<JsonObject> processAllFlights(JsonArray flights);
  void saveWeatherDataToDb(JsonObject data);


}
