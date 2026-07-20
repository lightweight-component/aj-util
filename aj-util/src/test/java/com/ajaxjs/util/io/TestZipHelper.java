package com.ajaxjs.util.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class TestZipHelper {
    @TempDir
    Path tempDir;

    @Test
    void testUnzipMultipleEntriesAndNestedDirectories() throws IOException {
        Path zip = createZip("multiple.zip", entries(
                "one.txt", "one",
                "nested/two.txt", "two"
        ));
        Path target = tempDir.resolve("target");

        ZipHelper.unzip(target.toString(), zip.toString());

        assertEquals("one", readUtf8(target.resolve("one.txt")));
        assertEquals("two", readUtf8(target.resolve("nested/two.txt")));
    }

    @Test
    void testRejectsZipSlip() throws IOException {
        Path zip = createZip("slip.zip", entries("../escape.txt", "escaped"));
        Path target = tempDir.resolve("safe");

        assertThrows(UncheckedIOException.class, () -> ZipHelper.unzip(target.toString(), zip.toString()));
        assertFalse(Files.exists(tempDir.resolve("escape.txt")));
    }

    @Test
    void testExtractionLimits() throws IOException {
        Path zip = createZip("large.zip", entries("large.txt", "123456"));
        Path target = tempDir.resolve("limited");
        ZipHelper.ExtractionLimits limits = new ZipHelper.ExtractionLimits(10, 5, 100, 100);

        assertThrows(UncheckedIOException.class,
                () -> ZipHelper.unzip(target.toString(), zip.toString(), limits));
        assertFalse(Files.exists(target.resolve("large.txt")));
    }

    @Test
    void testInitFolderCreatesOnlyParentDirectories() {
        Path targetFile = tempDir.resolve("a/b/file.txt");

        ZipHelper.initFolder(targetFile.toFile());

        assertTrue(Files.isDirectory(targetFile.getParent()));
        assertFalse(Files.exists(targetFile));
    }

    private Path createZip(String filename, Map<String, String> content) throws IOException {
        Path zip = tempDir.resolve(filename);

        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            for (Map.Entry<String, String> entry : content.entrySet()) {
                out.putNextEntry(new ZipEntry(entry.getKey()));
                out.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }

        return zip;
    }

    private static Map<String, String> entries(String... values) {
        Map<String, String> entries = new LinkedHashMap<>();

        for (int i = 0; i < values.length; i += 2)
            entries.put(values[i], values[i + 1]);

        return entries;
    }

    private static String readUtf8(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
