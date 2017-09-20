/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import java.util.List;
import java.util.Optional;
import uk.nhs.digital.eps.dos.model.Dispenser;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public interface DispenserDetailService {
    
    public void dispenserDetail(String requestId, String ods, Handler<AsyncResult<Dispenser>> responseHandler);
    public void searchDispenserByName(String requestId, String name, Optional<String> postcode, Optional<Double> distance, Handler<AsyncResult<List<Dispenser>>> responseHandler);
    
}
