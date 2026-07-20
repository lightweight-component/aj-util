package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestRandomTools {
    @Test
    void testGenerateRandomString() {
        String randomString = RandomTools.generateRandomString(6);
        System.out.println(randomString);

        assertNotNull(randomString);

        randomString = RandomTools.generateRandomString();
        System.out.println(randomString);
        assertNotNull(randomString);
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
        System.out.println(RandomTools.uuid());
        System.out.println(RandomTools.uuid());
        System.out.println(RandomTools.uuid());
    }

    @Test
    void testShowTime() {
        assertEquals("Mon Oct 27 11:31:18 CST 2025", RandomTools.showTime("019a23b8-66c2-7297-83ae-a1f17a1c23ac").toString());
    }
}
