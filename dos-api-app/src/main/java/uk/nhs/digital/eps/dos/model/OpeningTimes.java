package uk.nhs.digital.eps.dos.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Opening time as an open/closed pair in local time. A null value incidates closed on that day.
 **/
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class OpeningTimes   {
  
  private Boolean open247 = null;
  private List<OpeningPeriod> sun = null;
  private List<OpeningPeriod> mon = null;
  private List<OpeningPeriod> tue = null;
  private List<OpeningPeriod> wed = null;
  private List<OpeningPeriod> thu = null;
  private List<OpeningPeriod> fri = null;
  private List<OpeningPeriod> sat = null;
  private List<OpeningPeriod> bankHoliday = null;
  private Map<String, List<OpeningPeriod>> specifiedDate = new HashMap<>();

  public OpeningTimes () {

  }

  public OpeningTimes (Boolean open247, List<OpeningPeriod> sun, List<OpeningPeriod> mon, List<OpeningPeriod> tue, List<OpeningPeriod> wed, List<OpeningPeriod> thu, List<OpeningPeriod> fri, List<OpeningPeriod> sat, List<OpeningPeriod> bankHoliday, Map<String, List<OpeningPeriod>> specifiedDate) {
    this.open247 = open247;
    this.sun = sun;
    this.mon = mon;
    this.tue = tue;
    this.wed = wed;
    this.thu = thu;
    this.fri = fri;
    this.sat = sat;
    this.bankHoliday = bankHoliday;
    this.specifiedDate = specifiedDate;
  }

    
  @JsonProperty("open_247")
  public Boolean getOpen247() {
    return open247;
  }
  public void setOpen247(Boolean open247) {
    this.open247 = open247;
  }

    
  @JsonProperty("sun")
  public List<OpeningPeriod> getSun() {
    return sun;
  }
  public void setSun(List<OpeningPeriod> sun) {
    this.sun = sun;
  }

    
  @JsonProperty("mon")
  public List<OpeningPeriod> getMon() {
    return mon;
  }
  public void setMon(List<OpeningPeriod> mon) {
    this.mon = mon;
  }

    
  @JsonProperty("tue")
  public List<OpeningPeriod> getTue() {
    return tue;
  }
  public void setTue(List<OpeningPeriod> tue) {
    this.tue = tue;
  }

    
  @JsonProperty("wed")
  public List<OpeningPeriod> getWed() {
    return wed;
  }
  public void setWed(List<OpeningPeriod> wed) {
    this.wed = wed;
  }

    
  @JsonProperty("thu")
  public List<OpeningPeriod> getThu() {
    return thu;
  }
  public void setThu(List<OpeningPeriod> thu) {
    this.thu = thu;
  }

    
  @JsonProperty("fri")
  public List<OpeningPeriod> getFri() {
    return fri;
  }
  public void setFri(List<OpeningPeriod> fri) {
    this.fri = fri;
  }

    
  @JsonProperty("sat")
  public List<OpeningPeriod> getSat() {
    return sat;
  }
  public void setSat(List<OpeningPeriod> sat) {
    this.sat = sat;
  }

    
  @JsonProperty("bank_holiday")
  public List<OpeningPeriod> getBankHoliday() {
    return bankHoliday;
  }
  public void setBankHoliday(List<OpeningPeriod> bankHoliday) {
    this.bankHoliday = bankHoliday;
  }

    
  @JsonProperty("specified_date")
  public Map<String, List<OpeningPeriod>> getSpecifiedDate() {
    return specifiedDate;
  }
  public void setSpecifiedDate(Map<String, List<OpeningPeriod>> specifiedDate) {
    this.specifiedDate = specifiedDate;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OpeningTimes inlineResponse200Opening = (OpeningTimes) o;
    return Objects.equals(open247, inlineResponse200Opening.open247) &&
        Objects.equals(sun, inlineResponse200Opening.sun) &&
        Objects.equals(mon, inlineResponse200Opening.mon) &&
        Objects.equals(tue, inlineResponse200Opening.tue) &&
        Objects.equals(wed, inlineResponse200Opening.wed) &&
        Objects.equals(thu, inlineResponse200Opening.thu) &&
        Objects.equals(fri, inlineResponse200Opening.fri) &&
        Objects.equals(sat, inlineResponse200Opening.sat) &&
        Objects.equals(bankHoliday, inlineResponse200Opening.bankHoliday) &&
        Objects.equals(specifiedDate, inlineResponse200Opening.specifiedDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(open247, sun, mon, tue, wed, thu, fri, sat, bankHoliday, specifiedDate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OpeningTimes {\n");
    
    sb.append("    open247: ").append(toIndentedString(open247)).append("\n");
    sb.append("    sun: ").append(toIndentedString(sun)).append("\n");
    sb.append("    mon: ").append(toIndentedString(mon)).append("\n");
    sb.append("    tue: ").append(toIndentedString(tue)).append("\n");
    sb.append("    wed: ").append(toIndentedString(wed)).append("\n");
    sb.append("    thu: ").append(toIndentedString(thu)).append("\n");
    sb.append("    fri: ").append(toIndentedString(fri)).append("\n");
    sb.append("    sat: ").append(toIndentedString(sat)).append("\n");
    sb.append("    bankHoliday: ").append(toIndentedString(bankHoliday)).append("\n");
    sb.append("    specifiedDate: ").append(toIndentedString(specifiedDate)).append("\n");
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
