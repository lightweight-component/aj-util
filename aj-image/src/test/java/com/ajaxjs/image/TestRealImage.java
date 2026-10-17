package com.ajaxjs.image;

import com.ajaxjs.image.model.ImageEmbedded;
import com.ajaxjs.image.model.RectangleEmbedded;
import com.ajaxjs.image.model.TextEmbedded;
import com.ajaxjs.image.model.constant.ImageFormat;
import com.ajaxjs.image.model.constant.ZoomMode;
import com.ajaxjs.image.utils.ImageConvert;
import com.ajaxjs.image.utils.ImageEffect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** 使用仓库中的真实照片验证完整处理流程，避免依赖开发者电脑上的绝对路径。 */
class TestRealImage {
    @TempDir
    Path directory;

    private byte[] photoBytes() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/test.jpg");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            assertNotNull(input, "测试资源 test.jpg 必须存在");
            byte[] buffer = new byte[4096];
            int length;
            while ((length = input.read(buffer)) != -1)
                output.write(buffer, 0, length);
            return output.toByteArray();
        }
    }

    private BufferedImage decode(byte[] bytes) throws Exception {
        try (InputStream input = new ByteArrayInputStream(bytes)) {
            BufferedImage image = ImageIO.read(input);
            assertNotNull(image, "输出应为可以解码的图片");
            return image;
        }
    }

    @Test
    void photoLoadsFromClasspathBytesFileAndPath() throws Exception {
        byte[] bytes = photoBytes();
        BufferedImage original = decode(bytes);
        assertTrue(original.getWidth() > 0 && original.getHeight() > 0);
        Path path = directory.resolve("photo.jpg");
        java.nio.file.Files.write(path, bytes);
        for (ImageConvert converter : new ImageConvert[]{new ImageConvert(bytes),
                new ImageConvert(path.toFile()), new ImageConvert(path.toString())}) {
            BufferedImage result = decode(converter.setImageFormat(ImageFormat.PNG).convert());
            assertPixelsEqual(original, result);
        }
    }

    @Test
    void photoConvertsToAllSupportedFormats() throws Exception {
        byte[] bytes = photoBytes();
        BufferedImage original = decode(bytes);
        for (ImageFormat format : new ImageFormat[]{ImageFormat.JPG, ImageFormat.PNG, ImageFormat.GIF, ImageFormat.BMP}) {
            BufferedImage result = decode(new ImageConvert(bytes).setImageFormat(format).convert());
            assertEquals(original.getWidth(), result.getWidth(), format.toString());
            assertEquals(original.getHeight(), result.getHeight(), format.toString());
            // JPEG 有损、GIF 有调色板限制，不能要求这些格式逐像素相同。
            if (format == ImageFormat.PNG || format == ImageFormat.BMP)
                assertPixelsEqual(original, result);
        }
    }

    @Test
    void photoCanBeThumbnailedRoundedSavedAndReadBack() throws Exception {
        BufferedImage original = decode(photoBytes());
        int[] before = pixels(original);
        BufferedImage thumbnail = new ImageEffect(original).thumbnail(120, 80, 2).getOutput();
        assertTrue(thumbnail.getWidth() > 0 && thumbnail.getWidth() <= 120);
        assertTrue(thumbnail.getHeight() > 0 && thumbnail.getHeight() <= 80);
        ImageEffect rounded = new ImageEffect(thumbnail).roundCorner(20);
        assertEquals(0, rounded.getOutput().getRGB(0, 0) >>> 24);
        Path path = directory.resolve("rounded.png");
        rounded.outputAsFile(ImageFormat.PNG, 1, path.toString());
        assertPixelsEqual(rounded.getOutput(), ImageIO.read(path.toFile()));
        assertArrayEquals(before, pixels(original), "处理过程不应改变原始照片");
    }

    @Test
    void photoBackgroundComposesWithAllThreePaintersAndExports() throws Exception {
        Path path = directory.resolve("background.jpg");
        java.nio.file.Files.write(path, photoBytes());
        ImageContainer container = new ImageContainer();
        container.setOutputFormat(ImageFormat.PNG);
        container.setBackgroundImage(path.toString(), ZoomMode.WidthHeight, 160, 120);
        RectangleEmbedded rectangle = new RectangleEmbedded();
        rectangle.setX(0);
        rectangle.setY(0);
        rectangle.setWidth(40);
        rectangle.setHeight(40);
        rectangle.setColor(Color.BLUE);
        container.addElement(rectangle);
        ImageEmbedded image = new ImageEmbedded();
        image.setImage(decode(photoBytes()));
        image.setX(60);
        image.setY(10);
        image.setWidth(40);
        image.setHeight(30);
        image.setZoomMode(ZoomMode.WidthHeight);
        container.addElement(image);
        TextEmbedded text = new TextEmbedded();
        text.setText("Photo");
        text.setFont(new Font("SansSerif", Font.PLAIN, 14));
        text.setX(5);
        text.setDrawY(100);
        container.addElement(text);
        BufferedImage result = container.combine();
        assertEquals(160, result.getWidth());
        assertEquals(120, result.getHeight());
        assertEquals(Color.BLUE.getRGB(), result.getRGB(5, 5));
        try (InputStream input = container.getCombinedImageStream()) {
            assertPixelsEqual(result, ImageIO.read(input));
        }
        assertPixelsEqual(result, container.combine());
    }

    private int[] pixels(BufferedImage image) {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

    private void assertPixelsEqual(BufferedImage expected, BufferedImage actual) {
        assertNotNull(actual);
        assertEquals(expected.getWidth(), actual.getWidth());
        assertEquals(expected.getHeight(), actual.getHeight());
        assertArrayEquals(pixels(expected), pixels(actual));
    }
}
