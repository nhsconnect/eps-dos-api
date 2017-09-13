/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.model;

import java.util.HashMap;
import java.util.Map;
import uk.nhs.digital.eps.dos.model.Errorbase;

/**
 *| Code  | Message                                                                                            | Fields           | HTTP Status |
* |-------|----------------------------------------------------------------------------------------------------|------------------|-------------|
* | 1     | Unknown error                                                                                      | `null`           | 500         |
* | 2     | Dispenser not found                                                                                | `ods`            | 404         |
* | 3     | Exceeded rate limit                                                                                | `null`           | 429         |
* | 4     | Invalid parameter                                                                                  | `ods` \| `postcode` \| `distance` \| `open_within` \| `availability_start` \| `service_type` \| `name` | 400 |
* | 5     | Invalid dispenser                                                                                  | `null`           | 400         |
* | 6     | This account is not authorised to use this method                                                  | `null`           | 403         |
* | 7     | Opening time service not responding                                                                | `null`           | 500         |
* | 8     | Dispenser search service not responding                                                            | `null`           | 500         |
* | 9     | Opening time service returned error - {error}                                                      | `null`           | 500         |
* | 10    | Dispenser search service returned error - {error}                                                  | `null`           | 500         |
* | 11    | Authentication is required to access this resource                                                 | `null`           | 403         |
* | 12    | Authentication invalid                                                                             | `null`           | 403         |
* | 13    | Service is down for maintenance                                                                    | `null`           | 501         |
* | 14    | Too many search results                                                                            | `null`           | 500         |
* | 15    | No matching dispenser                                                                              | `null`           | 500         |
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public enum ApiErrorbase implements Errorbase {
    
    UNKNOWN(1, "Unknown error", 500),
    NOT_FOUND(2, "Dispenser not found", 404),
    EXCEEDED_RATE(3, "Exceeded rate limit", 429),
    INVALID_PARAMETER(4, "Invalid parameter", 400),
    INVALID_DISPENSER(5, "Invalid dispenser", 400),
    NOT_AUTHORISED(6, "This account is not authorised to use this method", 403),
    OPENING_TIME_NOT_RESPONDING(7, "Opening time service not responding", 500),
    SEARCH_NOT_RESPONDING(8,"Dispenser search service not responding", 500),
    OPENING_TIME_ERROR(9,"Opening time service returned error - ", 500),
    SEARCH_ERROR(10,"Dispenser search service returned error - ", 500),
    AUTHENTICATION_REQUIRED(11,"Authentication is required to access this resource", 403),
    AUTHENTICATION_INVALID(12,"Authentication invalid", 403),
    SERVICE_DOWN(13,"Service is down for maintenance", 501),
    TOO_MANY_DISPENSERS(14,"Too many search results", 500),
    NO_MATCH(15,"No matching dispenser", 500);

    private final String name;
    private final int code, statusCode;

    private static final Map<Integer, ApiErrorbase> intToEnum = new HashMap<>();

    static { // Initialize map from constant name to enum constant
        for (ApiErrorbase vocab : values()) {
            intToEnum.put(vocab.getCode(), vocab);
        }
    }

    ApiErrorbase(int code, String name, int statusCode) {
        this.code = code;
        this.name = name;
        this.statusCode = statusCode;
    }

    @Override
    public ApiErrorbase fromCode(int code) {
        return intToEnum.get(code);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getStatusCode() {
        return statusCode;
    }
    
    
}
