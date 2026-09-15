package com.ajaxjs.util.httpremote;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestMultipartWriter {
    @TempDir
    Path tempDir;

    @Test
    void textFieldsPreserveWhitespaceWithoutAddingAnExtraNewline() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", " 张三\r\n");
        data.put("empty", null);
        data.put("number", 42);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new MultipartWriter(out, "test-boundary").write(data);

        String expected = textPart("name", " 张三\r\n")
                + textPart("empty", "") + textPart("number", "42")
                + "--test-boundary--\r\n";
        assertArrayEquals(expected.getBytes(StandardCharsets.UTF_8), out.toByteArray());
    }

    @Test
    void generatedBoundaryMatchesContentTypeAndClosingDelimiter() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MultipartWriter writer = new MultipartWriter(out);
        String contentType = writer.getContentType();
        assertTrue(contentType.startsWith("multipart/form-data; boundary="));
        String boundary = contentType.substring(contentType.indexOf("boundary=") + 9);
        assertTrue(boundary.matches("[A-Za-z0-9_-]{1,70}"));
        writer.write(Collections.singletonMap("text", "value"));
        String body = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(body.startsWith("--" + boundary + "\r\n"));
        assertTrue(body.endsWith("\r\n--" + boundary + "--\r\n"));
    }

    @Test
    void rejectsInvalidBoundariesAndNullOutput() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThrows(NullPointerException.class, () -> new MultipartWriter(null));
        assertThrows(NullPointerException.class, () -> new MultipartWriter(out, null));
        for (String boundary : new String[]{"", "a b", "a\r\nb", "a;b", String.join("", Collections.nCopies(71, "a"))})
            assertThrows(IllegalArgumentException.class, () -> new MultipartWriter(out, boundary));

        assertDoesNotThrow(() -> new MultipartWriter(out, String.join("", Collections.nCopies(70, "a"))));
    }

    @Test
    void byteArrayPreservesBinaryContentAndUsesFallbackMimeType() throws IOException {
        byte[] data = {0, 1, 13, 10, (byte) 255};
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new MultipartWriter(out, "test-boundary").write(data, "payload.unknown-extension", "attachment");
        assertArrayEquals(filePart("attachment", "payload.unknown-extension", "application/octet-stream", data), out.toByteArray());
    }

    @Test
    void localFileUsesDefaultFieldAndInferredMimeType() throws IOException {
        byte[] data = "文件正文".getBytes(StandardCharsets.UTF_8);
        Path file = Files.write(tempDir.resolve("note.txt"), data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new MultipartWriter(out, "test-boundary").write(file.toFile());
        assertArrayEquals(filePart("file", "note.txt", "text/plain", data), out.toByteArray());
    }

    @Test
    void explicitFileFieldOverridesConfiguredDefault() throws IOException {
        Path file = Files.write(tempDir.resolve("empty.txt"), new byte[0]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MultipartWriter writer = new MultipartWriter(out, "test-boundary");
        writer.setFieldName("default-field");
        writer.write(file.toFile(), "explicit-field");
        assertArrayEquals(filePart("explicit-field", "empty.txt", "text/plain", new byte[0]), out.toByteArray());
    }

    @Test
    void byteArrayConvenienceOverloadUsesConfiguredField() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MultipartWriter writer = new MultipartWriter(out, "test-boundary");
        writer.setFieldName("attachment");
        writer.write(new byte[0], "empty.txt");
        assertArrayEquals(filePart("attachment", "empty.txt", "text/plain", new byte[0]), out.toByteArray());
    }

    @Test
    void streamOverloadsPreserveContentAndDoNotCloseCallerStreams() throws IOException {
        byte[] data = new byte[20000];
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) i;

        for (boolean explicit : new boolean[]{true, false}) {
            TrackingInput in = new TrackingInput(data);
            TrackingOutput out = new TrackingOutput();
            MultipartWriter writer = new MultipartWriter(out, "test-boundary");
            if (explicit)
                writer.write(in, "data.bin", "file");
            else
                writer.write(in, "data.bin");

            assertArrayEquals(filePart("file", "data.bin", "application/octet-stream", data), out.toByteArray());
            assertFalse(in.closed);
            assertFalse(out.closed);
            assertFalse(out.flushed);
        }
    }

    @Test
    void rejectsNullInputsEmptyFormsAndMissingFileMetadata() {
        MultipartWriter writer = new MultipartWriter(new ByteArrayOutputStream(), "test-boundary");
        assertThrows(IllegalArgumentException.class, () -> writer.write((Map<String, Object>) null));
        assertThrows(IllegalArgumentException.class, () -> writer.write(Collections.emptyMap()));
        assertThrows(NullPointerException.class, () -> writer.write((File) null));
        assertThrows(NullPointerException.class, () -> writer.write((InputStream) null, "a.txt"));
        assertThrows(NullPointerException.class, () -> writer.write((byte[]) null, "a.txt"));
        assertThrows(NullPointerException.class, () -> writer.write(new byte[0], null));
        assertThrows(NullPointerException.class, () -> writer.write(new byte[0], "a.txt", null));
        assertThrows(IllegalArgumentException.class, () -> writer.write(new byte[0], "", "file"));
        assertThrows(IllegalArgumentException.class, () -> writer.write(Collections.singletonMap("file", new byte[0])));
        assertThrows(IllegalArgumentException.class, () -> writer.write(Collections.singletonMap("file", new ByteArrayInputStream(new byte[0]))));
    }

    @Test
    void rejectsHeaderInjection() {
        for (String invalid : new String[]{"bad\rname", "bad\nname"}) {
            MultipartWriter writer = new MultipartWriter(new ByteArrayOutputStream(), "test-boundary");
            assertThrows(IllegalArgumentException.class, () -> writer.write(Collections.singletonMap(invalid, "text")));
            assertThrows(IllegalArgumentException.class, () -> writer.write(new byte[0], invalid, "file"));
            assertThrows(IllegalArgumentException.class, () -> writer.write(new byte[0], "a.txt", invalid));
        }
    }

    @Test
    void escapesQuotesAndBackslashesInHeaders() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new MultipartWriter(out, "test-boundary").write(new byte[0], "a\"b\\c.txt", "x\"y\\z");
        String body = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(body.contains("name=\"x\\\"y\\\\z\"; filename=\"a\\\"b\\\\c.txt\""));
    }

    @Test
    void propagatesReadAndWriteFailuresWithOriginalCause() {
        IOException failure = new IOException("test failure");
        OutputStream out = new OutputStream() {
            @Override
            public void write(int value) throws IOException {
                throw failure;
            }
        };
        UncheckedIOException writeError = assertThrows(UncheckedIOException.class,
                () -> new MultipartWriter(out).write(Collections.singletonMap("text", "value")));
        assertSame(failure, writeError.getCause());

        InputStream in = new InputStream() {
            @Override
            public int read() throws IOException {
                throw failure;
            }
        };
        UncheckedIOException readError = assertThrows(UncheckedIOException.class,
                () -> new MultipartWriter(new ByteArrayOutputStream()).write(in, "a.txt"));
        assertSame(failure, readError.getCause());
    }

    @Test
    void missingLocalFileReportsIoFailure() {
        assertThrows(UncheckedIOException.class,
                () -> new MultipartWriter(new ByteArrayOutputStream()).write(tempDir.resolve("missing.txt").toFile()));
    }

    private static String textPart(String name, String value) {
        return "--test-boundary\r\nContent-Disposition: form-data; name=\"" + name
                + "\"\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\n" + value + "\r\n";
    }

    private static byte[] filePart(String field, String name, String mime, byte[] data) throws IOException {
        ByteArrayOutputStream expected = new ByteArrayOutputStream();
        expected.write(("--test-boundary\r\nContent-Disposition: form-data; name=\"" + field
                + "\"; filename=\"" + name + "\"\r\nContent-Type: " + mime + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        expected.write(data);
        expected.write("\r\n--test-boundary--\r\n".getBytes(StandardCharsets.UTF_8));

        return expected.toByteArray();
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

    private static class TrackingOutput extends ByteArrayOutputStream {
        boolean closed;
        boolean flushed;

        @Override
        public void close() {
            closed = true;
        }

        @Override
        public void flush() {
            flushed = true;
        }
    }
}
