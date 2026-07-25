package com.ajaxjs.util.date;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TestFormatter {

    @Test
    void testFormat_Default() {
        LocalDateTime localDateTime = LocalDateTime.of(2023, 4, 15, 11, 30, 45);
        Formatter formatter = new Formatter(localDateTime);
        String result = formatter.format();
        assertEquals("2023-04-15 11:30:45", result);
    }

    @Test
    void testFormat_WithCustomFormat() {
        LocalDateTime localDateTime = LocalDateTime.of(2023, 4, 15, 11, 30, 45);
        Formatter formatter = new Formatter(localDateTime);
        String result = formatter.format("yyyy/MM/dd HH:mm");
        assertEquals("2023/04/15 11:30", result);
    }

    @Test
    void testFormat_Date() {
        Date date = DateTools.object2Date("2023-04-15 11:30:45");
        Formatter formatter = new Formatter(date);
        String result = formatter.format();
        assertEquals("2023-04-15 11:30:45", result);
    }

    @Test
    void testFormat_LocalDate() {
        LocalDate localDate = LocalDate.of(2023, 4, 15);
        Formatter formatter = new Formatter(localDate);
        String result = formatter.format(Formatter.DATE);
        assertEquals("2023-04-15", result);
    }

    @Test
    void testGetDateFormatter_WithFormat() {
        DateTimeFormatter formatter = Formatter.getDateFormatter("yyyy-MM-dd");
        assertNotNull(formatter);
        // 测试缓存机制，再次获取应该是同一个对象
        DateTimeFormatter formatter2 = Formatter.getDateFormatter("yyyy-MM-dd");
        assertSame(formatter, formatter2);
    }

    @Test
    void testGetDateFormatter_Default() {
        DateTimeFormatter formatter = Formatter.getDateFormatter();
        assertNotNull(formatter);
        String result = formatter.format(LocalDate.of(2023, 4, 15));
        assertEquals("2023-04-15", result);
    }

    @Test
    void testGetDateTimeFormatter() {
        DateTimeFormatter formatter = Formatter.getDateTimeFormatter();
        assertNotNull(formatter);
        String result = formatter.format(LocalDateTime.of(2023, 4, 15, 11, 30, 45));
        assertEquals("2023-04-15 11:30:45", result);
    }

    @Test
    void testGetDateTimeShortFormatter() {
        DateTimeFormatter formatter = Formatter.getDateTimeShortFormatter();
        assertNotNull(formatter);
        String result = formatter.format(LocalDateTime.of(2023, 4, 15, 11, 30, 45));
        assertEquals("2023-04-15 11:30", result);
    }

    @Test
    void testGMT_FORMATTER() {
        assertNotNull(Formatter.GMT_FORMATTER);
        assertEquals(ZoneId.of("GMT"), Formatter.GMT_FORMATTER.getZone());
    }

    @Test
    void testISO8601_FORMATTER() {
        assertNotNull(Formatter.ISO8601_FORMATTER);
        String result = Formatter.ISO8601_FORMATTER.format(
                java.time.Instant.parse("2023-04-15T11:30:45Z")
        );
        assertEquals("2023-04-15T11:30:45Z", result);
    }

    @Test
    void testFormatterCache() {
        // 测试格式化器缓存是否正常工作
        DateTimeFormatter formatter1 = Formatter.getDateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
        DateTimeFormatter formatter2 = Formatter.getDateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
        assertSame(formatter1, formatter2, "Formatter should be cached and return the same instance");
    }

    @Test
    void testFormat_VariousPatterns() {
        LocalDateTime localDateTime = LocalDateTime.of(2023, 4, 15, 11, 30, 45);
        Formatter formatter = new Formatter(localDateTime);

        assertEquals("2023-04-15", formatter.format("yyyy-MM-dd"));
        assertEquals("2023/04/15", formatter.format("yyyy/MM/dd"));
        assertEquals("15-04-2023", formatter.format("dd-MM-yyyy"));
        assertEquals("20230415", formatter.format("yyyyMMdd"));
        assertEquals("11:30:45", formatter.format("HH:mm:ss"));
        assertEquals("11:30", formatter.format("HH:mm"));
    }
}