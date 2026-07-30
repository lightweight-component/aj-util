package com.ajaxjs.util.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(testString, result);
    }

    @Test
    void readAsStringPreservesOriginalLineEndingsAndTrailingNewline() {
        String content = "first\r\nsecond\nthird";

        String result = new DataReader(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))
                .readAsString();

        assertEquals(content, result);
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

    @Test
    void readStreamAsBytesReportsExactChunkSizes() {
        byte[] content = "abcdefghij".getBytes(StandardCharsets.UTF_8);
        List<Integer> sizes = new ArrayList<>();
        ByteArrayOutputStreamCollector collector = new ByteArrayOutputStreamCollector();

        new DataReader(new ByteArrayInputStream(content)).readStreamAsBytes(4, (size, buffer) -> {
            sizes.add(size);
            collector.append(buffer, size);
        });

        assertEquals(Arrays.asList(4, 4, 2), sizes);
        assertArrayEquals(content, collector.toByteArray());
    }

    private static final class ByteArrayOutputStreamCollector {
        private final java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();

        void append(byte[] bytes, int length) {
            output.write(bytes, 0, length);
        }

        byte[] toByteArray() {
            return output.toByteArray();
        }
    }
}
