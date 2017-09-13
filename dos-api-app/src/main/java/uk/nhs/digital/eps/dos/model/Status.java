/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.model;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public class Status {
    private APIException error=null;

    public APIException getError() {
        return error;
    }

    public void setError(APIException error) {
        this.error = error;
    }
    
}
