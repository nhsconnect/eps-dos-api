/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.service;

import uk.nhs.digital.eps.dos.model.ApiErrorbase;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import uk.nhs.digital.eps.dos.model.APIException;
import uk.nhs.digital.eps.dos.model.Dispenser;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public class DispenserDetailServiceImpl implements DispenserDetailService {

    private static final Logger LOG = Logger.getLogger(DispenserDetailServiceImpl.class.getName());

    Vertx vertx;
    JsonObject config;

    WebClient client;
    private static final ObjectMapper MAPPER;
    static {
        MAPPER = new ObjectMapper();
        MAPPER
            .disable(MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS)
            .enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
    }

    public static final String DISPENSER_TRANSFORM = "/dispenser_response_to_json.xsl";

    public static final String CHOICES_HOST_KEY = "choices_host";
    private static final String CHOICES_HOST_DEFAULT = "nww.etpwebservices.cfh.nhs.uk";
    public static final String CHOICES_DISPENSER_RESOURCE_KEY = "choices_dispenser";
    public static final String CHOICES_DISPENSER_RESOURCE_DEFAULT = "/ETPWebservices/service.asmx/GetDispenserByNacsCode?strnacscode=%s";
    public static final String CHOICES_SSL_KEY = "choices_use_ssl";
    public static final String CHOICES_PORT_KEY = "choices_port";
    public static final String CHOICES_DISPENSER_SEARCH_RESOURCE_KEY = "choices_search";
    public static final String CHOICES_DISPENSER_SEARCH_RESOURCE_DEFAULT = "/ETPWebservices/service.asmx/GetDispenserByName?strorganisationame=%s&intservicetype=1&streps=YES";
    
    private final int port;
    private final String host;

    public DispenserDetailServiceImpl(Vertx vertx, JsonObject config) {
        LOG.log(Level.INFO, "Dispenser detail service configured with config={0}", config.toString());
        this.vertx = vertx;
        this.config = config;
        
        this.port = config.getInteger(CHOICES_PORT_KEY, 443);
        this.host = config.getString(CHOICES_HOST_KEY, CHOICES_HOST_DEFAULT);

        client = WebClient.create(vertx);
    }
    
    private List<Dispenser> parseDispenserList(String xmlString) throws TransformerConfigurationException, TransformerException, IOException {
        //we need to call retainAll() on this later which isn't supported by the lsit returned by Arrays.asList
        return new ArrayList<>(Arrays.asList(parseResponse(xmlString, Dispenser[].class)));
    }

    private Dispenser parseDispenser(String xmlString) throws TransformerConfigurationException, TransformerException, IOException {
        return parseResponse(xmlString, Dispenser.class);
    }

    
    private <T> T parseResponse(String xmlString, final Class<T> type) throws TransformerConfigurationException, TransformerException, IOException {
        LOG.log(Level.FINE, "Parsing response");

        StringReader reader = new StringReader(xmlString);
        StringWriter writer = new StringWriter();

        SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();

        Source xslt = new StreamSource(this.getClass().getResourceAsStream(DISPENSER_TRANSFORM));

        Transformer t = stf.newTransformer(xslt);

        t.transform(new StreamSource(reader), new StreamResult(writer));
        String out = writer.toString();
        return MAPPER.readValue(out, type);
    }

    @Override
    public void dispenserDetail(String requestId, String ods, Handler<AsyncResult<Dispenser>> serviceResponseHandler) {
        LOG.log(Level.FINE, "dispenserDetail service call with request.id={0}", requestId);

        if (Strings.isNullOrEmpty(ods)) {
            LOG.log(Level.FINE, "Null or empty ODS parameter in query  with request.id={0}", requestId);
            serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.INVALID_PARAMETER, "ods", null)));
            return;
        }

        String resource=String.format(config.getString(CHOICES_DISPENSER_RESOURCE_KEY, CHOICES_DISPENSER_RESOURCE_DEFAULT), ods);
        HttpRequest<Buffer> request = client.get(port,host,resource)
            .ssl(config.getBoolean(CHOICES_SSL_KEY, true))
            .putHeader("x-Request-Id", requestId);
        
        LOG.log(Level.FINE, "Requesting {0}:{1}{2} x-Request-Id: {3}", new Object[]{host, port, resource, requestId});

        request.send(response -> {
            if (response.failed()) {
                //no response
                LOG.log(Level.INFO, "Choices query for request.id={0} failed with exception {1}", new Object[]{requestId, response.cause().toString()});
                serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.SEARCH_NOT_RESPONDING)));
            } else {
                //there was a response
                if (response.result().statusCode() > 204) {
                    //an error response
                    switch (response.result().bodyAsString().trim()) {
                    case "NACS_CODE_NOT_RECOGNISED":
                        LOG.log(Level.INFO, "Dispenser with ODS={0} not found for query with request.id={1}", requestId);
                        serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.NOT_FOUND)));
                        break;
                    case "NACS_CODE_WRONG_FORMAT":
                        LOG.log(Level.INFO, "Query with request.id={1} had ODS={0} not correct format", new Object[]{ods, requestId});
                        serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.INVALID_PARAMETER, "ods", null)));
                        break;
                    default:
                        LOG.log(Level.INFO, "Query with ODS={0} and request.id={1} failed with response {2}", new Object[]{ods, requestId, response.result().bodyAsString().trim()});
                        serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.UNKNOWN)));
                        break;
                    } 
                } else {
                    //a dispenser response
                    try {
                        String unescapedResponse = response.result().bodyAsString().replaceAll("&lt;", "<").replaceAll("&gt;", ">");
                        serviceResponseHandler.handle(Future.succeededFuture(parseDispenser(unescapedResponse)));
                    } catch (TransformerException | IOException e) {
                        serviceResponseHandler.handle(Future.failedFuture(e));
                    }
                }
            }
        });
    }

    @Override
    public void searchDispenserByName(String requestId, String name, Handler<AsyncResult<List<Dispenser>>> serviceResponseHandler) {
        LOG.log(Level.FINE, "searchDispenserByName service call with request.id={0}", requestId);
        if (Strings.isNullOrEmpty(name)) {
            LOG.log(Level.FINE, "Null or empty name parameter in query  with request.id={0}", requestId);
            serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.INVALID_PARAMETER, "name", null)));
            return;
        }
        
        String resource=String.format(config.getString(CHOICES_DISPENSER_SEARCH_RESOURCE_KEY, CHOICES_DISPENSER_SEARCH_RESOURCE_DEFAULT), name);
        HttpRequest<Buffer> request = client.get(port,host,resource)
            .ssl(config.getBoolean(CHOICES_SSL_KEY, Boolean.TRUE))
            .putHeader("x-Request-Id", requestId);
        
        LOG.log(Level.FINE, "Requesting {0}:{1}{2} x-Request-Id: {3}", new Object[]{host, port, resource, requestId});

        request.send(response -> {
            if (response.failed()) {
                //no response
                LOG.log(Level.INFO, "Choices query for request.id={0} failed with exception {1}", new Object[]{requestId, response.cause().toString()});
                serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.SEARCH_NOT_RESPONDING)));
            } else {
                //there was a response
                if (response.result().statusCode() > 204) {
                    //an error response
                    switch (response.result().bodyAsString().trim()) {
                    case "NO_ORGANISATION_NAME":
                        LOG.log(Level.INFO, "Name parameter name={0} not found in query with request.id={1}", new Object[]{name, requestId});
                        serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.INVALID_PARAMETER, "name", null)));
                        break;
                    case "NO_DISPENSERS_FOUND":
                        LOG.log(Level.INFO, "Query with request.id={1} and name={0} returned no result", new Object[]{requestId, name});
                        serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.NO_MATCH)));
                        break;
                    default:
                        LOG.log(Level.INFO, "Query with name={0} and request.id={1} failed with response {2}", new Object[]{name, requestId, response.result().bodyAsString().trim()});
                        serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.UNKNOWN)));
                        break;
                    } 
                } else {
                    //a dispenser response
                    if (response.result().headers().contains("Content-Type") &&
                            response.result().getHeader("Content-Type").startsWith("text/xml"))
                    {
                        try {
                            String unescapedResponse = response.result().bodyAsString().replaceAll("&lt;", "<").replaceAll("&gt;", ">");
                            serviceResponseHandler.handle(Future.succeededFuture(parseDispenserList(unescapedResponse)));
                        } catch (TransformerException | IOException e) {
                            serviceResponseHandler.handle(Future.failedFuture(e));
                        }
                    } else {
                        LOG.log(Level.WARNING, "Non XML reponse to query with name={0} and request.id={1} and response {2}", new Object[]{name, requestId, response.result().bodyAsString().trim()});
                        serviceResponseHandler.handle(Future.failedFuture(new APIException(ApiErrorbase.UNKNOWN)));
                    }
                }
            }
        });
    }

}
