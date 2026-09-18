package com.ajaxjs.util.cryptography;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestCipherResult {
    @Test
    void exposesBinaryResultInSupportedTextRepresentations() {
        byte[] utf8 = "内容".getBytes(StandardCharsets.UTF_8);
        CipherResult result = new CipherResult(utf8);

        assertArrayEquals(utf8, result.getResult());
        assertEquals("内容", result.toUtf8());
        assertEquals("5YaF5a65", result.toBase64());
        assertEquals("E58685E5AEB9", result.toHex());
    }
}
