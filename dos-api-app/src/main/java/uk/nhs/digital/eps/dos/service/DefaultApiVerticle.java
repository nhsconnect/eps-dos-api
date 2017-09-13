package uk.nhs.digital.eps.dos.service;

import uk.nhs.digital.eps.dos.service.DosApi;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.math.BigDecimal;

import java.util.List;
import java.util.Map;
import uk.nhs.digital.eps.dos.model.Dispenser;

public class DefaultApiVerticle extends AbstractVerticle {
    final static Logger LOGGER = LoggerFactory.getLogger(DefaultApiVerticle.class); 
    
    final static String DELETE_DISPENSER_SERVICE_ID = "DELETE_dispenser_ods";
    final static String GET_DISPENSER_SERVICE_ID = "GET_dispenser";
    final static String PUT_DISPENSER_SERVICE_ID = "PUT_dispenser_ods";
    final static String GET_DISPENSERS_SERVICE_ID = "GET_dispensers_byLocationOpening";
    final static String GET_DISPENSERS_BYNAME_NAME_SERVICE_ID = "GET_dispensers_byName_name";
    final static String GET_STATUS_SERVICE_ID = "GET_status";
    
    //TODO : create Implementation
    DosApi service = new DosApiTestImpl();

    @Override
    public void start() throws Exception {
        
        //Consumer for DELETE_dispenser_ods
        vertx.eventBus().<JsonObject> consumer(DELETE_DISPENSER_SERVICE_ID).handler(message -> {
            try {
                String ods = message.body().getString("ods");
                service.dispenserDelete(ods, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "DELETE_dispenser_ods");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("DELETE_dispenser_ods", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for GET_dispenser_ods
        vertx.eventBus().<JsonObject> consumer(GET_DISPENSER_SERVICE_ID).handler(message -> {
            try {
                String ods = message.body().getString("ods");
                service.dispenserGet(ods, result -> {
                    if (result.succeeded())
                        message.reply(new JsonObject(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "GET_dispenser_ods");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("GET_dispenser_ods", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for PUT_dispenser_ods
        vertx.eventBus().<JsonObject> consumer(PUT_DISPENSER_SERVICE_ID).handler(message -> {
            try {
                String ods = message.body().getString("ods");
                Dispenser dispenser = Json.mapper.readValue(message.body().getJsonObject("body").encode(), Dispenser.class);
                service.dispenserPut(ods, dispenser, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_dispenser_ods");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_dispenser_ods", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for GET_dispensers_byLocationOpening
        vertx.eventBus().<JsonObject> consumer(GET_DISPENSERS_SERVICE_ID).handler(message -> {
            try {
                String postcode = message.body().getString("postcode");
                Integer openWithin = Json.mapper.readValue(message.body().getString("open_within"), Integer.class);
                Double distance = Json.mapper.readValue(message.body().getString("distance"), Double.class);
                String availabilityStart = message.body().getString("availability_start");
                String serviceType = message.body().getString("service_type");
                service.dispensersByLocationOpening(postcode, openWithin, distance, availabilityStart, serviceType, result -> {
                    if (result.succeeded())
                        message.reply(new JsonArray(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "GET_dispensers_byLocationOpening");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("GET_dispensers_byLocationOpening", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for GET_dispensers_byName_name
        vertx.eventBus().<JsonObject> consumer(GET_DISPENSERS_BYNAME_NAME_SERVICE_ID).handler(message -> {
            try {
                String name = message.body().getString("name");
                String postcode = message.body().getString("postcode");
                BigDecimal distance = Json.mapper.readValue(message.body().getString("distance"), BigDecimal.class);
                service.dispensersByName(name, postcode, distance, result -> {
                    if (result.succeeded())
                        message.reply(new JsonArray(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "GET_dispensers_byName_name");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("GET_dispensers_byName_name", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for GET_status
        vertx.eventBus().<JsonObject> consumer(GET_STATUS_SERVICE_ID).handler(message -> {
            try {
                service.status(result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "GET_status");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("GET_status", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
    }
    
    private void manageError(Message<JsonObject> message, Throwable cause, String serviceName) {
        int code = MainApiException.INTERNAL_SERVER_ERROR.getStatusCode();
        String statusMessage = MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage();
        if (cause instanceof MainApiException) {
            code = ((MainApiException)cause).getStatusCode();
            statusMessage = ((MainApiException)cause).getStatusMessage();
        } else {
            logUnexpectedError(serviceName, cause); 
        }
            
        message.fail(code, statusMessage);
    }
    
    private void logUnexpectedError(String serviceName, Throwable cause) {
        LOGGER.error("Unexpected error in "+ serviceName, cause);
    }
}
