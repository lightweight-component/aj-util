package com.ajaxjs.util.io;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for file operations, providing methods for reading, writing, copying,
 * moving, deleting, and manipulating files and directories in Java.
 * This class uses Java NIO Path and Files API for efficient file operations.
 * All methods throw UncheckedIOException for IO errors, wrapping the checked IOException.
 */
@Data
@Slf4j
@Accessors(chain = true)
public class FileHelper {
    /**
     * The path to the file or directory.
     */
    private final Path path;

    /**
     * Creates a new FileHelper instance with the specified Path object.
     *
     * @param path the path to the file or directory
     */
    public FileHelper(Path path) {
        this.path = path;
    }

    /**
     * Creates a new FileHelper instance with the specified File object.
     *
     * @param path the file or directory
     */
    public FileHelper(File path) {
        this.path = path.toPath();
    }

    /**
     * Creates a new FileHelper instance with the specified path string.
     *
     * @param path the path string to the file or directory
     */
    public FileHelper(String path) {
        this.path = Paths.get(path);
    }

    /**
     * Reads the text content of a file.
     *
     * @return the text content of the file
     * @throws UncheckedIOException if an IO error occurs during reading
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
            log.error("Error reading file " + path, e);
            throw new UncheckedIOException("Error reading file " + path, e);
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
            log.error("Error reading file " + path + "(bytes)", e);
            throw new UncheckedIOException("Error reading file " + path + "(bytes)", e);
        }
    }

    /**
     * Write string content to a file.
     *
     * @param content The content to write to the file
     * @throws UncheckedIOException if an error occurs during file writing
     */
    public void writeFileContent(String content) {
        try {
            Files.write(path, content.getBytes());
        } catch (IOException e) {
            log.error("Error writing string content to file", e);
            throw new UncheckedIOException("Error writing string content to file", e);
        }
    }

    /**
     * Deletes a file or directory.
     * <p>
     * For directories, recursively delete all files and subdirectories using reverse order traversal.
     * For files, delete the file directly.
     *
     * @throws UncheckedIOException if an IO error occurs during deletion
     */
    public void delete() {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException error) throws IOException {
                    if (error != null)
                        throw error;

                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Delete failed on: {}", path, e);
            throw new UncheckedIOException("Delete failed on: " + path, e);
        }
    }

    /**
     * Lists the contents of a directory.
     *
     * @return the list of file/directory names in the directory
     * @throws IllegalArgumentException if the path is not a directory
     * @throws UncheckedIOException     if an IO error occurs during listing
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
     * Creates a directory or multiple levels of directories.
     *
     * @throws UncheckedIOException if an IO error occurs during directory creation
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
     * Gets the size of a file or directory in bytes.
     *
     * @return the size in bytes
     * @throws UncheckedIOException if an IO error occurs during size calculation
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
     * The target location for copy or move operations.
     */
    private Path target;

    /**
     * Sets the target file location for copy or move operations.
     *
     * @param target the target file
     * @return this FileHelper instance for method chaining
     */
    public FileHelper setTarget(File target) {
        this.target = target.toPath();
        return this;
    }

    /**
     * Sets the target path location for copy or move operations.
     *
     * @param target the target path string
     * @return this FileHelper instance for method chaining
     */
    public FileHelper setTarget(String target) {
        this.target = Paths.get(target);
        return this;
    }

    /**
     * Copies a file or directory to another location.
     * <p>
     * For directories, recursively copy all files and subdirectories while preserving the relative structure.
     * Use StandardCopyOption.REPLACE_EXISTING to overwrite
     * files with the same name at the destination.
     * <p>
     * Note: The target path must be set before calling this method using setTarget().
     *
     * @throws IllegalStateException if the target path is not set
     * @throws UncheckedIOException  if an IO error occurs during copying
     */
    public void copyTo() {
        if (target == null)
            throw new IllegalStateException("Target path not set");

        try {
            Path source = path.toAbsolutePath().normalize();
            Path destination = target.toAbsolutePath().normalize();

            if (Files.isSymbolicLink(source))
                throw new IOException("Symbolic links are not supported as copy sources: " + path);

            if (Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS)) {
                Path sourceReal = source.toRealPath();
                Path resolvedDestination = resolveAgainstRealAncestor(destination);
                if (destination.startsWith(source) || resolvedDestination.startsWith(sourceReal))
                    throw new IllegalArgumentException("Copy target must not be inside the source directory: " + target);

                copyDirectory(sourceReal, destination);
            } else
                Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Copy failed, on: " + path, e);
            throw new UncheckedIOException("Copy failed, on: " + path, e);
        }
    }

    private static void copyDirectory(Path source, Path destination) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (Files.isSymbolicLink(dir))
                    throw new IOException("Symbolic links are not supported while copying directories: " + dir);

                Files.createDirectories(destination.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (attrs.isSymbolicLink() || Files.isSymbolicLink(file))
                    throw new IOException("Symbolic links are not supported while copying directories: " + file);
                if (!attrs.isRegularFile())
                    throw new IOException("Unsupported file type while copying directory: " + file);

                Files.copy(file, destination.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static Path resolveAgainstRealAncestor(Path path) throws IOException {
        Deque<Path> missingParts = new ArrayDeque<>();
        Path existing = path;

        while (existing != null && !Files.exists(existing, LinkOption.NOFOLLOW_LINKS)) {
            missingParts.addFirst(existing.getFileName());
            existing = existing.getParent();
        }

        if (existing == null)
            return path;

        Path resolved = existing.toRealPath();
        for (Path part : missingParts)
            resolved = resolved.resolve(part);

        return resolved.normalize();
    }

    /**
     * Moves a file or directory to another location.
     * <p>
     * This operation renames or moves a file to a target file or directory.
     * If the target is a directory, the source is moved into that directory.
     * Use StandardCopyOption.REPLACE_EXISTING to overwrite
     * files with the same name at the destination.
     * <p>
     * Note: The target path must be set before calling this method using setTarget().
     *
     * @throws IllegalStateException if the target path is not set
     * @throws UncheckedIOException  if an IO error occurs during moving
     */
    public void moveTo() {
        try {
            if (target == null)
                throw new IllegalStateException("Target path not set");

            Files.move(path, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Move file failed: " + path, e);
            throw new UncheckedIOException("Move file failed: " + path, e);
        }
    }

    /**
     * Splits a file into chunks of the specified size in the same directory.
     * Uses zero-copy for efficient file slicing and merging operations.
     *
     * @param chunkSize the size of each chunk in bytes
     * @throws IllegalArgumentException if the file doesn't exist, is a directory, or chunkSize is less than 1
     * @throws UncheckedIOException     if an IO error occurs during chunking
     */
    public void chunkFile(long chunkSize) {
        if (Files.notExists(path) || Files.isDirectory(path))
            throw new IllegalArgumentException("The file doesn't exist or it's a folder, on: " + path);

        if (chunkSize < 1)
            throw new IllegalArgumentException("分片大小不能小于1个字节:" + chunkSize);

        List<Path> createdChunks = new ArrayList<>();
        try {
            long fileSize = Files.size(path); // 原始文件大小
            long numberOfChunk = fileSize % chunkSize == 0 ? fileSize / chunkSize : (fileSize / chunkSize) + 1; // 分片数量
            String fileName = path.getFileName().toString();   // 原始文件名称

            // 读取原始文件
            try (FileChannel fileChannel = FileChannel.open(path, EnumSet.of(StandardOpenOption.READ))) {
                for (long i = 0; i < numberOfChunk; i++) {
                    long start = i * chunkSize;
                    long end = start + chunkSize;

                    if (end > fileSize)
                        end = fileSize;

                    Path chunkFile = Paths.get(fileName + "-" + (i + 1));  // 分片文件名称

                    try (FileChannel chunkFileChannel = FileChannel.open(path.resolveSibling(chunkFile),
                            EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))) {
                        Path created = path.resolveSibling(chunkFile);
                        createdChunks.add(created);
                        transferFully(fileChannel, start, end - start, chunkFileChannel);
                    }
                }
            }
        } catch (IOException e) {
            for (Path chunk : createdChunks) {
                try {
                    Files.deleteIfExists(chunk);
                } catch (IOException cleanupError) {
                    e.addSuppressed(cleanupError);
                }
            }

            log.error("Chunk file failed, on: " + path, e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Merges multiple chunk files into a single file.
     *
     * @param chunkFiles the chunk files to merge
     * @throws IllegalArgumentException if chunkFiles is null or empty
     * @throws UncheckedIOException     if an IO error occurs during merging
     */
    public void mergeFile(Path... chunkFiles) {
        if (chunkFiles == null || chunkFiles.length == 0)
            throw new IllegalArgumentException("分片文件不能为空");

        Path destination = path.toAbsolutePath().normalize();
        Path parent = destination.getParent();
        Path temporary = null;

        try {
            if (parent == null)
                throw new IOException("Merge destination has no parent directory: " + path);
            if (Files.exists(destination))
                throw new FileAlreadyExistsException(destination.toString());

            temporary = Files.createTempFile(parent, ".aj-merge-", ".tmp");
            try (FileChannel fileChannel = FileChannel.open(temporary, EnumSet.of(StandardOpenOption.WRITE))) {
                for (Path chunkFile : chunkFiles) {
                    try (FileChannel chunkChannel = FileChannel.open(chunkFile, EnumSet.of(StandardOpenOption.READ))) {
                        transferFully(chunkChannel, 0, chunkChannel.size(), fileChannel);
                    }
                }
            }

            Files.move(temporary, destination);
        } catch (IOException e) {
            log.error("Merge file failed", e);
            throw new UncheckedIOException("Error merging files", e);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException e) {
                    log.warn("Unable to delete temporary merge file: " + temporary, e);
                }
            }
        }
    }

    private static void transferFully(FileChannel source, long position, long length, FileChannel target) throws IOException {
        long remaining = length;

        while (remaining > 0) {
            long transferred = source.transferTo(position, remaining, target);
            if (transferred > 0) {
                position += transferred;
                remaining -= transferred;
                continue;
            }

            ByteBuffer buffer = ByteBuffer.allocate((int) Math.min(8192, remaining));
            source.position(position);
            int read = source.read(buffer);
            if (read < 0)
                throw new IOException("Unexpected end of file while transferring data.");
            if (read == 0)
                continue;

            buffer.flip();
            while (buffer.hasRemaining())
                target.write(buffer);

            position += read;
            remaining -= read;
        }
    }
}
