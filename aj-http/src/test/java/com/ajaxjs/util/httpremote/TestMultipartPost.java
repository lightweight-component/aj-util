package com.ajaxjs.util.httpremote;

import com.ajaxjs.util.httpremote.model.Response;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(15)
class TestMultipartPost {
    @TempDir
    Path tempDir;

    private HttpServer server;
    private String url;
    private final BlockingQueue<CapturedRequest> requests = new LinkedBlockingQueue<>();
    private final AtomicInteger redirectHits = new AtomicInteger();
    private volatile int responseCode = 201;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/upload", exchange -> {
            try {
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                try (InputStream in = exchange.getRequestBody()) {
                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = in.read(buffer)) != -1)
                        body.write(buffer, 0, length);
                }
                CapturedRequest request = new CapturedRequest();
                request.method = exchange.getRequestMethod();
                request.contentType = exchange.getRequestHeaders().getFirst("Content-Type");
                request.transferEncoding = exchange.getRequestHeaders().getFirst("Transfer-Encoding");
                request.customHeader = exchange.getRequestHeaders().getFirst("X-Test");
                request.body = body.toByteArray();
                requests.add(request);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                if (responseCode == 302)
                    exchange.getResponseHeaders().set("Location", "/redirected");

                byte[] reply = "response-body".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(responseCode, reply.length);
                exchange.getResponseBody().write(reply);
            } finally {
                exchange.close();
            }
        });
        server.createContext("/redirected", exchange -> {
            redirectHits.incrementAndGet();
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();
        url = "http://127.0.0.1:" + server.getAddress().getPort() + "/upload";
    }

    @AfterEach
    void stopServer() {
        if (server != null)
            server.stop(0);
    }

    @Test
    void uploadsTextAndFileWithMatchingBoundaryAndCustomHeaders() throws Exception {
        Path file = Files.write(tempDir.resolve("note.txt"), "文件内容".getBytes(StandardCharsets.UTF_8));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", "张三");
        data.put("empty", null);
        data.put("attachment", file.toFile());
        AtomicInteger callbacks = new AtomicInteger();

        Response response = MultipartPost.upload(url, data, connection -> {
            callbacks.incrementAndGet();
            connection.setRequestProperty("X-Test", "custom-value");
            connection.setRequestProperty("Content-Type", "incorrect/type");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
        });

        assertTrue(response.isOk());
        assertEquals(Integer.valueOf(201), response.getHttpCode());
        assertNull(response.getEx());
        assertEquals("response-body", response.getResponseText());
        assertEquals(1, callbacks.get());
        CapturedRequest request = takeRequest();
        assertEquals("POST", request.method);
        assertEquals("chunked", request.transferEncoding);
        assertEquals("custom-value", request.customHeader);
        String boundary = boundary(request);
        String body = new String(request.body, StandardCharsets.UTF_8);
        assertTrue(body.startsWith("--" + boundary + "\r\n"));
        assertTrue(body.endsWith("\r\n--" + boundary + "--\r\n"));
        assertTrue(body.contains("\r\n\r\n张三\r\n--" + boundary));
        assertTrue(body.contains("name=\"empty\"\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\n\r\n--" + boundary));
        assertTrue(body.contains("name=\"attachment\"; filename=\"note.txt\""));
        assertTrue(body.contains("\r\n\r\n文件内容\r\n--" + boundary));
    }

    @Test
    void uploadsLocalFileWithExplicitFieldName() throws Exception {
        byte[] content = {0, 1, (byte) 255, 13, 10};
        Path file = Files.write(tempDir.resolve("payload.bin"), content);
        assertTrue(MultipartPost.uploadFile(url, "document", file.toFile(), null).isOk());
        assertFile(takeRequest(), "document", "payload.bin", content);
    }

    @Test
    void uploadsByteArraysIncludingEmptyFiles() throws Exception {
        for (byte[] content : new byte[][]{new byte[0], {0, 13, 10, (byte) 255}}) {
            assertTrue(MultipartPost.uploadFile(url, "file", "payload.bin", content, null).isOk());
            assertFile(takeRequest(), "file", "payload.bin", content);
        }
    }

    @Test
    void uploadsLargeStreamWithoutClosingCallerInput() throws Exception {
        byte[] content = new byte[32769];
        for (int i = 0; i < content.length; i++)
            content[i] = (byte) i;

        TrackingInput in = new TrackingInput(content);
        try {
            assertTrue(MultipartPost.uploadFile(url, "stream", "payload.bin", in, null).isOk());
            assertFalse(in.closed);
            assertFile(takeRequest(), "stream", "payload.bin", content);
        } finally {
            in.close();
        }
    }

    @Test
    void generatesIndependentBoundariesForEachRequest() throws Exception {
        MultipartPost.upload(url, Collections.singletonMap("text", "first"), null);
        String first = boundary(takeRequest());
        MultipartPost.upload(url, Collections.singletonMap("text", "second"), null);
        assertNotEquals(first, boundary(takeRequest()));
    }

    @Test
    void preservesHttpErrorStatusAndResponseBody() throws Exception {
        responseCode = 422;
        Response response = MultipartPost.upload(url, Collections.singletonMap("text", "value"), null);
        assertFalse(response.isOk());
        assertEquals(Integer.valueOf(422), response.getHttpCode());
        assertEquals("response-body", response.getResponseText());
        takeRequest();
    }

    @Test
    void doesNotFollowRedirectsOrReplayUploads() throws Exception {
        responseCode = 302;
        Response response = MultipartPost.upload(url, Collections.singletonMap("text", "value"), null);
        assertFalse(response.isOk());
        assertEquals(Integer.valueOf(302), response.getHttpCode());
        takeRequest();
        assertEquals(0, redirectHits.get());
    }

    @Test
    void rejectsInvalidArgumentsBeforeSending() {
        assertThrows(IllegalArgumentException.class, () -> MultipartPost.upload(url, null, null));
        assertThrows(IllegalArgumentException.class, () -> MultipartPost.upload(url, Collections.emptyMap(), null));
        assertThrows(IllegalArgumentException.class, () -> MultipartPost.upload(" ", Collections.singletonMap("text", "x"), null));
        assertThrows(IllegalArgumentException.class, () -> MultipartPost.upload(null, Collections.singletonMap("text", "x"), null));
        assertThrows(NullPointerException.class, () -> MultipartPost.uploadFile(url, "file", (File) null, null));
        assertThrows(IllegalArgumentException.class, () -> MultipartPost.uploadFile(url, "file", tempDir.toFile(), null));
        assertThrows(IllegalArgumentException.class, () -> MultipartPost.uploadFile(url, "file", tempDir.resolve("missing").toFile(), null));
        assertThrows(NullPointerException.class, () -> MultipartPost.uploadFile(url, "file", "a.txt", (byte[]) null, null));
        assertThrows(NullPointerException.class, () -> MultipartPost.uploadFile(url, "file", "a.txt", (InputStream) null, null));
        assertTrue(requests.isEmpty());
    }

    @Test
    void propagatesConnectionCallbackFailureWithoutSending() {
        IllegalStateException failure = new IllegalStateException("callback failure");
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> MultipartPost.upload(url, Collections.singletonMap("text", "x"), connection -> {
                    throw failure;
                })));
        assertTrue(requests.isEmpty());
    }

    private CapturedRequest takeRequest() throws InterruptedException {
        CapturedRequest request = requests.poll(3, TimeUnit.SECONDS);
        assertNotNull(request, "The local server did not receive the upload.");

        return request;
    }

    private static String boundary(CapturedRequest request) {
        String prefix = "multipart/form-data; boundary=";
        assertNotNull(request.contentType);
        assertTrue(request.contentType.startsWith(prefix));

        return request.contentType.substring(prefix.length());
    }

    private static void assertFile(CapturedRequest request, String field, String name, byte[] content) throws IOException {
        String boundary = boundary(request);
        ByteArrayOutputStream expected = new ByteArrayOutputStream();
        expected.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + field
                + "\"; filename=\"" + name + "\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        expected.write(content);
        expected.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        assertArrayEquals(expected.toByteArray(), request.body);
    }

    private static class CapturedRequest {
        String method;
        String contentType;
        String transferEncoding;
        String customHeader;
        byte[] body;
    }

    private static class TrackingInput extends ByteArrayInputStream {
        boolean closed;

        TrackingInput(byte[] data) {
            super(data);
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
