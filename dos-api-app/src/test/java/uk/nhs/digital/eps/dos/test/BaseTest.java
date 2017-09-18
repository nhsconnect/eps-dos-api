/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.nhs.digital.eps.dos.model.OpeningPeriod;

/**
 * Utility class for unit tests.
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public abstract class BaseTest {

    /**
     * Manipulate environment variables for use in unit testing.
     * @param newenv 
     */
    protected static void setEnv(Map<String, String> newenv) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            try {
                Class[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for (Class cl : classes) {
                    if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(newenv);
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    
    /**
     * Load a file from the classpath.
     * @param filename
     * @return 
     */
    public static String getFile(String filename){
        try {
            URL resource = FetchDispenserTest.class.getResource(filename);
            if (resource == null) throw new FileNotFoundException();
            return new String(Files.readAllBytes(Paths.get(resource.toURI())),StandardCharsets.UTF_8);
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(BaseTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public static List<OpeningPeriod> listOfOpening(String open, String close) {
        List<OpeningPeriod> list = new ArrayList<>(1);
        list.add(new OpeningPeriod(open, close));
        return list;
    }
}
