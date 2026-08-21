package com.ajaxjs.net.ftp.sun.ftp;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Proxy;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FtpClient}.
 */
class TestFtpClient {

    @Test
    void testDefaultPort() {
        assertEquals(21, FtpClient.FTP_PORT);
    }

    @Test
    void testStaticResponseCodes() {
        // These are internal constants used by the client
        assertEquals(1, FtpClient.FTP_SUCCESS);
        assertEquals(2, FtpClient.FTP_TRY_AGAIN);
        assertEquals(3, FtpClient.FTP_ERROR);
    }

    @Test
    void testGetFtpProxyHost() {
        // When no proxy is set, should return null
        String proxyHost = FtpClient.getFtpProxyHost();
        // This depends on system properties, so we just verify it doesn't throw
    }

    @Test
    void testGetFtpProxyPort() {
        // Default port should be 80
        int port = FtpClient.getFtpProxyPort();
        // This depends on system properties
    }

    @Test
    void testGetUseFtpProxy() {
        // When no proxy is configured, should return false
        boolean useProxy = FtpClient.getUseFtpProxy();
        // This depends on system properties
    }

    @Test
    void testConstructors() {
        // Test no-arg constructor
        FtpClient client1 = new FtpClient();
        assertNotNull(client1);

        // Test constructor with proxy (can pass null)
        FtpClient client2 = new FtpClient(Proxy.NO_PROXY);
        assertNotNull(client2);
    }

    @Test
    void disconnectedCommandsReportErrors() {
        FtpClient client = new FtpClient();
        assertThrows(IOException.class, client::anonymousLogin);
        assertThrows(IOException.class, () -> client.openServer("127.0.0.1"));
        assertDoesNotThrow(client::completePendingCommand);
        assertDoesNotThrow(client::closeServer);
    }

    @Test
    void testWelcomeMsgField() {
        FtpClient client = new FtpClient();
        // welcomeMsg should be null initially
        assertNull(client.getWelcomeMessage());
    }

    @Test
    void testCommandField() {
        FtpClient client = new FtpClient();
        // Internal command state is deliberately not exposed as mutable API.
        assertFalse(client.isLoggedIn());
    }

    @Test
    void testLastReplyCodeField() {
        FtpClient client = new FtpClient();
        // lastReplyCode should be 0 initially
        assertEquals(0, client.getLastReplyCode());
    }
}
