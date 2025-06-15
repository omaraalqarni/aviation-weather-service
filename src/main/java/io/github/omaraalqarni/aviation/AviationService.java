package io.github.omaraalqarni.aviation;

import io.github.omaraalqarni.aviation.models.FlightResponse;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public interface AviationService {
  String parseFlights(String flights);


}
