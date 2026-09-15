package com.ajaxjs.util.httpremote;

import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.StrUtil;
import com.ajaxjs.util.httpremote.model.HttpConstant;
import com.ajaxjs.util.io.DataWriter;
import lombok.Setter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

public class FormFileUpload {
    @Setter
    String fieldName = "file";

    /**
     * where is the data writes to
     */
    OutputStream out;

    /**
     * 定义数据分隔线
     */
    String boundary;

    Map<String, ?> data;

    /**
     * 换行符
     */
    private static final String NEWLINE = "\r\n";

    /**
     * Prefix used before the multipart boundary.
     */
    private static final String BOUNDARY_PREFIX = "--";

    private static final String TEXT_PART = BOUNDARY_PREFIX + "${boundary}" + NEWLINE
            + "Content-Disposition: form-data; name=\"${name}\"" + NEWLINE
            + "Content-Type: text/plain; charset=UTF-8" + NEWLINE + NEWLINE
            + "${value}" + NEWLINE;

    private static final String FILE_PART = BOUNDARY_PREFIX + "${boundary}" + NEWLINE
            + "Content-Disposition: form-data; name=\"${name}\"; filename=\"${fileName}\"" + NEWLINE
            + "Content-Type: " + HttpConstant.FILE_TYPE + NEWLINE + NEWLINE;

    public FormFileUpload(OutputStream out) {
        if (out == null)
            throw new IllegalArgumentException("Output stream must not be null.");

        this.out = out;
        boundary = "file-ulpoad-" + UUID.randomUUID();
    }

    public void write(File file) {
        write(file, fieldName);
    }

    public void write(File file, String fieldName) {
        Map<String, Object> data = ObjectHelper.mapOf(fieldName, file);
        write(data);
    }

    public void write(InputStream in, String fileName, String fieldName) {
        Map<String, Object> data = ObjectHelper.mapOf(fieldName + "_" + fileName, in);
        write(data);
    }

    public void write(InputStream in, String fileName) {
        write(in, fileName, fieldName);
    }

    public void write(byte[] _data, String fileName, String fieldName) {
        Map<String, Object> data = ObjectHelper.mapOf(fieldName + "_" + fileName, _data);
        write(data);
    }

    public void write(byte[] data, String fileName) {
        write(data, fileName, fieldName);
    }

    public void write(Map<String, Object> data) {
        if (ObjectHelper.isEmpty(data))
            throw new IllegalArgumentException("Multipart data must not be null or empty.");

        try {
            for (String name : data.keySet()) {
                Object value = data.get(name);

                if (value instanceof byte[]) {
                    String[] arr = name.split("_");
                    name = arr[0];
                    String fileName = arr[1];
                    Map<String, String> params = ObjectHelper.mapOf("boundary", boundary, "name", name, "fileName", fileName);

                    writeText(params);

                    byte[] _data = (byte[]) value;
                    out.write(_data);
                } else if (value instanceof InputStream) {
                    String[] arr = name.split("_");
                    name = arr[0];
                    String fileName = arr[1];
                    Map<String, String> params = ObjectHelper.mapOf("boundary", boundary, "name", name, "fileName", fileName);
                    writeText(params);

                    InputStream in = (InputStream) value;

                    DataWriter.write(out, in);
                } else if (value instanceof File) {
                    File file = (File) value;
                    Map<String, String> params = ObjectHelper.mapOf("boundary", boundary, "name", name, "fileName", file.getName());
                    writeText(params);

                    try (InputStream in = Files.newInputStream(file.toPath())) {
                        DataWriter.write(out, in);
                    }
                } else {
                    String str = value == null ? "" : value.toString();
                    Map<String, String> params = ObjectHelper.mapOf("boundary", boundary, "name", name, "value", str);
                    writeText(params);
                }

                out.write(NEWLINE.getBytes(StandardCharsets.UTF_8));
            }

//        writeClosingBoundary(out, boundary);
            out.write((BOUNDARY_PREFIX + boundary + BOUNDARY_PREFIX + NEWLINE).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void writeText(Map<String, String> params) throws IOException {
        byte[] fieldData = StrUtil.simpleTpl(TEXT_PART, params).getBytes(StandardCharsets.UTF_8);
        out.write(fieldData);
    }
}
