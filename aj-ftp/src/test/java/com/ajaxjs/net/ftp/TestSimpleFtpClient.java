package com.ajaxjs.net.ftp;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TestSimpleFtpClient {
    @Test
    void publishReplacesTarget() throws IOException {
        Path directory = Files.createTempDirectory("aj-ftp-publish-");
        Path temporary = directory.resolve("download.part");
        Path target = directory.resolve("download.bin");
        try {
            Files.write(temporary, "new".getBytes(StandardCharsets.UTF_8));
            Files.write(target, "old".getBytes(StandardCharsets.UTF_8));
            SimpleFtpClient.publish(temporary, target);
            assertArrayEquals("new".getBytes(StandardCharsets.UTF_8), Files.readAllBytes(target));
            assertFalse(Files.exists(temporary));
        } finally {
            Files.deleteIfExists(temporary);
            Files.deleteIfExists(target);
            Files.deleteIfExists(directory);
        }
    }

    @Test
    void publishReportsMissingTemporaryFile() throws IOException {
        Path directory = Files.createTempDirectory("aj-ftp-publish-");
        try {
            assertThrows(IOException.class, () -> SimpleFtpClient.publish(
                    directory.resolve("missing.part"), directory.resolve("target.bin")));
        } finally {
            Files.deleteIfExists(directory);
        }
    }
}
