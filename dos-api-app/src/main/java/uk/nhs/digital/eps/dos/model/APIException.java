package uk.nhs.digital.eps.dos.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIgnoreType(true)
public class APIException extends Throwable {

    private Integer code = null;
    private String message = null;
    private String fields = null;
    private Throwable cause = null;

    public APIException() {
        this(ApiErrorbase.UNKNOWN);
    }

    public APIException(Integer code, String message, String fields, Throwable cause) {
        this.code = code;
        this.message = message;
        this.fields = fields;
        this.cause = cause;
    }

    public APIException(Integer code, String message, String fields) {
        this(code, message, fields, null);
    }

    public APIException(ApiErrorbase error, String fields, Throwable cause) {
        this(error.getCode(), error.getName(), fields, cause);
    }

    public APIException(ApiErrorbase error) {
        this(error, null, null);
    }

    @JsonProperty("code")
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("fields")
    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        APIException error = (APIException) o;
        return Objects.equals(code, error.code)
                && Objects.equals(message, error.message)
                && Objects.equals(fields, error.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, fields);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Error {\n");

        sb.append("    code: ").append(toIndentedString(code)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
        sb.append("    fields: ").append(toIndentedString(fields)).append("\n");
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
