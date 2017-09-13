package uk.nhs.digital.eps.dos.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Street address of the dispenser
 **/
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class Address   {
  
  private List<String> line = new ArrayList<>();
  private String postcode = null;

  public Address () {

  }

  public Address (List<String> line, String postcode) {
    this.line = line;
    this.postcode = postcode;
  }

    
  @JsonProperty("line")
  public List<String> getLine() {
    return line;
  }
  public void setLine(List<String> line) {
    this.line = line;
  }

    
  @JsonProperty("postcode")
  public String getPostcode() {
    return postcode;
  }
  public void setPostcode(String postcode) {
    this.postcode = postcode;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Address address = (Address) o;
    return Objects.equals(line, address.line) &&
        Objects.equals(postcode, address.postcode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(line, postcode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Address {\n");
    
    sb.append("    line: ").append(toIndentedString(line)).append("\n");
    sb.append("    postcode: ").append(toIndentedString(postcode)).append("\n");
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
