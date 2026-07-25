package com.ajaxjs.util.date;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TestDateTools {
    @Test
    void object2Date_DateObject_ReturnsSameDate() {
        Date date = new Date();
        Date result = DateTools.object2Date(date);
        assertNotNull(result);
        assertEquals(date, result);
    }

    @Test
    void object2Date_Null_ReturnsNull() {
        assertNull(DateTools.object2Date(null));
    }

    @Test
    void object2Date_EmptyString_ReturnsNull() {
        assertNull(DateTools.object2Date(""));
        assertNull(DateTools.object2Date("   "));
    }

    @Test
    void testObject2Date() {
        long time = 1681584645000L; // GMT: Saturday, April 15, 2023 11:30:45.000
        Date date = DateTools.object2Date(time);

        assertEquals("2023-04-16 02:50:45", DateTools.toLocalDateTime(date).format(Formatter.getDateTimeFormatter()));

        int i = 1681584645;
        date = DateTools.object2Date(i);
        assertEquals("2023-04-16 02:50:45", DateTools.toLocalDateTime(date).format(Formatter.getDateTimeFormatter()));

        String dateTimeStr = "2023-04-15 11:30:45";
        date = DateTools.object2Date(dateTimeStr);
        assertEquals(dateTimeStr, DateTools.toLocalDateTime(date).format(Formatter.getDateTimeFormatter()));
    }

    @Test
    void object2Date_LocalDateTime_ReturnsDate() {
        LocalDateTime localDateTime = LocalDateTime.of(2023, 4, 15, 11, 30, 45);
        Date date = DateTools.object2Date(localDateTime);
        assertNotNull(date);
        assertEquals("2023-04-15 11:30:45", DateTools.toLocalDateTime(date).format(Formatter.getDateTimeFormatter()));
    }

    @Test
    void object2Date_LocalDate_ReturnsDate() {
        LocalDate localDate = LocalDate.of(2023, 4, 15);
        Date date = DateTools.object2Date(localDate);
        assertNotNull(date);
        assertEquals(LocalDate.of(2023, 4, 15), DateTools.toLocalDateTime(date).toLocalDate());
    }

    @Test
    void object2Date_ShortDateTimeFormat_ReturnsDate() {
        // 测试短日期时间格式 "yyyy-MM-dd HH:mm"
        String shortDateTimeStr = "2023-04-15 11:30";
        Date date = DateTools.object2Date(shortDateTimeStr);
        assertNotNull(date);
        assertEquals("2023-04-15 11:30:00", DateTools.toLocalDateTime(date).format(Formatter.getDateTimeFormatter()));
    }

    @Test
    void object2Date_UnsupportedType_ReturnsNull() {
        assertNull(DateTools.object2Date(3.14));
        assertNull(DateTools.object2Date(new Object()));
    }

    @Test
    void testFormat() {
        Date date = DateTools.object2Date("2026-01-23");
        assertNotNull(date);
        assertEquals(LocalDate.of(2026, 1, 23), DateTools.toLocalDateTime(date).toLocalDate());
    }

    @Test
    void object2Date_InvalidDate_ThrowsExceptionInsteadOfAdjustingIt() {
        assertThrows(DateTimeParseException.class, () -> DateTools.object2Date("2023-02-29"));
        assertThrows(DateTimeParseException.class, () -> DateTools.object2Date("2023-04-31"));
    }

    @Test
    void object2Date_ValidLeapDate_ReturnsDate() {
        Date date = DateTools.object2Date("2024-02-29");

        assertNotNull(date);
        assertEquals(LocalDate.of(2024, 2, 29), DateTools.toLocalDateTime(date).toLocalDate());
    }

    @Test
    void testToLocalDateTime() {
        Date date = new Date(1681584645000L);
        LocalDateTime localDateTime = DateTools.toLocalDateTime(date);
        assertNotNull(localDateTime);
        // 由于时区差异，这里只验证不为null
    }

    @Test
    void testNow_WithFormatter() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String result = DateTools.now(formatter);
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void testNow_WithFormatString() {
        String result = DateTools.now("yyyy/MM/dd");
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}/\\d{2}/\\d{2}"));
    }

    @Test
    void testNow_Default() {
        String result = DateTools.now();
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testNowShort() {
        String result = DateTools.nowShort();
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"));
    }

    @Test
    void testNowGMTDate() {
        String result = DateTools.nowGMTDate();
        assertNotNull(result);
        // GMT格式示例: Sat, 15 Apr 2023 11:30:45 GMT
        assertTrue(result.contains("GMT"));
    }

    @Test
    void testNewISO8601Date() {
        String s = DateTools.newISO8601Date();
        System.out.println(s);
        assertNotNull(s);
        assertTrue(s.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"));
    }

    @Test
    void testNewISO8601DateWithHigherPrecision() {
        String s = DateTools.newISO8601DateWithHigherPrecision();
        System.out.println(s);
        assertNotNull(s);
        // 高精度格式包含微秒，例如: 2023-04-15T11:30:45.123456Z
        assertTrue(s.contains("T") && s.endsWith("Z"));
    }
}
