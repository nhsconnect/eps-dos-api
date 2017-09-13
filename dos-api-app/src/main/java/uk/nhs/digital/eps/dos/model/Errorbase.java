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
public interface Errorbase {
    public Errorbase fromCode(int code);
    public int getCode();
    public String getName();
}
