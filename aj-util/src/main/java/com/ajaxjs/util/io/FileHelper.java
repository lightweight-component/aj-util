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
import com.ajaxjs.util.ObjectHelper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for file operations, providing methods for reading, writing, copying,
 * moving, deleting, and manipulating files and directories in Java.
 * This class uses Java NIO Path and Files API for efficient file operations.
 * All methods throw UncheckedIOException for IO errors, wrapping the checked IOException.
 */
@Getter
@Slf4j
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
        this.path = Objects.requireNonNull(path, "FileHelper.path");
    }

    /**
     * Creates a new FileHelper instance with the specified File object.
     *
     * @param path the file or directory
     */
    public FileHelper(File path) {
        this(Objects.requireNonNull(path, "FileHelper.path").toPath());
    }

    /**
     * Creates a new FileHelper instance with the specified path string.
     *
     * @param path the path string to the file or directory
     */
    public FileHelper(String path) {
        this(Paths.get(Objects.requireNonNull(path, "FileHelper.path")));
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
            throw new UncheckedIOException("Read file content error: " + path, e);
        }

        StringBuilder sb = new StringBuilder();
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) { // 要关闭文件，否则文件被锁定
            String sep = System.lineSeparator();
            lines.forEach(line -> sb.append(line).append(sep));

            if (sb.length() > 0)  // 移除最后一个多余的换行符（如果文件非空）
                sb.setLength(sb.length() - sep.length());

            return sb.toString();
        } catch (IOException e) {
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
        Objects.requireNonNull(content, "writeFileContent.content");

        try {
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
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
        if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS))
            return;

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException error) throws IOException {
                    if (error != null)
                        throw error;

                    Files.deleteIfExists(dir);

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
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
            throw new UncheckedIOException("Create directory: " + path + " failed.", e);
        }
    }

    /**
     * Gets the file-system metadata size of this single path in bytes.
     * For a directory, this is not the recursive size of its contents.
     *
     * @return the size in bytes
     * @throws UncheckedIOException if an IO error occurs during size calculation
     */
    public long getFileSize() {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

            return attrs.size();
        } catch (IOException e) {
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
        this.target = Objects.requireNonNull(target, "FileHelper.target").toPath();
        return this;
    }

    /**
     * Sets the target path location for copy or move operations.
     *
     * @param target the target path string
     * @return this FileHelper instance for method chaining
     */
    public FileHelper setTarget(String target) {
        this.target = Paths.get(Objects.requireNonNull(target, "FileHelper.target"));
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
            } else {
                createParentDirectories(destination);

                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Copy failed, on: " + path, e);
        }
    }

    /**
     * Recursively copies the source directory to the destination, preserving relative structure.
     *
     * @param source      the source directory
     * @param destination the destination directory
     * @throws IOException if an IO error occurs during copying
     */
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

    /**
     * Resolves the real path of an existing ancestor and appends any missing trailing parts.
     *
     * @param path the path to resolve
     * @return the resolved normalized path
     * @throws IOException if an IO error occurs while resolving the real path
     */
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
     * The target represents the final destination path.
     * Use StandardCopyOption.REPLACE_EXISTING to overwrite
     * files with the same name at the destination.
     * <p>
     * Note: The target path must be set before calling this method using setTarget().
     *
     * @throws IllegalStateException if the target path is not set
     * @throws UncheckedIOException  if an IO error occurs during moving
     */
    public void moveTo() {
        if (target == null)
            throw new IllegalStateException("Target path not set");

        if (Files.isSymbolicLink(path))
            throw new IllegalArgumentException("Symbolic links are not supported as move sources: " + path);

        try {
            Path destination = target.toAbsolutePath().normalize();
            createParentDirectories(destination);

            Files.move(path, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Move file failed: " + path, e);
        }
    }

    /**
     * Creates all missing parent directories for the specified path.
     *
     * <p>The path itself is not created. Only its parent directory hierarchy
     * is created. If the path has no parent, this method does nothing.</p>
     *
     * @param path the file or directory path whose parent directories should be created
     * @throws NullPointerException if {@code path} is {@code null}
     * @throws UncheckedIOException if the parent directories cannot be created
     */
    public static void createParentDirectories(Path path) {
        Path parent = path.getParent();

        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
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
        if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(path) || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
            throw new IllegalArgumentException("The source must be a regular file: " + path);

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
                    long length = Math.min(chunkSize, fileSize - start);
                    Path chunkFile = path.resolveSibling(fileName + CommonConstant.HYPHEN_STR + (i + 1));

                    try (FileChannel chunkFileChannel = FileChannel.open(chunkFile, EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))) {
                        createdChunks.add(chunkFile);
                        transferFully(fileChannel, start, length, chunkFileChannel);
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
        if (ObjectHelper.isEmpty(chunkFiles))
            throw new IllegalArgumentException("分片文件不能为空");

        for (Path chunkFile : chunkFiles) {
            Objects.requireNonNull(chunkFile, "chunkFiles element");

            if (!Files.isRegularFile(chunkFile, LinkOption.NOFOLLOW_LINKS))
                throw new IllegalArgumentException("Chunk is not a regular file: " + chunkFile);
        }

        Path destination = path.toAbsolutePath().normalize();
        Path parent = destination.getParent();
        Path temporary = null;

        try {
            if (parent == null)
                throw new IOException("Merge destination has no parent directory: " + path);

            Files.createDirectories(parent);

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

    /**
     * Transfers {@code length} bytes from {@code source} to {@code target}, starting at {@code position}.
     *
     * @param source   the source file channel
     * @param position the start position in the source channel
     * @param length   the number of bytes to transfer
     * @param target   the target file channel
     * @throws IOException if an IO error occurs during transfer
     */
    private static void transferFully(FileChannel source, long position, long length, FileChannel target) throws IOException {
        long remaining = length;

        while (remaining > 0) {
            long transferred = source.transferTo(position, remaining, target);

            if (transferred > 0) {
                position += transferred;
                remaining -= transferred;
                continue;
            }

            ByteBuffer buffer = ByteBuffer.allocate((int) Math.min(CommonConstant.BUFFER_SIZE, remaining));
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
