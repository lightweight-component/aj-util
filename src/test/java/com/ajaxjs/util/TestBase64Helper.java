package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBase64Helper {
    @Test
    void testEncode() {
        String urlEncoded = Base64Helper.encode()
                .input("hello world!")
                .urlSafe()
                .getString();
        assertEquals("aGVsbG8gd29ybGQh", urlEncoded);
    }

    @Test
    void testDecode() {
        String text = Base64Helper.decode()
                .input("aGVsbG8gd29ybGQh")
                .getString();

        assertEquals("hello world!", text);
    }
}
