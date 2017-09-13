package uk.nhs.digital.eps.dos.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * contact details for the patient to contact the dispenser
 **/
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class PatientContact   {
  
  private String tel = null;
  private String webAddress = null;

  public PatientContact () {

  }

  public PatientContact (String tel, String webAddress) {
    this.tel = tel;
    this.webAddress = webAddress;
  }

    
  @JsonProperty("tel")
  public String getTel() {
    return tel;
  }
  public void setTel(String tel) {
    this.tel = tel;
  }

    
  @JsonProperty("web_address")
  public String getWebAddress() {
    return webAddress;
  }
  public void setWebAddress(String webAddress) {
    this.webAddress = webAddress;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PatientContact inlineResponse200PatientContact = (PatientContact) o;
    return Objects.equals(tel, inlineResponse200PatientContact.tel) &&
        Objects.equals(webAddress, inlineResponse200PatientContact.webAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tel, webAddress);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse200PatientContact {\n");
    
    sb.append("    tel: ").append(toIndentedString(tel)).append("\n");
    sb.append("    webAddress: ").append(toIndentedString(webAddress)).append("\n");
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
