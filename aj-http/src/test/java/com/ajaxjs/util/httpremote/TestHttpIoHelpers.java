package com.ajaxjs.util.httpremote;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class TestHttpIoHelpers {
    @TempDir
    Path tempDir;

    @Test
    void multipartUsesUtf8StreamsFilesAndHandlesNullValues() throws Exception {
        Path file = tempDir.resolve("content.txt");
        Files.write(file, "文件内容".getBytes(StandardCharsets.UTF_8));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("text", "你好");
        data.put("empty", null);
        data.put("file", file.toFile());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        new MultipartWriter(out, "test-boundary").write(data);
        String body = new String(out.toByteArray(), StandardCharsets.UTF_8);

        assertTrue(body.contains("你好"));
        assertTrue(body.contains("文件内容"));
        assertTrue(body.contains("name=\"empty\""));
        assertTrue(body.endsWith("--test-boundary--\r\n"));
        assertThrows(IllegalArgumentException.class,
                () -> new MultipartWriter(out, "boundary").write(new LinkedHashMap<>()));
    }

    @Test
    void gzipReturnsOriginalStreamOrReportsInvalidGzip() throws Exception {
        InputStream plain = new ByteArrayInputStream("plain".getBytes(StandardCharsets.UTF_8));
        assertSame(plain, Head.gzip(connection(null), plain));

        InputStream invalid = new ByteArrayInputStream("not-gzip".getBytes(StandardCharsets.UTF_8));
        UncheckedIOException error =
                assertThrows(UncheckedIOException.class, () -> Head.gzip(connection("gzip"), invalid));
        assertNotNull(error.getCause());
    }

    @Test
    void gzipRecognizesCaseInsensitiveEncodingTokenOnly() throws Exception {
        byte[] content = "gzip content".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(compressed)) {
            gzip.write(content);
        }

        try (InputStream in = Head.gzip(connection(" GZip "), new ByteArrayInputStream(compressed.toByteArray()))) {
            assertArrayEquals(content, readAll(in));
        }

        InputStream chained = new ByteArrayInputStream(compressed.toByteArray());
        assertSame(chained, Head.gzip(connection("gzip, br"), chained));
    }

    private static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[128];
        int length;

        while ((length = in.read(buffer)) != -1)
            out.write(buffer, 0, length);

        return out.toByteArray();
    }

    private static HttpURLConnection connection(String contentEncoding) throws Exception {
        return new HttpURLConnection(new URL("http://example.test")) {
            public String getHeaderField(String name) {
                return "Content-Encoding".equals(name) ? contentEncoding : null;
            }

            public void disconnect() {
            }

            public boolean usingProxy() {
                return false;
            }

            public void connect() {
            }
        };
    }
}
