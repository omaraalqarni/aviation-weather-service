package io.github.omaraalqarni.aviation;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

public class AviationApi {
  private final WebClient webClient;

  public AviationApi(Vertx vertx) {
    this.webClient = WebClient.create(vertx);
  }

  public Future<String> fetchflights(){

    return null;
//    should return flights json

  }
}
