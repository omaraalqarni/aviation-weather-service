package io.github.omaraalqarni.aviation.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlightSummary {

  @JsonProperty("flight_status")
  private String flightStatus;

  @JsonProperty("flight_date")
  private LocalDateTime flightDate;

  private FlightInfo departure;
  private FlightInfo arrival;
  private Airline airline;


}


