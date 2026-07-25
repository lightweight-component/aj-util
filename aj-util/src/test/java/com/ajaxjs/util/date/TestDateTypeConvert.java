package com.ajaxjs.util.date;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class TestDateTypeConvert {
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
    void testLocalDateToLocalDateDoesNotApplyZoneRules() {
        LocalDate skippedDate = LocalDate.of(2011, 12, 30);

        LocalDate result = new DateTypeConvert(skippedDate)
                .to(LocalDate.class, ZoneId.of("Pacific/Apia"));

        assertSame(skippedDate, result);
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

    // ==================== 新增测试 ====================

    @Test
    void testIntTimestampToInstant() {
        // 测试 int 类型时间戳（秒级）转换为 Instant
        int timestampSeconds = 1681584645; // 2023-04-15 19:50:45 UTC
        Instant result = new DateTypeConvert(timestampSeconds).to(Instant.class, null);
        assertEquals(Instant.ofEpochMilli(1681584645000L), result);
    }

    @Test
    void testTimestampToInstant2() {
        // 测试 java.sql.Timestamp 转换为 Instant
        Timestamp timestamp = Timestamp.from(Instant.EPOCH);
        Instant result = new DateTypeConvert(timestamp).to(Instant.class, null);
        assertEquals(Instant.EPOCH, result);
    }

    @Test
    void testTimestampToDate() {
        // 测试 java.sql.Timestamp 转换为 Date
        Timestamp timestamp = Timestamp.from(Instant.EPOCH);
        Date result = new DateTypeConvert(timestamp).to(Date.class, null);
        assertEquals(Date.from(Instant.EPOCH), result);
    }

    @Test
    void testLocalTimeToLocalTime() {
        // 测试 LocalTime 输入转换为 LocalTime
        LocalTime localTime = LocalTime.of(10, 30, 45);
        LocalTime result = new DateTypeConvert(localTime).to(LocalTime.class, null);
        assertEquals(localTime, result);
    }

    @Test
    void testLocalTimeToOffsetTime() {
        // 测试 LocalTime 输入转换为 OffsetTime
        LocalTime localTime = LocalTime.of(10, 30, 45);
        OffsetTime result = new DateTypeConvert(localTime).to(OffsetTime.class, ZoneOffset.UTC);
        assertEquals(localTime, result.toLocalTime());
    }

    @Test
    void testStringToDate() {
        // 测试 String 输入转换为 Date
        Date result = new DateTypeConvert("2023-04-15 11:30:45").to(Date.class, null);
        assertNotNull(result);
    }

    @Test
    void testLocalDateTimeToLocalDateTime() {
        // 测试 LocalDateTime 输入转换为 LocalDateTime（直接返回）
        LocalDateTime localDateTime = LocalDateTime.of(2023, 4, 15, 11, 30, 45);
        LocalDateTime result = new DateTypeConvert(localDateTime).to(LocalDateTime.class, null);
        assertEquals(localDateTime, result);
    }

    @Test
    void testLocalDateTimeToLocalDate() {
        // 测试 LocalDateTime 输入转换为 LocalDate
        LocalDateTime localDateTime = LocalDateTime.of(2023, 4, 15, 11, 30, 45);
        LocalDate result = new DateTypeConvert(localDateTime).to(LocalDate.class, null);
        assertEquals(LocalDate.of(2023, 4, 15), result);
    }

    @Test
    void testLocalDateTimeToLocalTime() {
        // 测试 LocalDateTime 输入转换为 LocalTime
        LocalDateTime localDateTime = LocalDateTime.of(2023, 4, 15, 11, 30, 45);
        LocalTime result = new DateTypeConvert(localDateTime).to(LocalTime.class, null);
        assertEquals(LocalTime.of(11, 30, 45), result);
    }

    @Test
    void testDateToSqlDate() {
        // 测试 Date 转换为 java.sql.Date
        Date date = Date.from(Instant.EPOCH);
        java.sql.Date result = new DateTypeConvert(date).to(java.sql.Date.class, ZoneOffset.UTC);
        assertEquals(java.sql.Date.valueOf("1970-01-01"), result);
    }

    @Test
    void testDateToTimestamp() {
        // 测试 Date 转换为 Timestamp
        Date date = Date.from(Instant.EPOCH);
        Timestamp result = new DateTypeConvert(date).to(Timestamp.class, null);
        assertEquals(Timestamp.from(Instant.EPOCH), result);
    }

    @Test
    void testInstantToOffsetTime() {
        // 测试 Instant 转换为 OffsetTime
        OffsetTime result = new DateTypeConvert(Instant.EPOCH).to(OffsetTime.class, ZoneOffset.UTC);
        assertNotNull(result);
    }

    @Test
    void testNoInputThrowsException() {
        // 测试没有输入时抛出异常
        DateTypeConvert converter = new DateTypeConvert((Date) null);
        assertThrows(UnsupportedOperationException.class,
                () -> converter.to(Instant.class, null));
    }

    @Test
    void testUnsupportedTargetTypeThrowsException() {
        // 测试不支持的目标类型抛出异常
        assertThrows(UnsupportedOperationException.class,
                () -> new DateTypeConvert(Instant.EPOCH).to(String.class, null));
    }

    @Test
    void testOffsetTimeToUnsupportedTypeThrowsException() {
        // 测试 OffsetTime 转换为不支持的类型抛出异常
        OffsetTime offsetTime = OffsetTime.of(10, 30, 0, 0, ZoneOffset.ofHours(8));
        assertThrows(UnsupportedOperationException.class,
                () -> new DateTypeConvert(offsetTime).to(Date.class, null));
    }
}
