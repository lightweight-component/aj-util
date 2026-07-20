package com.ajaxjs.util.io;

import com.ajaxjs.util.CommonConstant;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(Paths.get(zipFile)))) {
            ZipEntry ze;

            while ((ze = zis.getNextEntry()) != null) {
                checkEntryCount(state, limits);
                Path target = resolveZipEntry(root, ze.getName());

                if (ze.isDirectory())
                    createSecureDirectories(root, target);
                else
                    extractEntry(zis, root, target, ze, state, limits);

                zis.closeEntry();
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
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(saveZip)));
             ZipOutputStream zipOut = new ZipOutputStream(bos)) {

            for (File fc : fileContent)
                addFileToZip(fc, fc.getName(), zipOut, useStore);
        } catch (IOException e) {
            log.warn("一维文件数组压缩为 ZIP", e);
        }
    }

    /**
     * 递归压缩目录为ZIP
     *
     * @param sourceDir 目录路径
     * @param saveZip   目标 zip 文件路径
     * @param useStore  true: 仅存储(STORED)，false: 标准压缩(DEFLATED)
     */
    public static void zipDirectory(String sourceDir, String saveZip, boolean useStore) {
        File dir = new File(sourceDir);

        if (!dir.exists() || !dir.isDirectory())
            throw new IllegalArgumentException("Source directory does not exist or is not a directory: " + sourceDir);

        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(saveZip)));
             ZipOutputStream zipOut = new ZipOutputStream(bos)) {
            String basePath = dir.getCanonicalPath();
            zipDirectoryRecursive(dir, basePath, zipOut, useStore);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 目录压缩，用于递归
     */
    private static void zipDirectoryRecursive(File file, String basePath, ZipOutputStream zipOut, boolean useStore) throws IOException {
        String relativePath = basePath.equals(file.getCanonicalPath())
                ? CommonConstant.EMPTY_STRING
                : file.getCanonicalPath().substring(basePath.length() + 1).replace(File.separatorChar, '/');

        if (file.isDirectory()) {
            File[] files = file.listFiles();

            if (files != null && files.length == 0 && !relativePath.isEmpty()) {
                ZipEntry entry = new ZipEntry(relativePath + "/"); // 空目录也要加入Zip
                zipOut.putNextEntry(entry);
                zipOut.closeEntry();
            } else if (files != null) {
                for (File child : files)
                    zipDirectoryRecursive(child, basePath, zipOut, useStore);
            }
        } else
            addFileToZip(file, relativePath, zipOut, useStore);
    }

    /**
     * 单文件添加到 zip
     */
    private static void addFileToZip(File file, String zipEntryName, ZipOutputStream zipOut, boolean useStore) throws IOException {
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
    private static long getFileCRCCode(File file) {
        CRC32 crc32 = new CRC32();

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(file.toPath()));
             CheckedInputStream checkedinputstream = new CheckedInputStream(bufferedInputStream, crc32)) {
            while (checkedinputstream.read() != -1) {
                // 只需遍历即可统计
            }
        } catch (IOException e) {
            log.warn("getFileCRCCode", e);
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

        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(saveZip)));
             ZipOutputStream zipOut = new ZipOutputStream(bos)) {
            addFileToZip(file, file.getName(), zipOut, useStore);
        } catch (IOException e) {
            log.warn("单文件压缩为 ZIP 失败: " + sourceFile, e);
            throw new UncheckedIOException(e);
        }
    }

}
