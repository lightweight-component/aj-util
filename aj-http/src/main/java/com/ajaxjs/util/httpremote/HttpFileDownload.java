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

import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.UrlCodec;
import com.ajaxjs.util.io.FileHelper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloads files from HTTP or HTTPS URLs.
 *
 * <p>The downloader supports both explicit target file paths and target
 * directories. When a directory is supplied, the final file name is resolved
 * using the following priority:</p>
 *
 * <ol>
 *     <li>Filename supplied by the HTTP {@code Content-Disposition} header</li>
 *     <li>Filename contained in the URL path</li>
 *     <li>A generated filename based on UUID and HTTP {@code Content-Type}</li>
 * </ol>
 *
 * <p>If the target itself explicitly contains a file name, that file name
 * always takes precedence over names supplied by the server or URL.</p>
 *
 * <p>Downloads are streamed directly to a temporary file and therefore do
 * not require the complete response body to be held in memory. After the
 * download completes successfully, the temporary file is moved to its final
 * destination.</p>
 */
public class HttpFileDownload {
    /**
     * URL used for a single-file download.
     */
    private String url;

    /**
     * Target file or directory used for a single-file download.
     */
    private Path targetPath;

    /**
     * Whether {@link #targetPath} should explicitly be interpreted as a
     * directory.
     */
    private boolean targetIsDirectory;

    /**
     * URLs used for batch downloading.
     */
    private String[] urls;

    /**
     * Destination directory used for batch downloading.
     */
    private Path targetPathDir;

    /**
     * Creates a downloader for one URL.
     *
     * <p>If {@code target} ends with {@code /} or {@code \}, it is explicitly
     * treated as a directory. An existing directory is also treated as a
     * directory. Otherwise, the target is interpreted as the final file path.</p>
     *
     * @param url    HTTP or HTTPS URL
     * @param target target file or directory
     */
    public HttpFileDownload(String url, String target) {
        Objects.requireNonNull(target, "HttpFileDownload.target");

        this.url = Objects.requireNonNull(url, "HttpFileDownload.url");
        this.targetPath = Paths.get(target);

        this.targetIsDirectory = endsWithPathSeparator(target) || Files.isDirectory(this.targetPath);
    }

    /**
     * Creates a downloader for one URL.
     *
     * <p>An existing directory is interpreted as a target directory.
     * A non-existing path is interpreted as the final file path because a
     * {@link Path} alone does not preserve whether the caller originally
     * supplied a trailing path separator.</p>
     *
     * @param url        HTTP or HTTPS URL
     * @param targetPath target file or existing directory
     */
    public HttpFileDownload(String url, Path targetPath) {
        this.url = Objects.requireNonNull(url, "HttpFileDownload.url");
        this.targetPath = Objects.requireNonNull(targetPath, "HttpFileDownload.targetPath");
        this.targetIsDirectory = Files.isDirectory(targetPath);
    }

    /**
     * Creates a downloader for multiple URLs.
     *
     * <p>{@code targetDir} is always interpreted as a directory, regardless
     * of whether it currently exists.</p>
     *
     * @param urls      URLs to download
     * @param targetDir destination directory
     */
    public HttpFileDownload(String[] urls, String targetDir) {
        this(urls, Paths.get(Objects.requireNonNull(targetDir, "HttpFileDownload.targetPathDir")));
    }

    /**
     * Creates a downloader for multiple URLs.
     *
     * <p>The supplied path is always interpreted as a destination directory
     * and is created if necessary.</p>
     *
     * @param urls          URLs to download
     * @param targetPathDir destination directory
     */
    public HttpFileDownload(String[] urls, Path targetPathDir) {
        this.urls = Objects.requireNonNull(urls, "HttpFileDownload.urls");
        this.targetPathDir = Objects.requireNonNull(targetPathDir, "HttpFileDownload.targetPathDir");
    }

    /**
     * Downloads the configured single URL.
     *
     * <p>The actual target file is resolved after the HTTP response headers
     * have been received, because the server may supply the filename through
     * {@code Content-Disposition}.</p>
     *
     * @throws IllegalStateException    if this instance was configured for a batch download instead of a single URL
     * @throws IllegalArgumentException if the URL does not use HTTP or HTTPS
     * @throws UncheckedIOException     if the download fails
     */
    public void download() {
        if (url == null || targetPath == null)
            throw new IllegalStateException("This downloader is not configured for a single download.");

        download(url, targetPath, targetIsDirectory);
    }

    /**
     * Performs one HTTP download.
     *
     * @param downloadUrl       source URL
     * @param requestedTarget   requested target path
     * @param targetIsDirectory whether the target must be interpreted as a directory
     */
    private static void download(String downloadUrl, Path requestedTarget, boolean targetIsDirectory) {
        HttpURLConnection conn = null;
        Path temporary = null;

        try {
            URL httpUrl = new URL(downloadUrl);
            String protocol = httpUrl.getProtocol();

            if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol))
                throw new IllegalArgumentException("Only HTTP/HTTPS URLs are supported: " + downloadUrl);

            conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(30_000);
            conn.setInstanceFollowRedirects(true);

            int code = conn.getResponseCode();

            if (code < 200 || code >= 300)
                throw new IOException("HTTP download failed, status: " + code);

            Path target = resolveTarget(requestedTarget, targetIsDirectory, conn.getURL(), conn);
            FileHelper.createParentDirectories(target);
            temporary = Files.createTempFile(target.getParent(), ".download-", ".tmp");

            try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
                Files.copy(in, temporary, StandardCopyOption.REPLACE_EXISTING);
            }

            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Download failed: " + downloadUrl, e);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                }
            }
            if (conn != null)
                conn.disconnect();
        }
    }

    /**
     * Resolves the final target file path.
     *
     * <p>If the requested target is explicitly a file path, it is returned
     * directly and therefore has the highest priority.</p>
     *
     * <p>If the target is a directory, the filename is resolved in the following order:</p>
     *
     * <ol>
     *     <li>HTTP {@code Content-Disposition}</li>
     *     <li>URL path</li>
     *     <li>Generated fallback filename</li>
     * </ol>
     */
    private static Path resolveTarget(Path requestedTarget, boolean targetIsDirectory, URL httpUrl, HttpURLConnection conn) throws IOException {
        Path target = requestedTarget.toAbsolutePath().normalize();

        /*
         * An already existing directory always wins even when the original
         * constructor could not know that it was intended as a directory.
         */
        boolean directory = targetIsDirectory || Files.isDirectory(target);

        if (!directory)
            return target;

        Files.createDirectories(target);
        String fileName = getContentDispositionFileName(conn);

        if (ObjectHelper.isEmptyText(fileName))
            fileName = getUrlFileName(httpUrl);

        if (ObjectHelper.isEmptyText(fileName))
            fileName = createFallbackFileName(conn.getContentType());

        fileName = sanitizeFileName(fileName);

        return target.resolve(fileName);
    }

    /**
     * Extracts a filename from the HTTP {@code Content-Disposition} header.
     *
     * <p>RFC 5987/8187 style {@code filename*=UTF-8''...} takes precedence
     * over the traditional {@code filename="..."} parameter.</p>
     */
    private static String getContentDispositionFileName(HttpURLConnection conn) {
        String disposition = conn.getHeaderField("Content-Disposition");

        if (ObjectHelper.isEmptyText(disposition))
            return null;

        Matcher extended = CONTENT_DISPOSITION_FILENAME_EXTENDED.matcher(disposition);

        if (extended.find()) {
            String encoded = extended.group(1).trim();

            try {
                return new UrlCodec(encoded).decodeQueryValue();
            } catch (RuntimeException ignored) {
                // Fall through to the traditional filename parameter.
            }
        }

        Matcher regular = CONTENT_DISPOSITION_FILENAME.matcher(disposition);

        if (!regular.find())
            return null;

        String quoted = regular.group(1);

        return quoted != null ? quoted : regular.group(2);
    }

    /**
     * Extracts a filename from the last path segment of the URL.
     *
     * <p>Query parameters and fragments do not participate in filename
     * resolution.</p>
     */
    private static String getUrlFileName(URL url) {
        String path = url.getPath();

        if (ObjectHelper.isEmptyText(path) || path.endsWith("/"))
            return null;

        int slash = path.lastIndexOf('/');

        String name = slash >= 0 ? path.substring(slash + 1) : path;

        if (ObjectHelper.isEmptyText(name))
            return null;

        try {
            return new UrlCodec(name).decodeQueryValue();
        } catch (RuntimeException e) {
            return name;
        }
    }

    /**
     * Generates a fallback filename when neither the target, response headers
     * nor URL provide one.
     *
     * <p>The extension is inferred from {@code Content-Type} for common
     * formats. Unknown media types use {@code .bin}.</p>
     */
    private static String createFallbackFileName(String contentType) {
        return "download-" + UUID.randomUUID() + extensionFromContentType(contentType);
    }

    /**
     * Maps common HTTP media types to filename extensions.
     */
    private static String extensionFromContentType(String contentType) {

        if (ObjectHelper.isEmptyText(contentType))
            return ".bin";

        int semicolon = contentType.indexOf(';');
        String type = (semicolon < 0 ? contentType : contentType.substring(0, semicolon)).trim().toLowerCase(Locale.ROOT);

        switch (type) {
            case "application/pdf":
                return ".pdf";
            case "application/zip":
                return ".zip";
            case "application/json":
                return ".json";
            case "text/plain":
                return ".txt";
            case "text/html":
                return ".html";
            case "image/jpeg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "image/gif":
                return ".gif";
            case "image/webp":
                return ".webp";
            case "application/octet-stream":
            default:
                return ".bin";
        }
    }

    /**
     * Sanitizes a server-provided or URL-derived filename before it is
     * resolved against the destination directory.
     *
     * <p>Directory components are discarded so values such as
     * {@code ../../file.txt} or {@code C:\temp\file.txt} cannot escape the
     * destination directory.</p>
     */
    private static String sanitizeFileName(String fileName) {
        Objects.requireNonNull(fileName, "sanitizeFileName.fileName");
        String name = fileName.replace('\\', '/').trim();

        int slash = name.lastIndexOf('/');

        if (slash >= 0)
            name = name.substring(slash + 1);

        /*
         * Remove control characters which do not belong in a filesystem
         * filename and may have originated from malformed HTTP headers.
         */
        name = name.replaceAll("[\\x00-\\x1F\\x7F]", "");

        if (name.isEmpty() || ".".equals(name) || "..".equals(name))
            return "download-" + UUID.randomUUID() + ".bin";

        return name;
    }

    /**
     * Returns whether a String target explicitly ends with a directory
     * separator.
     */
    private static boolean endsWithPathSeparator(String path) {
        return path.endsWith("/") || path.endsWith("\\");
    }

    /**
     * Matches RFC 5987/8187 style UTF-8 filenames.
     *
     * <p>Example:</p>
     *
     * <pre>
     * filename*=UTF-8''%E4%B8%AD%E6%96%87.pdf
     * </pre>
     */
    private static final Pattern CONTENT_DISPOSITION_FILENAME_EXTENDED = Pattern.compile("(?i)filename\\*\\s*=\\s*UTF-8''([^;]+)");

    /**
     * Matches traditional quoted or unquoted Content-Disposition filenames.
     */
    private static final Pattern CONTENT_DISPOSITION_FILENAME = Pattern.compile("(?i)filename\\s*=\\s*(?:\"([^\"]*)\"|([^;\\s]+))");


    /**
     * Shared executor used by asynchronous downloads.
     */

    private static final ExecutorService DOWNLOAD_EXECUTOR = Executors.newFixedThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable, "http-file-download");
        thread.setDaemon(true);

        return thread;
    });

    /**
     * Downloads the configured single file asynchronously.
     *
     * @return future completed when the download has finished
     */
    public CompletableFuture<Void> downloadAsync() {
        return CompletableFuture.runAsync(this::download, DOWNLOAD_EXECUTOR);
    }

    /**
     * Downloads all URLs configured by the batch constructor.
     *
     * <p>The batch destination is always treated as a directory and filenames
     * are independently resolved for each response.</p>
     */
    public void downloadAll() {
        if (urls == null || targetPathDir == null)
            throw new IllegalStateException("This downloader is not configured for batch download.");

        for (String itemUrl : urls) {
            Objects.requireNonNull(itemUrl, "HttpFileDownload.urls element");

            download(itemUrl, targetPathDir, true);
        }
    }

    /**
     * Downloads all configured URLs concurrently.
     *
     * @return future completed when all downloads have completed
     */
    public CompletableFuture<Void> downloadAllAsync() {
        if (urls == null || targetPathDir == null)
            throw new IllegalStateException("This downloader is not configured for batch download.");

        CompletableFuture<?>[] futures = new CompletableFuture<?>[urls.length];

        for (int i = 0; i < urls.length; i++) {
            final String itemUrl = Objects.requireNonNull(urls[i], "HttpFileDownload.urls element");
            futures[i] = CompletableFuture.runAsync(() -> download(itemUrl, targetPathDir, true), DOWNLOAD_EXECUTOR);
        }

        return CompletableFuture.allOf(futures);
    }
}