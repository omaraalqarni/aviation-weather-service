package io.github.omaraalqarni.service.impl;

import io.github.omaraalqarni.service.DBService;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DBServiceImpl implements DBService {
  private final PgPool client;
  private final Logger logger = LoggerFactory.getLogger(DBServiceImpl.class);

  public DBServiceImpl(PgPool client) {
    this.client = client;
  }

  @Override
  public Future<JsonObject> getAirportCoordinates(JsonArray icaoCodes) {
    String sqlQuery = "SELECT ident, lat, long FROM airports WHERE ident = ANY($1)\n";
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
          .put("lon", row.getDouble("long"))));
        logger.info("icao res:");
        logger.info(result.encodePrettily());
        return result;
      });
  }
}
