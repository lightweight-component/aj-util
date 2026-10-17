package com.ajaxjs.image.utils;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import static com.ajaxjs.image.utils.ImageTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class TestImageEffect {
    @Test
    void resizePreservesCustomPalettesAndTransparency() {
        java.awt.image.IndexColorModel palette = new java.awt.image.IndexColorModel(8, 3,
                new byte[]{(byte) 255, 0, 0}, new byte[]{0, 0, 0},
                new byte[]{0, (byte) 255, 0}, new byte[]{(byte) 255, (byte) 255, 0});
        BufferedImage source = new BufferedImage(3, 1, BufferedImage.TYPE_BYTE_INDEXED, palette);
        source.getRaster().setSamples(0, 0, 3, 1, 0, new int[]{0, 1, 2});
        BufferedImage result = new ImageEffect(source).resize(3, 1, false).getOutput();
        assertSame(palette, result.getColorModel());
        assertPixelsEqual(source, result);
        java.awt.image.IndexColorModel binaryPalette = new java.awt.image.IndexColorModel(1, 2,
                new byte[]{(byte) 255, 0}, new byte[]{0, 0}, new byte[]{0, (byte) 255});
        BufferedImage binary = new BufferedImage(2, 1, BufferedImage.TYPE_BYTE_BINARY, binaryPalette);
        binary.getRaster().setSamples(0, 0, 2, 1, 0, new int[]{0, 1});
        assertPixelsEqual(binary, new ImageEffect(binary).resize(2, 1, false).getOutput());
    }

    @Test
    void constrainedResizeKeepsThinImagesAtLeastOnePixelWideAndHigh() {
        for (int[] size : new int[][]{{1000, 1}, {1, 1000}}) {
            BufferedImage source = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_RGB);
            BufferedImage result = new ImageEffect(source).resize(1, 1, true).getOutput();
            assertEquals(1, result.getWidth());
            assertEquals(1, result.getHeight());
            assertThrows(IllegalArgumentException.class, () -> new ImageEffect(source).resize(0, 1, true));
            assertThrows(IllegalArgumentException.class, () -> new ImageEffect(source).resize(1, -1, true));
        }
    }

    @Test
    void scaleChangesDimensionsWithoutChangingSource() {
        BufferedImage original = sample();
        int[] before = pixels(original);
        ImageEffect effect = new ImageEffect(original);
        assertSame(effect, effect.scale(2));
        assertEquals(8, effect.getOutput().getWidth());
        assertEquals(4, effect.getOutput().getHeight());
        assertArrayEquals(before, pixels(original));
        assertNotSame(original, effect.getOutput());
    }

    @Test
    void invalidAndTooSmallScaleFactorsAreRejected() {
        for (double scale : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, 0.0001})
            assertThrows(IllegalArgumentException.class, () -> new ImageEffect(sample()).scale(scale));
    }

    @Test
    void resizeCanStretchOrFitInsideEitherLimitingDimension() {
        ImageEffect effect = new ImageEffect(sample());
        assertSame(effect, effect.resize(10, 10, false));
        assertEquals(10, effect.getOutput().getWidth());
        assertEquals(10, effect.getOutput().getHeight());
        effect.resize(4, 4, true);
        assertEquals(4, effect.getOutput().getWidth());
        assertEquals(2, effect.getOutput().getHeight());
        effect.resize(8, 1, true);
        assertEquals(2, effect.getOutput().getWidth());
        assertEquals(1, effect.getOutput().getHeight());
    }

    @Test
    void resizeSupportsCustomColorModels() {
        BufferedImage custom = new ImageMatrix(sample()).getMatrix(com.ajaxjs.image.model.constant.MatType.RGB)
                .createRGBImage(4, 2).getOutput();
        assertEquals(BufferedImage.TYPE_CUSTOM, custom.getType());
        BufferedImage result = new ImageEffect(custom).resize(8, 4, false).getOutput();
        assertSame(custom.getColorModel(), result.getColorModel());
        assertEquals(8, result.getWidth());
        assertEquals(4, result.getHeight());
    }

    @Test
    void invalidResizeDimensionsAreRejected() {
        for (int[] size : new int[][]{{0, 1}, {1, 0}, {-1, 1}, {1, -1}})
            assertThrows(IllegalArgumentException.class,
                    () -> new ImageEffect(sample()).resize(size[0], size[1], false));
    }

    @Test
    void thumbnailCropsTheCenterOfLandscapeAndPortraitImages() {
        for (boolean portrait : new boolean[]{false, true}) {
            BufferedImage original = new BufferedImage(portrait ? 4 : 12, portrait ? 12 : 4,
                    BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < original.getHeight(); y++)
                for (int x = 0; x < original.getWidth(); x++) {
                    int band = (portrait ? y : x) / 4;
                    original.setRGB(x, y, band == 0 ? 0xff0000 : band == 1 ? 0x00ff00 : 0x0000ff);
                }
            int[] before = pixels(original);
            BufferedImage result = new ImageEffect(original).thumbnail(4, 4, 2).getOutput();
            assertEquals(4, result.getWidth());
            assertEquals(4, result.getHeight());
            assertTrue(Arrays.stream(pixels(result)).allMatch(pixel -> pixel == 0xff00ff00));
            assertArrayEquals(before, pixels(original));
        }
    }

    @Test
    void thumbnailWithoutCroppingPreservesTheWholeAspectRatio() {
        BufferedImage original = new BufferedImage(12, 4, BufferedImage.TYPE_INT_RGB);
        BufferedImage result = new ImageEffect(original).thumbnail(4, 4, 100).getOutput();
        assertEquals(4, result.getWidth());
        assertEquals(1, result.getHeight());
    }

    @Test
    void smallerThumbnailInputIsCopiedWithoutUpscalingOrSharingPixels() {
        BufferedImage original = sample();
        BufferedImage result = new ImageEffect(original).thumbnail(100, 100, 2).getOutput();
        assertPixelsEqual(original, result);
        assertNotSame(original, result);
        result.setRGB(0, 0, 0xffffffff);
        assertEquals(0xff112233, original.getRGB(0, 0));
    }

    @Test
    void thumbnailRejectsInvalidLimitsAndThresholds() {
        for (int[] size : new int[][]{{0, 1}, {1, 0}, {-1, 1}})
            assertThrows(IllegalArgumentException.class,
                    () -> new ImageEffect(sample()).thumbnail(size[0], size[1], 2));
        for (double threshold : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY})
            assertThrows(IllegalArgumentException.class, () -> new ImageEffect(sample()).thumbnail(2, 2, threshold));
    }

    @Test
    void roundCornerProducesTransparentEdgesAndLeavesSourceIntact() {
        BufferedImage original = sample();
        int[] before = pixels(original);
        ImageEffect effect = new ImageEffect(original);
        assertSame(effect, effect.roundCorner(20, 20, 12));
        assertEquals(20, effect.getOutput().getWidth());
        assertEquals(20, effect.getOutput().getHeight());
        assertEquals(0, effect.getOutput().getRGB(0, 0) >>> 24);
        assertEquals(255, effect.getOutput().getRGB(10, 10) >>> 24);
        assertTrue(effect.getOutput().getColorModel().hasAlpha());
        assertArrayEquals(before, pixels(original));
        effect.roundCorner(2);
        assertEquals(4, effect.getOutput().getWidth());
        assertEquals(2, effect.getOutput().getHeight());
    }

    @Test
    void growCanvasAddsBlackPaddingAtTheRequestedOffsets() {
        BufferedImage original = sample();
        ImageEffect effect = new ImageEffect(original);
        assertSame(effect, effect.growCanvas(1, 2, 3, 4));
        BufferedImage result = effect.getOutput();
        assertEquals(8, result.getWidth());
        assertEquals(8, result.getHeight());
        assertEquals(BufferedImage.TYPE_3BYTE_BGR, result.getType());
        assertEquals(0xff000000, result.getRGB(0, 0));
        assertEquals(0xff000000, result.getRGB(7, 7));
        assertArrayEquals(pixels(original), result.getRGB(1, 2, 4, 2, null, 0, 4));
    }

    @Test
    void typedCanvasUsesRequestedPixelTypeAndOpaqueBlackPadding() {
        BufferedImage result = new ImageEffect(sample())
                .growCanvas(BufferedImage.TYPE_INT_ARGB, 1, 1, 1, 1).getOutput();
        assertEquals(BufferedImage.TYPE_INT_ARGB, result.getType());
        assertEquals(0xff000000, result.getRGB(0, 0));
        assertEquals(sample().getRGB(0, 0), result.getRGB(1, 1));
    }

    @Test
    void negativeCanvasPaddingIsRejectedOnEverySide() {
        for (int[] sides : new int[][]{{-1, 0, 0, 0}, {0, -1, 0, 0}, {0, 0, -1, 0}, {0, 0, 0, -1}})
            assertThrows(IllegalArgumentException.class, () -> new ImageEffect(sample())
                    .growCanvas(sides[0], sides[1], sides[2], sides[3]));
    }

    @Test
    void squareCanvasKeepsOriginalAtTopLeftAndPadsShorterSide() {
        for (boolean portrait : new boolean[]{false, true}) {
            BufferedImage original = new BufferedImage(portrait ? 2 : 4, portrait ? 4 : 2,
                    BufferedImage.TYPE_INT_RGB);
            original.setRGB(0, 0, 0xffffff);
            BufferedImage result = new ImageEffect(original).growSquareCanvas().getOutput();
            assertEquals(4, result.getWidth());
            assertEquals(4, result.getHeight());
            assertEquals(0xffffffff, result.getRGB(0, 0));
            assertEquals(0xff000000, result.getRGB(3, 3));
        }
    }

    @Test
    void successiveEffectsReadSourceRatherThanImplicitlyChainingOutputs() {
        ImageEffect effect = new ImageEffect(sample());
        effect.resize(10, 10, false).roundCorner(4);
        assertEquals(4, effect.getOutput().getWidth());
        assertEquals(2, effect.getOutput().getHeight());
        BufferedImage chained = new ImageEffect(new ImageEffect(sample()).resize(10, 10, false).getOutput())
                .roundCorner(4).getOutput();
        assertEquals(10, chained.getWidth());
        assertEquals(10, chained.getHeight());
    }
}
