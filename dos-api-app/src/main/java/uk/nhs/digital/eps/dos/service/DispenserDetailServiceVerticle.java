/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.service;

import uk.nhs.digital.eps.dos.model.ApiErrorbase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import joptsimple.internal.Strings;
import uk.nhs.digital.eps.dos.model.APIException;
import uk.nhs.digital.eps.dos.model.Dispenser;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public class DispenserDetailServiceVerticle extends AbstractVerticle {

    private static final Logger LOG = Logger.getLogger(DispenserDetailServiceVerticle.class.getName());
    private static final ObjectMapper mapper;
    static {
        mapper= new ObjectMapper();
        mapper
            .disable(MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    }
    private static String objectToJson(Object obj){
        try {
            String json =  mapper.writeValueAsString(obj);
            LOG.log(Level.FINE,"Mapped object {0} to json {1}", new Object[]{obj, json});
            LOG.fine(json);
            return json;
        } catch (JsonProcessingException ex) {
            LOG.log(Level.SEVERE, "Error converting object to JSON with exception {0}", ex.toString());
            return "";
        }
    }
    
    DispenserDetailService service;
    
    
    public static final String ADDRESS = "dispenser.detail.service";
    
    private void handleDispenserList(AsyncResult<List<Dispenser>> result){
        return;
    }
    
    private void handleDispenser(AsyncResult<Dispenser> result){
        return;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture); 
        service = new DispenserDetailServiceImpl(vertx, config());
        LOG.log(Level.INFO, "DispenserDetailServiceVerticle starting with address {0}", ADDRESS);
        EventBus eb = vertx.eventBus();
        
        eb.consumer(ADDRESS, (Message<String> message) -> {
            final String requestId = message.headers().get("request.id");

            LOG.log(Level.INFO, "Dispenser detail message received with request.id={0}", requestId);
            Dispenser queryDispenser = null;
            
            try {
                queryDispenser = mapper.readValue(message.body(), Dispenser.class);
            } catch (IOException ex) {
                APIException apiEx = new APIException(ApiErrorbase.UNKNOWN, null, ex);
                LOG.log(Level.SEVERE, "Error converting request object in request received with request.id={0} with exception {1}", 
                        new Object[]{requestId, apiEx.getCause().toString()}
                );
                message.fail(apiEx.getCode(), objectToJson(apiEx));
            }
            String dispenserOds = queryDispenser.getOds();
            String dispenserName = queryDispenser.getName();
            
            if (Strings.isNullOrEmpty(dispenserOds)){
                if (Strings.isNullOrEmpty(dispenserName)){
                    APIException ex = new APIException(ApiErrorbase.INVALID_PARAMETER);
                    message.fail(ex.getCode(), objectToJson(ex));
                    return;
                } else{
                    service.searchDispenserByName(requestId, dispenserName, this::handleDispenserList);
                }
                service.dispenserDetail(requestId, dispenserOds, this::handleDispenser);
            }
            
            LOG.log(Level.INFO, "Requesting dispenser ODS={0} with request.id={1}", new Object[]{dispenserOds,requestId});
            
            service.dispenserDetail(requestId, dispenserOds, (AsyncResult<Dispenser> response) -> {
                LOG.log(Level.INFO, "Service response recieved for query for dispenser ODS={0} with request.id={1}", new Object[]{dispenserOds,requestId});
                if (response.failed()){
                    if (response.cause() instanceof APIException){
                        APIException ex = (APIException)response.cause();
                        LOG.log(Level.FINE,"Dispenser not retrieved for request with request.id={0} due to exception {1}", new Object[]{requestId, ex});
                        message.fail(ex.getCode(), objectToJson(ex));
                    } else {
                        APIException ex = new APIException(ApiErrorbase.UNKNOWN, null, response.cause());
                        LOG.log(
                            Level.WARNING, 
                            "Error retrieving dispenser with request.id={0} with exception {1}", 
                            new Object[]{
                                requestId, 
                                ex.getCause().toString()
                            }
                        );
                        message.fail(ex.getCode(), objectToJson(ex));
                    }
                } else {
                    DeliveryOptions options = new DeliveryOptions().addHeader("request.id", requestId);
                    message.reply(objectToJson(response.result()), options);
                }
            });
        });
    }
    
    
    
}
