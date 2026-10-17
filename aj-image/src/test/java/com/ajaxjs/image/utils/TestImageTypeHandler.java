package com.ajaxjs.image.utils;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.UncheckedIOException;

import static com.ajaxjs.image.utils.ImageTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用真正的 PNG/JPEG 数据验证读取器参数，资源异常路径另由生命周期测试覆盖。
 */
class TestImageTypeHandler {
    @Test
    void metadataReportsDimensionsFormatAndMimeBeforeDecoding() throws Exception {
        try (ImageTypeHandler handler = new ImageTypeHandler(png(sample()), "png")) {
            assertEquals(4, handler.getImageInfo().getWidth().intValue());
            assertEquals(2, handler.getImageInfo().getHeight().intValue());
            assertEquals("png", handler.getImageInfo().getSuffix());
            assertEquals("image/png", handler.getImageInfo().getMime());
        }
    }

    @Test
    void defaultReadPreservesPixelsAndRejectsReuse() throws Exception {
        ImageTypeHandler handler = new ImageTypeHandler(png(sample()), "png");
        assertPixelsEqual(sample(), handler.read((ImageReadParam) null));
        assertThrows(IllegalStateException.class, () -> handler.read((ImageReadParam) null));
        handler.close();
    }

    @Test
    void regionReadPreservesSelectedPixelsWithOrWithoutExplicitType() throws Exception {
        BufferedImage original = ImageBase.copy(sample(), BufferedImage.TYPE_3BYTE_BGR);
        for (ImageTypeSpecifier type : new ImageTypeSpecifier[]{null,
                ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_3BYTE_BGR)}) {
            ImageTypeHandler handler = new ImageTypeHandler(png(original), "png");
            BufferedImage result = handler.read(new Rectangle(1, 0, 2, 2), type);
            assertEquals(2, result.getWidth());
            assertEquals(2, result.getHeight());
            assertArrayEquals(original.getRGB(1, 0, 2, 2, null, 0, 2), pixels(result));
            if (type != null)
                assertEquals(BufferedImage.TYPE_3BYTE_BGR, result.getType());
        }
    }

    @Test
    void explicitFullRegionAndNullRegionDecodeWholeImage() throws Exception {
        for (Rectangle region : new Rectangle[]{null, new Rectangle(0, 0, 4, 2)})
            assertPixelsEqual(sample(), new ImageTypeHandler(png(sample()), "png").read(region, null));
    }

    @Test
    void subsamplingReturnsEverySecondPixel() throws Exception {
        BufferedImage original = sample();
        ImageReadParam param = new ImageReadParam();
        param.setSourceSubsampling(2, 2, 0, 0);
        BufferedImage result = new ImageTypeHandler(png(original), "png").read(param);
        assertEquals(2, result.getWidth());
        assertEquals(1, result.getHeight());
        assertEquals(original.getRGB(0, 0), result.getRGB(0, 0));
        assertEquals(original.getRGB(2, 0), result.getRGB(1, 0));
    }

    @Test
    void destinationOffsetIsRespected() throws Exception {
        ImageReadParam param = new ImageReadParam();
        param.setDestinationOffset(new Point(2, 3));
        BufferedImage result = new ImageTypeHandler(png(sample()), "png").read(param);
        assertEquals(6, result.getWidth());
        assertEquals(5, result.getHeight());
        assertArrayEquals(pixels(sample()), result.getRGB(2, 3, 4, 2, null, 0, 4));
    }

    @Test
    void jpegHeaderAndPixelsCanBeDecoded() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(ImageBase.copy(sample(), BufferedImage.TYPE_INT_RGB), "jpeg", bytes));
        try (ImageTypeHandler handler = new ImageTypeHandler(bytes.toByteArray(), "jpeg")) {
            assertEquals(4, handler.getImageInfo().getWidth().intValue());
            assertEquals(2, handler.read((ImageReadParam) null).getHeight());
        }
    }

    @Test
    void invalidHeaderOrUnknownFormatFailsDuringConstruction() {
        assertThrows(IllegalStateException.class, () -> new ImageTypeHandler(new byte[]{1}, "not-an-image-format"));
        assertThrows(UncheckedIOException.class, () -> new ImageTypeHandler(new byte[]{1, 2, 3}, "png"));
    }

    @Test
    void invalidRegionClosesReaderAndPreventsReuse() throws Exception {
        ImageTypeHandler handler = new ImageTypeHandler(png(sample()), "png");
        assertThrows(IllegalArgumentException.class, () -> handler.read(new Rectangle(0, 0, 0, 1), null));
        assertThrows(IllegalStateException.class, () -> handler.read((ImageReadParam) null));
    }
}
