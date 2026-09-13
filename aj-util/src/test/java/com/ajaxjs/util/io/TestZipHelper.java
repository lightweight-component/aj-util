package com.ajaxjs.util.io;

import com.ajaxjs.util.CommonConstant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

class TestZipHelper {
    @TempDir
    Path tempDir;

    @Test
    void testZipSingleFileWithStoredMethod() throws IOException {
        Path source = tempDir.resolve("source.txt");
        Path zip = tempDir.resolve("source.zip");

        Files.write(
                source,
                "content".getBytes(StandardCharsets.UTF_8)
        );

        ZipHelper helper = new ZipHelper(
                source.toFile(),
                zip.toString()
        );

        helper.setUseStore(true);
        helper.zip();

        try (ZipFile zipFile = new ZipFile(zip.toFile())) {
            ZipEntry entry = zipFile.getEntry("source.txt");

            assertNotNull(entry);
            assertEquals(ZipEntry.STORED, entry.getMethod());
            assertEquals(
                    "content",
                    new String(
                            readAllBytes(zipFile, entry),
                            StandardCharsets.UTF_8
                    )
            );
        }
    }

    @Test
    void testZipSingleFileUsesDeflatedByDefault() throws IOException {
        Path source = tempDir.resolve("source.txt");
        Path zip = tempDir.resolve("source.zip");

        Files.write(
                source,
                "content".getBytes(StandardCharsets.UTF_8)
        );

        new ZipHelper(
                source.toFile(),
                zip.toString()
        ).zip();

        try (ZipFile zipFile = new ZipFile(zip.toFile())) {
            ZipEntry entry = zipFile.getEntry("source.txt");

            assertNotNull(entry);
            assertEquals(ZipEntry.DEFLATED, entry.getMethod());
            assertEquals(
                    "content",
                    new String(
                            readAllBytes(zipFile, entry),
                            StandardCharsets.UTF_8
                    )
            );
        }
    }

    @Test
    void testZipCreatesDestinationParentDirectories() throws IOException {
        Path source = tempDir.resolve("source.txt");

        Files.write(
                source,
                "content".getBytes(StandardCharsets.UTF_8)
        );

        Path zip = tempDir.resolve("a/b/c/archive.zip");

        new ZipHelper(
                source.toFile(),
                zip.toString()
        ).zip();

        assertTrue(Files.isDirectory(zip.getParent()));
        assertTrue(Files.isRegularFile(zip));
    }

    @Test
    void testZipMultipleFiles() throws IOException {
        Path first = tempDir.resolve("one.txt");
        Path second = tempDir.resolve("two.txt");
        Path zip = tempDir.resolve("multiple.zip");

        Files.write(
                first,
                "one".getBytes(StandardCharsets.UTF_8)
        );

        Files.write(
                second,
                "two".getBytes(StandardCharsets.UTF_8)
        );

        new ZipHelper(
                new File[]{
                        first.toFile(),
                        second.toFile()
                },
                zip.toString()
        ).zip();

        try (ZipFile zipFile = new ZipFile(zip.toFile())) {
            ZipEntry firstEntry = zipFile.getEntry("one.txt");
            ZipEntry secondEntry = zipFile.getEntry("two.txt");

            assertNotNull(firstEntry);
            assertNotNull(secondEntry);

            assertEquals(
                    "one",
                    new String(
                            readAllBytes(zipFile, firstEntry),
                            StandardCharsets.UTF_8
                    )
            );

            assertEquals(
                    "two",
                    new String(
                            readAllBytes(zipFile, secondEntry),
                            StandardCharsets.UTF_8
                    )
            );
        }
    }

    @Test
    void testRejectsDuplicateFileNames() throws IOException {
        Path firstDir = Files.createDirectory(
                tempDir.resolve("first")
        );

        Path secondDir = Files.createDirectory(
                tempDir.resolve("second")
        );

        Path first = firstDir.resolve("same.txt");
        Path second = secondDir.resolve("same.txt");

        Files.write(
                first,
                "one".getBytes(StandardCharsets.UTF_8)
        );

        Files.write(
                second,
                "two".getBytes(StandardCharsets.UTF_8)
        );

        Path zip = tempDir.resolve("duplicate.zip");

        assertThrows(
                IllegalArgumentException.class,
                () -> new ZipHelper(
                        new File[]{
                                first.toFile(),
                                second.toFile()
                        },
                        zip.toString()
                )
        );

        assertFalse(Files.exists(zip));
    }

    @Test
    void testZipFailureIsPropagatedWithoutPublishingPartialArchive() {
        Path missing = tempDir.resolve("missing.txt");
        Path zip = tempDir.resolve("failed.zip");

        ZipHelper helper = new ZipHelper(
                new File[]{missing.toFile()},
                zip.toString()
        );

        assertThrows(
                UncheckedIOException.class,
                helper::zip
        );

        assertFalse(Files.exists(zip));
    }

    @Test
    void testZipDirectoryPreservesNestedDirectories()
            throws IOException {

        Path source = Files.createDirectory(
                tempDir.resolve("source")
        );

        Path nested = Files.createDirectories(
                source.resolve("a/b")
        );

        Files.write(
                source.resolve("root.txt"),
                "root".getBytes(StandardCharsets.UTF_8)
        );

        Files.write(
                nested.resolve("nested.txt"),
                "nested".getBytes(StandardCharsets.UTF_8)
        );

        Path zip = tempDir.resolve("directory.zip");

        new ZipHelper(
                source.toString(),
                zip.toString()
        ).zip();

        try (ZipFile zipFile = new ZipFile(zip.toFile())) {
            assertNotNull(zipFile.getEntry("a/"));
            assertNotNull(zipFile.getEntry("a/b/"));

            ZipEntry rootEntry =
                    zipFile.getEntry("root.txt");

            ZipEntry nestedEntry =
                    zipFile.getEntry("a/b/nested.txt");

            assertNotNull(rootEntry);
            assertNotNull(nestedEntry);

            assertEquals(
                    "root",
                    new String(
                            readAllBytes(zipFile, rootEntry),
                            StandardCharsets.UTF_8
                    )
            );

            assertEquals(
                    "nested",
                    new String(
                            readAllBytes(zipFile, nestedEntry),
                            StandardCharsets.UTF_8
                    )
            );
        }
    }

    @Test
    void testZipDirectoryPreservesEmptyDirectories()
            throws IOException {

        Path source = Files.createDirectory(tempDir.resolve("source"));
        Files.createDirectories(source.resolve("empty/nested"));
        Path zip = tempDir.resolve("empty-directory.zip");
        new ZipHelper(source.toString(), zip.toString()).zip();

        try (ZipFile zipFile = new ZipFile(zip.toFile())) {
            ZipEntry empty = zipFile.getEntry("empty/");
            ZipEntry nested = zipFile.getEntry("empty/nested/");

            assertNotNull(empty);
            assertNotNull(nested);

            assertTrue(empty.isDirectory());
            assertTrue(nested.isDirectory());
        }
    }

    @Test
    void testZipDirectoryRejectsSymbolicLinks()
            throws IOException {

        Path source = Files.createDirectory(
                tempDir.resolve("source")
        );

        Path target = tempDir.resolve("outside.txt");

        Files.write(
                target,
                "outside".getBytes(StandardCharsets.UTF_8)
        );

        try {
            Files.createSymbolicLink(
                    source.resolve("link.txt"),
                    target
            );
        } catch (
                UnsupportedOperationException
                | SecurityException
                | FileSystemException e) {

            return;
        }

        Path zip = tempDir.resolve("links.zip");

        assertThrows(
                UncheckedIOException.class,
                () -> new ZipHelper(
                        source.toString(),
                        zip.toString()
                ).zip()
        );

        assertFalse(Files.exists(zip));
    }

    @Test
    void testZipDirectoryRejectsDestinationInsideSource()
            throws IOException {

        Path source = Files.createDirectory(
                tempDir.resolve("self-containing")
        );

        Path zip = source.resolve("archive.zip");

        assertThrows(
                IllegalArgumentException.class,
                () -> new ZipHelper(
                        source.toString(),
                        zip.toString()
                ).zip()
        );

        assertFalse(Files.exists(zip));
    }

    @Test
    void testRejectsSymbolicLinkAsSingleSource()
            throws IOException {

        Path target = tempDir.resolve("real.txt");

        Files.write(
                target,
                "content".getBytes(StandardCharsets.UTF_8)
        );

        Path symbolicLink =
                tempDir.resolve("link.txt");

        try {
            Files.createSymbolicLink(
                    symbolicLink,
                    target
            );
        } catch (
                UnsupportedOperationException
                | SecurityException
                | FileSystemException e) {

            return;
        }

        Path zip = tempDir.resolve("link.zip");

        ZipHelper helper = new ZipHelper(
                symbolicLink.toFile(),
                zip.toString()
        );

        assertThrows(
                UncheckedIOException.class,
                helper::zip
        );

        assertFalse(Files.exists(zip));
    }

    private static byte[] readAllBytes(
            ZipFile zipFile,
            ZipEntry entry) throws IOException {

        try (
                InputStream input =
                        zipFile.getInputStream(entry);

                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {
            byte[] buffer =
                    new byte[CommonConstant.BUFFER_SIZE];

            int read;

            while ((read = input.read(buffer)) != -1)
                output.write(buffer, 0, read);

            return output.toByteArray();
        }
    }
}