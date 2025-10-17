package com.ajaxjs.util.io;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件操作工具类，提供了一些文件操作的常用方法。
 */
@Slf4j
public class FileHelper {
    /**
     * 读取文件内容并返回为字符串。
     *
     * @param filePath 文件路径
     * @return 文件内容
     * @throws UncheckedIOException 如果读取文件时发生错误
     */
    public static String readFileContent(String filePath) {
        Path path = Paths.get(filePath);

        try {
            if (!Files.exists(path))
                throw new IOException("文件不存在: " + filePath);

            if (Files.isDirectory(path))
                throw new IOException("参数 full path：" + filePath + " 不能是目录，请指定文件");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        StringBuilder sb = new StringBuilder();

        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) { // 要关闭文件，否则文件被锁定
            lines.forEach(sb::append);

            return sb.toString();
        } catch (IOException e) {
            log.error("读取文件 " + filePath + "时发生错误", e);
            throw new UncheckedIOException("读取文件 " + filePath + "时发生错误", e);
        }
    }

    /**
     * 通过文件路径读取文件内容并以字节数组形式返回
     * 该方法使用 NIO 文件通道来高效读取文件内容，适用于处理大文件或需要字节级操作的场景
     *
     * @param filePath 文件路径，指定要读取的文件
     * @return 文件内容的字节数组如果文件读取时发生错误，则抛出 RuntimeException
     * @throws RuntimeException 当文件读取过程中发生 IO 错误时
     */
    public static byte[] readFileBytes(String filePath) {
        Path path = Paths.get(filePath);

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("读取文件 " + filePath + "(byes) 时发生错误", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将字符串内容写入文件。
     *
     * @param filePath 文件路径
     * @param content  要写入的内容
     * @throws UncheckedIOException 如果写入文件时发生错误
     */
    public static void writeFileContent(String filePath, String content) {
        try {
            Files.write(Paths.get(filePath), content.getBytes());
        } catch (IOException e) {
            log.error("将字符串内容写入文件", e);
            throw new UncheckedIOException("将字符串内容写入文件", e);
        }
    }

    /**
     * 删除文件或目录。
     *
     * @param filePath 文件或目录路径
     * @throws UncheckedIOException 如果删除文件时发生错误
     */
    public static void deleteFileOrDirectory(String filePath) {
        Path path = Paths.get(filePath);

        try {
            if (Files.isDirectory(path)) {
                try (Stream<Path> walk = Files.walk(path)) {
                    walk.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
            } else
                Files.delete(path);
        } catch (IOException e) {
            log.error("删除文件或目录 {}", filePath, e);
            throw new UncheckedIOException("删除文件或目录", e);
        }
    }

    /**
     * 列出目录内容。
     *
     * @param directoryPath 目录路径
     * @return 目录内容列表
     * @throws UncheckedIOException 如果列出目录内容时发生错误
     */
    public static List<String> listDirectoryContents(String directoryPath) {
        try (Stream<Path> stream = Files.list(Paths.get(directoryPath))) {
            return stream.map(p -> p.getFileName().toString()).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("列出目录内容 {}", directoryPath, e);
            throw new UncheckedIOException("列出目录内容", e);
        }
    }

    /**
     * 创建目录。
     * 有创建多级目录的能力
     *
     * @param directoryPath 目录路径
     * @throws UncheckedIOException 如果创建目录时发生错误
     */
    public static void createDirectory(String directoryPath) {
        try {
            Files.createDirectories(Paths.get(directoryPath));
        } catch (IOException e) {
            log.error("创建目录 " + directoryPath, e);
            throw new UncheckedIOException("创建目录", e);
        }
    }

    /**
     * 检查文件或目录是否存在。
     *
     * @param filePath 文件或目录路径
     * @return 如果文件或目录存在则返回 true，否则返回 false
     */
    public static boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 获取文件或目录的大小。
     *
     * @param filePath 文件或目录路径
     * @return 文件或目录的大小（以字节为单位）
     * @throws UncheckedIOException 如果获取大小时发生错误
     */
    public static long getFileSize(String filePath) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(Paths.get(filePath), BasicFileAttributes.class);

            return attrs.size();
        } catch (IOException e) {
            log.error("获取文件或目录的大小 " + filePath, e);
            throw new UncheckedIOException("获取文件或目录的大小", e);
        }
    }

    /**
     * 复制文件或目录。
     *
     * @param source 源文件或目录路径
     * @param target 目标文件或目录路径
     * @throws UncheckedIOException 如果复制文件时发生错误
     */
    public static void copyFileOrDirectory(String source, String target) {
        Path sourcePath = Paths.get(source);
        Path targetPath = Paths.get(target);

        try {
            if (Files.isDirectory(sourcePath)) {

                try (Stream<Path> walk = Files.walk(sourcePath)) {
                    walk.forEach(sourceFilePath -> {
                        Path targetFilePath = targetPath.resolve(sourcePath.relativize(sourceFilePath));

                        try {
                            Files.copy(sourceFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            log.error("复制文件或目录", e);
                            throw new UncheckedIOException("复制文件或目录", e);
                        }
                    });
                }
            } else
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("复制文件或目录 ", e);
            throw new UncheckedIOException("复制文件或目录", e);
        }
    }

    /**
     * 移动文件或目录。
     *
     * @param source 源文件或目录路径
     * @param target 目标文件或目录路径
     * @throws UncheckedIOException 如果移动文件时发生错误
     */
    public static void moveFileOrDirectory(String source, String target) {
        Path sourcePath = Paths.get(source);
        Path targetPath = Paths.get(target);

        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("移动文件或目录 ", e);
            throw new UncheckedIOException("移动文件或目录", e);
        }
    }

    /**
     * 对文件按照指定大小进行分片，在文件所在目录生成分片后的文件块儿
     * 使用零拷贝对文件高效的切片和合并
     */
    public static void chunkFile(Path file, long chunkSize) {
        if (Files.notExists(file) || Files.isDirectory(file))
            throw new IllegalArgumentException("文件不存在:" + file);

        if (chunkSize < 1)
            throw new IllegalArgumentException("分片大小不能小于1个字节:" + chunkSize);

        try {
            long fileSize = Files.size(file); // 原始文件大小
            long numberOfChunk = fileSize % chunkSize == 0 ? fileSize / chunkSize : (fileSize / chunkSize) + 1; // 分片数量
            String fileName = file.getFileName().toString();   // 原始文件名称

            // 读取原始文件
            try (FileChannel fileChannel = FileChannel.open(file, EnumSet.of(StandardOpenOption.READ))) {
                for (int i = 0; i < numberOfChunk; i++) {
                    long start = i * chunkSize;
                    long end = start + chunkSize;

                    if (end > fileSize)
                        end = fileSize;

                    Path chunkFile = Paths.get(fileName + "-" + (i + 1));  // 分片文件名称

                    try (FileChannel chunkFileChannel = FileChannel.open(file.resolveSibling(chunkFile),
                            EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))) {

                        fileChannel.transferTo(start, end - start, chunkFileChannel);// 返回写入的数据长度
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 把多个文件合并为一个文件
     *
     * @param file       目标文件
     * @param chunkFiles 分片文件
     */
    public static void mergeFile(Path file, Path... chunkFiles) {
        if (chunkFiles == null || chunkFiles.length == 0)
            throw new IllegalArgumentException("分片文件不能为空");

        try (FileChannel fileChannel = FileChannel.open(file, EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))) {
            for (Path chunkFile : chunkFiles) {
                try (FileChannel chunkChannel = FileChannel.open(chunkFile, EnumSet.of(StandardOpenOption.READ))) {
                    chunkChannel.transferTo(0, chunkChannel.size(), fileChannel);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


}