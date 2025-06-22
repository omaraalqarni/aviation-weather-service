package io.github.omaraalqarni.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface DBService {
  Future<JsonObject> getAirportCoordinates(JsonArray icaoCodes);

  Future<String> saveWeatherData(double lat, double lon, JsonObject weatherData);

  Future<JsonObject> getWeatherDataFromDb(double lat, double lon);
}
