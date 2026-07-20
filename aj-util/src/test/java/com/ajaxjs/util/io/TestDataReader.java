package com.ajaxjs.util.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestDataReader {
    private InputStream inputStream;

    private String testString;

    @BeforeEach
    public void setUp() {
        testString = "测试字符串";
        inputStream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testReadAsString() {
        String result = new DataReader(inputStream).readAsString();
        assertEquals(testString + "\n", result);
    }

    @Test
    public void testReadAsBytes() {
        byte[] result = new DataReader(inputStream).readAsBytes();
        assertEquals(testString, new String(result, StandardCharsets.UTF_8));
    }

    @Test
    void readStreamAsBytesRejectsZeroBufferSize() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new DataReader(inputStream).readStreamAsBytes(0, (size, bytes) -> {
                }));

        assertEquals("Buffer size must be greater than zero: 0", error.getMessage());
    }

    @Test
    void readStreamAsBytesRejectsNegativeBufferSize() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new DataReader(inputStream).readStreamAsBytes(-1, (size, bytes) -> {
                }));

        assertEquals("Buffer size must be greater than zero: -1", error.getMessage());
    }
}
