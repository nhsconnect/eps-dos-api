/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.lang.js.SucceededResult;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.NoSuchElementException;
import uk.nhs.digital.eps.dos.model.Location;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public class GeocodeServiceImpl implements GeocodeService {

    private static final Logger LOG = Logger.getLogger(GeocodeServiceImpl.class.getName());
    
    private String requestId;
    private Vertx vertx;
    private JsonObject config;
    private static final Map<String,Location> POSTCODE_LOOKUP = new HashMap<>();
    static {
        POSTCODE_LOOKUP.put("YO231AY", new Location(123,456));
    }

    public GeocodeServiceImpl(String requestId, Vertx vertx, JsonObject config) {
        this.requestId = requestId;
        this.vertx = vertx;
        this.config = config;
    }
    
    @Override
    public void searchDispensersAvailableFromWithin(String requestId, String postcode, Handler<AsyncResult<Location>> resultHandler) {
        if (POSTCODE_LOOKUP.containsKey(postcode)) resultHandler.handle(Future.succeededFuture(POSTCODE_LOOKUP.get(postcode)));
        else resultHandler.handle(Future.failedFuture(new NoSuchElementException("Postcode not found")));
    }
   
}
