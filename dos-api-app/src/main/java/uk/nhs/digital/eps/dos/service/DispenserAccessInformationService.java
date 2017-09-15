/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import java.util.Date;
import java.util.List;
import uk.nhs.digital.eps.dos.model.Dispenser;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public interface DispenserAccessInformationService {
    
    public void searchDispensersAvailableFromWithin(String requestId, Date timestamp, int hours, double distance, String postcode, Handler<AsyncResult<List<Dispenser>>> resultHandler);
    
    public void dispenserAccessInformation(String requestId, String odsCode, Handler<AsyncResult<Dispenser>> resultHandler);
    
}
