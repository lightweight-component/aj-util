package com.ajaxjs.util.date;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TestDateTools {
    @Test
    void object2Date_NullObject_ReturnsNull() {
        Date date = DateTools.object2Date(null);
        assertNull(date);
    }

    @Test
    void object2Date_DateObject_ReturnsSameDate() {
        Date date = new Date();
        Date result = DateTools.object2Date(date);
        assertNotNull(result);
        assertEquals(date, result);
    }

    @Test
    void object2Date_LongObject_ReturnsCorrectDate() {
        long time = 1681584645000L; // GMT: Saturday, April 15, 2023 11:30:45.000
        Date date = DateTools.object2Date(time);
        assertNotNull(date);
        assertEquals("2023-04-15 11:30:45", DateTools.formatDateTime(DateTools.toLocalDateTime(date)));
    }

    @Test
    void object2Date_IntegerObject_ReturnsCorrectDate() {
        long time = 1681584645000L; // GMT: Saturday, April 15, 2023 11:30:45.000
        Date date = DateTools.object2Date(time);
        assertNotNull(date);
        assertEquals("2023-04-15 11:30:45", DateTools.formatDateTime(DateTools.toLocalDateTime(date)));
    }

    @Test
    void object2Date_StringObject_ReturnsCorrectDate() {
        String dateTimeStr = "2023-04-15 11:30:45";
        Date date = DateTools.object2Date(dateTimeStr);
        assertNotNull(date);
        assertEquals("2023-04-15 11:30:45", DateTools.formatDateTime(DateTools.toLocalDateTime(date)));
    }
}
