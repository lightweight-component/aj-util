package com.ajaxjs.net.ftp.sun;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TelnetOutputStream}.
 */
class TestTelnetOutputStream {

    @Test
    void testConstructor() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TelnetOutputStream telnetOut = new TelnetOutputStream(out, true);

        assertNotNull(telnetOut);
    }

    @Test
    void testWriteSingleByte() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TelnetOutputStream telnetOut = new TelnetOutputStream(out, true);

        telnetOut.write(65); // 'A'
        telnetOut.flush();

        assertEquals("A", out.toString());

        telnetOut.close();
    }

    @Test
    void testWriteMultipleBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TelnetOutputStream telnetOut = new TelnetOutputStream(out, true);

        telnetOut.write("Hello".getBytes());
        telnetOut.flush();

        assertEquals("Hello", out.toString());

        telnetOut.close();
    }

    @Test
    void testWriteWithOffset() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TelnetOutputStream telnetOut = new TelnetOutputStream(out, true);

        byte[] data = "Hello World".getBytes();
        telnetOut.write(data, 6, 5); // Write "World"
        telnetOut.flush();

        assertEquals("World", out.toString());

        telnetOut.close();
    }

    @Test
    void testBinaryMode() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TelnetOutputStream telnetOut = new TelnetOutputStream(out, true);

        // Write binary data
        byte[] binaryData = {0, 1, 127, (byte) 128, (byte) 255};
        telnetOut.write(binaryData);
        telnetOut.flush();

        assertArrayEquals(binaryData, out.toByteArray());

        telnetOut.close();
    }

    @Test
    void testAsciiMode() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TelnetOutputStream telnetOut = new TelnetOutputStream(out, false);

        // In ASCII mode, line endings might be handled differently
        telnetOut.write("Line 1\nLine 2".getBytes());
        telnetOut.flush();

        // The output should contain the data
        assertTrue(out.size() > 0);

        telnetOut.close();
    }

    @Test
    void testSetStickyCRLF() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TelnetOutputStream telnetOut = new TelnetOutputStream(out, false);

        // Test setting sticky CRLF
        telnetOut.setStickyCRLF(true);
        telnetOut.write("Test\r\n".getBytes());
        telnetOut.flush();

        // Output should contain the CRLF handling
        assertTrue(out.size() > 0);

        telnetOut.close();
    }

    @Test
    void testFlush() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TelnetOutputStream telnetOut = new TelnetOutputStream(out, true);

        telnetOut.write(65);
        telnetOut.flush();

        // After flush, data should be available
        assertEquals("A", out.toString());

        telnetOut.close();
    }

    @Test
    void testClose() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TelnetOutputStream telnetOut = new TelnetOutputStream(out, true);

        telnetOut.write("Test".getBytes());
        telnetOut.close();

        // After close, data should be flushed
        assertTrue(out.size() > 0);

        // ByteArrayOutputStream.close() has no effect by contract.
        assertDoesNotThrow(() -> telnetOut.write(65));
    }

    @Test
    void testWriteLargeData() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TelnetOutputStream telnetOut = new TelnetOutputStream(out, true);

        // Create large data (1MB)
        byte[] largeData = new byte[1024 * 1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }

        telnetOut.write(largeData);
        telnetOut.flush();

        assertArrayEquals(largeData, out.toByteArray());

        telnetOut.close();
    }
}
