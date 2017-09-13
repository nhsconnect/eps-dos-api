package uk.nhs.digital.eps.dos.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL) 
public class Location   {
  
  private double easting;
  private double northing;

  public Location () {

  }

  public Location (double easting, double northing) {
    this.easting = easting;
    this.northing = northing;
  }

    
  @JsonProperty("easting")
  public double getEasting() {
    return easting;
  }
  public void setEasting(double easting) {
    this.easting = easting;
  }

    
  @JsonProperty("northing")
  public double getNorthing() {
    return northing;
  }
  public void setNorthing(double northing) {
    this.northing = northing;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Location location = (Location) o;
    return Objects.equals(easting, location.easting) &&
        Objects.equals(northing, location.northing);
  }

  @Override
  public int hashCode() {
    return Objects.hash(easting, northing);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Location {\n");
    
    sb.append("    easting: ").append(toIndentedString(easting)).append("\n");
    sb.append("    northing: ").append(toIndentedString(northing)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
