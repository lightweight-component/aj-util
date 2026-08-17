package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestHashHelper {
    @Test
    void testCalcFileMD5() {
        byte[] content = "你好 Hi".getBytes(StandardCharsets.UTF_8);

        assertEquals(HashHelper.md5("你好 Hi"), HashHelper.calcFileMD5(content));
    }

    @Test
    void standardDigestAndHmacMatchKnownVectors() {
        assertEquals("900150983cd24fb0d6963f7d28e17f72", HashHelper.md5("abc"));
        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                HashHelper.getSHA256("abc")
        );
        assertEquals(
                "UDH+PZicbRU3oBP6bnOdojRj/a7DtwE32Cjjas4iG9A=",
                HashHelper.getHmacSHA256("data", "key", false)
        );
    }

    @Test
    void macRequiresAnExplicitKey() {
        HashHelper helper = new HashHelper(HashHelper.HMAC_SHA256, "data");

        assertEquals(
                "HMAC key is required.",
                assertThrows(IllegalStateException.class, helper::getMac).getMessage()
        );
    }
}
