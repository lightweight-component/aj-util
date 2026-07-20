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
import java.util.zip.ZipFile;
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
    void testCompressionRatioLimitUsesCentralDirectorySizes() throws IOException {
        StringBuilder repeated = new StringBuilder();
        for (int i = 0; i < 10_000; i++)
            repeated.append('a');

        Path zip = createZip("high-ratio.zip", entries("repeated.txt", repeated.toString()));
        Path target = tempDir.resolve("ratio-limited");
        ZipHelper.ExtractionLimits limits = new ZipHelper.ExtractionLimits(10, 20_000, 20_000, 2);

        assertThrows(UncheckedIOException.class,
                () -> ZipHelper.unzip(target.toString(), zip.toString(), limits));
        assertFalse(Files.exists(target.resolve("repeated.txt")));
    }

    @Test
    void testInitFolderCreatesOnlyParentDirectories() {
        Path targetFile = tempDir.resolve("a/b/file.txt");

        ZipHelper.initFolder(targetFile.toFile());

        assertTrue(Files.isDirectory(targetFile.getParent()));
        assertFalse(Files.exists(targetFile));
    }

    @Test
    void testZipSingleFileWithStoredMethod() throws IOException {
        Path source = tempDir.resolve("source.txt");
        Path zip = tempDir.resolve("source.zip");
        Files.write(source, "content".getBytes(StandardCharsets.UTF_8));

        ZipHelper.zipSingleFile(source.toString(), zip.toString(), true);

        try (ZipFile zipFile = new ZipFile(zip.toFile())) {
            ZipEntry entry = zipFile.getEntry("source.txt");
            assertNotNull(entry);
            assertEquals(ZipEntry.STORED, entry.getMethod());
            assertEquals("content", new String(readAllBytes(zipFile, entry), StandardCharsets.UTF_8));
        }
    }

    @Test
    void testZipFailureIsPropagatedWithoutPublishingPartialArchive() {
        Path missing = tempDir.resolve("missing.txt");
        Path zip = tempDir.resolve("failed.zip");

        assertThrows(UncheckedIOException.class,
                () -> ZipHelper.zipFile(new java.io.File[]{missing.toFile()}, zip.toString(), true));
        assertFalse(Files.exists(zip));
    }

    @Test
    void testZipDirectoryRejectsSymbolicLinks() throws IOException {
        Path source = Files.createDirectory(tempDir.resolve("source"));
        Path target = tempDir.resolve("outside.txt");
        Files.write(target, "outside".getBytes(StandardCharsets.UTF_8));

        try {
            Files.createSymbolicLink(source.resolve("link.txt"), target);
        } catch (UnsupportedOperationException | SecurityException e) {
            return; // Symbolic links are not available on this platform.
        }

        Path zip = tempDir.resolve("links.zip");
        assertThrows(UncheckedIOException.class,
                () -> ZipHelper.zipDirectory(source.toString(), zip.toString(), false));
        assertFalse(Files.exists(zip));
    }

    @Test
    void testZipDirectoryRejectsDestinationInsideSource() throws IOException {
        Path source = Files.createDirectory(tempDir.resolve("self-containing"));

        assertThrows(IllegalArgumentException.class,
                () -> ZipHelper.zipDirectory(source.toString(), source.resolve("archive.zip").toString(), false));
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

    private static byte[] readAllBytes(ZipFile zipFile, ZipEntry entry) throws IOException {
        try (java.io.InputStream input = zipFile.getInputStream(entry);
             java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;

            while ((read = input.read(buffer)) != -1)
                output.write(buffer, 0, read);

            return output.toByteArray();
        }
    }
}
