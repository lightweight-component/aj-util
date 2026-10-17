package com.ajaxjs.image.utils;

import com.ajaxjs.image.model.constant.ImageFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ajaxjs.image.utils.ImageTestSupport.assertPixelsEqual;
import static com.ajaxjs.image.utils.ImageTestSupport.sample;
import static org.junit.jupiter.api.Assertions.*;

class TestImageEncoder {
    @TempDir
    Path directory;

    @Test
    void pngEncodingPreservesColorAndTransparencyForAllQualityModes() throws Exception {
        java.awt.image.BufferedImage original = sample();
        original.setRGB(0, 0, 0x80ff0000);
        original.setRGB(1, 0, 0);
        for (Float quality : new Float[]{null, 0f, 0.5f, 1f})
            assertPixelsEqual(original, ImageIO.read(new ByteArrayInputStream(
                    ImageEncoder.encode(original, ImageFormat.PNG, quality))));
    }

    @Test
    void invalidArgumentsFailBeforeWriting() {
        assertThrows(NullPointerException.class, () -> ImageEncoder.encode(null, ImageFormat.PNG, null));
        assertThrows(NullPointerException.class, () -> ImageEncoder.encode(sample(), null, null));
        assertThrows(NullPointerException.class, () -> ImageEncoder.write(sample(), ImageFormat.PNG, null, null));
        for (Float quality : new Float[]{-1f, 2f, Float.NaN, Float.POSITIVE_INFINITY})
            assertThrows(IllegalArgumentException.class, () -> ImageEncoder.encode(sample(), ImageFormat.PNG, quality));
    }

    @Test
    void downstreamStreamFailureRetainsCauseAndLeavesCallerStreamOpen() {
        IOException original = new IOException("stream unavailable");
        class FailingStream extends OutputStream {
            boolean closed;

            @Override
            public void write(int value) throws IOException {
                throw original;
            }

            @Override
            public void close() {
                closed = true;
            }
        }
        FailingStream stream = new FailingStream();
        UncheckedIOException failure = assertThrows(UncheckedIOException.class,
                () -> ImageEncoder.write(sample(), ImageFormat.PNG, null, stream));
        // PNG Writer 可以再包装一层 IIOException，检查完整 cause 链而非依赖某个 JDK 的文案。
        Throwable root = failure;
        while (root.getCause() != null)
            root = root.getCause();
        assertSame(original, root);
        assertFalse(stream.closed);
    }

    @Test
    void missingParentFailsWithoutCreatingFilesElsewhere() throws Exception {
        assertThrows(UncheckedIOException.class,
                () -> ImageEncoder.writeFile(new byte[]{1}, directory.resolve("missing/image.png").toString()));
        try (java.util.stream.Stream<Path> files = Files.list(directory)) {
            assertEquals(0, files.count());
        }
    }
}
