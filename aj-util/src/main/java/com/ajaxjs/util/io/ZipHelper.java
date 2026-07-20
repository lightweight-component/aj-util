package com.ajaxjs.util.io;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.*;

/**
 * ZIP Compression and Decompression Utility Class
 * <p>
 * This class provides comprehensive functionality for working with ZIP files, including
 * extracting ZIP contents, creating ZIP archives from files and directories,
 * handling Chinese filenames, and utility methods for ZIP file operations.
 * It supports both STORED (uncompressed) and DEFLATED (compressed) modes.
 */
@Slf4j
public class ZipHelper {
    private static final int EXTRACT_BUFFER_SIZE = 8192;

    public static final ExtractionLimits DEFAULT_EXTRACTION_LIMITS = new ExtractionLimits(
            10_000, 1024L * 1024 * 1024, 10L * 1024 * 1024 * 1024, 100.0
    );

    /**
     * Resource limits applied while extracting a ZIP archive.
     */
    public static final class ExtractionLimits {
        private final int maxEntries;
        private final long maxEntrySize;
        private final long maxTotalSize;
        private final double maxCompressionRatio;

        public ExtractionLimits(int maxEntries, long maxEntrySize, long maxTotalSize, double maxCompressionRatio) {
            if (maxEntries <= 0 || maxEntrySize <= 0 || maxTotalSize <= 0 || maxCompressionRatio <= 0)
                throw new IllegalArgumentException("ZIP extraction limits must be greater than zero.");

            this.maxEntries = maxEntries;
            this.maxEntrySize = maxEntrySize;
            this.maxTotalSize = maxTotalSize;
            this.maxCompressionRatio = maxCompressionRatio;
        }
    }

    private static final class ExtractionState {
        private int entries;
        private long totalSize;
    }

    /**
     * 解压文件
     *
     * @param save    解压文件的路径，必须为目录
     * @param zipFile 输入的解压文件路径，例如 C:/temp/foo.zip 或 c:\\temp\\bar.zip
     */
    public static void unzip(String save, String zipFile) {
        unzip(save, zipFile, DEFAULT_EXTRACTION_LIMITS);
    }

    public static void unzip(String save, String zipFile, ExtractionLimits limits) {
        Objects.requireNonNull(limits, "limits");
        long start = System.currentTimeMillis();
        Path root = prepareExtractionRoot(save);
        ExtractionState state = new ExtractionState();

        try (ZipFile archive = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = archive.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                checkEntryCount(state, limits);
                Path target = resolveZipEntry(root, entry.getName());

                if (entry.isDirectory())
                    createSecureDirectories(root, target);
                else {
                    try (InputStream input = archive.getInputStream(entry)) {
                        extractEntry(input, root, target, entry, state, limits);
                    }
                }
            }
        } catch (IOException e) {
            log.warn("unzip", e);
            throw new UncheckedIOException(e);
        }

        log.info("解压缩完成，耗时：{}ms，保存在{}", System.currentTimeMillis() - start, save);
    }

    /**
     * 解压文件
     *
     * @param save        解压文件的路径，必须为目录
     * @param zipFilePath 输入的解压文件路径，例如 C:/temp/foo.zip 或 c:\\temp\\bar.zip
     */
    public static void unzipWithChineseFilename(String save, String zipFilePath) {
        unzipWithChineseFilename(save, zipFilePath, DEFAULT_EXTRACTION_LIMITS);
    }

    public static void unzipWithChineseFilename(String save, String zipFilePath, ExtractionLimits limits) {
        Objects.requireNonNull(limits, "limits");
        long start = System.currentTimeMillis();
        Path root = prepareExtractionRoot(save);
        ExtractionState state = new ExtractionState();

        try (ZipFile zipFile = new ZipFile(zipFilePath, Charset.forName("GBK"))) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                checkEntryCount(state, limits);
                Path target = resolveZipEntry(root, entry.getName());

                if (entry.isDirectory())
                    createSecureDirectories(root, target);
                else {
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        extractEntry(is, root, target, entry, state, limits);
                    }
                }
            }
        } catch (IOException e) {
            log.warn("unzip", e);
            throw new UncheckedIOException(e);
        }

        log.info("解压缩完成，耗时：{}ms，保存在{}", System.currentTimeMillis() - start, save);
    }

    private static Path prepareExtractionRoot(String save) {
        try {
            Path root = Paths.get(save).toAbsolutePath().normalize();
            Files.createDirectories(root);

            return root.toRealPath();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to create ZIP extraction directory: " + save, e);
        }
    }

    private static Path resolveZipEntry(Path root, String entryName) throws IOException {
        if (entryName == null)
            throw new IOException("ZIP entry name is null.");

        Path target = root.resolve(entryName.replace('\\', '/')).normalize();
        if (!target.startsWith(root))
            throw new IOException("ZIP entry escapes the extraction directory: " + entryName);

        return target;
    }

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

    private static void checkEntryCount(ExtractionState state, ExtractionLimits limits) throws IOException {
        state.entries++;
        if (state.entries > limits.maxEntries)
            throw new IOException("ZIP archive contains too many entries.");
    }

    private static void extractEntry(InputStream in, Path root, Path target, ZipEntry entry,
                                     ExtractionState state, ExtractionLimits limits) throws IOException {
        long declaredSize = entry.getSize();
        if (declaredSize > limits.maxEntrySize)
            throw new IOException("ZIP entry exceeds the maximum uncompressed size: " + entry.getName());

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
            byte[] buffer = new byte[EXTRACT_BUFFER_SIZE];

            try (OutputStream out = Files.newOutputStream(temporary)) {
                int read;
                while ((read = in.read(buffer)) != -1) {
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

    private static void checkCompressionRatio(ZipEntry entry, long uncompressedSize,
                                              ExtractionLimits limits) throws IOException {
        long compressedSize = entry.getCompressedSize();
        if (compressedSize < 0)
            throw new IOException("ZIP entry has no compressed-size metadata: " + entry.getName());
        if (compressedSize == 0 && uncompressedSize > 0)
            throw new IOException("ZIP entry has an invalid zero compressed size: " + entry.getName());
        if (compressedSize > 0 && uncompressedSize > compressedSize * limits.maxCompressionRatio)
            throw new IOException("ZIP entry exceeds the maximum compression ratio: " + entry.getName());
    }

    /**
     * Compress an array of files into a ZIP archive
     * <p>
     * Takes an array of File objects and compresses them into a single ZIP file.
     * All files are added to the root of the ZIP archive without preserving directory structure.
     *
     * @param fileContent Array of files to be compressed into the ZIP archive
     * @param saveZip     Path where the resulting ZIP file will be saved
     * @param useStore    Compression mode: true for STORED (no compression), false for DEFLATED (standard compression)
     */
    public static void zipFile(File[] fileContent, String saveZip, boolean useStore) {
        writeZip(Paths.get(saveZip), zipOut -> {
            for (File fc : fileContent)
                addFileToZip(fc, fc.getName(), zipOut, useStore);
        });
    }

    /**
     * 递归压缩目录为ZIP
     *
     * @param sourceDir 目录路径
     * @param saveZip   目标 zip 文件路径
     * @param useStore  true: 仅存储(STORED)，false: 标准压缩(DEFLATED)
     */
    public static void zipDirectory(String sourceDir, String saveZip, boolean useStore) {
        Path source = Paths.get(sourceDir).toAbsolutePath().normalize();
        Path destination = Paths.get(saveZip).toAbsolutePath().normalize();

        if (!Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS))
            throw new IllegalArgumentException("Source directory does not exist or is not a directory: " + sourceDir);
        if (Files.isSymbolicLink(source))
            throw new IllegalArgumentException("Symbolic links are not supported as ZIP source directories: " + sourceDir);

        try {
            Path sourceReal = source.toRealPath();
            if (destination.startsWith(source) || destination.startsWith(sourceReal))
                throw new IllegalArgumentException("The destination ZIP must not be inside the source directory: " + saveZip);

            writeZip(destination, zipOut -> addDirectoryToZip(sourceReal, zipOut, useStore));
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to access ZIP source directory: " + sourceDir, e);
        }
    }

    private static void addDirectoryToZip(Path source, ZipOutputStream zipOut, boolean useStore) throws IOException {
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

                addFileToZip(file.toFile(), toZipEntryName(source.relativize(file)), zipOut, useStore);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static String toZipEntryName(Path relativePath) {
        return relativePath.toString().replace(File.separatorChar, '/');
    }

    /**
     * 单文件添加到 zip
     */
    private static void addFileToZip(File file, String zipEntryName, ZipOutputStream zipOut, boolean useStore) throws IOException {
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
                entry.setMethod(ZipEntry.DEFLATED);// // DEFLATED 模式不需要设置 size 和 crc，ZipOutputStream 会自动处理

            zipOut.putNextEntry(entry);

            byte[] buffer = new byte[8192];
            int len;
            while ((len = bin.read(buffer)) != -1)
                zipOut.write(buffer, 0, len);

            zipOut.closeEntry();
        }
    }

    /**
     * 获取 CRC32
     * CheckedInputStream 一种输入流，它还维护正在读取的数据的校验和。然后可以使用校验和来验证输入数据的完整性。
     *
     * @param file 必须是文件，不是目录
     */
    private static long getFileCRCCode(File file) throws IOException {
        CRC32 crc32 = new CRC32();

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(file.toPath()));
             CheckedInputStream checkedinputstream = new CheckedInputStream(bufferedInputStream, crc32)) {
            byte[] buffer = new byte[8192];
            while (checkedinputstream.read(buffer) != -1) {
                // Reading updates the checksum.
            }
        }

        return crc32.getValue();
    }

    /**
     * 检测文件所在的目录是否存在，如果没有则建立。可以跨多个未建的目录
     *
     * @param file 必须是文件，不是目录
     */
    public static void initFolder(File file) {
        if (file.isDirectory())
            throw new IllegalArgumentException("参数必须是文件，不是目录");

        Path parent = file.toPath().toAbsolutePath().getParent();

        if (parent != null)
            new FileHelper(parent).createDirectory();
    }

    /**
     * 检测文件所在的目录是否存在，如果没有则建立。可以跨多个未建的目录
     *
     * @param file 必须是文件，不是目录
     */
    public static void initFolder(String file) {
        initFolder(new File(file));
    }

    /**
     * 检查给定的字节数组是否为 Zip 文件的魔数。
     * 魔数是一段特定的字节序列，用于标识文件的类型。
     * 此方法专门检查 Zip 文件的魔数，该魔数由 PK\03\04 组成。
     *
     * @param magicNumber 字节数组，代表待检查的文件的前四个字节。
     * @return 如果字节数组的前四个字节与 Zip 文件的魔数匹配，则返回 true；否则返回 false。
     */
    private static boolean isZipFile(byte[] magicNumber) {
        // 比较字节数组的前四个字
        return magicNumber[0] == 0x50 && magicNumber[1] == 0x4b && magicNumber[2] == 0x03 && magicNumber[3] == 0x04;
    }

//    private static final String ZIP_MAGIC_NUMBER = "504B0304";

    /**
     * 判断给定的文件路径是否为 ZIP 文件。
     *
     * @param filePath 文件路径
     * @return 如果是 ZIP 文件则返回 true，否则返回 false
     */
    public static boolean isZipFile(String filePath) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            byte[] magicNumber = new byte[4];

            if (inputStream.read(magicNumber, 0, 4) == 4)  // 读取到了 4 个字节
                return magicNumber[0] == 0x50 && magicNumber[1] == 0x4b && magicNumber[2] == 0x03 && magicNumber[3] == 0x04;
            // ZIP_MAGIC_NUMBER.equalsIgnoreCase(StreamHelper.bytesToHexStr(magicNumber));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    /**
     * 压缩单个文件为ZIP
     *
     * @param sourceFile 源文件路径
     * @param saveZip    目标ZIP文件路径
     * @param useStore   true: 仅存储(STORED)，false: 标准压缩(DEFLATED)
     */
    public static void zipSingleFile(String sourceFile, String saveZip, boolean useStore) {
        File file = new File(sourceFile);

        if (!file.exists() || file.isDirectory())
            throw new IllegalArgumentException("Source file does not exist or is a directory: " + sourceFile);

        writeZip(Paths.get(saveZip), zipOut -> addFileToZip(file, file.getName(), zipOut, useStore));
    }

    @FunctionalInterface
    private interface ZipWriter {
        void write(ZipOutputStream zipOut) throws IOException;
    }

    private static void writeZip(Path destination, ZipWriter writer) {
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
                writer.write(zipOut);
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

}
