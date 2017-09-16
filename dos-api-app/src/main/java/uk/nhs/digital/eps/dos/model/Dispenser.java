package uk.nhs.digital.eps.dos.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dispenser {

    private String ods = null;
    private String name = null;

    public enum ServiceTypeEnum {
        PHARMACY("eps_pharmacy");

        private String value;

        ServiceTypeEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

    private ServiceTypeEnum serviceType = null;
    private Address address = null;
    private PatientContact patientContact = null;
    private PrescriberContact prescriberContact = null;
    private Location location = null;
    private OpeningTimes opening = null;
    private Double distance;

    public Dispenser() {
    }

    public Dispenser(String ods, String name, ServiceTypeEnum serviceType, Address address, PatientContact patientContact, PrescriberContact prescriberContact, Location location, OpeningTimes opening) {
        this.ods = ods;
        this.name = name;
        this.serviceType = serviceType;
        this.address = address;
        this.patientContact = patientContact;
        this.prescriberContact = prescriberContact;
        this.location = location;
        this.opening = opening;
    }

    @JsonProperty("distance")
    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    @JsonProperty("ods")
    public String getOds() {
        return ods;
    }

    public void setOds(String ods) {
        this.ods = ods;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("service_type")
    public ServiceTypeEnum getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceTypeEnum serviceType) {
        this.serviceType = serviceType;
    }

    @JsonProperty("address")
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @JsonProperty("patient_contact")
    public PatientContact getPatientContact() {
        return patientContact;
    }

    public void setPatientContact(PatientContact patientContact) {
        this.patientContact = patientContact;
    }

    @JsonProperty("prescriber_contact")
    public PrescriberContact getPrescriberContact() {
        return prescriberContact;
    }

    public void setPrescriberContact(PrescriberContact prescriberContact) {
        this.prescriberContact = prescriberContact;
    }

    @JsonProperty("location")
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @JsonProperty("opening")
    public OpeningTimes getOpening() {
        return opening;
    }

    public void setOpening(OpeningTimes opening) {
        this.opening = opening;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Dispenser dispenser = (Dispenser) o;
        
        if (dispenser.getOds() != null && !"".equals(dispenser.getOds())){
            return dispenser.getOds().equals(ods);
        }
        
        return Objects.equals(ods, dispenser.ods)
                && Objects.equals(name, dispenser.name)
                && Objects.equals(serviceType, dispenser.serviceType)
                && Objects.equals(address, dispenser.address)
                && Objects.equals(patientContact, dispenser.patientContact)
                && Objects.equals(prescriberContact, dispenser.prescriberContact)
                && Objects.equals(location, dispenser.location)
                && Objects.equals(opening, dispenser.opening);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ods, name, serviceType, address, patientContact, prescriberContact, location, opening);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Dispenser {\n");

        sb.append("    ods: ").append(toIndentedString(ods)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    serviceType: ").append(toIndentedString(serviceType)).append("\n");
        sb.append("    address: ").append(toIndentedString(address)).append("\n");
        sb.append("    patientContact: ").append(toIndentedString(patientContact)).append("\n");
        sb.append("    prescriberContact: ").append(toIndentedString(prescriberContact)).append("\n");
        sb.append("    location: ").append(toIndentedString(location)).append("\n");
        sb.append("    opening: ").append(toIndentedString(opening)).append("\n");
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
