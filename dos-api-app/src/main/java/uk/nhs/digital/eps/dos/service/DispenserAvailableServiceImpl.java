/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.digital.eps.dos.service;

import com.google.common.base.Strings;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.threeten.extra.Interval;
import uk.nhs.digital.eps.dos.model.Dispenser;
import uk.nhs.digital.eps.dos.model.OpeningPeriod;

/**
 *
 * @author Aled Greenhalgh <aled.greenhalgh@nhs.net>
 */
public class DispenserAvailableServiceImpl implements DispenserAvailableService{

    private static final Logger LOG = Logger.getLogger(DispenserAvailableServiceImpl.class.getName());
    
    public static final String BANK_HOLIDAY_LIST_KEY = "bank_holidays";

    private List<String> bankholidays = new ArrayList<>();
    
    Vertx vertx;
    JsonObject config;

    public DispenserAvailableServiceImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
        if (config.containsKey(BANK_HOLIDAY_LIST_KEY)) {
            config.getJsonArray(BANK_HOLIDAY_LIST_KEY).forEach( bh -> {bankholidays.add((String)bh);});
        } else {
            LOG.log(Level.WARNING, "DispenserAvailableServiceImpl starting with no bank holidays configured in {0}", BANK_HOLIDAY_LIST_KEY);
        }
    }
    
    private static Interval intervalFromOpeningPeriod(OpeningPeriod p, Instant day) throws ParseException {
        if (Strings.isNullOrEmpty(p.getClose()) || Strings.isNullOrEmpty(p.getOpen())) {
            throw new IllegalArgumentException("Opening period must be fully bounded");
        }
        ZoneId z = ZoneId.of("Europe/London");
        
        LocalTime openTime = LocalTime.parse(p.getOpen(), DateTimeFormatter.ISO_LOCAL_TIME);
        LocalTime closeTime = LocalTime.parse(p.getClose(), DateTimeFormatter.ISO_LOCAL_TIME);
        
        Instant openInstant = LocalDateTime.of(LocalDate.from(day.atZone(z)), openTime).toInstant(z.getRules().getOffset(day));
        
        Instant closeInstant = LocalDateTime.of(LocalDate.from(day.atZone(z)), closeTime).toInstant(z.getRules().getOffset(day));

        return Interval.of(openInstant, closeInstant);
    }
    
    private static boolean intervalWithinOpeningTimes(Interval interval, List<OpeningPeriod> openingTimes, Instant date) throws ParseException{
        for (OpeningPeriod openingPeriod : openingTimes){
                if (intervalFromOpeningPeriod(openingPeriod, date).overlaps(interval)) return true;
        }
        return false;
    }
    
    private boolean isBankHoliday(Instant timestamp) {
        ZoneId z = ZoneId.of("Europe/London");
        return this.bankholidays.contains(timestamp.atZone(z).toLocalDate().toString());
    }

    private List<OpeningPeriod> getSessionForDate(Dispenser d, Instant timestamp) {
        ZoneId zone = ZoneId.of("Europe/London");
        String date = LocalDate.from(timestamp.atZone(zone)).toString();

        if (d.getOpening().getSpecifiedDate().containsKey(date)) {
            return d.getOpening().getSpecifiedDate().get(date);
        }

        if (isBankHoliday(timestamp)) {
            return d.getOpening().getBankHoliday();
        }

        if (d.getOpening().getOpen247()) {
            List<OpeningPeriod> list = new ArrayList<>(1);
            list.add(new OpeningPeriod("00:00", "23:59"));
            return list ;
        }

        ZoneId z = ZoneId.of("Europe/London");

        switch (timestamp.atZone(z).getDayOfWeek()) {
            case MONDAY:
                return d.getOpening().getMon();
            case TUESDAY:
                return d.getOpening().getTue();
            case WEDNESDAY:
                return d.getOpening().getWed();
            case THURSDAY:
                return d.getOpening().getThu();
            case FRIDAY:
                return d.getOpening().getFri();
            case SATURDAY:
                return d.getOpening().getSat();
            case SUNDAY:
                return d.getOpening().getSun();
        }

        return null;
    }


    @Override
    public List<Dispenser> availableDispensers(String requestId, Date timestamp, int hours, List<Dispenser> dispensers) {
                List<Dispenser> results = new ArrayList<>(5);

        Instant patientIntervalStart = timestamp.toInstant();
        
        Instant tomorrow = timestamp.toInstant().plus(1, ChronoUnit.DAYS);

        Interval patientInterval = Interval.of(patientIntervalStart, Duration.of(hours, ChronoUnit.HOURS));
        
        Iterator<Dispenser> i = dispensers.iterator();
        
        while (i.hasNext() && results.size() < 5){
            Dispenser d = i.next();
            try {
                List<OpeningPeriod> sessionsToday, sessionsTomorrow;
                //sessionToday = getSessionForDate(d, patientIntervalStart.truncatedTo(ChronoUnit.DAYS));
                sessionsToday = getSessionForDate(d, patientIntervalStart);
                sessionsTomorrow = getSessionForDate(d, tomorrow);
                if (sessionsToday != null && intervalWithinOpeningTimes(patientInterval, sessionsToday, patientIntervalStart)) {
                    results.add(d);
                    continue;//pharmacy should appear only once in the resultset
                }   
                if (sessionsTomorrow != null && intervalWithinOpeningTimes(patientInterval, sessionsTomorrow, tomorrow)) {
                    results.add(d);
                    continue;
                }
            } catch (ParseException ex) {
                LOG.log(Level.WARNING, "Exception while parsing  opening time data for dipsenser with ods={0} for query with request.id={1}",
                        new Object[]{d.getOds(), requestId});
                continue; //bad data: skip this dispenser
            }
        }

        return results;
    }
}
