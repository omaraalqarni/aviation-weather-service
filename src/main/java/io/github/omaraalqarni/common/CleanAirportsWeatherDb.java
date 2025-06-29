package io.github.omaraalqarni.common;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CleanAirportsWeatherDb {
  public static void main(String[] args) throws IOException {
    Path path = Paths.get("src/main/resources/airports_weather.json");

    String content = Files.readString(path);

    JsonObject root = new JsonObject(content);

    for (String icao : root.fieldNames()) {
      JsonObject airport = root.getJsonObject(icao);
      airport.put("weather", new JsonObject());
    }

    Files.writeString(path, root.encodePrettily());
    System.out.println("Weather data cleared.");
  }
}
