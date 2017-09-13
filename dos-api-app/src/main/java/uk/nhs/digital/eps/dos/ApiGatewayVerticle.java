/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Route;
import java.util.logging.Logger;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.util.UUID;
import java.util.logging.Level;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public class ApiGatewayVerticle extends AbstractVerticle{

    private static final Logger LOG = Logger.getLogger(ApiGatewayVerticle.class.getName());
    
    private static final int DEFAULT_PORT = 8888;
    
    private static final String API_VERSION = "0.0.1";
    
    private static final String API_ROOT = "/v".concat(API_VERSION.concat("/"));
    
    public static final String REQUEST_ID_HEADER = "x-Request-Id";

    @Override
    public void start(Future<Void> future) throws Exception {
        super.start(); 
        String host = config().getString("api.gateway.http.address", "localhost");
        int port = config().getInteger("api.gateway.http.port", DEFAULT_PORT);
        
        Router router= Router.router(vertx);
        
        router.route().path("/v" + API_VERSION).handler(this::dispatchRequests);
        
        router.route("/*").handler(context -> context.response().setStatusCode(404).setStatusMessage("Resource not available in this version"));      
        
        //TODO: enable HTTPS
        HttpServerOptions httpServerOptions = new HttpServerOptions()
        /*  .setSsl(true)
            .setKeyStoreOptions(new JksOptions().setPath("server.jks").setPassword("changeit"))
        */;
                
        
        vertx.createHttpServer(httpServerOptions)
            .requestHandler(router::accept)
            .listen(port, host, ar -> {
                if (ar.succeeded()) {
                    LOG.log(Level.INFO, "Gateway server started on host={0} port={1)", new Object[]{host, port});
                    future.complete();
                } else {
                    LOG.log(Level.SEVERE, "Unable to start server on host={0} port={1}", new Object[]{host, port});
                    future.fail(ar.cause());
                }
            });
    }
    
    private void dispatchRequests(RoutingContext context){
        String requestId = UUID.randomUUID().toString();
        context.request().headers().add(REQUEST_ID_HEADER, requestId);
        LOG.log(Level.INFO, "API request allocated request.id={0}", requestId);
        
        
    }
    
}
