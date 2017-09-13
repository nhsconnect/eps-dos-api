package uk.nhs.digital.eps.dos.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * contact details for the prescriber to contact the dispenser
 **/
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class PrescriberContact   {
  
  private String tel = null;
  private String fax = null;
  private String email = null;

  public PrescriberContact () {

  }

  public PrescriberContact (String tel, String fax, String email) {
    this.tel = tel;
    this.fax = fax;
    this.email = email;
  }

    
  @JsonProperty("tel")
  public String getTel() {
    return tel;
  }
  public void setTel(String tel) {
    this.tel = tel;
  }

    
  @JsonProperty("fax")
  public String getFax() {
    return fax;
  }
  public void setFax(String fax) {
    this.fax = fax;
  }

    
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PrescriberContact inlineResponse200PrescriberContact = (PrescriberContact) o;
    return Objects.equals(tel, inlineResponse200PrescriberContact.tel) &&
        Objects.equals(fax, inlineResponse200PrescriberContact.fax) &&
        Objects.equals(email, inlineResponse200PrescriberContact.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tel, fax, email);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse200PrescriberContact {\n");
    
    sb.append("    tel: ").append(toIndentedString(tel)).append("\n");
    sb.append("    fax: ").append(toIndentedString(fax)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
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
