package com.ajaxjs.image.utils;

import com.ajaxjs.image.model.constant.MatType;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static com.ajaxjs.image.utils.ImageTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class TestImageMatrix {
    @Test
    void rgbAndBgrRespectChannelOrderForDifferentPixelStorageTypes() {
        for (int type : new int[]{BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_3BYTE_BGR,
                BufferedImage.TYPE_4BYTE_ABGR}) {
            BufferedImage original = ImageBase.copy(sample(), type);
            ImageMatrix matrix = new ImageMatrix(original);
            assertSame(matrix, matrix.getMatrix(MatType.RGB));
            assertArrayEquals(expectedChannels(original, false), matrix.getMatrix());
            assertArrayEquals(expectedChannels(original, true), matrix.getMatrixBGR());
            assertArrayEquals(expectedChannels(original, true), matrix.getMatrix(MatType.BGR).getMatrix());
        }
    }

    @Test
    void rgbaIncludesAlphaAndReturnsRowMajorPixels() {
        for (int type : new int[]{BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_4BYTE_ABGR,
                BufferedImage.TYPE_INT_ARGB_PRE, BufferedImage.TYPE_4BYTE_ABGR_PRE}) {
            BufferedImage source = new BufferedImage(2, 1, type);
            source.setRGB(0, 0, 0x80402010);
            source.setRGB(1, 0, 0xffaabbcc);
            assertArrayEquals(new byte[]{64, 32, 16, (byte) 128, (byte) 170, (byte) 187, (byte) 204, (byte) 255},
                    new ImageMatrix(source).getMatrix(MatType.RGBA).getMatrix());
        }
    }

    @Test
    void grayHandlesExistingGraySamplesAndColorConversion() {
        BufferedImage gray = new BufferedImage(2, 1, BufferedImage.TYPE_BYTE_GRAY);
        gray.getRaster().setSample(0, 0, 0, 17);
        gray.getRaster().setSample(1, 0, 0, 239);
        assertArrayEquals(new byte[]{17, (byte) 239}, new ImageMatrix(gray).getMatrix(MatType.GRAY).getMatrix());
        BufferedImage color = new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB);
        color.setRGB(1, 0, 0xffffff);
        assertArrayEquals(new byte[]{0, (byte) 255}, new ImageMatrix(color).getMatrix(MatType.GRAY).getMatrix());
    }

    @Test
    void unsupportedMatrixFormatIsRejected() {
        assertThrows(UnsupportedOperationException.class, () -> new ImageMatrix(sample()).getMatrix(MatType.NV21));
    }

    @Test
    void croppingCopiesCorrectRowsWithoutChangingOriginalMatrix() {
        ImageMatrix matrix = new ImageMatrix(sample()).getMatrix(MatType.RGB);
        byte[] original = matrix.getMatrix().clone();
        byte[] cropped = matrix.cutMatrix(new Rectangle(0, 0, 4, 2), new Rectangle(1, 0, 2, 2));
        byte[] expected = new byte[12];
        System.arraycopy(original, 3, expected, 0, 6);
        System.arraycopy(original, 15, expected, 6, 6);
        assertArrayEquals(expected, cropped);
        assertArrayEquals(original, matrix.getMatrix());
        cropped[0] = 0;
        assertArrayEquals(original, matrix.getMatrix());
    }

    @Test
    void fullCropReturnsOriginalAndOutOfBoundsIsRejected() {
        ImageMatrix matrix = new ImageMatrix(sample()).getMatrix(MatType.RGB);
        Rectangle full = new Rectangle(0, 0, 4, 2);
        assertSame(matrix.getMatrix(), matrix.cutMatrix(full, null));
        assertSame(matrix.getMatrix(), matrix.cutMatrix(full, full));
        assertThrows(IllegalArgumentException.class, () -> matrix.cutMatrix(full, new Rectangle(3, 1, 2, 1)));
        assertThrows(IllegalArgumentException.class, () -> matrix.cutMatrix(full, new Rectangle(-1, 0, 1, 1)));
    }

    @Test
    void rgbRoundTripPreservesPixelsAndSharesDocumentedBackingArray() {
        ImageMatrix matrix = new ImageMatrix(sample()).getMatrix(MatType.RGB);
        assertSame(matrix, matrix.createRGBImage(4, 2));
        assertPixelsEqual(sample(), matrix.getOutput());
        matrix.getMatrix()[0] = (byte) 255;
        assertEquals(255, (matrix.getOutput().getRGB(0, 0) >>> 16) & 255);
    }

    @Test
    void rgbaImageCreationUsesColorsButDropsAlphaAsDocumented() {
        ImageMatrix matrix = new ImageMatrix(sample());
        matrix.matrix = new byte[]{64, 32, 16, (byte) 128};
        assertSame(matrix, matrix.createRGBAImage(1, 1));
        assertEquals(0xff402010, matrix.getOutput().getRGB(0, 0));
        assertFalse(matrix.getOutput().getColorModel().hasAlpha());
    }

    @Test
    void imageCreationWithoutAMatrixAllocatesEmptyPixels() {
        for (boolean rgba : new boolean[]{false, true}) {
            ImageMatrix matrix = new ImageMatrix(sample());
            if (rgba) matrix.createRGBAImage(3, 2);
            else matrix.createRGBImage(3, 2);
            assertEquals(3, matrix.getOutput().getWidth());
            assertEquals(2, matrix.getOutput().getHeight());
            assertTrue(Arrays.stream(pixels(matrix.getOutput())).allMatch(pixel -> pixel == 0xff000000));
        }
    }

    private byte[] expectedChannels(BufferedImage image, boolean bgr) {
        int[] pixels = pixels(image);
        byte[] expected = new byte[pixels.length * 3];
        for (int i = 0; i < pixels.length; i++) {
            expected[i * 3] = (byte) (pixels[i] >>> (bgr ? 0 : 16));
            expected[i * 3 + 1] = (byte) (pixels[i] >>> 8);
            expected[i * 3 + 2] = (byte) (pixels[i] >>> (bgr ? 16 : 0));
        }
        return expected;
    }
}
