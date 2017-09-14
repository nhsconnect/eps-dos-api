/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.test;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import uk.nhs.digital.eps.dos.model.Address;
import uk.nhs.digital.eps.dos.model.Dispenser;
import uk.nhs.digital.eps.dos.model.Location;
import uk.nhs.digital.eps.dos.model.OpeningPeriod;
import uk.nhs.digital.eps.dos.model.OpeningTimes;
import uk.nhs.digital.eps.dos.model.PatientContact;
import uk.nhs.digital.eps.dos.model.PrescriberContact;
import uk.nhs.digital.eps.dos.service.DispenserAccessInformationServiceImpl;
import static uk.nhs.digital.eps.dos.service.DispenserAccessInformationServiceImpl.PATHWAYS_USE_SSL_KEY;
import uk.nhs.digital.eps.dos.service.DispenserDetailServiceImpl;
import uk.nhs.digital.eps.dos.service.DispenserInformationServiceVerticle;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
@RunWith(VertxUnitRunner.class)
public class DispenserInformationServiceVerticleTest {

    private static final Logger LOG = Logger.getLogger(DispenserInformationServiceVerticleTest.class.getName());
    

    static Vertx vertx;
    JsonObject config;
    
    int choicesPort, pathwaysPort, verticlePort;
    HttpServer choicesServer, pathwaysServer;
    String verticleDeployId;

    @Before
    public void setUp(TestContext context) {
        vertx.exceptionHandler(context.exceptionHandler());
        
        ServerSocket socket;
        try {
            socket = new ServerSocket(0);
            choicesPort = socket.getLocalPort();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(FetchDispenserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            socket = new ServerSocket(0);
            pathwaysPort = socket.getLocalPort();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(FetchDispenserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            socket = new ServerSocket(0);
            verticlePort = socket.getLocalPort();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(FetchDispenserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        config = new JsonObject()
                .put(DispenserDetailServiceImpl.CHOICES_DISPENSER_RESOURCE_KEY, "/ETPWebservices/service.asmx/GetDispenserByNacsCode?strnacscode=%s")
                .put(DispenserDetailServiceImpl.CHOICES_DISPENSER_SEARCH_RESOURCE_KEY, "/ETPWebservices/service.asmx/GetDispenserByName?strorganisationame=%s&intservicetype=1&streps=YES")
                .put(DispenserDetailServiceImpl.CHOICES_PORT_KEY, choicesPort )
                .put(DispenserDetailServiceImpl.CHOICES_SSL_KEY, false)
                .put(DispenserDetailServiceImpl.CHOICES_HOST_KEY, "localhost")
                .put(DispenserAccessInformationServiceImpl.PATHWAYS_DISPENSER_RESOURCE_KEY, "/app/controllers/api/v1.0/services/byOdsCode/%s")
                .put(DispenserAccessInformationServiceImpl.PATHWAYS_PORT_KEY, pathwaysPort)
                .put(DispenserAccessInformationServiceImpl.PATHWAYS_USE_SSL_KEY, false)
                .put(DispenserAccessInformationServiceImpl.PATHWAYS_HOST_KEY,"localhost")
                .put(DispenserInformationServiceVerticle.PORT_KEY, verticlePort);

        DeploymentOptions options = new DeploymentOptions().setConfig(config);
        DispenserInformationServiceVerticle verticle = new DispenserInformationServiceVerticle();
        vertx.deployVerticle(verticle, options, r -> verticleDeployId = verticle.deploymentID());
        
        choicesServer = vertx.createHttpServer(new HttpServerOptions().setLogActivity(true).setHost("localhost").setPort(choicesPort));
        pathwaysServer = vertx.createHttpServer(new HttpServerOptions().setLogActivity(true).setHost("localhost").setPort(pathwaysPort));
    }

    @After
    public void tearDown(TestContext context) {
        LOG.fine("teardown " + verticleDeployId);
        Async async=context.async();
        choicesServer.close();
        pathwaysServer.close();
        vertx.undeploy(verticleDeployId, ar -> {
            if (ar.succeeded()) {
                async.complete();
            } else {
                context.fail(ar.cause());
                async.complete();
            }
        });
    }

    @BeforeClass
    public static void setUpSuite() {
        vertx = Vertx.vertx();
    }

    //@Test
    public void getDispenserTest(TestContext context) {
        LOG.fine("testing verticle " + verticleDeployId);
         Async async = context.async();

        choicesServer.requestHandler( request -> {
            request.response()
                    .putHeader("Content-Type", "text/xml")
                    .end(BaseTest.getFile("/choices_dispenser_FA242.xml"));})
                    .listen();
        
        pathwaysServer.requestHandler( request -> {
            request.response()
                    .end(BaseTest.getFile("/pathways_dispenser.json"));})
                    .listen();
        
        WebClient client = WebClient.create(vertx);
        client.get(verticlePort, "localhost", "/dispenser/FA242")
                .ssl(false)
                .putHeader("Authorization", "Basic ".concat("auth-placeholder"))
                .putHeader("x-Request-Id", "getDispenserTest-2222222222").send((ar) -> {
                    context.assertTrue(ar.succeeded());
                    LOG.fine("result:" + ar.result().bodyAsString());
                    async.complete();
                });
    }
    
    @Test
    public void getDispenserByNameTest(TestContext context) {
        LOG.fine("getDispenserByNameTest");
         Async async = context.async();

        choicesServer.requestHandler( request -> {
            request.response()
                    .putHeader("Content-Type", "text/xml")
                    .end(BaseTest.getFile("/choices_dispenser_FA242.xml"));})
                    .listen();
        
        pathwaysServer.requestHandler( request -> {
            request.response()
                    .end(BaseTest.getFile("/pathways_dispenser.json"));})
                    .listen();
        
        WebClient client = WebClient.create(vertx);
        client.get(verticlePort, "localhost", "/dispensers/byName/LloydsPharmacy")
                .ssl(false)
                .putHeader("Authorization", "Basic ".concat("auth-placeholder"))
                .putHeader("x-Request-Id", "getDispenserByNameTest-111111111").send((ar) -> {
                    context.assertTrue(ar.succeeded());
                    LOG.fine("result:" + ar.result().bodyAsString());
                    async.complete();
                });
    }
    
    //@Test
    public void mergeTest(TestContext context){
        LOG.fine("mergeTest");
        Map<String,OpeningPeriod> testDate = new HashMap<>();
        testDate.put("28-08-2017", new OpeningPeriod("10:00", "17:00"));
        Dispenser d1 = new Dispenser(
            "FA242", null, null, null, 
            new PatientContact("01904 623472", ""),
            new PrescriberContact("", "01904 623472", "lp6937@lloydspharmacy.co.uk"),
            new Location(460095, 453531),
            new OpeningTimes(false, 
                //sun    
                null,
                //mon,
                new OpeningPeriod("09:00", "17:30"),
                //tue,
                new OpeningPeriod("09:00", "17:30"),
                //wed,
                new OpeningPeriod("09:00", "17:30"),
                //thu,
                new OpeningPeriod("09:00", "17:30"),
                //fri,
                new OpeningPeriod("09:00", "17:30"),
                //sat
                new OpeningPeriod("09:00", "13:00"),
                //bank hol
                null,
                //specifiedDate
                testDate
            )
        );
        Dispenser d2 = new Dispenser(
            "FA242", "LloydsPharmacy" , null, new Address(Arrays.asList("22-24 Acaster Lane","Bishopthorpe", "York", ""), "YO23 2SJ"), 
            null,
            null,
            null,
            null
        );
        
        Dispenser testDispenser = (Dispenser) DispenserInformationServiceVerticle.merge(d1,d2);
        
        context.assertEquals(testDispenser.getName(), d2.getName());
        
        Dispenser d3 = new Dispenser(
            "FA242", "LloydsPharmacy" , null, new Address(Arrays.asList("22-24 Acaster Lane","Bishopthorpe", "York", ""), "YO23 2SJ"), 
            null,
            null,
            null,
            null
        );
        
        Dispenser testDispenser2 = (Dispenser) DispenserInformationServiceVerticle.merge(d1, d3);
        
        context.assertEquals(testDispenser2.getName(), d3.getName());
    }
    
    @Test
    public void getDispenserByNameMultipleIncompleteTest(TestContext context) {
        LOG.fine("getDispenserByNameMultipleIncompleteTest");
         Async async = context.async();

        choicesServer.requestHandler( request -> {
            request.response()
                    .putHeader("Content-Type", "text/xml")
                    .end(BaseTest.getFile("/choices_dispenser_search_BISHOP.xml"));})
                    .listen(context.asyncAssertSuccess());
        
        pathwaysServer.requestHandler( request -> {
            String[] resource = request.uri().split("/");
            switch (resource[resource.length-1]){
                case "FF072":
                    request.response().end(BaseTest.getFile("/pathways_dispenser_FF072.json"));
                    break;
                case "FMG15":
                    request.response().end(BaseTest.getFile("/pathways_dispenser_FMG15.json"));
                    break;
                default:
                    request.response().end();
            }
        }).listen(context.asyncAssertSuccess());

        WebClient client = WebClient.create(vertx);
        client.get(verticlePort, "localhost", "/dispensers/byName/Bishop")
                .ssl(false)
                .putHeader("Authorization", "Basic ".concat("auth-placeholder"))
                .putHeader("x-Request-Id", "getDispenserByNameTest-3333333333").send((ar) -> {
                    context.assertTrue(ar.succeeded());
                    LOG.fine("result:" + ar.result().bodyAsString());
                    async.complete();
                });
    }
}
