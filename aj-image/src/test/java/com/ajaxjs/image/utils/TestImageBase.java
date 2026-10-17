package com.ajaxjs.image.utils;

import com.ajaxjs.image.model.constant.ImageFormat;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ajaxjs.image.utils.ImageTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class TestImageBase {
    @Test
    void invalidImageBytesFailAtConstruction() {
        for (byte[] bytes : new byte[][]{new byte[0], {1, 2, 3}})
            assertThrows(IllegalArgumentException.class, () -> new ImageEffect(bytes));
    }

    @Test
    void unrecognizedHttpImageFailsAtConstruction() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/invalid", exchange -> {
            try {
                exchange.sendResponseHeaders(200, 3);
                exchange.getResponseBody().write(new byte[]{1, 2, 3});
            } finally {
                exchange.close();
            }
        });
        server.start();
        try {
            assertThrows(IllegalArgumentException.class,
                    () -> new ImageEffect("http://127.0.0.1:" + server.getAddress().getPort() + "/invalid"));
        } finally {
            server.stop(0);
        }
    }

    @TempDir
    Path directory;

    @Test
    void imageByteFileAndPathConstructorsReadSamePixels() throws Exception {
        BufferedImage original = sample();
        byte[] bytes = png(original);
        Path path = directory.resolve("input.png");
        Files.write(path, bytes);
        ImageEffect direct = new ImageEffect(original);
        assertSame(original, direct.source);
        assertNull(direct.getOutput());
        for (ImageEffect effect : new ImageEffect[]{new ImageEffect(bytes),
                new ImageEffect(path.toFile()), new ImageEffect(path.toString())}) {
            assertPixelsEqual(original, effect.source);
            assertNull(effect.getOutput());
        }
    }

    @Test
    void urlConstructorReadsLocalHttpImageWithoutExternalService() throws Exception {
        byte[] bytes = png(sample());
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/image.png", exchange -> {
            try {
                exchange.getResponseHeaders().set("Content-Type", "image/png");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            } finally {
                exchange.close();
            }
        });
        server.start();
        try {
            ImageEffect effect = new ImageEffect("http://127.0.0.1:" + server.getAddress().getPort() + "/image.png");
            assertPixelsEqual(sample(), effect.source);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void missingAndUnrecognizableLocalFilesFailAtReadTime() throws Exception {
        assertThrows(UncheckedIOException.class, () -> new ImageEffect(directory.resolve("missing.png").toFile()));
        Path invalid = directory.resolve("not-an-image.png");
        Files.write(invalid, new byte[]{1, 2, 3});
        assertThrows(IllegalArgumentException.class, () -> new ImageEffect(invalid.toString()));
    }

    @Test
    void explicitCopyPreservesAlphaAndDoesNotSharePixels() {
        BufferedImage original = sample();
        original.setRGB(0, 0, 0x80ff0000);
        BufferedImage copy = ImageBase.copy(original, BufferedImage.TYPE_INT_ARGB);
        assertPixelsEqual(original, copy);
        copy.setRGB(0, 0, 0xff00ff00);
        assertEquals(0x80ff0000, original.getRGB(0, 0));
    }

    @Test
    void defaultCopyIsOpaqueAndDoesNotPublishAnEffectResult() {
        ImageEffect effect = new ImageEffect(sample());
        BufferedImage copy = effect.copy();
        assertPixelsEqual(sample(), copy);
        assertEquals(BufferedImage.TYPE_3BYTE_BGR, copy.getType());
        assertNotSame(effect.source, copy);
        assertNull(effect.getOutput());
    }

    @Test
    void outputRequiresACompletedEffect() {
        assertThrows(NullPointerException.class, () -> new ImageEffect(sample()).output(ImageFormat.PNG, 1));
    }

    @Test
    void outputEncodesTheProcessedImageRatherThanOriginal() throws Exception {
        ImageEffect effect = new ImageEffect(sample()).resize(2, 1, false);
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(effect.output(ImageFormat.PNG, 1)));
        assertPixelsEqual(effect.getOutput(), decoded);
        Path path = directory.resolve("processed.png");
        effect.outputAsFile(ImageFormat.PNG, 1, path.toString());
        assertPixelsEqual(effect.getOutput(), ImageIO.read(path.toFile()));
    }
}
