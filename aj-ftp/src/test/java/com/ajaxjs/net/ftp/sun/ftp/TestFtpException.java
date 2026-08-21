package com.ajaxjs.net.ftp.sun.ftp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FTP exception classes.
 */
class TestFtpException {

    @Test
    void testFtpLoginExceptionWithMessage() {
        String message = "Invalid credentials";
        FtpLoginException exception = new FtpLoginException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testFtpLoginExceptionInheritance() {
        FtpLoginException exception = new FtpLoginException("test");

        // Should be a subclass of IOException
        assertTrue(exception instanceof java.io.IOException);
    }

    @Test
    void testFtpProtocolExceptionWithMessage() {
        String message = "Protocol error occurred";
        FtpProtocolException exception = new FtpProtocolException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testFtpProtocolExceptionInheritance() {
        FtpProtocolException exception = new FtpProtocolException("test");

        // Should be a subclass of IOException
        assertTrue(exception instanceof java.io.IOException);
    }

    @Test
    void testExceptionChaining() {
        Throwable cause = new RuntimeException("Root cause");

        // Test that exceptions can have causes set via initCause
        FtpProtocolException exception = new FtpProtocolException("Wrapper");
        exception.initCause(cause);

        assertEquals(cause, exception.getCause());
    }

    @Test
    void testEmptyMessage() {
        FtpLoginException exception1 = new FtpLoginException("");
        FtpProtocolException exception2 = new FtpProtocolException("");

        assertEquals("", exception1.getMessage());
        assertEquals("", exception2.getMessage());
    }

    @Test
    void testNullMessage() {
        // Testing with null - should store null
        FtpLoginException exception1 = new FtpLoginException(null);
        FtpProtocolException exception2 = new FtpProtocolException(null);

        assertNull(exception1.getMessage());
        assertNull(exception2.getMessage());
    }
}
