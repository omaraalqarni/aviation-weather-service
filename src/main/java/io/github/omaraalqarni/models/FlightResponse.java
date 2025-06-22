package io.github.omaraalqarni.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

// i should cancel all this. get a plain json object and fill it with flight & weather & errors!
@Deprecated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FlightResponse {

  @JsonProperty("flight_status")
  private String flightStatus;

  @JsonProperty("flight_date")
  private LocalDateTime flightDate;

  private FlightInfo departure;
  private FlightInfo arrival;
  private Airline airline;
  private FlightDetail flight;
  private Aircraft aircraft;
  private LiveData livedata;


}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
class FlightInfo {

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


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
class Airline {
  private String name;
  private String iata;
  private String icao;
}


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
class FlightDetail {
  private String number;
  private String iata;
  private String icao;
  private CodeShared codeShared;

}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
class CodeShared {
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


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
class Aircraft {

  @JsonProperty("registration")
  private String registrationNumber;
  private String iata;
  private String icao;
  private String icao24;

}


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
class LiveData {
  private LocalDateTime updated;
  private Double latitude;
  private Double longitude;
  private Double altitude;
  private Double direction;

  @JsonProperty("speed_horizontal")
  private Double speedHorizontal;
  @JsonProperty("speed_vertical")
  private Double speedVertical;
  @JsonProperty("is_ground")
  private boolean isGround;
}

