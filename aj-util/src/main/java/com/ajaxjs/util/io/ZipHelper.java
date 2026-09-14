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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for creating ZIP archives from a directory, a single file,
 * or multiple files.
 *
 * <p>The source is configured through one of the constructors:</p>
 *
 * <ul>
 *     <li>A directory and all of its regular files</li>
 *     <li>A single regular file</li>
 *     <li>Multiple regular files</li>
 * </ul>
 *
 * <p>Directory compression preserves the relative directory structure,
 * including empty directories. Symbolic links and non-regular files are
 * rejected.</p>
 *
 * <p>The ZIP archive is first written to a temporary file located in the same
 * directory as the final destination. Once the archive has been written
 * successfully, the temporary file is moved to the configured destination.
 * This prevents a partially written archive from replacing an existing ZIP
 * file if compression fails.</p>
 *
 * <p>By default, entries use {@link ZipEntry#DEFLATED} compression.
 * {@link ZipEntry#STORED} mode can be enabled through
 * checksum to be calculated before the entry is written.</p>
 *
 * <p>Source files should not be modified while compression is in progress,
 * especially when STORED mode is enabled, because the file is read once to
 * calculate its CRC32 value and again when its contents are written to the
 * archive.</p>
 *
 * <p>This class wraps I/O failures in {@link UncheckedIOException}.</p>
 */
@Slf4j
public class ZipHelper {
    /**
     * Destination path of the ZIP archive.
     */
    private final String zipFilePath;

    /**
     * Source directory to archive.
     *
     * <p>When this field is set, all regular files below the directory are
     * recursively added to the ZIP archive while preserving their relative paths.</p>
     */
    private String sourceDir;

    /**
     * Single source file to archive.
     */
    private File sourceFile;

    /**
     * Multiple source files to archive.
     *
     * <p>Each file is added to the root of the ZIP archive using
     * {@link File#getName()} as its entry name. Therefore, all files in this
     * array must have unique file names.</p>
     */
    private File[] sourceFiles;

    /**
     * Whether ZIP entries should use {@link ZipEntry#STORED} instead of
     * {@link ZipEntry#DEFLATED}.
     *
     * <p>The default value is {@code false}, meaning DEFLATED compression is used.</p>
     *
     * <p>When enabled, each file's uncompressed size and CRC32 checksum are
     * calculated before the entry is written.</p>
     */
    @Setter
    private boolean useStore;

    /**
     * Creates a ZIP helper for recursively archiving a directory.
     *
     * <p>The contents of {@code sourceDir} are added relative to the source
     * directory itself. The source directory name is not added as an outer ZIP entry.</p>
     *
     * <p>The destination ZIP file must not be located inside the source directory.</p>
     *
     * @param sourceDir   source directory to archive
     * @param zipFilePath destination ZIP file path
     * @throws NullPointerException if {@code sourceDir} or {@code zipFilePath} is {@code null}
     */
    public ZipHelper(String sourceDir, String zipFilePath) {
        this.sourceDir = Objects.requireNonNull(sourceDir, "ZipHelper.sourceDir");
        this.zipFilePath = Objects.requireNonNull(zipFilePath, "ZipHelper.zipFilePath");
    }

    /**
     * Creates a ZIP helper for archiving a single file.
     *
     * <p>The file is stored at the root of the ZIP archive using its original file name.</p>
     *
     * @param file        source file
     * @param zipFilePath destination ZIP file path
     * @throws NullPointerException     if {@code file} or {@code zipFilePath} is {@code null}
     * @throws IllegalArgumentException if the source does not exist or is a directory
     */
    public ZipHelper(File file, String zipFilePath) {
        this.sourceFile = Objects.requireNonNull(file, "ZipHelper.file");
        this.zipFilePath = Objects.requireNonNull(zipFilePath, "ZipHelper.zipFilePath");

        if (!file.exists() || file.isDirectory())
            throw new IllegalArgumentException("Source file does not exist or is a directory: " + sourceFile);
    }

    /**
     * Creates a ZIP helper for archiving multiple files.
     *
     * <p>Each source file is stored at the root of the ZIP archive using
     * {@link File#getName()} as the ZIP entry name. Consequently, source file
     * names must be unique even if the files originate from different directories.</p>
     *
     * @param files       source files
     * @param zipFilePath destination ZIP file path
     * @throws NullPointerException     if {@code files}, {@code zipFilePath}, or any element in
     *                                  {@code files} is {@code null}
     * @throws IllegalArgumentException if two source files produce the same ZIP entry name
     */
    public ZipHelper(File[] files, String zipFilePath) {
        this.sourceFiles = Objects.requireNonNull(files, "ZipHelper.files");
        this.zipFilePath = Objects.requireNonNull(zipFilePath, "ZipHelper.zipFilePath");

        Set<String> names = new HashSet<>();

        for (File file : files) {
            Objects.requireNonNull(file, "ZipHelper.files element");

            if (!names.add(file.getName()))
                throw new IllegalArgumentException("Duplicate ZIP entry name: " + file.getName());
        }
    }

    /**
     * Creates the ZIP archive using the configured source.
     *
     * <p>For directory sources, the directory is traversed recursively.
     * Symbolic links are rejected and only regular files are accepted.
     * Empty directories are preserved as directory entries.</p>
     *
     * <p>For single-file and multi-file sources, each source file is added to
     * the root of the archive.</p>
     *
     * <p>The generated archive is first written to a temporary file and moved
     * to the final destination only after the ZIP output stream has been
     * successfully closed.</p>
     *
     * @throws IllegalArgumentException if the source directory is invalid, symbolic, or contains the destination ZIP
     * @throws IllegalStateException    if no source has been configured
     * @throws UncheckedIOException     if the source cannot be read or the ZIP archive cannot be created
     */
    public void zip() {
        if (sourceDir != null) {
            Path source = Paths.get(sourceDir).toAbsolutePath().normalize();

            if (!Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS))
                throw new IllegalArgumentException("Source directory does not exist or is not a directory: " + sourceDir);

            if (Files.isSymbolicLink(source))
                throw new IllegalArgumentException("Symbolic links are not supported as ZIP source directories: " + sourceDir);

            try {
                Path destination = Paths.get(zipFilePath).toAbsolutePath().normalize();
                Path sourceReal = source.toRealPath();

                if (destination.startsWith(source) || destination.startsWith(sourceReal))
                    throw new IllegalArgumentException("The destination ZIP must not be inside the source directory: " + zipFilePath);

                writeZip((ZipOutputStream zipOut) -> {
                    try {
                        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                if (Files.isSymbolicLink(dir))
                                    throw new IOException("Symbolic links are not supported in ZIP source directories: " + dir);

                                if (!source.equals(dir)) {
                                    ZipEntry entry = new ZipEntry(toZipEntryName(source.relativize(dir)) + "/");
                                    zipOut.putNextEntry(entry);
                                    zipOut.closeEntry();
                                }

                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                if (attrs.isSymbolicLink() || Files.isSymbolicLink(file))
                                    throw new IOException("Symbolic links are not supported in ZIP source directories: " + file);
                                if (!attrs.isRegularFile())
                                    throw new IOException("Unsupported ZIP source file type: " + file);

                                addFileToZip(file.toFile(), toZipEntryName(source.relativize(file)), zipOut);

                                return FileVisitResult.CONTINUE;
                            }
                        });
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to access ZIP source directory: " + sourceDir, e);
            }
        } else if (sourceFile != null) {
            writeZip(zipOut -> addFileToZip(sourceFile, sourceFile.getName(), zipOut));
        } else if (sourceFiles != null) {
            writeZip(zipOut -> {
                for (File fc : sourceFiles)
                    addFileToZip(fc, fc.getName(), zipOut);
            });
        } else
            throw new IllegalStateException("No ZIP source configured.");
    }

    /**
     * Converts a relative filesystem path to a ZIP entry name.
     *
     * <p>ZIP entry names use forward slashes ({@code /}) as path separators
     * regardless of the host operating system. This method therefore replaces
     * the platform-specific separator with {@code /}.</p>
     *
     * @param relativePath relative path inside the source directory
     * @return ZIP-compatible entry name
     */
    private static String toZipEntryName(Path relativePath) {
        return relativePath.toString().replace(File.separatorChar, '/');
    }

    /**
     * Adds a regular file to an open ZIP output stream.
     *
     * <p>Symbolic links and non-regular files are rejected.</p>
     *
     * <p>If {@link #useStore} is enabled, the entry uses
     * {@link ZipEntry#STORED}. Its size and CRC32 checksum are calculated
     * before the entry is written. Otherwise,
     * {@link ZipEntry#DEFLATED} is used and {@link ZipOutputStream} handles the
     * compressed size and checksum automatically.</p>
     *
     * @param file         source file
     * @param zipEntryName entry name inside the ZIP archive
     * @param zipOut       open ZIP output stream
     * @throws UncheckedIOException if the source file cannot be read or the entry cannot be written
     */
    private void addFileToZip(File file, String zipEntryName, ZipOutputStream zipOut) {
        try {
            if (Files.isSymbolicLink(file.toPath()))
                throw new IOException("Symbolic links are not supported as ZIP source files: " + file);

            if (!Files.isRegularFile(file.toPath(), LinkOption.NOFOLLOW_LINKS))
                throw new IOException("ZIP source is not a regular file: " + file);

            try (BufferedInputStream bin = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
                ZipEntry entry = new ZipEntry(zipEntryName);

                if (useStore) {
                    entry.setMethod(ZipEntry.STORED);
                    entry.setSize(file.length());
                    entry.setCrc(getFileCRCCode(file));
                } else
                    entry.setMethod(ZipEntry.DEFLATED); // DEFLATED 模式不需要设置 size 和 crc，ZipOutputStream 会自动处理

                zipOut.putNextEntry(entry);

                byte[] buffer = new byte[CommonConstant.BUFFER_SIZE];
                int len;

                while ((len = bin.read(buffer)) != -1)
                    zipOut.write(buffer, 0, len);

                zipOut.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Creates the ZIP archive at the configured destination.
     *
     * <p>The archive is first written to a temporary file in the destination
     * directory. After the ZIP stream has been closed successfully, the
     * temporary file replaces the destination file.</p>
     *
     * <p>Using a temporary file ensures that an existing destination archive
     * is not replaced by a partially written ZIP if compression fails.</p>
     *
     * @param writer callback responsible for populating ZIP entries
     * @throws IllegalArgumentException if the destination has no parent directory
     * @throws UncheckedIOException     if the destination directory cannot be
     *                                  created, the archive cannot be written,
     *                                  or the temporary file cannot be moved
     */
    private void writeZip(Consumer<ZipOutputStream> writer) {
        Path destination = Paths.get(zipFilePath);
        Path absolute = destination.toAbsolutePath().normalize();
        Path parent = absolute.getParent();

        if (parent == null)
            throw new IllegalArgumentException("ZIP destination has no parent directory: " + destination);

        Path temporary = null;

        try {
            Files.createDirectories(parent);
            temporary = Files.createTempFile(parent, ".aj-zip-", ".tmp");

            try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(temporary));
                 ZipOutputStream zipOut = new ZipOutputStream(bos)) {
                writer.accept(zipOut);
            }

            Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to create ZIP archive: " + destination, e);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException e) {
                    log.warn("Unable to delete temporary ZIP file: " + temporary, e);
                }
            }
        }
    }

    /**
     * Calculates the CRC32 checksum of a file.
     *
     * <p>The file is read completely through a {@link CheckedInputStream}.
     * Reading the stream updates the associated {@link CRC32} checksum.</p>
     *
     * <p>This method is used when creating {@link ZipEntry#STORED} entries,
     * because STORED entries require the CRC32 value to be known before
     * {@link ZipOutputStream#putNextEntry(ZipEntry)} is called.</p>
     *
     * @param file regular file whose CRC32 checksum should be calculated
     * @return unsigned CRC32 value represented as a {@code long}
     * @throws IOException if the file cannot be read
     */
    private static long getFileCRCCode(File file) throws IOException {
        CRC32 crc32 = new CRC32();

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(file.toPath()));
             CheckedInputStream checkedinputstream = new CheckedInputStream(bufferedInputStream, crc32)) {
            byte[] buffer = new byte[CommonConstant.BUFFER_SIZE];

            while (checkedinputstream.read(buffer) != -1) {
                // Reading updates the checksum.
            }
        }

        return crc32.getValue();
    }
}
