package com.ajaxjs.image.utils;

import com.ajaxjs.image.model.constant.ImageFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ajaxjs.image.utils.ImageTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class TestImageConvert {
    @TempDir
    Path directory;

    @Test
    void pngRoundTripPreservesPixelsAndSource() throws Exception {
        BufferedImage original = sample();
        int[] before = pixels(original);
        ImageConvert convert = new ImageConvert(original);
        assertSame(convert, convert.setImageFormat(ImageFormat.PNG));
        assertSame(convert, convert.setQuality(null));
        assertPixelsEqual(original, ImageIO.read(new ByteArrayInputStream(convert.convert())));
        assertArrayEquals(before, pixels(original));
        assertNull(convert.getOutput());
    }

    @Test
    void qualityCanBeSetThenResetToDefault() throws Exception {
        ImageConvert convert = new ImageConvert(sample()).setImageFormat(ImageFormat.PNG);
        for (Float quality : new Float[]{1f, 0f, 0.5f, null})
            assertNotNull(ImageIO.read(new ByteArrayInputStream(convert.setQuality(quality).convert())));
    }

    @Test
    void jpegFallbackAcceptsTransparentSourceAndProducesOpaqueImage() throws Exception {
        BufferedImage source = sample();
        source.setRGB(0, 0, 0);
        BufferedImage result = ImageIO.read(new ByteArrayInputStream(new ImageConvert(source)
                .setImageFormat(ImageFormat.JPG).convert()));
        assertEquals(4, result.getWidth());
        assertEquals(2, result.getHeight());
        assertFalse(result.getColorModel().hasAlpha());
        assertEquals(0, source.getRGB(0, 0));
    }

    @Test
    void outputStreamRemainsUsableAndContainsCompletePng() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageConvert convert = new ImageConvert(sample()).setImageFormat(ImageFormat.PNG);
        convert.convert(output);
        assertPixelsEqual(sample(), ImageIO.read(new ByteArrayInputStream(output.toByteArray())));
        int length = output.size();
        output.write(42);
        assertEquals(length + 1, output.size());
    }

    @Test
    void fileOutputUsesSelectedFormatRatherThanFileExtension() throws Exception {
        Path path = directory.resolve("output.data");
        new ImageConvert(sample()).setImageFormat(ImageFormat.PNG).outputAsFile(path.toString());
        byte[] bytes = Files.readAllBytes(path);
        assertArrayEquals(new byte[]{(byte) 137, 80, 78, 71, 13, 10, 26, 10}, java.util.Arrays.copyOf(bytes, 8));
        assertPixelsEqual(sample(), ImageIO.read(path.toFile()));
    }

    @Test
    void missingFormatAndInvalidQualityFailBeforeOutput() {
        ImageConvert convert = new ImageConvert(sample());
        assertThrows(NullPointerException.class, convert::convert);
        for (float quality : new float[]{-0.1f, 1.1f, Float.NaN, Float.NEGATIVE_INFINITY})
            assertThrows(IllegalArgumentException.class, () -> convert.setQuality(quality));
    }
}
