package com.ajaxjs.net.ftp.sun;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TransferProtocolClient}.
 */
class TestTransferProtocolClient {

    @Test
    void testDefaultConstructor() {
        TransferProtocolClient client = new TransferProtocolClient();

        assertNotNull(client);
    }

    @Test
    void testServerIsOpenBeforeConnect() {
        TransferProtocolClient client = new TransferProtocolClient();

        assertFalse(client.serverIsOpen());
    }

    @Test
    void testOpenServer() {
        TransferProtocolClient client = new TransferProtocolClient();

        // Attempting to connect to non-existent server should throw
        assertThrows(IOException.class, () -> {
            client.openServer("nonexistent.host.example", 21);
        });
    }

    @Test
    void testCloseServerWithoutOpen() throws IOException {
        TransferProtocolClient client = new TransferProtocolClient();

        // Closing when not open should not throw
        client.closeServer();

        assertFalse(client.serverIsOpen());
    }

    @Test
    void testGetResponseStringWithoutConnection() {
        TransferProtocolClient client = new TransferProtocolClient();

        // May return null or empty string when not connected
        String response = client.getResponseString();
        assertTrue(response == null || response.isEmpty());
    }

    @Test
    void testServerResponseVector() {
        TransferProtocolClient client = new TransferProtocolClient();

        // serverResponse should be accessible and initially empty
        assertNotNull(client.serverResponse);
        assertTrue(client.serverResponse.isEmpty());
    }

    @Test
    void testGetResponseStringWithMultipleLines() {
        TransferProtocolClient client = new TransferProtocolClient();

        // Add some mock responses
        client.serverResponse.add("220 Welcome");
        client.serverResponse.add("331 Password required");

        // The response string should contain all lines
        String response = client.getResponseString();
        assertNotNull(response);
        assertTrue(response.contains("220 Welcome") || response.contains("331 Password required"));
        assertEquals(2, client.getResponseStrings().size());
        assertThrows(UnsupportedOperationException.class,
                () -> client.getResponseStrings().add("500 mutation"));
    }
}
