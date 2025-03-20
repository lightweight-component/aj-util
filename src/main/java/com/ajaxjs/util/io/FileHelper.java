package com.ajaxjs.util.io;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
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
            log.error("删除文件或目录 " + filePath, e);
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
            log.error("列出目录内容 " + directoryPath, e);
            throw new UncheckedIOException("列出目录内容", e);
        }
    }

    /**
     * 创建目录。
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
}