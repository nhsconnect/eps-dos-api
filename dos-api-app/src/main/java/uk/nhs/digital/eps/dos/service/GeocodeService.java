/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import uk.nhs.digital.eps.dos.model.Location;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public interface GeocodeService {
    
        public void searchDispensersAvailableFromWithin(String requestId, String postcode, Handler<AsyncResult<Location>> resultHandler);
    
}
