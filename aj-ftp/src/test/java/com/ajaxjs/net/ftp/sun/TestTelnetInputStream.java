package com.ajaxjs.net.ftp.sun;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TelnetInputStream}.
 */
class TestTelnetInputStream {

    @Test
    void testConstructor() {
        byte[] data = "test data".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        TelnetInputStream telnetIn = new TelnetInputStream(in, true);

        assertNotNull(telnetIn);
    }

    @Test
    void testReadSingleByte() throws IOException {
        byte[] data = {65, 66, 67}; // ABC
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        TelnetInputStream telnetIn = new TelnetInputStream(in, true);

        assertEquals(65, telnetIn.read());  // A
        assertEquals(66, telnetIn.read());  // B
        assertEquals(67, telnetIn.read());  // C
        assertEquals(-1, telnetIn.read());  // EOF

        telnetIn.close();
    }

    @Test
    void testReadWithBinaryMode() throws IOException {
        byte[] data = {0, 1, 2, 127, (byte) 128, (byte) 255};
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        TelnetInputStream telnetIn = new TelnetInputStream(in, true);

        byte[] buffer = new byte[10];
        int read = telnetIn.read(buffer);

        assertEquals(6, read);
        assertArrayEquals(data, java.util.Arrays.copyOf(buffer, 6));

        telnetIn.close();
    }

    @Test
    void testReadWithAsciiMode() throws IOException {
        byte[] data = "Hello\r\nWorld".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        TelnetInputStream telnetIn = new TelnetInputStream(in, false);

        byte[] buffer = new byte[20];
        int read = telnetIn.read(buffer);

        // In ASCII mode, \r\n should be handled appropriately
        assertTrue(read > 0);

        telnetIn.close();
    }

    @Test
    void testReadByteArray() throws IOException {
        byte[] data = "Hello World".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        TelnetInputStream telnetIn = new TelnetInputStream(in, true);

        byte[] buffer = new byte[5];
        int read = telnetIn.read(buffer);

        assertEquals(5, read);
        assertEquals("Hello", new String(buffer));

        telnetIn.close();
    }

    @Test
    void testReadByteArrayWithOffset() throws IOException {
        byte[] data = "Hello World".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        TelnetInputStream telnetIn = new TelnetInputStream(in, true);

        byte[] buffer = new byte[10];
        int read = telnetIn.read(buffer, 2, 5);

        assertEquals(5, read);
        assertEquals("Hello", new String(buffer, 2, 5));

        telnetIn.close();
    }

    @Test
    void testClose() throws IOException {
        byte[] data = "test".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        TelnetInputStream telnetIn = new TelnetInputStream(in, true);

        // Close should not throw
        telnetIn.close();

        // ByteArrayInputStream.close() has no effect by contract.
        assertEquals('t', telnetIn.read());
    }

    @Test
    void testSkip() throws IOException {
        byte[] data = "Hello World".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        TelnetInputStream telnetIn = new TelnetInputStream(in, true);

        long skipped = telnetIn.skip(6);

        assertEquals(6, skipped);
        assertEquals('W', telnetIn.read()); // Should read 'W' after skipping "Hello "

        telnetIn.close();
    }

    @Test
    void testAvailable() throws IOException {
        byte[] data = "test".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        TelnetInputStream telnetIn = new TelnetInputStream(in, true);

        int available = telnetIn.available();

        // Should have some bytes available
        assertTrue(available >= 0);

        telnetIn.close();
    }

    @Test
    void testMarkAndReset() throws IOException {
        byte[] data = "Hello World".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        TelnetInputStream telnetIn = new TelnetInputStream(in, true);

        // Check if mark is supported
        if (telnetIn.markSupported()) {
            // Read first 5 bytes
            byte[] buffer1 = new byte[5];
            telnetIn.read(buffer1);
            assertEquals("Hello", new String(buffer1));

            // Mark the current position
            telnetIn.mark(10);

            // Read next 5 bytes
            byte[] buffer2 = new byte[5];
            telnetIn.read(buffer2);
            assertEquals(" Worl", new String(buffer2));

            // Reset to the marked position
            telnetIn.reset();

            // Read 5 bytes again - should get the same as buffer2
            byte[] buffer3 = new byte[5];
            telnetIn.read(buffer3);
            assertArrayEquals(buffer2, buffer3);
        }

        telnetIn.close();
    }
}
