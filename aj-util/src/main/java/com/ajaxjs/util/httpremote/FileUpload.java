package com.ajaxjs.util.httpremote;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Utilities for uploading files via multipart/form-data POST requests.
 */
public class FileUpload {
    /**
     * 换行符
     */
    private static final String NEWLINE = "\r\n";

    /**
     * Prefix used before the multipart boundary.
     */
    private static final String BOUNDARY_PREFIX = "--";

    /**
     * 定义数据分隔线
     */
    private static String newBoundary() {
        return "ajutil-" + UUID.randomUUID().toString();
    }

    /**
     * Template for a single file field in a multipart request.
     */
    /**
     * 以POST方法上传文件
     *
     * @param url       上传文件的URL
     * @param fieldName 文件字段名
     * @param fileName  文件名
     * @param file      文件内容
     * @param fn        保留参数；当前实现不会应用该连接回调
     * @return 服务端响应解析得到的 Map
     */
    public static Map<String, Object> uploadFile(String url, String fieldName, String fileName, byte[] file, Consumer<HttpURLConnection> fn) {
        if (file == null)
            throw new IllegalArgumentException("File content must not be null.");

        String boundary = newBoundary();
        Post post = preparePost(url, boundary, fn);
        post.setOutputStreamConsumer(out -> {
            try {
                writeFilePart(out, boundary, fieldName, fileName, new ByteArrayInputStream(file));
                writeClosingBoundary(out, boundary);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to write multipart upload.", e);
            }
        });
        post.initData();
        post.connect();

        return post.getResp().responseAsJson();
    }

    public static Map<String, Object> upload(String url, Map<String, ?> data, Consumer<HttpURLConnection> fn) {
        if (data == null || data.isEmpty())
            throw new IllegalArgumentException("Multipart data must not be null or empty.");

        String boundary = newBoundary();
        Post post = preparePost(url, boundary, fn);
        post.setOutputStreamConsumer(out -> writeFormData(data, out, boundary));
        post.initData();
        post.connect();

        return post.getResp().responseAsJson();
    }

    private static Post preparePost(String url, String boundary, Consumer<HttpURLConnection> fn) {
        if (url == null || url.trim().isEmpty())
            throw new IllegalArgumentException("URL must not be null or empty.");

        Post post = new Post(HttpConstant.HttpMethod.POST, url);
        Consumer<HttpURLConnection> multipart =
                conn -> conn.setRequestProperty(HttpConstant.CONTENT_TYPE, "multipart/form-data; boundary=" + boundary);
        post.init(fn == null ? multipart : fn.andThen(multipart));

        return post;
    }

    public static void writeFormData(Map<String, ?> data, OutputStream out, String boundary) {
        if (data == null || data.isEmpty())
            throw new IllegalArgumentException("Multipart data must not be null or empty.");
        if (out == null)
            throw new IllegalArgumentException("Output stream must not be null.");
        if (boundary == null || boundary.isEmpty())
            throw new IllegalArgumentException("Boundary must not be null or empty.");

        try {
            for (Map.Entry<String, ?> entry : data.entrySet()) {
                String name = entry.getKey();
                if (name == null || name.isEmpty())
                    throw new IllegalArgumentException("Multipart field name must not be null or empty.");

                Object value = entry.getValue();
                if (value instanceof File) {
                    File file = (File) value;
                    try (InputStream in = Files.newInputStream(file.toPath())) {
                        writeFilePart(out, boundary, name, file.getName(), in);
                    }
                } else
                    writeTextPart(out, boundary, name, value == null ? "" : value.toString());
            }
            writeClosingBoundary(out, boundary);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write multipart form data.", e);
        }
    }

    private static void writeTextPart(OutputStream out, String boundary, String name, String value) throws IOException {
        writeUtf8(out, BOUNDARY_PREFIX + boundary + NEWLINE
                + "Content-Disposition: form-data; name=\"" + name + "\"" + NEWLINE
                + "Content-Type: text/plain; charset=UTF-8" + NEWLINE + NEWLINE
                + value + NEWLINE);
    }

    private static void writeFilePart(OutputStream out, String boundary, String fieldName,
                                      String fileName, InputStream in) throws IOException {
        if (fieldName == null || fieldName.isEmpty())
            throw new IllegalArgumentException("File field name must not be null or empty.");
        if (fileName == null || fileName.isEmpty())
            throw new IllegalArgumentException("File name must not be null or empty.");

        writeUtf8(out, BOUNDARY_PREFIX + boundary + NEWLINE
                + "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"" + NEWLINE
                + "Content-Type: " + HttpConstant.FILE_TYPE + NEWLINE + NEWLINE);
        byte[] buffer = new byte[8192];
        int length;
        while ((length = in.read(buffer)) != -1)
            out.write(buffer, 0, length);
        writeUtf8(out, NEWLINE);
    }

    private static void writeClosingBoundary(OutputStream out, String boundary) throws IOException {
        writeUtf8(out, BOUNDARY_PREFIX + boundary + BOUNDARY_PREFIX + NEWLINE);
    }

    private static void writeUtf8(OutputStream out, String value) throws IOException {
        out.write(value.getBytes(StandardCharsets.UTF_8));
    }
}
