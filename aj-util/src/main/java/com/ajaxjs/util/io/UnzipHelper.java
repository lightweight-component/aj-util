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
package com.ajaxjs.util.io;

import com.ajaxjs.util.CommonConstant;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utility class for securely extracting ZIP archives.
 *
 * <p>This class provides ZIP extraction with several security protections,
 * including:</p>
 *
 * <ul>
 *     <li>ZIP Slip / path traversal protection</li>
 *     <li>Symbolic-link traversal protection</li>
 *     <li>Maximum entry count limits</li>
 *     <li>Maximum uncompressed size per entry</li>
 *     <li>Maximum total uncompressed archive size</li>
 *     <li>Maximum compression ratio protection against ZIP bombs</li>
 *     <li>Temporary-file extraction to avoid leaving partially written files</li>
 * </ul>
 *
 * <p>By default, extracted files are placed in a directory next to the ZIP
 * archive. For example, {@code /data/archive.zip} is extracted to
 * {@code /data/archive}.</p>
 *
 * <p>The default extraction limits are:</p>
 *
 * <ul>
 *     <li>Maximum entries: 10,000</li>
 *     <li>Maximum size of one entry: 1 GB</li>
 *     <li>Maximum total extracted size: 10 GB</li>
 *     <li>Maximum compression ratio: 100:1</li>
 * </ul>
 *
 * <p>This class wraps checked I/O failures in {@link UncheckedIOException}.</p>
 */
@Slf4j
public class UnzipHelper {
    /**
     * Path of the ZIP archive to extract.
     */
    private final String zipFilePath;

    /**
     * Target extraction directory.
     *
     * <p>If {@code null}, a directory is created next to the ZIP archive,
     * normally using the ZIP file name without its extension.</p>
     */
    private final String targetDir;

    /**
     * Security and resource limits applied during extraction.
     */
    private final ExtractionLimits limits;

    /**
     * Charset used to decode ZIP entry names.
     *
     * <p>If {@code null}, the default {@link ZipFile} behavior is used,
     * which uses UTF-8 for ZIP entry names.</p>
     *
     * <p>For ZIP files whose entry names were encoded using another charset,
     * such as GBK, configure it explicitly:</p>
     *
     * <pre>{@code
     * helper.setCharset(Charset.forName("GBK"));
     * }</pre>
     */
    @Setter
    private Charset charset;

    /**
     * Default extraction limits.
     *
     * <ul>
     *     <li>10,000 entries</li>
     *     <li>1 GB maximum uncompressed size per entry</li>
     *     <li>10 GB maximum total uncompressed size</li>
     *     <li>100:1 maximum compression ratio</li>
     * </ul>
     */
    private final static ExtractionLimits DEFAULT_LIMIT = new ExtractionLimits(
            10_000, 1024L * 1024 * 1024, 10L * 1024 * 1024 * 1024, 100.0
    );

    /**
     * Creates an extractor using the default target directory
     * and default extraction limits.
     *
     * @param zipFilePath path of the ZIP archive
     * @throws NullPointerException if {@code zipFilePath} is {@code null}
     */
    public UnzipHelper(String zipFilePath) {
        this(zipFilePath, null);
    }

    /**
     * Creates an extractor using the specified target directory
     * and default extraction limits.
     *
     * @param zipFilePath path of the ZIP archive
     * @param targetDir   target extraction directory;
     *                    {@code null} to use a directory next to the archive
     * @throws NullPointerException if {@code zipFilePath} is {@code null}
     */
    public UnzipHelper(String zipFilePath, String targetDir) {
        this(zipFilePath, targetDir, null);
    }

    /**
     * Creates an extractor with custom extraction limits.
     *
     * @param zipFilePath path of the ZIP archive
     * @param targetDir   target extraction directory; {@code null} to use the default directory
     * @param limits      extraction limits; {@code null} to use {@link #DEFAULT_LIMIT}
     * @throws NullPointerException if {@code zipFilePath} is {@code null}
     */
    public UnzipHelper(String zipFilePath, String targetDir, ExtractionLimits limits) {
        Objects.requireNonNull(zipFilePath);
        this.zipFilePath = zipFilePath;
        this.targetDir = targetDir;
        this.limits = limits == null ? DEFAULT_LIMIT : limits;
    }

    /**
     * Security and resource limits used during ZIP extraction.
     *
     * <p>These limits protect the application from malicious or unexpectedly
     * large ZIP archives, including ZIP bomb attacks.</p>
     */
    @RequiredArgsConstructor
    public static final class ExtractionLimits {
        /**
         * Maximum number of entries allowed in the archive.
         */
        final int maxEntries;

        /**
         * Maximum uncompressed size, in bytes, allowed for a single entry.
         */
        final long maxEntrySize;

        /**
         * Maximum total uncompressed size, in bytes, allowed for the archive.
         */
        final long maxTotalSize;

        /**
         * Maximum allowed ratio between uncompressed and compressed sizes.
         *
         * <p>For example, a value of {@code 100.0} means an entry whose
         * uncompressed size exceeds 100 times its compressed size is rejected.</p>
         */
        final double maxCompressionRatio;
    }

    /**
     * Mutable extraction state maintained while processing one archive.
     */
    private static final class ExtractionState {
        /**
         * Number of entries processed so far.
         */
        private int entries;

        /**
         * Total number of uncompressed bytes written so far.
         */
        private long totalSize;
    }

    /**
     * Extracts the configured ZIP archive.
     *
     * <p>Each entry is validated before and during extraction. Files are first
     * written to temporary files and moved to their final destination only
     * after extraction succeeds.</p>
     *
     * <p>The extraction is aborted if any configured limit is exceeded or if
     * an unsafe path or symbolic link is detected.</p>
     *
     * @throws UncheckedIOException if the archive cannot be opened or extracted,
     *                              or if a security/resource limit is violated
     */
    public void extract() {
        long start = System.currentTimeMillis();
        Path root = prepareExtractionRoot();
        ExtractionState state = new ExtractionState();

        try (ZipFile archive = charset == null ? new ZipFile(zipFilePath) : new ZipFile(zipFilePath, charset)) {
            Enumeration<? extends ZipEntry> entries = archive.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                state.entries++; // check entry count

                if (state.entries > limits.maxEntries)
                    throw new IOException("ZIP archive contains too many entries.");

                Path target = resolveZipEntry(root, entry.getName());

                if (entry.isDirectory())
                    createSecureDirectories(root, target);
                else {
                    try (InputStream input = archive.getInputStream(entry)) {
                        long declaredSize = entry.getSize();

                        if (declaredSize >= 0) {
                            if (declaredSize > limits.maxEntrySize)
                                throw new IOException("ZIP entry exceeds the maximum uncompressed size: " + entry.getName());

                            if (state.totalSize > limits.maxTotalSize - declaredSize)
                                throw new IOException("ZIP archive exceeds the maximum total uncompressed size.");
                        }

                        if (declaredSize >= 0)
                            checkCompressionRatio(entry, declaredSize, limits);

                        Path parent = target.getParent();

                        if (parent == null)
                            throw new IOException("ZIP entry has no parent directory: " + entry.getName());

                        createSecureDirectories(root, parent);

                        if (Files.isSymbolicLink(target))
                            throw new IOException("ZIP entry target is a symbolic link: " + entry.getName());

                        Path temporary = Files.createTempFile(parent, ".aj-unzip-", ".tmp");

                        try {
                            long entrySize = 0;
                            byte[] buffer = new byte[CommonConstant.BUFFER_SIZE];

                            try (OutputStream out = Files.newOutputStream(temporary)) {
                                int read;

                                while ((read = input.read(buffer)) != -1) {
                                    if (read == 0)
                                        continue;

                                    if (entrySize > limits.maxEntrySize - read)
                                        throw new IOException("ZIP entry exceeds the maximum uncompressed size: " + entry.getName());
                                    if (state.totalSize > limits.maxTotalSize - read)
                                        throw new IOException("ZIP archive exceeds the maximum total uncompressed size.");

                                    entrySize += read;
                                    state.totalSize += read;
                                    checkCompressionRatio(entry, entrySize, limits);
                                    out.write(buffer, 0, read);
                                }
                            }

                            checkCompressionRatio(entry, entrySize, limits);
                            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
                        } finally {
                            Files.deleteIfExists(temporary);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to extract ZIP file: " + zipFilePath, e);
        }

        log.info("解压缩完成，耗时：{}ms，保存在{}", System.currentTimeMillis() - start, root);
    }

    /**
     * Creates a directory hierarchy while preventing extraction through
     * symbolic links or outside the configured extraction root.
     *
     * <p>Every path component is checked using
     * {@link LinkOption#NOFOLLOW_LINKS}. Existing symbolic links and
     * non-directory path components are rejected.</p>
     *
     * @param root      canonical extraction root
     * @param directory directory to create
     * @throws IOException if the requested directory escapes the extraction root, traverses a symbolic link, or cannot be created
     */
    private static void createSecureDirectories(Path root, Path directory) throws IOException {
        if (!directory.startsWith(root))
            throw new IOException("Directory escapes the ZIP extraction root: " + directory);

        Path current = root;

        for (Path part : root.relativize(directory)) {
            current = current.resolve(part);

            if (Files.exists(current, LinkOption.NOFOLLOW_LINKS)) {
                if (Files.isSymbolicLink(current) || !Files.isDirectory(current, LinkOption.NOFOLLOW_LINKS))
                    throw new IOException("Unsafe ZIP extraction path: " + current);
            } else
                Files.createDirectory(current);

            if (!current.toRealPath().startsWith(root))
                throw new IOException("ZIP extraction path escapes through a symbolic link: " + current);
        }
    }

    /**
     * Resolves a ZIP entry name against the extraction root and validates
     * that the resulting path remains inside the root directory.
     *
     * <p>Backslashes are converted to forward slashes before resolution so
     * that Windows-style traversal sequences are handled consistently.</p>
     *
     * @param root      extraction root
     * @param entryName ZIP entry name
     * @return normalized target path inside the extraction root
     * @throws IOException if the entry name is {@code null} or resolves outside the extraction root
     */
    private static Path resolveZipEntry(Path root, String entryName) throws IOException {
        if (entryName == null)
            throw new IOException("ZIP entry name is null.");

        Path target = root.resolve(entryName.replace('\\', '/')).normalize();

        if (!target.startsWith(root))
            throw new IOException("ZIP entry escapes the extraction directory: " + entryName);

        return target;
    }

    /**
     * Prepares and returns the canonical extraction root directory.
     *
     * <p>If {@link #targetDir} is specified, that directory is used.
     * Otherwise, a sibling directory based on the ZIP file name is created.
     * For example:</p>
     *
     * <pre>
     * /data/archive.zip
     *       ↓
     * /data/archive/
     * </pre>
     *
     * <p>The returned path is resolved using {@link Path#toRealPath} so that
     * subsequent security checks operate on the canonical filesystem path.</p>
     *
     * @return canonical extraction root path
     * @throws UncheckedIOException if the extraction directory cannot be created
     */
    private Path prepareExtractionRoot() {
        Path zip = Paths.get(zipFilePath).toAbsolutePath().normalize();
        Path root;

        if (targetDir == null) {
            String filename = zip.getFileName().toString();
            int dot = filename.lastIndexOf('.');
            String directoryName = dot > 0 ? filename.substring(0, dot) : filename + "_unzip";
            root = zip.resolveSibling(directoryName);
        } else
            root = Paths.get(targetDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(root);
            return root.toRealPath();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to create ZIP extraction directory: " + root, e);
        }
    }

    /**
     * Validates the compression ratio of a ZIP entry.
     *
     * <p>The compression ratio is calculated as:</p>
     *
     * <pre>
     * uncompressed size / compressed size
     * </pre>
     *
     * <p>An entry is rejected if the compressed-size metadata is unavailable,
     * if a non-empty entry declares a compressed size of zero, or if the
     * configured compression-ratio limit is exceeded.</p>
     *
     * @param entry            ZIP entry being validated
     * @param uncompressedSize current or declared uncompressed size
     * @param limits           extraction limits
     * @throws IOException if the compression metadata is invalid or the maximum compression ratio is exceeded
     */
    private static void checkCompressionRatio(ZipEntry entry, long uncompressedSize, ExtractionLimits limits) throws IOException {
        long compressedSize = entry.getCompressedSize();

        if (compressedSize < 0)
            throw new IOException("ZIP entry has no compressed-size metadata: " + entry.getName());

        if (compressedSize == 0) {
            if (uncompressedSize > 0)
                throw new IOException("ZIP entry has an invalid zero compressed size: " + entry.getName());

            return;
        }

        double ratio = (double) uncompressedSize / compressedSize;

        if (ratio > limits.maxCompressionRatio)
            throw new IOException("ZIP entry exceeds the maximum compression ratio: " + entry.getName());
    }
}