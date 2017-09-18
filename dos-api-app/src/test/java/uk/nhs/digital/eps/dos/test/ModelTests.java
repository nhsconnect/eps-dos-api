/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.hamcrest.CoreMatchers.*;
import org.hamcrest.core.IsCollectionContaining;
import static org.junit.Assert.*;
import org.junit.*;
import uk.nhs.digital.eps.dos.model.APIException;

import uk.nhs.digital.eps.dos.model.Dispenser;
import uk.nhs.digital.eps.dos.model.OpeningPeriod;
import uk.nhs.digital.eps.dos.model.OpeningTimes;
import uk.nhs.digital.eps.dos.model.ApiErrorbase;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public class ModelTests extends BaseTest {

    public static final String MAXIMAL_DISPENSER = getFile("/maximal_dispenser.json");
    public static final String DISPENSER_RESULT = getFile("/maximal_dispenser_result.json");

    private Dispenser dispenser;

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.disable(
                MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS
                );
        MAPPER.enable(
            DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT
        );
        
    }

    private static String objectToJson(Object obj) {
        try {
            String json = MAPPER.writeValueAsString(obj);
            return json;
        } catch (JsonProcessingException ex) {
            return "";
        }
    }

    @Test
    public void testMaximalDispenser() {
        try {
            dispenser = MAPPER.readValue(MAXIMAL_DISPENSER, Dispenser.class);
        } catch (IOException ex) {
            Logger.getLogger(ModelTests.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (dispenser == null) {
            fail();
        }
        assertThat("FLM42", equalTo(dispenser.getOds()));
        assertThat("Vantage Pharmacy", equalTo(dispenser.getName()));
        assertThat("eps_pharmacy", equalTo(dispenser.getServiceType().toString()));
        assertThat(
                Dispenser.ServiceTypeEnum.PHARMACY,
                is(dispenser.getServiceType())
        );

        final List<String> address = Arrays.asList("123 Brown Street", null, "York", "North Yorkshire");
        assertThat(dispenser.getAddress().getLine(), IsCollectionContaining.hasItems(address.get(0), address.get(1), address.get(2), address.get(3)));
        assertThat("YO1 3EH", equalTo(dispenser.getAddress().getPostcode()));
        assertThat("01952784465", equalTo(dispenser.getPatientContact().getTel()));
        assertThat("http://www.vantage-pharmacy.co.uk", equalTo(dispenser.getPatientContact().getWebAddress()));
        assertThat("01952784470", equalTo(dispenser.getPrescriberContact().getTel()));
        assertThat("01952784460", equalTo(dispenser.getPrescriberContact().getFax()));
        assertThat(55.45673, equalTo(dispenser.getLocation().getNorthing()));
        assertThat(1.45678, equalTo(dispenser.getLocation().getEasting()));
        assertThat("08:00", equalTo(dispenser.getOpening().getMon().get(0).getOpen()));
        assertThat("18:00", equalTo(dispenser.getOpening().getMon().get(0).getClose()));
        assertThat(dispenser.getOpening().getSun(), is(nullValue()));
        assertThat("09:00", equalTo(dispenser.getOpening().getBankHoliday().get(0).getOpen()));
        assertThat("12:00", equalTo(dispenser.getOpening().getBankHoliday().get(0).getClose()));
        Map<String, List<OpeningPeriod>> specifiedDate = new HashMap<>(1);
        List<OpeningPeriod> list = new ArrayList<>(1);
        list.add(new OpeningPeriod("09:00", "12:00"));
        specifiedDate.put("2018-01-10", list);
        assert (dispenser.getOpening().getSpecifiedDate().entrySet().containsAll(specifiedDate.entrySet()));

    }
/*
    @Test
    public void testDispenserResult() {
        try {
            dispenserResult = MAPPER.readValue(DISPENSER_RESULT, DispenserResult.class);
        } catch (Exception ex) {
            Logger.getLogger(ModelTests.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (dispenserResult == null) {
            fail();
        }
        assertThat("FLM42", equalTo(dispenserResult.getOds()));
        assertThat("Vantage Pharmacy", equalTo(dispenserResult.getName()));
        assertThat("eps_pharmacy", equalTo(dispenserResult.getServiceType().toString()));
        assertThat(
                Dispenser.ServiceTypeEnum.PHARMACY,
                is(dispenserResult.getServiceType())
        );

        final List<String> address = Arrays.asList("123 Brown Street", null, "York", "North Yorkshire");
        assertThat(dispenserResult.getAddress().getLine(), IsCollectionContaining.hasItems(address.get(0), address.get(1), address.get(2), address.get(3)));
        assertThat("YO1 3EH", equalTo(dispenserResult.getAddress().getPostcode()));
        assertThat("01952784465", equalTo(dispenserResult.getPatientContact().getTel()));
        assertThat("http://www.vantage-pharmacy.co.uk", equalTo(dispenserResult.getPatientContact().getWebAddress()));
        assertThat("01952784470", equalTo(dispenserResult.getPrescriberContact().getTel()));
        assertThat("01952784460", equalTo(dispenserResult.getPrescriberContact().getFax()));
        assertThat(55.45673, equalTo(dispenserResult.getLocation().getNorthing()));
        assertThat(1.45678, equalTo(dispenserResult.getLocation().getEasting()));
        assertThat("08:00", equalTo(dispenserResult.getOpening().getMon().getOpen()));
        assertThat("18:00", equalTo(dispenserResult.getOpening().getMon().getClose()));
        assertThat(123.4, equalTo(dispenserResult.getDistance()));
        assertThat(dispenserResult.getOpening().getSun(), is(nullValue()));
        assertThat("09:00", equalTo(dispenserResult.getOpening().getBankHoliday().getOpen()));
        assertThat("12:00", equalTo(dispenserResult.getOpening().getBankHoliday().getClose()));
        Map<String, OpeningPeriod> specifiedDate = new HashMap<>(1);
        specifiedDate.put("2018-01-10", new OpeningPeriod("09:00", "12:00"));
        assert (dispenserResult.getOpening().getSpecifiedDate().entrySet().containsAll(specifiedDate.entrySet()));
    }
*/
    @Test
    public void APIExceptionTest() {
        APIException ex = new APIException(ApiErrorbase.UNKNOWN);
        String json = "";
        try {
            json = MAPPER.writeValueAsString(ex);
        } catch (JsonProcessingException ex1) {
            fail();
        }
        JsonObject jsonObj = new JsonObject(json);
        assertThat(jsonObj.containsKey("stackTrace"), not(true));
        assertThat(1, equalTo(jsonObj.getInteger("code")));
        assertThat(ApiErrorbase.UNKNOWN.getName(), equalTo(jsonObj.getString("message")));
    }

    @Test
    public void APIExceptionStaticTest() {
        APIException ex = new APIException(ApiErrorbase.NOT_FOUND);
        String json = "";
        try {
            json = MAPPER.writeValueAsString((Object) ex);
        } catch (JsonProcessingException ex1) {
            fail();
        }
        JsonObject jsonObj = new JsonObject(json);
        assertThat(jsonObj.containsKey("stackTrace"), not(true));
        assertThat(ApiErrorbase.NOT_FOUND.getCode(), equalTo(jsonObj.getInteger("code")));
        assertThat(ApiErrorbase.NOT_FOUND.getName(), equalTo(jsonObj.getString("message")));
    }

    @Test
    public void APIExceptionConversionTest() {
        APIException ex = new APIException(ApiErrorbase.SEARCH_NOT_RESPONDING);
        String json = "";
        try {
            json = MAPPER.writeValueAsString((Object) ex);
        } catch (JsonProcessingException ex1) {
            fail();
        }
        JsonObject jsonObj = new JsonObject(json);
        assertThat(jsonObj.containsKey("stackTrace"), not(true));
        assertThat(ApiErrorbase.SEARCH_NOT_RESPONDING.getCode(), equalTo(jsonObj.getInteger("code")));
        assertThat(ApiErrorbase.SEARCH_NOT_RESPONDING.getName(), equalTo(jsonObj.getString("message")));
    }

    @Test
    public void dispenserWriteTest() {
        Dispenser d = new Dispenser();
        /*
        {
        "ods":"FLM42",
        "name":"Everett Hj (Chemists) Ltd",
        "address":{
            "line":["58-60 High Street", "Cosham", "", ""],
            "postcode":"PO6 3AG"
        }
    }
        
         */
        d.setOds("FLM42");
        System.out.println(objectToJson(d));
    }

    @Test
    public void dispenserChoicesReadTest() {
        Dispenser d = null;
        String dispenserJson = getFile("/choices_dispenser.json");
        try {
            d = MAPPER.readValue(dispenserJson, Dispenser.class);
        } catch (IOException ex) {
            fail(ex.getLocalizedMessage());
        }
        assertNotNull(d);
        assertThat(d.getOds(), equalTo("FLM42"));

    }
    
    @Test
    public void multipleOpeningTimes(){
        Dispenser d = null;
        String dispenserJson = getFile("/dispenser_multiple_opening.json");
        try {
            d = MAPPER.readValue(dispenserJson, Dispenser.class);
        } catch (IOException ex) {
            fail(ex.getLocalizedMessage());
        }
        List<OpeningPeriod> list = new ArrayList<>(2);
        list.add(new OpeningPeriod("08:00", "12:00"));
        list.add(new OpeningPeriod("13:00", "17:00"));

        assertNotNull(d);
        assertThat(d.getOds(), equalTo("FLM42"));
        assertThat(d.getOpening().getMon().get(0), equalTo(new OpeningPeriod("08:00", "12:00")));
        assertThat(d.getOpening().getMon().get(1), equalTo(new OpeningPeriod("13:00", "17:00")));
        assertThat(d.getOpening().getTue(), equalTo(null));
        assertThat(d.getOpening().getSpecifiedDate().get("28-08-2017"), equalTo(list));
    }
}
