package io.github.omaraalqarni.aviation.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Aircraft {

  @JsonProperty("registration")
  private String registrationNumber;
  private String iata;
  private String icao;
  private String icao24;

}
