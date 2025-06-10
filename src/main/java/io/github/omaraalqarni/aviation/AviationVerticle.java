package io.github.omaraalqarni.aviation;

import io.github.omaraalqarni.MainVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class AviationVerticle extends AbstractVerticle {

  public static void main(String[] args) {

  }

  @Override
  public void start() {
    Router router = Router.router(vertx);

    vertx.deployVerticle(new MainVerticle());
  }

  @Override
  public void stop()  {

  }
}
