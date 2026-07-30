package com.ajaxjs.util.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TestFileHelper {
    @TempDir
    Path tempDir;

    @Test
    void writesReadsAndReportsExactFileData() throws Exception {
        Path file = tempDir.resolve("data.txt");
        String content = "Hello, World! 你好世界";

        FileHelper helper = new FileHelper(file);
        helper.writeFileContent(content);

        assertEquals(content, helper.getFileContent());
        assertArrayEquals(content.getBytes(StandardCharsets.UTF_8), helper.readFileBytes());
        assertEquals(content.getBytes(StandardCharsets.UTF_8).length, helper.getFileSize());
    }

    @Test
    void readingTextPreservesLineEndingsAndTrailingNewline() throws Exception {
        Path file = tempDir.resolve("lines.txt");
        String content = "first\r\nsecond\n";
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));

        assertEquals(content, new FileHelper(file).getFileContent());
    }

    @Test
    void createsAndListsDirectoryContents() throws Exception {
        Path directory = tempDir.resolve("nested/directory");
        new FileHelper(directory).createDirectory();
        Files.write(directory.resolve("b.txt"), new byte[]{2});
        Files.write(directory.resolve("a.txt"), new byte[]{1});

        assertTrue(Files.isDirectory(directory));
        assertEquals(
                new java.util.HashSet<>(Arrays.asList("a.txt", "b.txt")),
                new java.util.HashSet<>(new FileHelper(directory).listDirectoryContents())
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new FileHelper(directory.resolve("a.txt")).listDirectoryContents()
        );
    }

    @Test
    void copiesAndMovesFilesWithoutTouchingProjectFiles() throws Exception {
        Path source = tempDir.resolve("source.txt");
        Path copy = tempDir.resolve("copy.txt");
        Path moved = tempDir.resolve("moved.txt");
        Files.write(source, "content".getBytes(StandardCharsets.UTF_8));

        new FileHelper(source).setTarget(copy.toString()).copyTo();
        assertEquals("content", new String(Files.readAllBytes(copy), StandardCharsets.UTF_8));
        assertTrue(Files.exists(source));

        new FileHelper(source).setTarget(moved.toString()).moveTo();
        assertFalse(Files.exists(source));
        assertEquals("content", new String(Files.readAllBytes(moved), StandardCharsets.UTF_8));
    }

    @Test
    void missingOrDirectoryTextInputFailsClearly() throws Exception {
        assertThrows(
                UncheckedIOException.class,
                () -> new FileHelper(tempDir.resolve("missing.txt")).getFileContent()
        );
        assertThrows(UncheckedIOException.class, () -> new FileHelper(tempDir).getFileContent());
    }
}
