package com.ajaxjs.image;

import com.ajaxjs.image.model.ImageEmbedded;
import com.ajaxjs.image.model.constant.ImageFormat;
import com.ajaxjs.image.model.constant.ZoomMode;
import com.ajaxjs.image.painter.ImagePainter;
import com.ajaxjs.image.utils.ImageConvert;
import com.ajaxjs.image.utils.ImageEffect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 检查输出失败保护、合成结果发布和模糊绘制的一致性。
 */
class TestImageOutputSafety {
    @TempDir
    Path directory;

    private BufferedImage image() {
        BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(3, 3, 0x80ff0000);
        image.setRGB(4, 4, 0xff00ff00);
        return image;
    }

    @Test
    void effectAndContainerCanEncodeEveryFormat() throws Exception {
        for (ImageFormat format : ImageFormat.values()) {
            byte[] bytes = new ImageEffect(image()).resize(8, 8, false).output(format, 0.8f);
            assertNotNull(ImageIO.read(new ByteArrayInputStream(bytes)), format.toString());
            ImageContainer container = new ImageContainer();
            container.setOutputFormat(format);
            container.setBackgroundImage(image(), ZoomMode.Origin, 8, 8);
            container.combine();
            try (InputStream input = container.getCombinedImageStream()) {
                assertNotNull(ImageIO.read(input), format.toString());
            }
            Path file = directory.resolve("container." + format.name().toLowerCase(java.util.Locale.ROOT));
            container.save(file.toString());
            assertNotNull(ImageIO.read(file.toFile()));
        }
    }

    @Test
    void encodingFailureDoesNotTruncateExistingFile() throws Exception {
        Path target = directory.resolve("keep.png");
        byte[] original = "existing contents".getBytes(StandardCharsets.UTF_8);
        Files.write(target, original);

        ImageConvert convert = new ImageConvert(image()); // 未设置格式，编码必然失败。
        assertThrows(RuntimeException.class, () -> convert.outputAsFile(target.toString()));
        assertArrayEquals(original, Files.readAllBytes(target));

        ImageEffect effect = new ImageEffect(image()).resize(8, 8, false);
        assertThrows(IllegalArgumentException.class,
                () -> effect.outputAsFile(ImageFormat.PNG, -1f, target.toString()));
        assertArrayEquals(original, Files.readAllBytes(target));

        ImageContainer container = new ImageContainer();
        container.setBackgroundImage(image(), ZoomMode.Origin, 8, 8);
        container.combine();
        container.setQuality(Float.NaN);
        assertThrows(IllegalArgumentException.class, () -> container.save(target.toString()));
        assertArrayEquals(original, Files.readAllBytes(target));
        assertNoTemporaryFiles();
    }

    @Test
    void successfulFileOutputReplacesOldContentsAndCleansTemporaryFiles() throws Exception {
        Path target = directory.resolve("replace.png");
        Files.write(target, new byte[1000]);
        new ImageConvert(image()).setImageFormat(ImageFormat.PNG).outputAsFile(target.toString());
        assertNotNull(ImageIO.read(target.toFile()));
        new ImageEffect(image()).resize(4, 4, false).outputAsFile(ImageFormat.PNG, 0.8f, target.toString());
        assertEquals(4, ImageIO.read(target.toFile()).getWidth());
        assertNoTemporaryFiles();
    }

    @Test
    void failedReplacementCleansTemporaryFileAndPreservesDirectoryContents() throws Exception {
        Path target = Files.createDirectory(directory.resolve("not-a-file"));
        Path child = target.resolve("keep.txt");
        Files.write(child, new byte[]{1, 2, 3});
        assertThrows(RuntimeException.class, () -> new ImageConvert(image()).setImageFormat(ImageFormat.PNG)
                .outputAsFile(target.toString()));
        assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(child));
        assertNoTemporaryFiles();
    }

    @Test
    void failedFirstCompositionCannotBeExported() {
        ImageContainer container = new ImageContainer();
        container.setBackgroundImage(image(), ZoomMode.Origin, 8, 8);
        container.addElement(null);
        assertThrows(UnsupportedOperationException.class, container::combine);
        assertThrows(IllegalStateException.class, container::getCombinedImageStream);
        assertThrows(IllegalStateException.class, () -> container.save(directory.resolve("partial.jpg").toString()));
    }

    @Test
    void failedLaterCompositionKeepsLastSuccessfulPixels() throws Exception {
        ImageContainer container = new ImageContainer();
        container.setOutputFormat(ImageFormat.PNG);
        container.setBackgroundImage(image(), ZoomMode.Origin, 8, 8);
        BufferedImage complete = container.combine();
        container.setBgColor(java.awt.Color.BLUE); // 后续绘制会先改动工作画布。
        container.addElement(null);
        assertThrows(UnsupportedOperationException.class, container::combine);
        try (InputStream input = container.getCombinedImageStream()) {
            assertArrayEquals(pixels(complete), pixels(ImageIO.read(input)));
        }
    }

    @Test
    void containerEncodingErrorRetainsOriginalCause() {
        ImageContainer container = new ImageContainer();
        container.setBackgroundImage(image(), ZoomMode.Origin, 8, 8);
        container.combine();
        container.setQuality(-1);
        RuntimeException failure = assertThrows(RuntimeException.class, container::getCombinedImageStream);
        assertInstanceOf(IllegalArgumentException.class, failure.getCause());
    }

    @Test
    void blurDoesNotModifySourceAndRepeatedDrawingIsStable() {
        BufferedImage source = image();
        int[] original = pixels(source);
        ImageEmbedded element = new ImageEmbedded();
        element.setImage(source);
        element.setZoomMode(ZoomMode.Origin);
        element.setBlur(1);
        BufferedImage first = draw(element), second = draw(element);
        assertArrayEquals(original, pixels(source));
        assertArrayEquals(pixels(first), pixels(second));
        assertEquals(0, first.getRGB(0, 0) >>> 24);
        assertEquals(0x80, first.getRGB(3, 3) >>> 24);
    }

    @Test
    void blurTilesHaveIdenticalPixelsAndKeepSource() {
        BufferedImage source = image();
        int[] original = pixels(source);
        ImageEmbedded element = new ImageEmbedded();
        element.setImage(source);
        element.setZoomMode(ZoomMode.Origin);
        element.setBlur(1);
        BufferedImage canvas = new BufferedImage(16, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        try {
            new ImagePainter().drawRepeat(g, element, 16, 8);
        } finally {
            g.dispose();
        }
        assertArrayEquals(canvas.getRGB(0, 0, 8, 8, null, 0, 8), canvas.getRGB(8, 0, 8, 8, null, 0, 8));
        assertArrayEquals(original, pixels(source));
    }

    private BufferedImage draw(ImageEmbedded element) {
        BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            new ImagePainter().draw(g, element, 8);
        } finally {
            g.dispose();
        }
        return image;
    }

    private int[] pixels(BufferedImage image) {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

    private void assertNoTemporaryFiles() throws Exception {
        try (Stream<Path> paths = Files.list(directory)) {
            assertFalse(paths.anyMatch(path -> path.getFileName().toString().endsWith(".part")));
        }
    }
}
