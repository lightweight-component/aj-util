package com.ajaxjs.net.ftp.sun;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TelnetProtocolException}.
 */
class TestTelnetProtocolException {

    @Test
    void testConstructorWithMessage() {
        String message = "Telnet protocol error occurred";
        TelnetProtocolException exception = new TelnetProtocolException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testInheritance() {
        TelnetProtocolException exception = new TelnetProtocolException("test");

        // Should be a subclass of IOException
        assertTrue(exception instanceof java.io.IOException);

    }

    @Test
    void testEmptyMessage() {
        TelnetProtocolException exception = new TelnetProtocolException("");
        assertEquals("", exception.getMessage());
    }

    @Test
    void testNullMessage() {
        TelnetProtocolException exception = new TelnetProtocolException(null);
        assertNull(exception.getMessage());
    }

    @Test
    void testExceptionChaining() {
        Throwable cause = new RuntimeException("Root cause of telnet error");
        TelnetProtocolException exception = new TelnetProtocolException("Wrapper exception");
        exception.initCause(cause);

        assertEquals(cause, exception.getCause());
        assertEquals("Root cause of telnet error", exception.getCause().getMessage());
    }

    @Test
    void testStackTrace() {
        TelnetProtocolException exception = new TelnetProtocolException("Error");

        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);

        // The first element should be this test method
        assertEquals(getClass().getName(), stackTrace[0].getClassName());
    }

    @Test
    void testToString() {
        TelnetProtocolException exception = new TelnetProtocolException("Test error");
        String toString = exception.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("TelnetProtocolException"));
        assertTrue(toString.contains("Test error"));
    }

    @Test
    void testLongMessage() {
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("Very long error message ");
        }

        TelnetProtocolException exception = new TelnetProtocolException(longMessage.toString());
        assertEquals(longMessage.toString(), exception.getMessage());
    }
}
