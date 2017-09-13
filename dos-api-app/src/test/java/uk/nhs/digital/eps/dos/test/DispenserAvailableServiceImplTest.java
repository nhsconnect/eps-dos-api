/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.test;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import uk.nhs.digital.eps.dos.model.Dispenser;
import uk.nhs.digital.eps.dos.model.OpeningPeriod;
import uk.nhs.digital.eps.dos.model.OpeningTimes;
import uk.nhs.digital.eps.dos.service.DispenserAvailableService;
import uk.nhs.digital.eps.dos.service.DispenserAvailableServiceImpl;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
@RunWith(VertxUnitRunner.class)
public class DispenserAvailableServiceImplTest {

    private static final Logger LOG = Logger.getLogger(DispenserAvailableServiceImplTest.class.getName());
    
    static Vertx vertx;
    JsonObject config;
    DispenserAvailableService service;
    
    @BeforeClass
    public static void setUpSuite(){
        vertx = Vertx.vertx();
    }
    
    @AfterClass
    public static void tearDownSuite(){
        vertx.close();
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testBasic(TestContext context){
        config = new JsonObject();
        config.put(DispenserAvailableServiceImpl.BANK_HOLIDAY_LIST_KEY, new JsonArray().add("2017-12-25").add("2017-12-26"));
        service = new DispenserAvailableServiceImpl(vertx, config);
        
        List<Dispenser> dispensers = new ArrayList<>();
        
        Map<String,OpeningPeriod> s1 = new HashMap<>();
        OpeningTimes o1 = new OpeningTimes(
                false, 
                null, //sun
                new OpeningPeriod("09:00","12:00"), //mon, 
                new OpeningPeriod("09:00","12:00"), //tue, 
                new OpeningPeriod("09:00","12:00"), //wed, 
                new OpeningPeriod("09:00","12:00"), //thu, 
                new OpeningPeriod("09:00","12:00"), //fri, 
                new OpeningPeriod("09:00","12:00"), //sat, 
                null, //bankHoliday, 
                s1
        );
        Dispenser d1 = new Dispenser("FA111", "D1", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d1);
        Dispenser d2 = new Dispenser("FA222", "D2", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d2);
        Dispenser d3 = new Dispenser("FA333", "D3", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d3);
        Dispenser d4 = new Dispenser("FA444", "D4", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d4);
        Dispenser d5 = new Dispenser("FA555", "D5", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d5);
        Dispenser d6 = new Dispenser("FA666", "D6", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d6);
        Date date = Date.from(ZonedDateTime.parse("2017-09-03T12:00:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant());
        List<Dispenser> response = service.availableDispensers("testBasic", date, 24, dispensers);
        context.assertTrue(response.size() == 5);
        context.assertTrue(response.contains(d1));
        context.assertTrue(response.contains(d2));
        context.assertTrue(response.contains(d3));
        context.assertTrue(response.contains(d4));
        context.assertTrue(response.contains(d5));
        context.assertFalse(response.contains(d6));
    }
    
    @Test
    public void testSameDay(TestContext context){
        config = new JsonObject();
        config.put(DispenserAvailableServiceImpl.BANK_HOLIDAY_LIST_KEY, new JsonArray().add("2017-12-25").add("2017-12-26"));
        service = new DispenserAvailableServiceImpl(vertx, config);
        
        List<Dispenser> dispensers = new ArrayList<>();
        
        //2017-09-03==sunday
        Map<String,OpeningPeriod> s1 = new HashMap<>();
        OpeningTimes o1 = new OpeningTimes(
                false, 
                new OpeningPeriod("09:00","14:00"), //sun
                null, //mon, 
                new OpeningPeriod("09:00","12:00"), //tue, 
                new OpeningPeriod("09:00","12:00"), //wed, 
                new OpeningPeriod("09:00","12:00"), //thu, 
                new OpeningPeriod("09:00","12:00"), //fri, 
                new OpeningPeriod("09:00","12:00"), //sat, 
                null, //bankHoliday, 
                s1
        );
        Dispenser d1 = new Dispenser("FA111", "D1", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d1);
        Dispenser d2 = new Dispenser("FA222", "D2", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d2);
        Dispenser d3 = new Dispenser("FA333", "D3", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d3);
        Dispenser d4 = new Dispenser("FA444", "D4", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d4);
        Dispenser d5 = new Dispenser("FA555", "D5", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d5);
        Dispenser d6 = new Dispenser("FA666", "D6", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d6);
        Date date = Date.from(ZonedDateTime.parse("2017-09-03T12:00:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant());
        List<Dispenser> response = service.availableDispensers("testBasic", date, 24, dispensers);
        context.assertTrue(response.size() == 5);
        context.assertTrue(response.contains(d1));
        context.assertTrue(response.contains(d2));
        context.assertTrue(response.contains(d3));
        context.assertTrue(response.contains(d4));
        context.assertTrue(response.contains(d5));
        context.assertFalse(response.contains(d6));
    }
    
    @Test
    public void testBankHoliday(TestContext context){
        config = new JsonObject();
        config.put(DispenserAvailableServiceImpl.BANK_HOLIDAY_LIST_KEY, new JsonArray().add("2017-09-04").add("2017-12-26"));
        service = new DispenserAvailableServiceImpl(vertx, config);
        
        List<Dispenser> dispensers = new ArrayList<>();
        
        //2017-09-03==sunday
        Map<String,OpeningPeriod> s1 = new HashMap<>();
        Map<String,OpeningPeriod> mondaySpecial = new HashMap<>();
        mondaySpecial.put("2017-09-04", new OpeningPeriod("09:00","12:00"));

        OpeningTimes special = new OpeningTimes(
                false, 
                null, //sun
                new OpeningPeriod("09:00","14:00"), //mon, 
                new OpeningPeriod("09:00","12:00"), //tue, 
                new OpeningPeriod("09:00","12:00"), //wed, 
                new OpeningPeriod("09:00","12:00"), //thu, 
                new OpeningPeriod("09:00","12:00"), //fri, 
                new OpeningPeriod("09:00","12:00"), //sat, 
                null, //bankHoliday, 
                mondaySpecial
        );
        OpeningTimes bhOpen = new OpeningTimes(
                false, 
                null, //sun
                new OpeningPeriod("09:00","12:00"), //mon, 
                new OpeningPeriod("09:00","12:00"), //tue, 
                new OpeningPeriod("09:00","12:00"), //wed, 
                new OpeningPeriod("09:00","12:00"), //thu, 
                new OpeningPeriod("09:00","12:00"), //fri, 
                new OpeningPeriod("09:00","12:00"), //sat, 
                new OpeningPeriod("09:00","14:00"), //bankHoliday, 
                s1
        );
        OpeningTimes bhClosed = new OpeningTimes(
                false, 
                null, //sun
                new OpeningPeriod("09:00","12:00"), //mon, 
                new OpeningPeriod("09:00","12:00"), //tue, 
                new OpeningPeriod("09:00","12:00"), //wed, 
                new OpeningPeriod("09:00","12:00"), //thu, 
                new OpeningPeriod("09:00","12:00"), //fri, 
                new OpeningPeriod("09:00","12:00"), //sat, 
                null, //bankHoliday, 
                s1
        );
        
        Dispenser d1 = new Dispenser("FA111", "D1", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, special);
        dispensers.add(d1);
        Dispenser d2 = new Dispenser("FA222", "D2", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, special);
        dispensers.add(d2);
        Dispenser d3 = new Dispenser("FA333", "D3", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, bhClosed);
        dispensers.add(d3);
        Dispenser d4 = new Dispenser("FA444", "D4", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, bhOpen);
        dispensers.add(d4);
        Dispenser d5 = new Dispenser("FA555", "D5", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, bhOpen);
        dispensers.add(d5);
        Dispenser d6 = new Dispenser("FA666", "D6", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, bhOpen);
        dispensers.add(d6);
        Date date = Date.from(ZonedDateTime.parse("2017-09-03T12:00:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant());
        List<Dispenser> response = service.availableDispensers("testBasic", date, 24, dispensers);
        context.assertTrue(response.size() == 5);
        context.assertTrue(response.contains(d1));
        context.assertTrue(response.contains(d2));
        context.assertFalse(response.contains(d3));
        context.assertTrue(response.contains(d4));
        context.assertTrue(response.contains(d5));
        context.assertTrue(response.contains(d6));
    }
    
    @Test
    public void testBST(TestContext context){
        config = new JsonObject();
        config.put(DispenserAvailableServiceImpl.BANK_HOLIDAY_LIST_KEY, new JsonArray().add("2017-12-25").add("2017-12-26"));
        service = new DispenserAvailableServiceImpl(vertx, config);
        
        List<Dispenser> dispensers = new ArrayList<>();
        
        //2017-09-03==sunday
        Map<String,OpeningPeriod> s1 = new HashMap<>();

        OpeningTimes o1 = new OpeningTimes(
                false, 
                new OpeningPeriod("13:00","14:00"), //sun
                null, //mon, 
                null, //tue, 
                null, //wed, 
                null, //thu, 
                null, //fri, 
                null, //sat, 
                null, //bankHoliday, 
                s1
        );
        
        OpeningTimes o2 = new OpeningTimes(
                false, 
                new OpeningPeriod("12:40","14:00"), //sun
                new OpeningPeriod("09:00","14:00"), //mon, 
                new OpeningPeriod("09:00","12:00"), //tue, 
                new OpeningPeriod("09:00","12:00"), //wed, 
                new OpeningPeriod("09:00","12:00"), //thu, 
                new OpeningPeriod("09:00","12:00"), //fri, 
                new OpeningPeriod("09:00","12:00"), //sat, 
                null, //bankHoliday, 
                s1
        );
        
        /*
        00:30
        00:
        */
             
        Dispenser d1 = new Dispenser("FA111", "D1", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d1);
        Dispenser d2 = new Dispenser("FA222", "D2", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d2);
        Dispenser d3 = new Dispenser("FA333", "D3", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d3);
        Dispenser d4 = new Dispenser("FA444", "D4", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o2);
        dispensers.add(d4);
        Dispenser d5 = new Dispenser("FA555", "D5", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o2);
        dispensers.add(d5);
        Dispenser d6 = new Dispenser("FA666", "D6", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o2);
        dispensers.add(d6);
        Date date = Date.from(ZonedDateTime.parse("2017-10-28T12:30:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant());
        List<Dispenser> response = service.availableDispensers("testBasic", date, 24, dispensers);
        LOG.fine("returned dispensers: " + response.size());
        LOG.fine(response.toString());
        context.assertTrue(response.size() == 3);
        context.assertTrue(response.contains(d1));
        context.assertTrue(response.contains(d2));
        context.assertTrue(response.contains(d3));
        context.assertFalse(response.contains(d4));
        context.assertFalse(response.contains(d5));
        context.assertFalse(response.contains(d6));
    }
    
    @Test
    public void test247(TestContext context){
        config = new JsonObject();
        config.put(DispenserAvailableServiceImpl.BANK_HOLIDAY_LIST_KEY, new JsonArray().add("2017-09-04").add("2017-12-26"));
        service = new DispenserAvailableServiceImpl(vertx, config);
        
        List<Dispenser> dispensers = new ArrayList<>();
        
        Map<String,OpeningPeriod> s1 = new HashMap<>();

        OpeningTimes o1 = new OpeningTimes(
                true, 
                null, //sun
                null, //mon, 
                null, //tue, 
                null, //wed, 
                null, //thu, 
                null, //fri, 
                null, //sat, 
                null, //bankHoliday, 
                s1
        );
        
        OpeningTimes o2 = new OpeningTimes(
                true, 
                null, //sun
                null, //mon, 
                null, //tue, 
                null, //wed, 
                null, //thu, 
                null, //fri, 
                null, //sat, 
                new OpeningPeriod("09:00","14:00"), //bankHoliday, 
                s1
        );
        
        Map<String,OpeningPeriod> s2 = new HashMap<>();
        s2.put("2017-09-04", new OpeningPeriod("09:00","14:00"));

        OpeningTimes o3 = new OpeningTimes(
                true, 
                null, //sun
                null, //mon, 
                null, //tue, 
                null, //wed, 
                null, //thu, 
                null, //fri, 
                null, //sat, 
                null, //bankHoliday, 
                s2
        );
        
        Map<String,OpeningPeriod> s3 = new HashMap<>();
        s3.put("2017-09-04", null);

        OpeningTimes o4 = new OpeningTimes(
                true, 
                null, //sun
                null, //mon, 
                null, //tue, 
                null, //wed, 
                null, //thu, 
                null, //fri, 
                null, //sat, 
                null, //bankHoliday, 
                s3
        );
             
        Dispenser d1 = new Dispenser("FA111", "D1", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d1);
        Dispenser d2 = new Dispenser("FA222", "D2", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o1);
        dispensers.add(d2);
        Dispenser d3 = new Dispenser("FA333", "D3", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o4);
        dispensers.add(d3);
        Dispenser d4 = new Dispenser("FA444", "D4", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o2);
        dispensers.add(d4);
        Dispenser d5 = new Dispenser("FA555", "D5", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o2);
        dispensers.add(d5);
        Dispenser d6 = new Dispenser("FA666", "D6", Dispenser.ServiceTypeEnum.PHARMACY, null, null, null, null, o3);
        dispensers.add(d6);
        Date date = Date.from(ZonedDateTime.parse("2017-09-04T03:30:00+01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant());
        List<Dispenser> response = service.availableDispensers("test247", date, 8, dispensers);
        LOG.fine("returned dispensers: " + response.size());
        LOG.fine(response.toString());
        context.assertTrue(response.size() == 3);
        context.assertFalse(response.contains(d1));
        context.assertFalse(response.contains(d2));
        context.assertFalse(response.contains(d3));
        context.assertTrue(response.contains(d4));
        context.assertTrue(response.contains(d5));
        context.assertTrue(response.contains(d6));
    }
       
}
