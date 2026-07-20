package com.ajaxjs.util.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestIoSafety {
    @TempDir
    Path tempDir;

    @Test
    void copyRequiresTarget() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new FileHelper(tempDir.resolve("source.txt")).copyTo());

        assertEquals("Target path not set", error.getMessage());
    }

    @Test
    void copyDirectoryRejectsTargetInsideSource() throws Exception {
        Path source = Files.createDirectory(tempDir.resolve("source"));
        Files.write(source.resolve("file.txt"), new byte[]{1});

        assertThrows(IllegalArgumentException.class,
                () -> new FileHelper(source).setTarget(source.resolve("copy").toString()).copyTo());
    }

    @Test
    void copyDirectoryRejectsSymbolicLinks() throws Exception {
        Path source = Files.createDirectory(tempDir.resolve("links"));
        Path outside = tempDir.resolve("outside.txt");
        Files.write(outside, new byte[]{1});

        try {
            Files.createSymbolicLink(source.resolve("link.txt"), outside);
        } catch (UnsupportedOperationException | SecurityException e) {
            return;
        }

        assertThrows(UncheckedIOException.class,
                () -> new FileHelper(source).setTarget(tempDir.resolve("copy").toString()).copyTo());
    }

    @Test
    void chunkAndMergePreserveEveryByte() throws Exception {
        byte[] content = new byte[25_123];
        for (int i = 0; i < content.length; i++)
            content[i] = (byte) i;

        Path source = tempDir.resolve("source.bin");
        Files.write(source, content);
        new FileHelper(source).chunkFile(4096);

        List<Path> chunks = new ArrayList<>();
        for (int i = 1; Files.exists(tempDir.resolve("source.bin-" + i)); i++)
            chunks.add(tempDir.resolve("source.bin-" + i));

        Path merged = tempDir.resolve("merged.bin");
        new FileHelper(merged).mergeFile(chunks.toArray(new Path[0]));
        assertArrayEquals(content, Files.readAllBytes(merged));
    }

    @Test
    void mergeFailureDoesNotPublishPartialFile() throws Exception {
        Path first = tempDir.resolve("first.part");
        Files.write(first, "first".getBytes(StandardCharsets.UTF_8));
        Path destination = tempDir.resolve("merged.bin");

        assertThrows(UncheckedIOException.class, () -> new FileHelper(destination)
                .mergeFile(first, tempDir.resolve("missing.part")));
        assertFalse(Files.exists(destination));
    }

    @Test
    void recursiveDeleteRemovesEverything() throws Exception {
        Path directory = Files.createDirectories(tempDir.resolve("delete/a/b"));
        Files.write(directory.resolve("file.txt"), new byte[]{1});

        Path root = tempDir.resolve("delete");
        new FileHelper(root).delete();
        assertFalse(Files.exists(root));
    }
}
