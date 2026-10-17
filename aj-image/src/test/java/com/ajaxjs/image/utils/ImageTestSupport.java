package com.ajaxjs.image.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 小型内存图片，供非 Painter 模块验证尺寸、颜色和原始数据。
 */
final class ImageTestSupport {
    private ImageTestSupport() {
    }

    static BufferedImage sample() {
        BufferedImage image = new BufferedImage(4, 2, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0xff112233);
        image.setRGB(1, 0, 0xffaabbcc);
        image.setRGB(2, 0, 0xffff0000);
        image.setRGB(3, 0, 0xff00ff00);
        image.setRGB(0, 1, 0xff0000ff);
        image.setRGB(1, 1, 0xffffffff);
        image.setRGB(2, 1, 0xff000000);
        image.setRGB(3, 1, 0xff123456);
        return image;
    }

    static byte[] png(BufferedImage image) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", bytes));
        return bytes.toByteArray();
    }

    static int[] pixels(BufferedImage image) {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

    static void assertPixelsEqual(BufferedImage expected, BufferedImage actual) {
        assertNotNull(actual);
        assertEquals(expected.getWidth(), actual.getWidth());
        assertEquals(expected.getHeight(), actual.getHeight());
        assertArrayEquals(pixels(expected), pixels(actual));
    }
}
