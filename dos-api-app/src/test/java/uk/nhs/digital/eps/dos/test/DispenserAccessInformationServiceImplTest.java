/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.test;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.io.IOException;
import java.net.ServerSocket;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import uk.nhs.digital.eps.dos.model.APIException;
import uk.nhs.digital.eps.dos.model.Dispenser;
import uk.nhs.digital.eps.dos.model.Location;
import uk.nhs.digital.eps.dos.model.OpeningPeriod;
import uk.nhs.digital.eps.dos.model.OpeningTimes;
import uk.nhs.digital.eps.dos.model.PatientContact;
import uk.nhs.digital.eps.dos.model.PrescriberContact;
import uk.nhs.digital.eps.dos.model.ApiErrorbase;
import uk.nhs.digital.eps.dos.service.DispenserAccessInformationService;
import uk.nhs.digital.eps.dos.service.DispenserAccessInformationServiceImpl;
import uk.nhs.digital.eps.dos.service.DispenserDetailService;
import uk.nhs.digital.eps.dos.service.DispenserDetailServiceImpl;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
@RunWith(VertxUnitRunner.class)
public class DispenserAccessInformationServiceImplTest extends BaseTest {

    private static final Logger LOG = Logger.getLogger(DispenserAccessInformationServiceImplTest.class.getName());
    
    
    private static Vertx vertx;
    private int port;
    private HttpServer server;
    private JsonObject config;
    DispenserAccessInformationService dispenserService;
    
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
            LOG.log(Level.SEVERE, null, ex);
        }
        
        HttpServerOptions options = new HttpServerOptions().setLogActivity(true).setHost("localhost").setPort(port);
        server = vertx.createHttpServer(options);
        
        config = new JsonObject()
                .put(DispenserAccessInformationServiceImpl.PATHWAYS_DISPENSER_RESOURCE_KEY, "/app/controllers/api/v1.0/services/byOdsCode/%s")
                .put(DispenserAccessInformationServiceImpl.PATHWAYS_HOST_KEY, "localhost")
                .put(DispenserAccessInformationServiceImpl.PATHWAYS_PORT_KEY, port)
                .put(DispenserAccessInformationServiceImpl.PATHWAYS_USE_SSL_KEY, false);
        
        this.dispenserService = new DispenserAccessInformationServiceImpl(vertx, config);

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
                    .end(BaseTest.getFile("/pathways_dispenser.json"));})
                    .listen(context.asyncAssertSuccess());
        dispenserService.dispenserAccessInformation("dispenserDetailTest", "FA242", response -> {
            if (response.failed()) LOG.warning(response.cause().getMessage() + ":" + response.cause());
            context.assertTrue(response.succeeded());
            Map<String,List<OpeningPeriod>> testDate = new HashMap<>();
            testDate.put("28-08-2017", listOfOpening("10:00", "17:00"));
            Dispenser testDispenser = new Dispenser(
                "FA242", null, null, null, 
                new PatientContact("01904 623472", ""),
                new PrescriberContact("", "01904 623472", "lp6937@lloydspharmacy.co.uk"),
                new Location(460095, 453531),
                new OpeningTimes(false, 
                    //sun    
                    null,
                    //mon,
                    listOfOpening("09:00", "17:30"),
                    //tue,
                    listOfOpening("09:00", "17:30"),
                    //wed,
                    listOfOpening("09:00", "17:30"),
                    //thu,
                    listOfOpening("09:00", "17:30"),
                    //fri,
                    listOfOpening("09:00", "17:30"),
                    //sat
                    listOfOpening("09:00", "13:00"),
                    //bank hol
                    null,
                    //specifiedDate
                    testDate
                )
            );
            context.assertEquals(response.result(), testDispenser);
            async.complete();
        });
    }
    
    @Test
    public void noDispenserTest(TestContext context){
        LOG.entering(getClass().getName(), "noDispenserTest");
        Async async = context.async();
        server.requestHandler( request -> {
            request.response()
                    .end(BaseTest.getFile("/pathways_no_dispenser.json"));})
                    .listen(context.asyncAssertSuccess());
        dispenserService.dispenserAccessInformation("noDispenserTest", "ZZZ", response -> {
            context.assertTrue(response.failed());
            context.assertEquals(response.cause(), new APIException(ApiErrorbase.NOT_FOUND));
            async.complete();
        });
    }
    
    @Test
    public void emptyOdsTest(TestContext context){
        LOG.entering(getClass().getName(), "emptyOdsTest");
        Async async = context.async(2);
        server.close();//invalid request should be filtered
        dispenserService.dispenserAccessInformation("emptyOdsTest1", "", response -> {
            context.assertTrue(response.failed());
            context.assertEquals(response.cause(), new APIException(ApiErrorbase.INVALID_PARAMETER, "ods", null));
            async.countDown();
        });
        dispenserService.dispenserAccessInformation("emptyOdsTest2", "", response -> {
            context.assertTrue(response.failed());
            context.assertEquals(response.cause(), new APIException(ApiErrorbase.INVALID_PARAMETER, "ods", null));
            async.countDown();
        });
    }
    
    @Test(timeout = 300000L)
    public void dispenserSearchTest(TestContext context) {
        LOG.entering(getClass().getName(), "dispenserSearchTest");
        Async async = context.async();
        server.requestHandler( request -> {
            request.response()
                    .end(BaseTest.getFile("/pathways_multiple.json"));})
                    .listen(context.asyncAssertSuccess());
        Date date = Date.from(ZonedDateTime.parse("2017-09-04T09:30:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant());
        dispenserService.searchDispensersAvailableFromWithin("dispenserSearchTest",  date, 2, 10, "YO231AY", (AsyncResult<List<Dispenser>> response) -> {
            if (response.failed()) LOG.warning(response.cause().getMessage() + ":" + response.cause());
            context.assertTrue(response.succeeded());
            Map<String,List<OpeningPeriod>> m = new HashMap<>();
            Dispenser closestDispenser = new Dispenser(
                "FWD97", null, null, null, 
                new PatientContact("01904 623509", ""),
                new PrescriberContact("", "01904 623307", null),
                new Location(460159, 450969),
                new OpeningTimes(false, 
                    //sun    
                    null,
                    //mon,
                    listOfOpening("09:00", "18:00"),
                    //tue,
                    listOfOpening("09:00", "18:00"),
                    //wed,
                    listOfOpening("09:00", "18:00"),
                    //thu,
                    listOfOpening("09:00", "18:00"),
                    //fri,
                    listOfOpening("09:00", "18:00"),
                    //sat
                    listOfOpening("09:00", "13:00"),
                    //bank hol
                    null,
                    //specifiedDate
                    m
                )
            );
            context.assertEquals(response.result().get(0), closestDispenser);
            context.assertTrue(5==response.result().size());
            async.complete();
        });
    }
    
}