package io.github.omaraalqarni.common;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

public final class PostgresConnector {
  private static PgPool client;

  private PostgresConnector() {
  }

  public static PgPool getClient(Vertx vertx) {
    if (client == null) {
      PgConnectOptions connectOptions = new PgConnectOptions()
        .setPort(5432)
        .setHost("localhost")
        .setDatabase("flight_weather")
        .setUser("omar");
//        .setPassword("your_password");

      PoolOptions poolOptions = new PoolOptions().setMaxSize(10);
      client = PgPool.pool(vertx, connectOptions, poolOptions);
    }
    return client;
  }
}

