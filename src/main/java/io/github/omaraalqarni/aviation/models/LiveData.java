package io.github.omaraalqarni.aviation.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LiveData {
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
