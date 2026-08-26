package com.ajaxjs.image;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class TestUtils {
    public static byte[] getFileAsBytes(String fileName) {
        try (InputStream inputStream = TestUtils.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null)
                throw new IOException("File not found: " + fileName);

            byte[] bytes = new byte[inputStream.available()];
            int bytesRead = inputStream.read(bytes);

            if (bytesRead <= 0)
                bytes = new byte[0]; // Handle empty files
            else if (bytesRead < bytes.length) {
                // Resize array if needed
                byte[] actualBytes = new byte[bytesRead];
                System.arraycopy(bytes, 0, actualBytes, 0, bytesRead);
                bytes = actualBytes;
            }

            return bytes;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
