package com.ajaxjs.s3client.util;

import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestURLEncoding {
    @Test
    void packageVisibleEncoderHonorsTheProvidedUnescapedSet() {
        BitSet unescaped = new BitSet();
        unescaped.set('a');
        unescaped.set('/');

        assertEquals("a/%20%2B", URLEncoding.encode("a/ +", unescaped));
        assertThrows(NullPointerException.class, () -> URLEncoding.encode(null, unescaped));
        assertThrows(NullPointerException.class, () -> URLEncoding.encode("a", null));
    }
}
