/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.service;

import uk.nhs.digital.eps.dos.model.ApiErrorbase;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import java.util.ArrayList;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.nhs.digital.eps.dos.model.APIException;
import uk.nhs.digital.eps.dos.model.Dispenser;
import uk.nhs.digital.eps.dos.model.Location;
import uk.nhs.digital.eps.dos.model.OpeningPeriod;
import uk.nhs.digital.eps.dos.model.OpeningTimes;
import uk.nhs.digital.eps.dos.model.PatientContact;
import uk.nhs.digital.eps.dos.model.PrescriberContact;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public class DispenserAccessInformationServiceImpl implements DispenserAccessInformationService {

    private static final Logger LOG = Logger.getLogger(DispenserAccessInformationServiceImpl.class.getName());

    public static final String PATHWAYS_HOST_KEY = "pathways_host";
    public static final String PATHWAYS_USE_SSL_KEY = "pathways_use_ssl";
    private static final String PATHWAYS_HOST_DEFAULT = "uat.pathwaysdos.nhs.uk";
    public static final String PATHWAYS_DISPENSER_RESOURCE_KEY = "pathways_dispenser";
    private static final String PATHWAYS_DISPENSER_RESOURCE_DEFAULT = "/app/controllers/api/v1.0/services/byOdsCode/%s";
    public static final String PATHWAYS_PORT_KEY = "pathways_port";
    public static final String PATHWAYS_DISPENSER_SEARCH_RESOURCE_KEY = "pathways_dispenser_search";
    private static final String PATHWAYS_DISPENSER_SEARCH_RESOURCE_DEFAULT = "/app/controllers/api/v1.0/services/byServiceType/0/%s/%d/0/0/0/0/13/50";
    private static final String PATHWAYS_AUTH_KEY = "pathways_auth";

    private Vertx vertx;
    private JsonObject config;
    private WebClient client;
    private DispenserAvailableService dispenserAvailableService;

    int port;
    String host;

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER
                .disable(MapperFeature.AUTO_DETECT_CREATORS,
                        MapperFeature.AUTO_DETECT_FIELDS,
                        MapperFeature.AUTO_DETECT_GETTERS,
                        MapperFeature.AUTO_DETECT_IS_GETTERS)
                .enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
                .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
    }

    public DispenserAccessInformationServiceImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;

        //warn if using default or insecure config
        if (!config.containsKey(PATHWAYS_AUTH_KEY)) {
            LOG.log(Level.WARNING, "Starting DispenserAccessInformationServiceImpl with no pathways authentication");
        }
        if (!config.containsKey(PATHWAYS_DISPENSER_SEARCH_RESOURCE_KEY)) {
            LOG.log(Level.WARNING, "Starting DispenserAccessInformationServiceImpl with default {0}={1}",
                    new Object[]{PATHWAYS_DISPENSER_SEARCH_RESOURCE_KEY, PATHWAYS_DISPENSER_SEARCH_RESOURCE_KEY}
            );
        }
        if (!config.containsKey(PATHWAYS_DISPENSER_RESOURCE_KEY)) {
            LOG.log(Level.WARNING, "Starting DispenserAccessInformationServiceImpl with default {0}={1}",
                    new Object[]{PATHWAYS_DISPENSER_RESOURCE_KEY, PATHWAYS_DISPENSER_RESOURCE_DEFAULT}
            );
        }
        if (!config.containsKey(PATHWAYS_HOST_KEY)) {
            LOG.log(Level.WARNING, "Starting DispenserAccessInformationServiceImpl with default {0}={1}",
                    new Object[]{PATHWAYS_HOST_KEY, PATHWAYS_HOST_DEFAULT}
            );
        }
        if (config.containsKey(PATHWAYS_USE_SSL_KEY) && !config.getBoolean(PATHWAYS_USE_SSL_KEY)) {
            LOG.log(Level.WARNING, "Starting DispenserAccessInformationServiceImpl with {0}={1}",
                    new Object[]{PATHWAYS_USE_SSL_KEY, config.getBoolean(PATHWAYS_USE_SSL_KEY)}
            );
        }

        this.port = config.getInteger(PATHWAYS_PORT_KEY, 443);
        this.host = config.getString(PATHWAYS_HOST_KEY, PATHWAYS_HOST_DEFAULT);
        this.dispenserAvailableService = new DispenserAvailableServiceImpl(vertx, config);
        this.client = WebClient.create(vertx);
    }

    private static OpeningPeriod periodFromJsonDay(JsonObject day) {

        JsonArray sessions = day.getJsonArray("sessions");
        if (sessions.size() == 0) {
            return null;
        }
        JsonObject session = sessions.getJsonObject(0);
        String startHours = session.getJsonObject("start").getString("hours");
        String startMinutes = session.getJsonObject("start").getString("minutes");
        String endHours = session.getJsonObject("end").getString("hours");
        String endMinutes = session.getJsonObject("end").getString("minutes");
        return new OpeningPeriod(startHours.concat(":".concat(startMinutes)),
                endHours.concat(":".concat(endMinutes))
        );
    }

    private static void populateOpening(Dispenser d, String day, OpeningPeriod time) {
        switch (day) {
            case "Monday":
                d.getOpening().setMon(time);
                break;
            case "Tuesday":
                d.getOpening().setTue(time);
                break;
            case "Wednesday":
                d.getOpening().setWed(time);
                break;
            case "Thursday":
                d.getOpening().setThu(time);
                break;
            case "Friday":
                d.getOpening().setFri(time);
                break;
            case "Saturday":
                d.getOpening().setSat(time);
                break;
            case "Sunday":
                d.getOpening().setSun(time);
                break;
            case "Bankholiday":
                d.getOpening().setBankHoliday(time);
                break;
            default:
                d.getOpening().getSpecifiedDate().put(day, time);
        }
    }

    private static List<Dispenser> parseDispensers(JsonArray json) {
        List<Dispenser> dispensers = new ArrayList<>();
        for (int i = 0; i < json.size(); i++) {
            dispensers.add(parseDispenser(json.getJsonObject(i)));
        }
        return dispensers;
    }

    private static Dispenser parseDispenser(JsonObject jsonDispenser) {
        Dispenser d = new Dispenser();
        try {
            d.setOds(jsonDispenser.getString("odsCode"));
            d.setLocation(new Location(
                    //This is necessary to avoid class cast exception: the API inconsistently represents easting & northing as string and number
                    Double.parseDouble(jsonDispenser.getValue("easting").toString()),
                    Double.parseDouble(jsonDispenser.getValue("northing").toString())
            ));
            d.setPatientContact(
                    new PatientContact(
                            jsonDispenser.getJsonObject("phone").getString("public"),
                            jsonDispenser.getString("web"))
            );
            d.setPrescriberContact(
                    new PrescriberContact(
                            jsonDispenser.getJsonObject("phone").getString("nonPublic"),
                            jsonDispenser.getJsonObject("phone").getString("fax"),
                            jsonDispenser.getString("email"))
            );
            if (jsonDispenser.containsKey("patientDistance")) {
                d.setDistance(Double.parseDouble(jsonDispenser.getString("patientDistance")));
            }
            JsonObject ot = jsonDispenser.getJsonObject("openingTimes");
            d.setOpening(new OpeningTimes());
            d.getOpening().setOpen247(ot.getBoolean("allHours"));
            JsonArray days = ot.getJsonArray("days").addAll(ot.getJsonArray("specifiedDates"));

            for (Object day : days) {
                JsonObject jsonDay = (JsonObject) day;
                populateOpening(d, jsonDay.getString("day", jsonDay.getString("date")), periodFromJsonDay(jsonDay));
            }
        } catch (Exception e) {
            //TODO
            LOG.log(Level.WARNING, "Exception while parsing");
        }
        return d;
    }

    private Future<List<Dispenser>> getDispensersByPostcode(String requestId, String postcode, double distance) {

        Future<List<Dispenser>> future = Future.future();
        String resource;
        try{
            resource = String.format(config.getString(PATHWAYS_DISPENSER_SEARCH_RESOURCE_KEY, PATHWAYS_DISPENSER_SEARCH_RESOURCE_DEFAULT), postcode, distance);
        } catch (IllegalFormatException e){
            LOG.log(Level.SEVERE, "Pathways query resource not in correct format");
            future.fail(new APIException(ApiErrorbase.OPENING_TIME_ERROR));
            return future;
        }
        HttpRequest<Buffer> request = client.get(port, host, resource)
            .ssl(config.getBoolean(PATHWAYS_USE_SSL_KEY, Boolean.TRUE))
            .putHeader("Authorization", "Basic ".concat(config.getString(PATHWAYS_AUTH_KEY, "")))
            .putHeader("x-Request-Id", requestId);

        LOG.log(Level.FINE, "Requesting {0}:{1}{2} x-Request-Id: {3}", new Object[]{host, port, resource, requestId});

        request.send(response -> {
            if (response.failed()) {
                //no response
                LOG.log(Level.INFO, "Pathways query for request.id={0} failed with exception {1}", new Object[]{requestId, response.cause().toString()});
                future.fail(new APIException(ApiErrorbase.SEARCH_NOT_RESPONDING));
            } else //there was a response
            {
                if (response.result().statusCode() > 204) {
                    //an error response
                    switch (response.result().bodyAsString().trim()) {
                        //TODO
                        default:
                            LOG.log(Level.INFO, "Query with request.id={0} failed with response {1}", new Object[]{requestId, response.result().bodyAsString().trim()});
                            future.fail(new APIException(ApiErrorbase.UNKNOWN));
                            break;
                    }
                } else {
                    //a json response
                    LOG.log(Level.INFO, "Response recieved with request.id={0}", requestId);
                    try {
                        JsonObject jsonResponse = response.result().bodyAsJsonObject();
                        if (jsonResponse.containsKey("error")) {
                            future.fail(new APIException(ApiErrorbase.NOT_FOUND));
                            return;
                        }
                        if (jsonResponse.containsKey("success")) {
                            if (jsonResponse.getJsonObject("success").getInteger("serviceCount") == 0) {
                                future.fail(new APIException(ApiErrorbase.NO_MATCH));
                                return;
                            }
                            JsonArray jsonDispensers = jsonResponse.getJsonObject("success").getJsonArray("services");
                            future.complete(parseDispensers(jsonDispensers));
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Exception parsing response for request.id={0} exception {1}", new Object[]{requestId, e});
                        future.fail(new APIException(ApiErrorbase.OPENING_TIME_ERROR));
                    }
                }
            }
        });

        return future;
    }

    private Future<List<Dispenser>> getOpenDispensers(String requestId, Date date, int hours, List<Dispenser> dispensers) {
        Future<List<Dispenser>> future = Future.future();
        future.complete(dispenserAvailableService.availableDispensers(requestId, date, hours, dispensers));
        return future;
    }

    @Override
    public void searchDispensersAvailableFromWithin(String requestId, Date timestamp, int hours, double distance, String postcode, Handler<AsyncResult<List<Dispenser>>> serviceResponseHandler) {

        LOG.log(Level.FINE, "searchDispensersAvailableFromWithin service call with request.id={0}", requestId);
        if (hours < 0 || hours > 24) {
            LOG.log(Level.FINE, "Null or empty hours parameter in query  with request.id={0}", requestId);
            serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.INVALID_PARAMETER, "open_within", null)));
            return;
        }

        if (distance < 0 || distance > 100) {
            LOG.log(Level.FINE, "Invalid distance parameter in query  with request.id={0}", requestId);
            serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.INVALID_PARAMETER, "distance", null)));
            return;
        }

        Future queryResult = Future.future();
        queryResult.setHandler(serviceResponseHandler);

        Future<List<Dispenser>> nearbyDispensers = getDispensersByPostcode(requestId, postcode, distance);
        nearbyDispensers.compose(d1 -> {
            LOG.log(Level.INFO, "getDispensersByPostcode for request.id={0} successful with results={1}", new Object[]{requestId, d1.size()});
            Future<List<Dispenser>> openDispensers = getOpenDispensers(requestId, timestamp, hours, d1);
            openDispensers.setHandler(queryResult);
        }, queryResult);
    }

    @Override
    public void dispenserAccessInformation(String requestId, String odsCode, Handler<AsyncResult<Dispenser>> serviceResponseHandler) {
        LOG.log(Level.FINE, "dispenserAccessInformation service call with request.id={0}", requestId);
        if (Strings.isNullOrEmpty(odsCode)) {
            LOG.log(Level.FINE, "Null or empty ODSCode parameter in query  with request.id={0}", requestId);
            serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.INVALID_PARAMETER, "ods", null)));
            return;
        }

        String resource = String.format(config.getString(PATHWAYS_DISPENSER_RESOURCE_KEY, PATHWAYS_DISPENSER_RESOURCE_DEFAULT), odsCode);
        HttpRequest<Buffer> request = client.get(port, host, resource)
                .ssl(config.getBoolean(PATHWAYS_USE_SSL_KEY, Boolean.TRUE))
                .putHeader("Authorization", "Basic ".concat(config.getString(PATHWAYS_AUTH_KEY, "")))
                .putHeader("x-Request-Id", requestId);

        LOG.log(Level.FINE, "Requesting {0}:{1}{2} x-Request-Id: {3}", new Object[]{host, port, resource, requestId});

        request.send((AsyncResult<HttpResponse<Buffer>> response) -> {
            if (response.failed()) {
                //no response
                LOG.log(Level.INFO, "pathways query for request.id={0} failed with exception {1}", new Object[]{requestId, response.cause().toString()});
                serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.SEARCH_NOT_RESPONDING)));
            } else//there was a response
            if (response.result().statusCode() > 204) {
                //an error response
                switch (response.result().bodyAsString().trim()) {
                    //TODO
                    default:
                        LOG.log(Level.INFO, "Query with ODS={0} and request.id={1} failed with response {2}", new Object[]{odsCode, requestId, response.result().bodyAsString().trim()});
                        serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.UNKNOWN)));
                        break;
                }
            } else {
                //a json response
                LOG.log(Level.INFO, "Response recieved with request.id={0}", requestId);
                try {
                    JsonObject jsonResponse = response.result().bodyAsJsonObject();
                    if (jsonResponse.containsKey("error")) {
                        LOG.log(Level.INFO, "Error response recieved for request.id={0}", requestId);
                        serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.OPENING_TIME_ERROR)));
                    } else if (jsonResponse.containsKey("success")) {
                        switch (jsonResponse.getJsonObject("success").getInteger("serviceCount")) {
                            case 0:
                                serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.NOT_FOUND)));
                                break;
                            case 1:
                                JsonObject jsonDispenser = jsonResponse.getJsonObject("success").getJsonArray("services").getJsonObject(0);
                                Dispenser d = parseDispenser(jsonDispenser);
                                serviceResponseHandler.handle(Future.succeededFuture(d));
                                break;
                            default:
                                LOG.log(Level.WARNING, "Multiple matches for ODS query with ODS={0} and request.id={1}", new Object[]{odsCode, requestId});
                                serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.OPENING_TIME_ERROR)));
                                break;
                        }
                    } else {
                        LOG.log(Level.WARNING, "Unrecognised response for request.id={0}", requestId);
                        serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.OPENING_TIME_ERROR)));
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Exception parsing response for request.id={0} exception {1}", new Object[]{requestId, e});
                    serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.OPENING_TIME_ERROR)));
                }
            }
        });

    }

}
