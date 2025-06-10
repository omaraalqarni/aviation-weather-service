package io.github.omaraalqarni.aviation.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Flight {
    private String number;
    private String iata;
    private String icao;
    private CodeShared codeShared;

}

@Data
class CodeShared{
  @JsonProperty("airline_name")
  private String airlineName;
  @JsonProperty("airline_iata")
  private String airline_iata;
  @JsonProperty("flight_number")
  private String airlineNumber;
  @JsonProperty("flight_iata")
  private String flightIata;
  @JsonProperty("flight_icao")
  private String flightIcao;

}

