package io.github.omaraalqarni.aviation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;


public class FlightInfo {

  @JsonProperty("airport")
  private String airportName;
  private String timezone;
  private String iata;
  private String icao;
  private String terminal;
  private String gate;
  private String baggage;
  private int delay;
  private LocalDateTime scheduled;
  private LocalDateTime actual;

  @JsonProperty("estimated_runway")
  private LocalDateTime estimatedRunway;
  @JsonProperty("actual_runway")
  private LocalDateTime actualRunway;

}
