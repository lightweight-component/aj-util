/**
 * Copyright Sp42 frank@ajaxjs.com Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.ajaxjs.util.httpremote;

import com.ajaxjs.util.httpremote.model.HttpConstant;
import com.ajaxjs.util.httpremote.model.HttpMethod;
import com.ajaxjs.util.httpremote.model.Response;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Sends multipart/form-data requests using {@link Post}.
 *
 * <p>{@link MultipartWriter} encodes the request body; this class manages
 * the connection and sends the request. Connection callbacks must only
 * configure the connection and must not connect or read/write data.</p>
 */
public final class MultipartPost {
    private MultipartPost() {
    }

    /**
     * Uploads text fields and files in a single multipart request.
     *
     * <p>Use File values in the map; use the dedicated overloads for streams
     * and byte arrays.</p>
     *
     * @param url target HTTP or HTTPS URL
     * @param data multipart fields; must not be null or empty
     * @param initConnection optional connection configuration callback
     * @return the server response, including HTTP status and request errors
     */
    public static Response upload(String url, Map<String, ?> data, Consumer<HttpURLConnection> initConnection) {
        if (data == null || data.isEmpty())
            throw new IllegalArgumentException("Multipart data must not be null or empty.");

        Map<String, Object> fields = new LinkedHashMap<>(data);

        return execute(url, form -> form.write(fields), initConnection);
    }

    /**
     * Uploads a single local file.
     *
     * @param url target HTTP or HTTPS URL
     * @param fieldName multipart file field name
     * @param file readable regular file to upload
     * @param initConnection optional connection configuration callback
     * @return the server response, including HTTP status and request errors
     */
    public static Response uploadFile(String url, String fieldName, File file, Consumer<HttpURLConnection> initConnection) {
        Objects.requireNonNull(file, "File must not be null.");

        if (!file.isFile() || !file.canRead())
            throw new IllegalArgumentException("File must be a readable regular file.");

        return execute(url, form -> form.write(file, fieldName), initConnection);
    }

    /**
     * Uploads a byte array as a file.
     *
     * @param url target HTTP or HTTPS URL
     * @param fieldName multipart file field name
     * @param fileName file name reported in the multipart header
     * @param data file content
     * @param initConnection optional connection configuration callback
     * @return the server response, including HTTP status and request errors
     */
    public static Response uploadFile(String url, String fieldName, String fileName, byte[] data, Consumer<HttpURLConnection> initConnection) {
        Objects.requireNonNull(data, "File content must not be null.");

        return execute(url, form -> form.write(data, fileName, fieldName), initConnection);
    }

    /**
     * Uploads an input stream as a file. The caller must close the input stream.
     *
     * @param url target HTTP or HTTPS URL
     * @param fieldName multipart file field name
     * @param fileName file name reported in the multipart header
     * @param in file content stream; not closed by this method
     * @param initConnection optional connection configuration callback
     * @return the server response, including HTTP status and request errors
     */
    public static Response uploadFile(String url, String fieldName, String fileName, InputStream in, Consumer<HttpURLConnection> initConnection) {
        Objects.requireNonNull(in, "Input stream must not be null.");

        return execute(url, form -> form.write(in, fileName, fieldName), initConnection);
    }

    /**
     * Executes the request lifecycle shared by all upload methods.
     */
    private static Response execute(String url, Consumer<MultipartWriter> writeBody, Consumer<HttpURLConnection> initConnection) {
        if (url == null || url.trim().isEmpty())
            throw new IllegalArgumentException("URL must not be empty.");

        String boundary = "file-upload-" + UUID.randomUUID();
        String contentType = HttpConstant.CONTENT_TYPE_FORM_UPLOAD + "; boundary=" + boundary;

        // Use the constructor that does not automatically send the request.
        Post post = new Post(HttpMethod.POST, url);
        post.setContentType(contentType);

        HttpURLConnection connection = post.init();

        try {
            if (initConnection != null)
                initConnection.accept(connection);

            // Apply this header after the callback so it matches the body boundary.
            connection.setRequestProperty(HttpConstant.CONTENT_TYPE, contentType);
            connection.setChunkedStreamingMode(8192);// Stream without buffering the entire file.
            connection.setInstanceFollowRedirects(false);// Let the caller handle redirects without replaying the upload.

            post.setOutputStreamConsumer(out -> {
                MultipartWriter form = new MultipartWriter(out, boundary);
                writeBody.accept(form);
            });

            post.initData();// Acquire, write and close the request output stream.

            return post.connect(); // Read the server response.
        } finally {
            connection.disconnect();
        }
    }
}
