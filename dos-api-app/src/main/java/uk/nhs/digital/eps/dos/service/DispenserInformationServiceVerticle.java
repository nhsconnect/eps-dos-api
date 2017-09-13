/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
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

    private void getDispenser(RoutingContext context) {
        String ods = context.request().getParam("ods");
        String requestId = context.request().getHeader(ApiGatewayVerticle.REQUEST_ID_HEADER);
        LOG.log(Level.INFO, "getDispenser called with param ODS={0} adn request.id={1}", new Object[]{ods, requestId});
        Future<Dispenser> openingInfo = Future.future();
        dispenserAccessInformation.dispenserAccessInformation(requestId, ods, openingInfo.completer());
        Future<Dispenser> detail = Future.future();
        dispenserDetailService.dispenserDetail(requestId, ods, openingInfo.completer());
        CompositeFuture.all(detail, openingInfo).setHandler(responses -> {
            if (responses.succeeded()) {
                JsonObject detailJson = JsonObject.mapFrom(detail.result());
                JsonObject openingJson = JsonObject.mapFrom(openingInfo.result());
                JsonObject responseJson = detailJson.mergeIn(openingJson, true);
                context.response().setStatusCode(200).end(responseJson.encodePrettily());
            }
        });
    }

    private void searchByName(RoutingContext context) {
        String name = context.request().getParam("name");
        String requestId = context.request().getHeader(ApiGatewayVerticle.REQUEST_ID_HEADER);
        LOG.log(Level.INFO, "searchByName REST request recieved with param name={0} and request.id={1}", new Object[]{name, requestId});
        Future<List<Dispenser>> queryFuture = Future.future();

        Future<List<Dispenser>> detail = Future.future();
        dispenserDetailService.searchDispenserByName(requestId, name, detail.completer());
        
        //once the detail search is complete get access info for each dispenser returned
        detail.compose((List<Dispenser> detailList) -> {
            List<Future> openingTimeResults = 
                detailList.stream().map(dispenserDetail -> {
                    Future<Dispenser> future = Future.future();
                    dispenserAccessInformation.dispenserAccessInformation(requestId, dispenserDetail.getOds(), future.completer());
                    return future;
                })
                .collect(Collectors.toList());
            
            CompositeFuture.all(openingTimeResults).setHandler(ar -> {
                
                List<Future> openingTimeSuccessfulResults = openingTimeResults;
                //get rid of any failed results
                if (ar.failed()) openingTimeSuccessfulResults = openingTimeResults.stream().filter(result -> result.failed()).collect(Collectors.toList());
                //convert Future to Dispenser
                List<Dispenser> openingTimeDispensers = openingTimeSuccessfulResults.stream().map(f->(Dispenser) f.result()).collect(Collectors.toList()) ;
                //remove any dispenser for which we don't have opening time detail        
                boolean noMatch = detailList.retainAll(openingTimeDispensers);
                
                if (noMatch){
                    LOG.log(Level.WARNING, "No matching dispenserAccessInformation for query for name={0} request.id={1}", new Object[]{name, requestId});
                }
                
                if (detailList.isEmpty()){
                    queryFuture.fail(new APIException(ApiErrorbase.NO_MATCH));
                }
                
                List<Dispenser> resultList = openingTimeDispensers.stream()
                    .map(f -> {
                        BeanUtils.copyProperties(detailList.stream().filter( d -> d.equals(d)).findFirst().get(),f);
                        return f;})
                    .collect(Collectors.toList());

                
                queryFuture.complete(resultList);
            });
            
        },queryFuture);
        
        queryFuture.setHandler(ar -> {
            if (ar.succeeded()){
                context.response().setStatusCode(200).end(JsonObject.mapFrom(ar.result()).encodePrettily());
            } else {
                Throwable ex = ar.cause();
                if (ex instanceof APIException){
                    context.response().setStatusCode(((APIException) ex).getStatusCode()).end(JsonObject.mapFrom(ar.cause()).encodePrettily());
                } else {
                    context.response().setStatusCode(ApiErrorbase.UNKNOWN.getStatusCode()).end(JsonObject.mapFrom(new APIException(ApiErrorbase.UNKNOWN)).encodePrettily());
                }
            }
        
        });
                
        

    }

    private void searchByLocationOpening(RoutingContext context) {
        context.response().setStatusCode(ApiErrorbase.SERVICE_DOWN.getStatusCode()).end(JsonObject.mapFrom(new APIException(ApiErrorbase.SERVICE_DOWN)).encodePrettily());
    }
    
    
    
}
