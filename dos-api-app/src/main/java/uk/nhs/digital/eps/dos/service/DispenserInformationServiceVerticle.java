/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.lang.reflect.InvocationTargetException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.BeanUtilsBean;
import uk.nhs.digital.eps.dos.ApiGatewayVerticle;
import uk.nhs.digital.eps.dos.model.APIException;
import uk.nhs.digital.eps.dos.model.ApiErrorbase;
import uk.nhs.digital.eps.dos.model.Dispenser;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public class DispenserInformationServiceVerticle extends AbstractVerticle {

    private static final Logger LOG = Logger.getLogger(DispenserInformationServiceVerticle.class.getName());

    public static final String SEARCH_BY_NAME = "/dispensers/byName/:name";
    public static final String SEARCH_BY_LOCATION = "/dispensers/byLocationOpening";
    public static final String DISPENSER = "/dispenser/:ods";
    public static final String ADDRESS_KEY = "dispenser.http.address";
    public static final String PORT_KEY = "dispenser.http.port";

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER
                .disable(MapperFeature.AUTO_DETECT_CREATORS,
                        MapperFeature.AUTO_DETECT_FIELDS,
                        MapperFeature.AUTO_DETECT_GETTERS,
                        MapperFeature.AUTO_DETECT_IS_GETTERS)
                .enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    private DispenserAccessInformationService dispenserAccessInformation;
    private DispenserDetailService dispenserDetailService;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);

        this.dispenserAccessInformation = new DispenserAccessInformationServiceImpl(vertx, config());
        this.dispenserDetailService = new DispenserDetailServiceImpl(vertx, config());
        final Router router = Router.router(vertx);
        router.get(DISPENSER).handler(this::getDispenser);
        router.get(SEARCH_BY_LOCATION).handler(this::searchByLocationOpening);
        router.get(SEARCH_BY_NAME).handler(this::searchByName);

        String host = config().getString(ADDRESS_KEY, "0.0.0.0");
        int port = config().getInteger(PORT_KEY, 8086);

        //TODO: ensure this completes
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(port, host);
    }

    private static String errorResponseAsJson(Object o) {
        try {
            return MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            LOG.severe("JsonProcessingException while parsing response");
            return "{\"code\" = 1, \"message\":\"Unknown error\", \"fields\": null}";
        }
    }

    public static Object merge(Object o1, Object o2) {
        try {
            new BeanUtilsBean() {
                @Override
                public void copyProperty(Object dest, String name, Object value) throws IllegalAccessException, InvocationTargetException {
                    if (value != null) {
                        super.copyProperty(dest, name, value);
                    }
                }
            }.copyProperties(o2, o1);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            LOG.log(Level.SEVERE, "Exception while merging objects");
        }
        return o2;
    }

    private void getDispenser(RoutingContext context) {
        String ods = context.request().getParam("ods");
        String requestId = 
                context.request().headers().contains(ApiGatewayVerticle.REQUEST_ID_HEADER)? 
                    context.request().getHeader(ApiGatewayVerticle.REQUEST_ID_HEADER) : "NO-REQUEST-ID";
        
        LOG.log(Level.INFO, "getDispenser called with param ODS={0} and request.id={1}", new Object[]{ods, requestId});
        
        if (Strings.isNullOrEmpty(ods)){
            rejectRequest(context, "ods", ods, requestId);
            return;
        }
        if (!ods.matches("[A-Z0-9]{5}")){
            rejectRequest(context, "ods", ods, requestId);
            return;
        }
            
        Future<Dispenser> openingInfo = Future.future();
        dispenserAccessInformation.dispenserAccessInformation(requestId, ods, openingInfo.completer());
        Future<Dispenser> detail = Future.future();
        dispenserDetailService.dispenserDetail(requestId, ods, detail.completer());
        CompositeFuture.all(detail, openingInfo).setHandler(responses -> {
            if (responses.succeeded()) {
                LOG.log(Level.INFO, "Queries completed for ODS={0} and request.id={1} merging results", new Object[]{ods, requestId});
                LOG.log(Level.FINE, "Results for ODS={0} and request.id={1} before merge {2} {3}", new Object[]{ods, requestId, detail.result(), openingInfo.result()});
                Dispenser result = (Dispenser) merge(detail.result(), openingInfo.result());
                LOG.log(Level.FINE, "Results for ODS={0} and request.id={1} after merge {2}", new Object[]{ods, requestId, result});
                try {
                    LOG.log(Level.INFO, "Responding with status=success to query for ODS={0} with request.id={1}", new Object[]{ods, requestId});
                    context.response().setStatusCode(200).end(MAPPER.writeValueAsString(result));
                } catch (JsonProcessingException ex) {
                    LOG.log(Level.WARNING, "Exception deserialising merged dipsenser info, responding with status=failed to query for ODS={0} with request.id={1}", new Object[]{ods, requestId});
                    context.response().setStatusCode(ApiErrorbase.UNKNOWN.getStatusCode()).end(errorResponseAsJson(new APIException(ApiErrorbase.UNKNOWN)));
                }
            } else {
                Throwable ex = responses.cause();
                if (ex instanceof APIException) {
                    LOG.log(Level.INFO, "getDispenser failed request.id={1}", new Object[]{ods, requestId});
                    context.response().setStatusCode(((APIException) ex).getStatusCode()).end(errorResponseAsJson(responses.cause()));
                } else {
                    LOG.log(Level.WARNING, "getDispenser failed with unknown error request.id={1} exception={0}", new Object[]{ex.getMessage(), requestId});
                    context.response().setStatusCode(ApiErrorbase.UNKNOWN.getStatusCode()).end(errorResponseAsJson(new APIException(ApiErrorbase.UNKNOWN)));
                }
            }
        });
    }

    private void searchByName(RoutingContext context) {
        String name = context.request().getParam("name");
        String postcode = context.request().getParam("postcode");
        String distanceString = context.request().getParam("distance");
        double distance;

        String requestId = 
            context.request().headers().contains(ApiGatewayVerticle.REQUEST_ID_HEADER)? 
                context.request().getHeader(ApiGatewayVerticle.REQUEST_ID_HEADER) : "NO-REQUEST-ID";
        LOG.log(Level.INFO, "searchByName REST request recieved with param name={0} and request.id={1}", new Object[]{name, requestId});
        
        if (!Strings.isNullOrEmpty(postcode) && !postcode.matches("[A-Z]{1,2}[0-9][0-9A-Z]?[0-9][A-Z]{2}")) {
            rejectRequest(context, "postcode", postcode, requestId);
            return;
        }
        
        if (!Strings.isNullOrEmpty(distanceString)) {
            try {
                distance = Double.parseDouble(distanceString);
            } catch (NumberFormatException ex) {
                rejectRequest(context, "distance", distanceString, requestId);
                return;
            }
            if (distance <= 0.0) {
                rejectRequest(context, "distance", distanceString, requestId);
                return;
            }
        }

//this marks the success of the whole query
        Future<List<Dispenser>> queryFuture = Future.future();

        Future<List<Dispenser>> detail = Future.future();
        dispenserDetailService.searchDispenserByName(requestId, name, detail.completer());

        //once the detail search is complete get access info for each dispenser returned
        detail.compose((List<Dispenser> detailList) -> {
            List<Future> openingTimeResults
                    = detailList.stream().map(dispenserDetail -> {
                        Future<Dispenser> future = Future.future();
                        dispenserAccessInformation.dispenserAccessInformation(requestId, dispenserDetail.getOds(), future.completer());
                        return future;
                    })
                    .collect(Collectors.toList());

            CompositeFuture.join(openingTimeResults).setHandler(ar -> {
                LOG.log(Level.FINE, "query openingTimeResults running");
                List<Future> openingTimeSuccessfulResults = openingTimeResults;
                //get rid of any failed results
                if (ar.failed()) {
                    openingTimeSuccessfulResults = openingTimeResults.stream().filter(result -> result.succeeded()).collect(Collectors.toList());
                }
                //convert Future to Dispenser
                List<Dispenser> openingTimeDispensers = openingTimeSuccessfulResults.stream().map(f -> (Dispenser) f.result()).collect(Collectors.toList());
                //remove any dispenser for which we don't have opening time detail

                List<Dispenser> detailDispensers = detail.result();
                boolean noMatch = detailDispensers.retainAll(openingTimeDispensers);

                if (noMatch) {
                    LOG.log(Level.WARNING, "No matching dispenserAccessInformation for query for name={0} request.id={1}", new Object[]{name, requestId});
                }

                if (detailList.isEmpty()) {
                    queryFuture.fail(new APIException(ApiErrorbase.NO_MATCH));
                }

                List<Dispenser> resultList = openingTimeDispensers.stream()
                        .map(dispenserWithOpening -> {
                            Dispenser d = (Dispenser) merge(detailList.stream().filter(dispenserWithName -> dispenserWithName.equals(dispenserWithOpening)).findFirst().get(), dispenserWithOpening);
                            return dispenserWithOpening;
                        })
                        .collect(Collectors.toList());
                queryFuture.complete(resultList);
            });

        }, queryFuture);

        queryFuture.setHandler(ar -> {
            if (ar.succeeded()) {
                try {
                    context.response().setStatusCode(200).end(MAPPER.writeValueAsString(ar.result()));
                } catch (JsonProcessingException ex) {
                    LOG.log(Level.SEVERE, "Unable to map result to json for request.id={0}", requestId);
                    context.response().setStatusCode(ApiErrorbase.UNKNOWN.getStatusCode()).end(errorResponseAsJson(new APIException(ApiErrorbase.UNKNOWN)));
                }
            } else {
                Throwable ex = ar.cause();
                if (ex instanceof APIException) {
                    context.response().setStatusCode(((APIException) ex).getStatusCode()).end(errorResponseAsJson(ar.cause()));
                } else {
                    context.response().setStatusCode(ApiErrorbase.UNKNOWN.getStatusCode()).end(errorResponseAsJson(new APIException(ApiErrorbase.UNKNOWN)));
                }
            }

        });

    }

    private static void rejectRequest(RoutingContext context, String parameter, String value, String requestId) {
        context.response().setStatusCode(ApiErrorbase.INVALID_PARAMETER.getStatusCode())
                .end(errorResponseAsJson(new APIException(ApiErrorbase.INVALID_PARAMETER, parameter, null)));
        LOG.log(Level.INFO, "request with request.id={0} rejected due to bad parameter name={1} value={2}",
                new Object[]{requestId, parameter, value});
    }

    private void searchByLocationOpening(RoutingContext context) {
        String postcode = context.request().getParam("postcode");
        String distanceString = context.request().getParam("distance");
        String timeStart = context.request().getParam("availability_start");
        String hoursString = context.request().getParam("open_within");
        String requestId = 
            context.request().headers().contains(ApiGatewayVerticle.REQUEST_ID_HEADER)? 
                context.request().getHeader(ApiGatewayVerticle.REQUEST_ID_HEADER) : "NO-REQUEST-ID";
        LOG.log(Level.INFO, "searchByLocationOpening request with param postcode={0} distance={1} availability_start={2} open_within={3} request.id={4}",
                new Object[]{postcode, distanceString, timeStart, hoursString, requestId});

        double distance = 36.0; //default =36
        Date date = new Date(); // default = now
        int hours;

        if (Strings.isNullOrEmpty(postcode)) {
            rejectRequest(context, "postcode", postcode, requestId);
            return;
        }
        if (!postcode.matches("[A-Z]{1,2}[0-9][0-9A-Z]?[0-9][A-Z]{2}")) {
            rejectRequest(context, "postcode", postcode, requestId);
            return;
        }

        if (!Strings.isNullOrEmpty(timeStart)) {
            try {
                date = Date.from(Instant.from(ZonedDateTime.parse(timeStart, DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
            } catch (DateTimeException | IllegalArgumentException e) {
                rejectRequest(context, "availability_start", timeStart, requestId);
                return;
            }
        }

        if (!Strings.isNullOrEmpty(distanceString)) {
            try {
                distance = Double.parseDouble(distanceString);
            } catch (NumberFormatException ex) {
                rejectRequest(context, "distance", distanceString, requestId);
                return;
            }
            if (distance <= 0.0) {
                rejectRequest(context, "distance", distanceString, requestId);
                return;
            }
        }

        if (!Strings.isNullOrEmpty(hoursString)) {
            try {
                hours = Integer.parseInt(hoursString);
            } catch (NumberFormatException ex) {
                rejectRequest(context, "open_within", hoursString, requestId);
                return;
            }
            if (hours <= 0) {
                rejectRequest(context, "open_within", hoursString, requestId);
                return;
            }
        } else {
            rejectRequest(context, "open_within", hoursString, requestId);
            return;
        }

        //TODO: this follows the same pattern as the other query above: parameterzie call a JoiningQuery object
        //this marks the success of the whole query
        Future<List<Dispenser>> queryFuture = Future.future();

        Future<List<Dispenser>> opening = Future.future();
        dispenserAccessInformation.searchDispensersAvailableFromWithin(requestId, date, hours, distance, postcode, opening.completer());

        opening.compose((List<Dispenser> openingTimeList) -> {
            List<Future> detailResults
                    = openingTimeList.stream().map(openingResult -> {
                        Future<Dispenser> future = Future.future();
                        dispenserDetailService.dispenserDetail(requestId, openingResult.getOds(), future.completer());
                        return future;
                    }).collect(Collectors.toList());

            CompositeFuture.join(detailResults).setHandler(ar -> {
                List<Future> detailSuccessfulResults = detailResults;
                if (ar.failed()) {
                    detailSuccessfulResults = detailResults.stream().filter(result -> result.succeeded()).collect(Collectors.toList());
                }
                List<Dispenser> detailDispensers = detailSuccessfulResults.stream().map(f -> (Dispenser) f.result()).collect(Collectors.toList());
                List<Dispenser> openingDispensers = opening.result();
                boolean noMatch = openingDispensers.retainAll(detailDispensers);
                if (noMatch) {
                    LOG.log(Level.WARNING, "Access information with no detail for query for request.id={o}", requestId);
                }
                if (openingDispensers.isEmpty()) {
                    queryFuture.fail(new APIException(ApiErrorbase.NO_MATCH));
                }

                List<Dispenser> resultList = detailDispensers.stream()
                        .map(dispenserWithDetail -> {
                            Dispenser d = (Dispenser) merge(openingDispensers.stream().filter(dispenserWithOpening -> dispenserWithOpening.equals(dispenserWithDetail)).findFirst().get(), dispenserWithDetail);
                            return dispenserWithDetail;
                        })
                        .collect(Collectors.toList());
                queryFuture.complete(resultList);

            });

        }, queryFuture);

        queryFuture.setHandler(ar -> {
            if (ar.succeeded()) {
                try {
                    context.response().setStatusCode(200).end(MAPPER.writeValueAsString(ar.result()));
                } catch (JsonProcessingException ex) {
                    LOG.log(Level.SEVERE, "Unable to map result to json for request.id={0}", requestId);
                    context.response().setStatusCode(ApiErrorbase.UNKNOWN.getStatusCode()).end(errorResponseAsJson(new APIException(ApiErrorbase.UNKNOWN)));
                }
            } else {
                Throwable ex = ar.cause();
                if (ex instanceof APIException) {
                    context.response().setStatusCode(((APIException) ex).getStatusCode()).end(errorResponseAsJson(ar.cause()));
                } else {
                    context.response().setStatusCode(ApiErrorbase.UNKNOWN.getStatusCode()).end(errorResponseAsJson(new APIException(ApiErrorbase.UNKNOWN)));
                }
            }

        });
    }

}
