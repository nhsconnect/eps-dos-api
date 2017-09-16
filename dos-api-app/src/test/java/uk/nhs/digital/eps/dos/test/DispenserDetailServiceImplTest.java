/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.nhs.digital.eps.dos.model.APIException;
import uk.nhs.digital.eps.dos.model.Address;
import uk.nhs.digital.eps.dos.model.Dispenser;
import uk.nhs.digital.eps.dos.model.ApiErrorbase;
import uk.nhs.digital.eps.dos.service.DispenserDetailService;
import uk.nhs.digital.eps.dos.service.DispenserDetailServiceImpl;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
@RunWith(VertxUnitRunner.class)
public class DispenserDetailServiceImplTest extends BaseTest {

    private static final Logger LOG = Logger.getLogger(DispenserDetailServiceImplTest.class.getName());
    
    
    private static Vertx vertx;
    private int port;
    private HttpServer server;
    private JsonObject config;
    DispenserDetailService dispenserService;
    
    public DispenserDetailServiceImplTest() {
    }
    
    @BeforeClass
    public static void setUpSuite(){
        vertx = Vertx.vertx();
    }
    
    @Before
    public void setUp(TestContext context) {
        vertx.exceptionHandler(context.exceptionHandler());
        ServerSocket socket;
        try {
            socket = new ServerSocket(0);
            port = socket.getLocalPort();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(FetchDispenserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //create a mock Choices API
        HttpServerOptions options = new HttpServerOptions().setLogActivity(true).setHost("localhost").setPort(port);
        server = vertx.createHttpServer(options);
        
        config = new JsonObject()
                .put(DispenserDetailServiceImpl.CHOICES_HOST_KEY, "localhost")
                .put(DispenserDetailServiceImpl.CHOICES_DISPENSER_RESOURCE_KEY,"/ETPWebservices/service.asmx/GetDispenserByNacsCode?strnacscode=%s")
                .put(DispenserDetailServiceImpl.CHOICES_DISPENSER_SEARCH_RESOURCE_KEY,"/ETPWebservices/service.asmx/GetDispenserByName?strorganisationame=%s&intservicetype=1&streps=YES")
                .put(DispenserDetailServiceImpl.CHOICES_PORT_KEY, port)
                .put(DispenserDetailServiceImpl.CHOICES_SSL_KEY, false);
        
        dispenserService = new DispenserDetailServiceImpl(vertx, config);

    }
    
    @After
    public void tearDown() {
        server.close();
    }
    
    @AfterClass
    public static void tearDownSuite(TestContext context){
        vertx.close(context.asyncAssertSuccess());
    }
    
    @Test
    public void dispenserDetailTest(TestContext context) {
        LOG.entering(getClass().getName(), "dispenserDetailTest");
        Async async = context.async();
        server.requestHandler( request -> {
            request.response()
                    .putHeader("Content-Type", "text/xml")
                    .end(BaseTest.getFile("/choices_dispenser.xml"));})
                .listen(context.asyncAssertSuccess());
        dispenserService.dispenserDetail("dispenserDetailTest", "FLM42", response -> {
            if (response.failed()) LOG.warning(response.cause().getMessage());
            context.assertTrue(response.succeeded());
            context.assertEquals(response.result().getOds(), "FLM42");
            context.assertEquals(response.result().getName(), "Everett Hj (Chemists) Ltd");
            async.complete();
        });
    }
    
    @Test
    public void invalidResponseTest(TestContext context) {
        LOG.entering(getClass().getName(), "invalidResponseTest");
        Async async = context.async();
        server.requestHandler( request -> {request.response().setStatusCode(500).end("Broken server");}).listen(context.asyncAssertSuccess());
        dispenserService.dispenserDetail("invalidResponseTest", "FLM42", response -> {
            context.assertTrue(response.failed());
            context.assertEquals(response.cause(),new APIException(ApiErrorbase.UNKNOWN));
            async.complete();
        });
    }
    
    @Test
    public void noResponseTest(TestContext context) {
        LOG.entering(getClass().getName(), "noResponseTest");
        Async async = context.async();
        server.close(context.asyncAssertSuccess());
        
        dispenserService.dispenserDetail("noResponseTest", "FLM42", response -> {
            context.assertTrue(response.failed());
            context.assertEquals(response.cause(),new APIException(ApiErrorbase.SEARCH_NOT_RESPONDING));
            async.complete();
        });
    }
    
    @Test
    public void emptyOdsTest(TestContext context) {
        LOG.entering(getClass().getName(), "emptyOdsTest");
        Async async = context.async();
        server.requestHandler( request -> {request.response().setStatusCode(500).end(BaseTest.getFile("/choices_invalid_format.response"));}).listen(context.asyncAssertSuccess());

        dispenserService.dispenserDetail("emptyOdsTest", "", response -> {
            context.assertTrue(response.failed());
            context.assertEquals(response.cause(),new APIException(ApiErrorbase.INVALID_PARAMETER, "ods", null));
            async.complete();
        });
    }
    
    @Test
    public void dispenserSearchTest(TestContext context) {
        LOG.entering(getClass().getName(), "dispenserSearchTest");
        Async async = context.async();
        server.requestHandler( request -> {
            request.response()
                    .putHeader("Content-Type", "text/xml")
                    .end(BaseTest.getFile("/choices_dispenser_name.response"));})
                    .listen(context.asyncAssertSuccess());
        dispenserService.searchDispenserByName("dispenserSearchTest", "Bishop", response -> {
            context.assertTrue(response.succeeded());
            Dispenser testDisepenser = new Dispenser("FWF90", "Bishopthorpe Pharmacy", null, 
                    new Address(Arrays.asList("22-24 Acaster Lane","Bishopthorpe", "York", ""), 
                            "YO23 2SJ"), null, null, null, null);
            context.assertTrue(response.result().contains(testDisepenser));
            context.assertEquals(response.result().get(4).getAddress(), testDisepenser.getAddress());
            context.assertEquals(response.result().get(4).getName(), testDisepenser.getName());
            context.assertEquals(response.result().get(4).getPatientContact(), testDisepenser.getPatientContact());
            context.assertEquals(response.result().size(), 6);
            async.complete();
        });
    }
    
    @Test
    public void dispenserSearchNoMatchTest(TestContext context) {
        LOG.entering(getClass().getName(), "dispenserSearchNoMatchTest");
        Async async = context.async();
        server.requestHandler( request -> {request.response().setStatusCode(500).end(BaseTest.getFile("/choices_no_dispenser_match.response"));}).listen(context.asyncAssertSuccess());
        dispenserService.searchDispenserByName("dispenserSearchNoMatchTest", "@", response -> {
            context.assertTrue(response.failed());
            context.assertEquals(new APIException(ApiErrorbase.NO_MATCH), response.cause());
            async.complete();
        });
    }
    
    @Test
    public void dispenserSearchEmptyName(TestContext context) {
        LOG.entering(getClass().getName(), "dispenserSearchEmptyName");
        Async async = context.async();
        server.requestHandler( request -> {request.response().setStatusCode(500).end(BaseTest.getFile("/choices_search_no_name.response"));}).listen(context.asyncAssertSuccess());
        dispenserService.searchDispenserByName("dispenserSearchEmptyName", "", response -> {
            context.assertTrue(response.failed());
            context.assertEquals(new APIException(ApiErrorbase.INVALID_PARAMETER, "name", null), response.cause());
            async.complete();
        });
    }
    
}
