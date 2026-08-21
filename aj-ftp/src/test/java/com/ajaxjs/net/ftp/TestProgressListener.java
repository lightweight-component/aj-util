package com.ajaxjs.net.ftp;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ProgressListener}.
 */
class TestProgressListener {

    @Test
    void testGetterAndSetter() {
        ProgressListener listener = new ProgressListener();

        // Test fileName
        listener.setFileName("test.txt");
        assertEquals("test.txt", listener.getFileName());

        // Test initial values
        assertEquals(0, listener.getBytesRead());
        assertEquals(0, listener.getContentLength());
    }

    @Test
    void testUpdate() {
        ProgressListener listener = new ProgressListener();
        listener.setFileName("test.txt");

        // Test update with 2048 bytes read, 4096 total
        listener.update(2048, 4096);

        // bytesRead should be in KB (2048 / 1024 = 2)
        assertEquals(2, listener.getBytesRead());
        // contentLength should be in KB (4096 / 1024 = 4)
        assertEquals(4, listener.getContentLength());
    }

    @Test
    void testCopyWithValidStreams() throws IOException {
        ProgressListener listener = new ProgressListener();

        byte[] testData = "Hello, World!".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(testData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        long copied = listener.copy(in, out, testData.length);

        assertEquals(testData.length, copied);
        assertArrayEquals(testData, out.toByteArray());
    }

    @Test
    void testCopyWithLargeData() throws IOException {
        ProgressListener listener = new ProgressListener();
        listener.setFileName("large.bin");

        // Create 16KB of test data
        byte[] testData = new byte[16384];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = (byte) (i % 256);
        }

        ByteArrayInputStream in = new ByteArrayInputStream(testData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        long copied = listener.copy(in, out, testData.length);

        assertEquals(testData.length, copied);
        assertArrayEquals(testData, out.toByteArray());
    }

    @Test
    void testCopyWithEmptyInput() throws IOException {
        ProgressListener listener = new ProgressListener();

        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        long copied = listener.copy(in, out, 0);

        assertEquals(0, copied);
        assertEquals(0, out.size());
    }

    @Test
    void testCopyWithIOException() {
        ProgressListener listener = new ProgressListener();

        // Create a mock InputStream that throws IOException
        InputStream throwingIn = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Test exception");
            }
        };

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        IOException error = assertThrows(IOException.class,
                () -> listener.copy(throwingIn, out, 100));
        assertEquals("Test exception", error.getMessage());
    }

    @Test
    void testCopyDoesNotCloseCallerStreams() throws IOException {
        class CloseAwareInputStream extends ByteArrayInputStream {
            boolean closed;

            CloseAwareInputStream(byte[] data) {
                super(data);
            }

            @Override
            public void close() {
                closed = true;
            }
        }
        class CloseAwareOutputStream extends ByteArrayOutputStream {
            boolean closed;

            @Override
            public void close() {
                closed = true;
            }
        }

        CloseAwareInputStream in = new CloseAwareInputStream(new byte[]{1, 2});
        CloseAwareOutputStream out = new CloseAwareOutputStream();
        new ProgressListener().copy(in, out, 2);
        assertFalse(in.closed);
        assertFalse(out.closed);
    }
}
