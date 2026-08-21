package com.ajaxjs.net.ftp.sun;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class TestNetworkClient {
    @Test
    void connectsConfiguresTimeoutsAndCloses() throws Exception {
        try (ServerSocket server = new ServerSocket(0)) {
            Thread accepter = new Thread(() -> {
                try (Socket ignored = server.accept()) {
                    while (!ignored.isClosed() && ignored.getInputStream().read() != -1) {
                        // Wait until the client closes.
                    }
                } catch (IOException ignored) {
                }
            });
            accepter.start();

            ExposedNetworkClient client = new ExposedNetworkClient();
            client.setConnectTimeout(2000);
            client.setReadTimeout(2000);
            assertEquals(2000, client.getConnectTimeout());
            assertEquals(2000, client.getReadTimeout());

            client.openServer("127.0.0.1", server.getLocalPort());
            assertTrue(client.serverIsOpen());
            assertNotNull(client.localAddress());
            client.controlEncoding("UTF-8");
            client.close();
            assertFalse(client.serverIsOpen());
            accepter.join(2000L);
        }
    }

    @Test
    void reportsInvalidConnectionAndMissingLocalAddress() {
        ExposedNetworkClient client = new ExposedNetworkClient();
        assertNotNull(client.newSocket());
        assertThrows(IOException.class, client::localAddress);
        assertThrows(IOException.class, () -> client.openServer("127.0.0.1", 0));
    }

    static final class ExposedNetworkClient extends NetworkClient {
        Socket newSocket() {
            return createSocket();
        }

        InetAddress localAddress() throws IOException {
            return getLocalAddress();
        }

        void controlEncoding(String encoding) throws IOException {
            setControlEncoding(encoding);
        }
    }
}
