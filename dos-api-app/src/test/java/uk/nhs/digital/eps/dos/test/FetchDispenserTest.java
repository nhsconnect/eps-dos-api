/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import static org.hamcrest.CoreMatchers.*;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import uk.nhs.digital.eps.dos.model.Address;
import uk.nhs.digital.eps.dos.model.Dispenser;
import uk.nhs.digital.eps.dos.model.PatientContact;
import uk.nhs.digital.eps.dos.model.PrescriberContact;
import uk.nhs.digital.eps.dos.service.DispenserDetailServiceImpl;
import uk.nhs.digital.eps.dos.service.DispenserDetailServiceVerticle;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
@RunWith(VertxUnitRunner.class)
public class FetchDispenserTest extends BaseTest {

    private static final Logger LOG = Logger.getLogger(FetchDispenserTest.class.getName());
    
    

    private Vertx vertx;
    private HttpServer server;
    private ObjectMapper mapper;

    private int port = 8080;
    
    public static String DISPENSER = BaseTest.getFile("/choices_dispenser.xml");
    
    @Before
    public void setUp(TestContext context) {
        Async async = context.async();
        vertx = Vertx.vertx();
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
        
        mapper= new ObjectMapper();
        
        //deploy the choicesClientVerticle
        DeploymentOptions verticleOptions = new DeploymentOptions();
        
        JsonObject config = new JsonObject()
                .put(DispenserDetailServiceImpl.CHOICES_HOST_KEY, "localhost")
                .put(DispenserDetailServiceImpl.CHOICES_DISPENSER_RESOURCE_KEY,"/ETPWebservices/service.asmx/GetDispenserByNacsCode?strnacscode=%s")
                .put(DispenserDetailServiceImpl.CHOICES_PORT_KEY, port)
                .put(DispenserDetailServiceImpl.CHOICES_SSL_KEY, false);
        verticleOptions.setConfig(config);
        vertx.deployVerticle(DispenserDetailServiceVerticle.class.getName(), verticleOptions, context.asyncAssertSuccess());
        async.complete();
    }

    @Test
    public void testDispenserRetrieval(TestContext context) {
        LOG.fine("Testing retrieval");
        Async async = context.async();
        LOG.fine(DISPENSER);
        //Async async = context.async();
        String requestID = UUID.randomUUID().toString();
        server.requestHandler(request->{
            LOG.log(Level.FINE, "request: {0}", request.absoluteURI());
            request.response()
                    .setStatusCode(200)
                    .setStatusMessage("OK")
                    .end(DISPENSER);
        }).listen(result -> {
            if (result.failed()){
                LOG.log(Level.SEVERE, result.cause().getLocalizedMessage());
            }
            LOG.log(Level.FINE, "Server listening");
            context.assertTrue(result.succeeded());
        });
        EventBus eb = vertx.eventBus();
        DeliveryOptions options = new DeliveryOptions();
        Dispenser dispenserQuery = new Dispenser();
        dispenserQuery.setOds("FLM42");
        String queryMessage=null;
        try {
            queryMessage=mapper.writeValueAsString(dispenserQuery);
        } catch (JsonProcessingException ex) {
            context.fail("Error converting query to object");
            Logger.getLogger(FetchDispenserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        options.addHeader("request.id", requestID);
        
        eb.send(DispenserDetailServiceVerticle.ADDRESS, queryMessage, options, (AsyncResult<Message<Object>> response) -> {
            LOG.fine("response message recieved");
            context.assertTrue(response.succeeded());
            
            Dispenser dispenser=null;
        
            try {
                dispenser=mapper.readValue((String)response.result().body(), Dispenser.class);
            }   catch (Exception ex) {
                fail("Error converting response into object");
                Logger.getLogger(FetchDispenserTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            Dispenser testDispenser = new Dispenser("FLM42", "Everett Hj (Chemists) Ltd", 
                    null, 
                    new Address(
                        Arrays.asList("58-60 High Street", "Cosham", "Hampshire", ""),
                        "PO6 3AG"
                    ), 
                    null, //new PatientContact("02392 375979", null),
                    null, //new PrescriberContact("02392 375979", "02392 375979", null),
                    null, // Choices response doesn't include location
                    null
            );
            context.assertEquals(testDispenser, dispenser);
            context.assertEquals(requestID, response.result().headers().get("request.id"));
            async.complete();
        });
    }
    
    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }
}
