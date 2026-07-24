package com.ajaxjs.util.date;

import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestDateTypeConvert {
    private final ZoneId zone = ZoneId.systemDefault();

    @Test
    void testDateToLocalDate() {
        Date date = new Date();
        LocalDate expected = date.toInstant().atZone(zone).toLocalDate();

        LocalDate result = new DateTypeConvert(date).to(LocalDate.class, null);
        assertEquals(expected, result);
    }

    @Test
    void testLocalDateToDate() {
        LocalDate localDate = LocalDate.of(2025, 10, 23);
        Date expected = Date.from(localDate.atStartOfDay(zone).toInstant());

        Date result = new DateTypeConvert(localDate).to(Date.class, null);
        assertEquals(expected, result);
    }

    @Test
    void testTimestampToInstant() {
        long timestamp = System.currentTimeMillis();
        Instant expected = Instant.ofEpochMilli(timestamp);

        Instant result = new DateTypeConvert(timestamp).to(Instant.class, null);
        assertEquals(expected, result);
    }

    @Test
    void testEpochZeroToInstant() {
        assertEquals(Instant.EPOCH, new DateTypeConvert(0L).to(Instant.class, ZoneOffset.UTC));
    }

    @Test
    void testInstantToLocalDateTime() {
        Instant instant = Instant.now();
        LocalDateTime expected = instant.atZone(zone).toLocalDateTime();

        LocalDateTime result = new DateTypeConvert(instant).to(LocalDateTime.class, null);
        assertEquals(expected, result);
    }

    @Test
    void testSqlDateToOffsetDateTime() {
        java.sql.Date sqlDate = java.sql.Date.valueOf("2025-10-23");
        OffsetDateTime expected = sqlDate.toLocalDate()
                .atStartOfDay(zone)
                .toInstant()
                .atOffset(zone.getRules().getOffset(sqlDate.toLocalDate().atStartOfDay()));

        OffsetDateTime result = new DateTypeConvert(sqlDate).to(OffsetDateTime.class, null);
        assertEquals(expected.toLocalDate(), result.toLocalDate()); // compare date only
    }

    @Test
    void testOffsetTimeDoesNotInventDate() {
        OffsetTime input = OffsetTime.of(10, 30, 0, 0, ZoneOffset.ofHours(8));

        assertEquals(input, new DateTypeConvert(input).to(OffsetTime.class, null));
        assertEquals(input.toLocalTime(), new DateTypeConvert(input).to(LocalTime.class, null));
        assertThrows(UnsupportedOperationException.class,
                () -> new DateTypeConvert(input).to(Instant.class, null));
    }

    @Test
    void testLocalDateTimeRejectsDstGapAndOverlap() {
        ZoneId newYork = ZoneId.of("America/New_York");

        assertThrows(DateTimeException.class,
                () -> new DateTypeConvert(LocalDateTime.of(2024, 3, 10, 2, 30)).to(Instant.class, newYork));
        assertThrows(DateTimeException.class,
                () -> new DateTypeConvert(LocalDateTime.of(2024, 11, 3, 1, 30)).to(Instant.class, newYork));
    }

    @Test
    void testPreservesOriginalZoneAndOffsetByDefault() {
        ZonedDateTime zoned = ZonedDateTime.of(2025, 1, 2, 3, 4, 5, 0, ZoneId.of("Europe/Paris"));
        OffsetDateTime offset = OffsetDateTime.of(2025, 1, 2, 3, 4, 5, 0, ZoneOffset.ofHoursMinutes(5, 30));

        assertEquals(zoned, new DateTypeConvert(zoned).to(ZonedDateTime.class, null));
        assertEquals(offset, new DateTypeConvert(offset).to(OffsetDateTime.class, null));
    }

    @Test
    void testCalendarUsesRequestedZone() {
        ZoneId tokyo = ZoneId.of("Asia/Tokyo");
        Calendar calendar = new DateTypeConvert(Instant.EPOCH).to(Calendar.class, tokyo);

        assertEquals(tokyo, calendar.getTimeZone().toZoneId());
        assertEquals(0L, calendar.getTimeInMillis());
    }

    @Test
    void testCalendarInputPreservesItsZoneByDefault() {
        ZoneId tokyo = ZoneId.of("Asia/Tokyo");
        Calendar input = Calendar.getInstance(TimeZone.getTimeZone(tokyo));
        input.setTimeInMillis(Instant.EPOCH.toEpochMilli());

        ZonedDateTime result = new DateTypeConvert(input).to(ZonedDateTime.class, null);

        assertEquals(tokyo, result.getZone());
        assertEquals(Instant.EPOCH, result.toInstant());
    }

    @Test
    void testExplicitZoneOverridesCalendarInputZone() {
        ZoneId tokyo = ZoneId.of("Asia/Tokyo");
        ZoneId paris = ZoneId.of("Europe/Paris");
        Calendar input = Calendar.getInstance(TimeZone.getTimeZone(tokyo));
        input.setTimeInMillis(Instant.EPOCH.toEpochMilli());

        ZonedDateTime result = new DateTypeConvert(input).to(ZonedDateTime.class, paris);

        assertEquals(paris, result.getZone());
        assertEquals(Instant.EPOCH, result.toInstant());
    }
}
