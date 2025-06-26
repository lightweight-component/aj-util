package com.ajaxjs.util;


import org.junit.jupiter.api.Test;

import static com.ajaxjs.util.BytesHelper.bytesToHexStr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBytesHelper {
    @Test
    public void testParseHexStr2Byte() {
        byte[] bs = BytesHelper.parseHexStr2Byte("1A2B3C");
        assert bs != null;
        assertEquals(0x1A, bs[0]);
    }

    /**
     * Test case: Standard byte array input.
     * Verifies that the method correctly converts a standard byte array to its hexadecimal string representation.
     */
    @Test
    public void testBytesToHexStr_StandardCase() {
        byte[] input = new byte[]{0x1A, 0x2B, 0x3C};
        String result = bytesToHexStr(input);
        assertEquals("1A2B3C", result);
    }

    /**
     * Test case: All zero bytes.
     * Verifies that the method correctly converts an array of zero bytes to the appropriate hexadecimal string.
     */
    @Test
    public void testBytesToHexStr_AllZeros() {
        byte[] input = new byte[]{0x00, 0x00, 0x00};
        String result = bytesToHexStr(input);
        assertEquals("000000", result);
    }

    /**
     * Test case: Bytes with high bit set.
     * Verifies that the method correctly handles bytes where the high bit is set (negative values in Java).
     */
    @Test
    public void testBytesToHexStr_HighBitSet() {
        byte[] input = new byte[]{(byte) 0x80, (byte) 0xFF};
        String result = bytesToHexStr(input);
        assertEquals("80FF", result);
    }

    /**
     * Test case: Empty byte array.
     * Verifies that the method returns an empty string when given an empty byte array.
     */
    @Test
    public void testBytesToHexStr_EmptyArray() {
        byte[] input = new byte[0];
        String result = bytesToHexStr(input);
        assertEquals("", result);
    }

    /**
     * Test case: Single byte values.
     * Verifies several specific single byte conversions.
     */
    @Test
    public void testBytesToHexStr_SingleBytes() {
        assertEquals("00", bytesToHexStr(new byte[]{0x00}));
        assertEquals("FF", bytesToHexStr(new byte[]{(byte) 0xFF}));
        assertEquals("7F", bytesToHexStr(new byte[]{0x7F}));
        assertEquals("80", bytesToHexStr(new byte[]{(byte) 0x80}));
    }

    /**
     * Test case: Long byte array.
     * Verifies that the method correctly converts a longer byte array without errors.
     */
    @Test
    public void testBytesToHexStr_LongArray() {
        byte[] input = new byte[100];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) i;
        }

        String result = bytesToHexStr(input);
        assertEquals(200, result.length()); // Each byte should produce 2 hex chars

        // Verify first few values
        assertTrue(result.startsWith("000102030405"));
    }
}
