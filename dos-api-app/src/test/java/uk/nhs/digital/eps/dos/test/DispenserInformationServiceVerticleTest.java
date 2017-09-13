/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.test;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import uk.nhs.digital.eps.dos.service.DispenserAccessInformationServiceImpl;
import uk.nhs.digital.eps.dos.service.DispenserDetailServiceImpl;
import uk.nhs.digital.eps.dos.service.DispenserInformationServiceVerticle;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public class DispenserInformationServiceVerticleTest {

    static Vertx vertx;
    JsonObject config;

    public DispenserInformationServiceVerticleTest() {
    }

    @Before
    public void setUp() {

        config = new JsonObject()
                .put(DispenserDetailServiceImpl.CHOICES_DISPENSER_RESOURCE_KEY, "/ETPWebservices/service.asmx/GetDispenserByNacsCode?strnacscode=%s")
                .put(DispenserDetailServiceImpl.CHOICES_DISPENSER_SEARCH_RESOURCE_KEY, "/ETPWebservices/service.asmx/GetDispenserByName?strorganisationame=%s&intservicetype=1&streps=YES")
                .put(DispenserDetailServiceImpl.CHOICES_PORT_KEY, 80)
                .put(DispenserDetailServiceImpl.CHOICES_SSL_KEY, false)
                .put(DispenserDetailServiceImpl.CHOICES_HOST_KEY, "localhost")
                .put(DispenserAccessInformationServiceImpl.PATHWAYS_DISPENSER_RESOURCE_KEY, "/app/controllers/api/v1.0/services/byOdsCode/%s")
                .put(DispenserAccessInformationServiceImpl.PATHWAYS_PORT_KEY, 80)
                .put(DispenserAccessInformationServiceImpl.PATHWAYS_USE_SSL_KEY, false)
                .put(DispenserAccessInformationServiceImpl.PATHWAYS_HOST_KEY,"localhost");

        DeploymentOptions options = new DeploymentOptions().setConfig(config);
        vertx.deployVerticle(new DispenserInformationServiceVerticle(), options);
    }

    @After
    public void tearDown() {
    }

    @BeforeClass
    public static void setUpSuite() {
        vertx = Vertx.vertx();
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
