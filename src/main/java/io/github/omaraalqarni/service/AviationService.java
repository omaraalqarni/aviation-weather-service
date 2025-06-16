package io.github.omaraalqarni.service;


import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface AviationService {
  public JsonObject parseResponse(JsonObject res);
  public JsonObject filterFlightsByDay(JsonArray data);
}
