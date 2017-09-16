/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.util;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.nhs.digital.eps.dos.service.DispenserInformationServiceVerticle;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public class VertxStarter {

    private static final Logger LOG = Logger.getLogger(VertxStarter.class.getName());
    

    public VertxStarter() {
        
    }
    public static String getFile(String filename){
        try {
            URL resource = VertxStarter.class.getResource(filename);
            if (resource == null) throw new FileNotFoundException();
            return new String(Files.readAllBytes(Paths.get(resource.toURI())),StandardCharsets.UTF_8);
        } catch (URISyntaxException | IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return "";
    }
    public static void main(String[] args){
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new DispenserInformationServiceVerticle(),new DeploymentOptions().setConfig(new JsonObject(getFile("/dos-local.json"))));
    }
}
