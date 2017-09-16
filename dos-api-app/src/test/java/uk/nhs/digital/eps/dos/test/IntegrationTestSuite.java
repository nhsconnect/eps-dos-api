/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.test;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import uk.nhs.digital.eps.dos.service.DispenserAccessInformationService;
import uk.nhs.digital.eps.dos.service.DispenserAccessInformationServiceImpl;
import uk.nhs.digital.eps.dos.service.DispenserDetailService;
import uk.nhs.digital.eps.dos.service.DispenserDetailServiceImpl;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
@RunWith(VertxUnitRunner.class)
public class IntegrationTestSuite extends BaseTest{
    
    static Vertx vertx;
    JsonObject config;
    
    @BeforeClass
    static public void setUp() {
        vertx = Vertx.vertx();
    }
    
    @AfterClass
    public static void tearDown() {
        vertx.close();
    }
    
    @Test
    public void accessInformationServiceTest(TestContext context){
        Async async = context.async();
        config = new JsonObject()
        .put(DispenserAccessInformationServiceImpl.PATHWAYS_AUTH_KEY, "cm9iZ29vY2g6QWJpZ2FpbDAx");
        
        DispenserAccessInformationService dispenserSccessInformationService = new DispenserAccessInformationServiceImpl(vertx, config);
        
        dispenserSccessInformationService.searchDispensersAvailableFromWithin("accessInformationServiceTest-111111", new Date(), 10, 10, "YO231AY", result -> {
            context.assertTrue(result.succeeded());
            async.complete();
        });
    }
    
    @Test
    public void detailServiceTest(TestContext context){
        Async async = context.async();
        config = new JsonObject();
        config.put(DispenserDetailServiceImpl.CHOICES_SSL_KEY, false);
        config.put(DispenserDetailServiceImpl.CHOICES_PORT_KEY, 80);

        
        DispenserDetailService dispenserDetailService = new DispenserDetailServiceImpl(vertx, config);
        
        dispenserDetailService.dispenserDetail("detailServiceTest-222222", "FA242", result -> {
            context.assertTrue(result.succeeded());
            async.complete();
        });
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
