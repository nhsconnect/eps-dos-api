/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.service;

import java.util.Date;
import java.util.List;
import uk.nhs.digital.eps.dos.model.Dispenser;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public interface DispenserAvailableService{
    public List<Dispenser> availableDispensers(String requestId, Date timestamp, int hours, List<Dispenser> dispensers);
}
