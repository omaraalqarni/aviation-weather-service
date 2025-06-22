package io.github.omaraalqarni.service.impl;

import io.github.omaraalqarni.service.DBService;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Tuple;

import java.util.List;
import java.util.NoSuchElementException;

public class DBServiceImpl implements DBService {
  private final PgPool client;

  public DBServiceImpl(PgPool client) {
    this.client = client;
  }

  @Deprecated
  @Override
  public Future<JsonObject> getAirportCoordinates(JsonArray icaoCodes) {
    String sqlQuery = "SELECT ident, lat, lon FROM airports WHERE ident = ANY($1)\n";
    List<String> codes = icaoCodes.getList();
    String[] codeArray = codes.toArray(new String[0]);

    return client
      .preparedQuery(sqlQuery)
      .execute(Tuple.of(codeArray))
      .map(rows -> {
        JsonObject result = new JsonObject();
        rows.forEach(row ->
          result.put(row.getString("ident"), new JsonObject()
            .put("lat", row.getDouble("lat"))
            .put("lon", row.getDouble("lon"))));
        return result;
      });
  }

  @Override
  public Future<String> saveWeatherData(double lat, double lon, JsonObject weatherData) {
    String sqlQuery = "INSERT INTO weather (lat, lon, weather_data) VALUES ($1, $2, $3);\n";
    return client.preparedQuery(sqlQuery)
      .execute(Tuple.of(lat, lon, weatherData))
      .map(rows -> "Weather data saved successfully.")
      .recover(err -> Future.succeededFuture("Weather save failed: " + err.getMessage()));
  }

  @Override
  public Future<JsonObject> getWeatherDataFromDb(double lat, double lon) {
    String sqlQuery = "SELECT weather_data FROM weather WHERE lat =$1 AND lon = $2";
    return client
      .preparedQuery(sqlQuery)
      .execute(Tuple.of(lat, lon))
      .map(rows -> {
        if (rows.iterator().hasNext()) {
          return rows.iterator().next().getJsonObject("weather_data");
        } else {
          throw new NoSuchElementException("No weather data found for lat: " + lat + ", lon: " + lon);
        }
      });
  }
}
