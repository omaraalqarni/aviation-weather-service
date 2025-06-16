package io.github.omaraalqarni.service.impl;

import io.github.omaraalqarni.service.AviationService;
import io.github.omaraalqarni.verticle.AviationVerticle;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.LocalDate;

public class AviationServiceImpl implements AviationService {
  private final Logger LOGGER = LoggerFactory.getLogger(AviationVerticle.class);


  public JsonObject parseResponse(JsonObject res) {
    JsonObject template = new JsonObject();

    template.put("success", true); // hardcoded for now
    template.put("source", "api"); //or db
    JsonArray resultArray = new JsonArray();

    JsonObject sortedFlights = filterFlightsByDay(res.getJsonArray("data"));

    JsonObject dataObj = new JsonObject();
    dataObj.put("pagination", res.getJsonObject("pagination"));
    dataObj.put("data", sortedFlights);
    resultArray.add(dataObj);

    template.put("result",resultArray);
    template.put("errors","");

    return template;
  }


  public JsonObject filterFlightsByDay(JsonArray data){
    LocalDate todayDate = LocalDate.now();
    LocalDate yesterdayDate = LocalDate.now().minusDays(1);

    JsonObject days = new JsonObject();
    JsonArray today = new JsonArray();
    JsonArray yesterday = new JsonArray();

    for(int i = 0; i < data.size(); i++){
      JsonObject flight =data.getJsonObject(i);
      flight.put("weather", ""); //hardcoded
      var flightDate = flight.getString("flight_date");
      if (flightDate.equals(todayDate.toString())){

        today.add(flight);
      }
      else if (yesterday.size()<10 && flightDate.equals(yesterdayDate.toString())){
        yesterday.add(flight);
      }
    }

    days.put("today", today);
    days.put("yesterday", yesterday);
    return days;
  }


}
