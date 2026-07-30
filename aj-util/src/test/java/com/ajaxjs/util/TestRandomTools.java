package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestRandomTools {
    @Test
    void testGenerateRandomString() {
        String randomString = RandomTools.generateRandomString(6);
        assertEquals(6, randomString.length());
        assertTrue(randomString.matches("[A-Za-z0-9]{6}"));

        randomString = RandomTools.generateRandomString();
        assertEquals(6, randomString.length());
        assertTrue(randomString.matches("[A-Za-z0-9]{6}"));
    }

    @Test
    void testInvalidLengths() {
        assertThrows(IllegalArgumentException.class, () -> RandomTools.generateNumber(0));
        assertThrows(IllegalArgumentException.class, () -> RandomTools.generateNumber(10));
        assertThrows(IllegalArgumentException.class, () -> RandomTools.generateRandomString(-1));
        assertThrows(IllegalArgumentException.class, () -> RandomTools.generateRandomString(0));
    }

    @Test
    void testUUIDv7() {
        java.util.UUID uuid = RandomTools.uuid();

        assertEquals(7, uuid.version());
        assertEquals(2, uuid.variant());
        assertEquals(32, RandomTools.uuidStr().length());
    }

    @Test
    void testShowTime() {
        assertEquals(1761535878850L, RandomTools.showTime("019a23b8-66c2-7297-83ae-a1f17a1c23ac").getTime());
    }
}
