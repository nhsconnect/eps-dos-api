/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.test;

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
import io.vertx.ext.web.client.WebClient;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
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

    public DispenserInformationServiceVerticleTest() {
    }

    @Before()
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
    }

    @After
    public void tearDown(TestContext context) {
        Async async=context.async();
        choicesServer.close();
        pathwaysServer.close();
        vertx.undeploy(verticleDeployId, context.asyncAssertSuccess());
    }

    @BeforeClass
    public static void setUpSuite() {
        vertx = Vertx.vertx();
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void getDispenserTest(TestContext context) {
         Async async = context.async();
        choicesServer = vertx.createHttpServer(new HttpServerOptions().setLogActivity(true).setHost("localhost").setPort(choicesPort));
        pathwaysServer = vertx.createHttpServer(new HttpServerOptions().setLogActivity(true).setHost("localhost").setPort(pathwaysPort));

        choicesServer.requestHandler( request -> {
            request.response()
                    .putHeader("Content-Type", "text/xml")
                    .end(BaseTest.getFile("/choices_dispenser_FA242.xml"));})
                    .listen(context.asyncAssertSuccess());
        
        pathwaysServer.requestHandler( request -> {
            request.response()
                    .end(BaseTest.getFile("/pathways_dispenser.json"));})
                    .listen(context.asyncAssertSuccess());
        
        WebClient client = WebClient.create(vertx);
        client.get(verticlePort, "localhost", "/dispenser/FA242")
                .ssl(false)
                .putHeader("Authorization", "Basic ".concat("auth-placeholder"))
                .putHeader("x-Request-Id", "getDispenserTest").send(ar -> {
                    context.assertTrue(ar.succeeded());
                    LOG.fine(ar.result().bodyAsString());
                    async.complete();
                });
    }
}
