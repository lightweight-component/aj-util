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

class TestUnzipHelper {
    @TempDir
    Path tempDir;

    @Test
    void testUnzipMultipleEntriesAndNestedDirectories() throws IOException {
        Path zip = createZip("multiple.zip", entries(
                "one.txt", "one",
                "nested/two.txt", "two"
        ));
        Path target = tempDir.resolve("target");

        new UnzipHelper(zip.toString(), target.toString()).extract();

        assertEquals("one", readUtf8(target.resolve("one.txt"))
        );

        assertEquals("two", readUtf8(target.resolve("nested/two.txt")));
    }

    @Test
    void testRejectsZipSlip() throws IOException {
        Path zip = createZip("slip.zip", entries("../escape.txt", "escaped"));
        Path target = tempDir.resolve("safe");

        assertThrows(UncheckedIOException.class,
                () -> new UnzipHelper(zip.toString(), target.toString()).extract()
        );

        assertFalse(Files.exists(tempDir.resolve("escape.txt")));
    }

    @Test
    void testExtractionLimits() throws IOException {
        Path zip = createZip("large.zip", entries("large.txt", "123456"));
        Path target = tempDir.resolve("limited");

        UnzipHelper.ExtractionLimits limits =
                new UnzipHelper.ExtractionLimits(
                        10,
                        5,
                        100,
                        100
                );

        assertThrows(
                UncheckedIOException.class,
                () -> new UnzipHelper(
                        zip.toString(),
                        target.toString(),
                        limits
                ).extract()
        );

        assertFalse(
                Files.exists(target.resolve("large.txt"))
        );
    }

    @Test
    void testCompressionRatioLimitUsesCentralDirectorySizes()
            throws IOException {

        StringBuilder repeated = new StringBuilder();

        for (int i = 0; i < 10_000; i++)
            repeated.append('a');

        Path zip = createZip(
                "high-ratio.zip",
                entries(
                        "repeated.txt",
                        repeated.toString()
                )
        );

        Path target =
                tempDir.resolve("ratio-limited");

        UnzipHelper.ExtractionLimits limits =
                new UnzipHelper.ExtractionLimits(
                        10,
                        20_000,
                        20_000,
                        2
                );

        assertThrows(
                UncheckedIOException.class,
                () -> new UnzipHelper(
                        zip.toString(),
                        target.toString(),
                        limits
                ).extract()
        );

        assertFalse(
                Files.exists(target.resolve("repeated.txt"))
        );
    }

    @Test
    void testEntryCountLimit() throws IOException {
        Path zip = createZip("entry-limit.zip", entries("one.txt", "1234", "two.txt", "5678"));

        Path target = tempDir.resolve("entry-limit");

        UnzipHelper.ExtractionLimits limits = new UnzipHelper.ExtractionLimits(
                1,
                100,
                100,
                100
        );

        assertThrows(UncheckedIOException.class, () -> new UnzipHelper(zip.toString(), target.toString(), limits).extract());
    }

    @Test
    void testTotalSizeLimit() throws IOException {
        Path zip = createZip("total-limit.zip", entries("one.txt", "1234", "two.txt", "5678"));

        Path target = tempDir.resolve("total-limit");

        UnzipHelper.ExtractionLimits limits = new UnzipHelper.ExtractionLimits(
                10,
                100,
                7,
                100
        );

        assertThrows(UncheckedIOException.class, () -> new UnzipHelper(zip.toString(), target.toString(), limits
                ).extract()
        );
    }

    @Test
    void testDefaultTargetDirectory() throws IOException {
        Path zip = createZip("default-target.zip", entries("hello.txt", "hello"));
        new UnzipHelper(zip.toString()).extract();
        Path target = tempDir.resolve("default-target");

        assertTrue(Files.isDirectory(target));
        assertEquals("hello", readUtf8(target.resolve("hello.txt")));
    }

    @Test
    void testExistingFileIsReplaced() throws IOException {
        Path zip = createZip("replace.zip", entries("data.txt", "new"));
        Path target = tempDir.resolve("replace-target");
        Files.createDirectories(target);
        Files.write(target.resolve("data.txt"), "old".getBytes(StandardCharsets.UTF_8));

        new UnzipHelper(zip.toString(), target.toString()).extract();

        assertEquals("new", readUtf8(target.resolve("data.txt")));
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