package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestBase64Utils {
    @Test
    void testBase64() {
        String result = new Base64Utils("abc").encodeAsString();
        assertEquals("YWJj", result);

        result = new Base64Utils("中国人").encodeAsString();
        assertEquals("5Lit5Zu95Lq6", result);

        assertEquals("中国人", new Base64Utils(new Base64Utils("中国人").encodeAsString()).decodeAsString());
    }

    @Test
    void testEncode() {
        String urlEncoded = new Base64Utils("hello world!").setUrlSafe(true).encodeAsString();
        assertEquals("aGVsbG8gd29ybGQh", urlEncoded);
    }

    @Test
    void testDecode() {
        String text = new Base64Utils("aGVsbG8gd29ybGQh").decodeAsString();
        assertEquals("hello world!", text);
    }
}
