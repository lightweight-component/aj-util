package com.ajaxjs.net.ftp;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests against an in-process Apache FTP server.
 */
class TestEmbeddedFtpServer {
    private FtpServer server;
    private Path ftpHome;
    private int port;

    @BeforeEach
    void startServer() throws Exception {
        ftpHome = Files.createTempDirectory("aj-ftp-home-");

        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }

        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(port);
        serverFactory.addListener("default", listenerFactory.createListener());

        UserManager userManager = serverFactory.getUserManager();
        BaseUser user = new BaseUser();
        user.setName("test");
        user.setPassword("password");
        user.setHomeDirectory(ftpHome.toString());
        user.setAuthorities(Collections.singletonList(new WritePermission()));
        userManager.save(user);

        server = serverFactory.createServer();
        server.start();
    }

    @AfterEach
    void stopServer() throws Exception {
        if (server != null)
            server.stop();
        deleteTree(ftpHome);
    }

    @Test
    void uploadsAndDownloadsBinaryContent() throws Exception {
        byte[] content = new byte[16385];
        for (int i = 0; i < content.length; i++)
            content[i] = (byte) (i * 31);

        Path source = Files.createTempFile("aj-ftp-source-", ".bin");
        Path target = Files.createTempFile("aj-ftp-target-", ".bin");
        try {
            Files.write(source, content);
            try (SimpleFtpClient client = new SimpleFtpClient("127.0.0.1", port)) {
                client.login("test", "password");
                client.upload(source.toString(), "/uploaded.bin");
                client.getFile("/uploaded.bin", target.toString());
                client.noop();
            }

            assertArrayEquals(content, Files.readAllBytes(ftpHome.resolve("uploaded.bin")));
            assertArrayEquals(content, Files.readAllBytes(target));
        } finally {
            Files.deleteIfExists(source);
            Files.deleteIfExists(target);
        }
    }

    @Test
    void supportsEmptyFilesAndReportsMissingRemoteFile() throws Exception {
        Path empty = Files.createTempFile("aj-ftp-empty-", ".bin");
        Path target = Files.createTempFile("aj-ftp-target-", ".bin");

        try {
            try (SimpleFtpClient client = new SimpleFtpClient("127.0.0.1", port)) {
                client.login("test", "password");
                client.upload(empty.toString(), "/empty.bin");
                client.getFile("/empty.bin", target.toString());
                assertThrows(java.io.IOException.class,
                        () -> client.getFile("/missing.bin", target.toString()));
            }

            assertEquals(0L, Files.size(ftpHome.resolve("empty.bin")));
            assertEquals(0L, Files.size(target));
        } finally {
            Files.deleteIfExists(empty);
            Files.deleteIfExists(target);
        }
    }

    @Test
    void supportsFileAndDirectoryCommands() throws Exception {
        Files.createDirectory(ftpHome.resolve("folder"));
        Files.write(ftpHome.resolve("original.txt"), new byte[]{1, 2});

        try (SimpleFtpClient client = new SimpleFtpClient("127.0.0.1", port)) {
            client.login("test", "password");
            assertTrue(client.isLoggedIn());
            assertNotNull(client.getWelcomeMessage());
            assertFalse(client.system().isEmpty());
            assertEquals("/", client.pwd());

            client.ascii();
            client.binary();
            client.rename("original.txt", "renamed.txt");
            client.cd("folder");
            assertEquals("/folder", client.pwd());
            client.cd("");
            client.cdUp();

            assertTrue(readAll(client.list()).length > 0);
            assertTrue(new String(readAll(client.nameList(null)), StandardCharsets.ISO_8859_1).contains("renamed.txt"));
            assertTrue(new String(readAll(client.nameList("/")), StandardCharsets.ISO_8859_1).contains("renamed.txt"));

            try (java.io.OutputStream out = client.append("renamed.txt")) {
                out.write(3);
            }

            client.completePendingCommand();
            assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(ftpHome.resolve("renamed.txt")));

            client.reInit();
            assertFalse(client.isLoggedIn());
        }
    }

    static byte[] readAll(InputStream input) throws IOException {
        try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[256];

            for (int count; (count = in.read(buffer)) != -1; )
                out.write(buffer, 0, count);

            return out.toByteArray();
        }
    }

    static void deleteTree(Path root) throws Exception {
        if (root == null || !Files.exists(root))
            return;

        try (java.util.stream.Stream<Path> paths = Files.walk(root)) {
            paths.sorted(Collections.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (java.io.IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
