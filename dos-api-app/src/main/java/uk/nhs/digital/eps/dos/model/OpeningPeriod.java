package uk.nhs.digital.eps.dos.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL) 
public class OpeningPeriod   {
  
  private String open = null;
  private String close = null;

  public OpeningPeriod () {

  }

  public OpeningPeriod (String open, String close) {
    this.open = open;
    this.close = close;
  }

    
  @JsonProperty("open")
  public String getOpen() {
    return open;
  }
  public void setOpen(String open) {
    this.open = open;
  }

    
  @JsonProperty("close")
  public String getClose() {
    return close;
  }
  public void setClose(String close) {
    this.close = close;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OpeningPeriod inlineResponse200OpeningSun = (OpeningPeriod) o;
    return Objects.equals(open, inlineResponse200OpeningSun.open) &&
        Objects.equals(close, inlineResponse200OpeningSun.close);
  }

  @Override
  public int hashCode() {
    return Objects.hash(open, close);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OpeningPeriod {\n");
    
    sb.append("    open: ").append(toIndentedString(open)).append("\n");
    sb.append("    close: ").append(toIndentedString(close)).append("\n");
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
