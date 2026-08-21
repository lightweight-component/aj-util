package com.ajaxjs.net.ftp;

import com.ajaxjs.net.ftp.sun.ftp.FtpClient;
import com.ajaxjs.net.ftp.sun.ftp.FtpLoginException;
import com.ajaxjs.net.ftp.sun.ftp.FtpProtocolException;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestFtpProtocolState {
    private static final byte[] CONTENT = "ftp-data".getBytes(StandardCharsets.US_ASCII);

    @Test
    void downloadGetsLongSizeBeforeRetrAndConsumes226() throws Exception {
        try (FakeFtpServer server = new FakeFtpServer(false)) {
            server.start();
            Path target = Files.createTempFile("aj-ftp-", ".bin");

            try (SimpleFtpClient client = new SimpleFtpClient("127.0.0.1", server.getPort())) {
                client.login("user", "password");
                client.getFile("remote.bin", target.toString());
                client.noop(); // proves that 226 was already consumed
            }

            assertArrayEquals(CONTENT, Files.readAllBytes(target));
            assertTrue(server.commands.indexOf("SIZE remote.bin") < server.commands.indexOf("RETR remote.bin"));
            Files.deleteIfExists(target);
        }
    }

    @Test
    void failedFinalTransferReplyIsReported() throws Exception {
        try (FakeFtpServer server = new FakeFtpServer(true)) {
            server.start();
            Path target = Files.createTempFile("aj-ftp-", ".bin");
            byte[] original = "original-target".getBytes(StandardCharsets.US_ASCII);
            Files.write(target, original);
            try (SimpleFtpClient client = new SimpleFtpClient("127.0.0.1", server.getPort())) {
                client.login("user", "password");
                assertThrows(FtpProtocolException.class,
                        () -> client.getFile("remote.bin", target.toString()));
            }

            assertArrayEquals(original, Files.readAllBytes(target));

            try (java.util.stream.Stream<Path> paths = Files.list(target.getParent())) {
                assertEquals(0L, paths
                        .filter(path -> path.getFileName().toString().contains(target.getFileName() + "."))
                        .filter(path -> path.getFileName().toString().endsWith(".part"))
                        .count());
            }

            Files.deleteIfExists(target);
        }
    }

    @Test
    void rejectsCarriageReturnAndDoesNotAcceptIncompleteLogin() throws Exception {
        TestClient client = new TestClient();
        client.responses("220 ready\r\n", "332 account required\r\n");
        assertThrows(FtpLoginException.class, () -> client.login("user", "password"));

        TestClient injectionClient = new TestClient();
        injectionClient.responses("220 ready\r\n");
        assertThrows(FtpProtocolException.class, () -> injectionClient.command("NOOP\rDELE file"));
    }

    @Test
    void canNegotiateUtf8ControlEncoding() throws Exception {
        try (FakeFtpServer server = new FakeFtpServer(false)) {
            server.start();

            try (SimpleFtpClient client = new SimpleFtpClient("127.0.0.1", server.getPort())) {
                client.login("user", "password");
                client.enableUtf8();
                client.noop();
            }

            assertTrue(server.commands.contains("OPTS UTF8 ON"));
        }
    }

    private static final class TestClient extends FtpClient {
        void responses(String... responses) throws IOException {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            for (String response : responses)
                bytes.write(response.getBytes(StandardCharsets.ISO_8859_1));
            serverInput = new ByteArrayInputStream(bytes.toByteArray());
            serverOutput = new PrintStream(new ByteArrayOutputStream(), true, "ISO-8859-1");
            serverSocket = new Socket();
            readReply();
        }

        int command(String command) throws IOException {
            return issueCommand(command);
        }
    }

    private static final class FakeFtpServer implements Closeable {
        final List<String> commands = Collections.synchronizedList(new ArrayList<String>());
        private final ServerSocket control = new ServerSocket(0);
        private final boolean failTransfer;
        private volatile ServerSocket data;
        private volatile Socket controlSocket;
        private Thread thread;

        FakeFtpServer(boolean failTransfer) throws IOException {
            this.failTransfer = failTransfer;
        }

        int getPort() {
            return control.getLocalPort();
        }

        void start() {
            thread = new Thread(() -> {
                try {
                    controlSocket = control.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            controlSocket.getInputStream(), StandardCharsets.ISO_8859_1));
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(
                            controlSocket.getOutputStream(), StandardCharsets.ISO_8859_1), true);
                    out.print("220 test ready\r\n");
                    out.flush();
                    String line;

                    while ((line = in.readLine()) != null) {
                        commands.add(line);

                        if (line.startsWith("USER ")) reply(out, "331 password required");
                        else if (line.startsWith("PASS ")) reply(out, "230 logged in");
                        else if (line.equals("TYPE I")) reply(out, "200 binary");
                        else if (line.equals("OPTS UTF8 ON")) reply(out, "200 utf8 enabled");
                        else if (line.startsWith("SIZE ")) reply(out, "213 3000000000");
                        else if (line.equals("EPSV ALL")) reply(out, "200 epsv all");
                        else if (line.equals("EPSV")) {
                            data = new ServerSocket(0);
                            reply(out, "229 Entering Extended Passive Mode (|||" + data.getLocalPort() + "|)");
                        } else if (line.startsWith("RETR ")) {
                            reply(out, "150 opening data");
                            try (Socket socket = data.accept()) {
                                socket.getOutputStream().write(CONTENT);
                            }
                            data.close();
                            data = null;
                            reply(out, failTransfer ? "451 transfer failed" : "226 transfer complete");
                        } else if (line.equals("NOOP")) reply(out, "200 ok");
                        else if (line.equals("QUIT")) {
                            reply(out, "221 bye");
                            break;
                        } else reply(out, "500 unsupported");
                    }
                } catch (IOException ignored) {
                    // Closing the fixture is expected to interrupt accept/read.
                }
            }, "fake-ftp-server");
            thread.start();
        }

        static void reply(PrintWriter out, String value) {
            out.print(value + "\r\n");
            out.flush();
        }

        @Override
        public void close() throws IOException {
            if (data != null)
                data.close();

            if (controlSocket != null)
                controlSocket.close();

            control.close();

            if (thread != null) {

                try {
                    thread.join(2000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
