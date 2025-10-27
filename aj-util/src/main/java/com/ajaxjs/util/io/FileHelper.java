package com.ajaxjs.util.io;

import lombok.Data;
import lombok.experimental.Accessors;
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

@Data
@Slf4j
@Accessors(chain = true)
public class FileHelper {
    private final Path path;

    public FileHelper(Path path) {
        this.path = path;
    }

    public FileHelper(File path) {
        this.path = path.toPath();
    }

    public FileHelper(String path) {
        this.path = Paths.get(path);
    }

    /**
     * Read the file text content
     *
     * @return Text content
     */
    public String getFileContent() {
        try {
            if (!Files.exists(path))
                throw new IOException("File doesn't exist: " + path);

            if (Files.isDirectory(path))
                throw new IOException("Argument：" + path + " is not a file, it's a folder.");
        } catch (IOException e) {
            log.error("Read file content error: " + path, e);
            throw new UncheckedIOException("Read file content error: " + path, e);
        }

        StringBuilder sb = new StringBuilder();
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) { // 要关闭文件，否则文件被锁定
            lines.forEach(sb::append);

            return sb.toString();
        } catch (IOException e) {
            log.error("读取文件 " + path + "时发生错误", e);
            throw new UncheckedIOException("读取文件 " + path + "时发生错误", e);
        }
    }

    /**
     * Read the file content in bytes.
     *
     * @return The file content in bytes.
     */
    public byte[] readFileBytes() {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("读取文件 " + path + "(byes) 时发生错误", e);
            throw new UncheckedIOException("读取文件 " + path + "(byes) 时发生错误", e);
        }
    }

    /**
     * 将字符串内容写入文件。
     *
     * @param content 要写入的内容
     * @throws UncheckedIOException 如果写入文件时发生错误
     */
    public void writeFileContent(String content) {
        try {
            Files.write(path, content.getBytes());
        } catch (IOException e) {
            log.error("将字符串内容写入文件", e);
            throw new UncheckedIOException("将字符串内容写入文件", e);
        }
    }

    /**
     * Delete a folder or a file.
     * If it's a folder, delete all files and sub-folders under it.
     */
    public void delete() {
        try {
            if (Files.isDirectory(path))
                try (Stream<Path> walk = Files.walk(path)) {
                    walk.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
            else
                Files.delete(path);
        } catch (IOException e) {
            log.error("Delete failed on: " + path, e);
            throw new UncheckedIOException("Delete failed on: " + path, e);
        }
    }

    /**
     * List the contents of a directory.
     *
     * @return The contents of the directory.
     */
    public List<String> listDirectoryContents() {
        if (!Files.isDirectory(path))
            throw new IllegalArgumentException("The argument: " + path + " is not a folder.");

        try (Stream<Path> stream = Files.list(path)) {
            return stream.map(p -> p.getFileName().toString()).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("List Directory Contents failed, on " + path, e);
            throw new UncheckedIOException("List Directory Contents failed, on " + path, e);
        }
    }

    /**
     * Create a directory.
     * It can create multiple levels of directories.
     */
    public void createDirectory() {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            log.error("Create directory: " + path + " failed.", e);
            throw new UncheckedIOException("Create directory: " + path + " failed.", e);
        }
    }

    /**
     * Get the size of a file or directory.
     *
     * @return Size in bytes.
     */
    public long getFileSize() {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

            return attrs.size();
        } catch (IOException e) {
            log.error("Get the size of a file or directory failed, on: " + path, e);
            throw new UncheckedIOException("Get the size of a file or directory failed, on: " + path, e);
        }
    }

    /**
     * The target location.
     */
    private Path target;

    public FileHelper setTarget(File target) {
        this.target = target.toPath();
        return this;
    }

    public FileHelper setTarget(String target) {
        this.target = Paths.get(target);
        return this;
    }

    /**
     * Copy a file or directory to another location.
     */
    public void copTo() {
        try {
            if (Files.isDirectory(path)) {
                try (Stream<Path> walk = Files.walk(path)) {
                    walk.forEach(sourceFilePath -> {
                        Path targetFilePath = target.resolve(path.relativize(sourceFilePath));

                        try {
                            Files.copy(sourceFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            log.error("Copy failed, on: " + path, e);
                            throw new UncheckedIOException("Copy failed, on: " + path, e);
                        }
                    });
                }
            } else
                Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Copy failed, on: " + path, e);
            throw new UncheckedIOException("Copy failed, on: " + path, e);
        }
    }

    /**
     * Move a file or directory to another location.
     */
    public void moveTo() {
        try {
            Files.move(path, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Move failed, on: " + path, e);
            throw new UncheckedIOException("Move failed, on: " + path, e);
        }
    }

    /**
     * 对文件按照指定大小进行分片，在文件所在目录生成分片后的文件块儿
     * 使用零拷贝对文件高效的切片和合并
     */
    public void chunkFile(long chunkSize) {
        if (Files.notExists(path) || Files.isDirectory(path))
            throw new IllegalArgumentException("The file doesn't exist or it's a folder, on: " + path);

        if (chunkSize < 1)
            throw new IllegalArgumentException("分片大小不能小于1个字节:" + chunkSize);

        try {
            long fileSize = Files.size(path); // 原始文件大小
            long numberOfChunk = fileSize % chunkSize == 0 ? fileSize / chunkSize : (fileSize / chunkSize) + 1; // 分片数量
            String fileName = path.getFileName().toString();   // 原始文件名称

            // 读取原始文件
            try (FileChannel fileChannel = FileChannel.open(path, EnumSet.of(StandardOpenOption.READ))) {
                for (int i = 0; i < numberOfChunk; i++) {
                    long start = i * chunkSize;
                    long end = start + chunkSize;

                    if (end > fileSize)
                        end = fileSize;

                    Path chunkFile = Paths.get(fileName + "-" + (i + 1));  // 分片文件名称

                    try (FileChannel chunkFileChannel = FileChannel.open(path.resolveSibling(chunkFile),
                            EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))) {

                        fileChannel.transferTo(start, end - start, chunkFileChannel);// 返回写入的数据长度
                    }
                }
            }
        } catch (IOException e) {
            log.error("Chunk file failed, on: " + path, e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 把多个文件合并为一个文件
     *
     * @param chunkFiles 分片文件
     */
    public void mergeFile(Path... chunkFiles) {
        if (chunkFiles == null || chunkFiles.length == 0)
            throw new IllegalArgumentException("分片文件不能为空");

        try (FileChannel fileChannel = FileChannel.open(path, EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))) {
            for (Path chunkFile : chunkFiles) {
                try (FileChannel chunkChannel = FileChannel.open(chunkFile, EnumSet.of(StandardOpenOption.READ))) {
                    chunkChannel.transferTo(0, chunkChannel.size(), fileChannel);
                }
            }
        } catch (IOException e) {
            log.error("Merge file failed", e);
            throw new UncheckedIOException(e);
        }
    }
}
