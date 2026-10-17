package com.ajaxjs.image;

import com.ajaxjs.image.model.EmbeddedElement;
import com.ajaxjs.image.model.ImageEmbedded;
import com.ajaxjs.image.model.RectangleEmbedded;
import com.ajaxjs.image.model.TextEmbedded;
import com.ajaxjs.image.model.constant.Direction;
import com.ajaxjs.image.model.constant.ImageFormat;
import com.ajaxjs.image.model.constant.MatType;
import com.ajaxjs.image.model.constant.ZoomMode;
import com.ajaxjs.image.painter.ImagePainter;
import com.ajaxjs.image.painter.Painter;
import com.ajaxjs.image.painter.RectanglePainter;
import com.ajaxjs.image.painter.TextPainter;
import com.ajaxjs.image.utils.ImageConvert;
import com.ajaxjs.image.utils.ImageEffect;
import com.ajaxjs.image.utils.ImageMatrix;
import com.ajaxjs.image.utils.ImageTypeHandler;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用内存图片检查尺寸、像素和绘制状态，不依赖外部文件。
 */
class TestImageRegression {
    private BufferedImage image(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(Color.RED);
            g.fillRect(0, 0, width, height);
        } finally {
            g.dispose();
        }
        return image;
    }

    @Test
    void backgroundUsesCanvasSizeAndCanBeCombinedTwice() {
        ImageContainer container = new ImageContainer();
        container.setBackgroundImage(image(40, 20), ZoomMode.WidthHeight, 20, 30);
        BufferedImage first = container.combine();
        assertEquals(20, first.getWidth());
        assertEquals(30, first.getHeight());
        assertEquals(Color.RED.getRGB(), first.getRGB(19, 29));
        assertPixelsEqual(first, container.combine());
    }

    @Test
    void defaultAndExplicitQualityProduceReadableImages() throws Exception {
        for (ImageFormat format : ImageFormat.values()) {
            for (Float quality : new Float[]{null, 0f, 0.8f}) {
                byte[] bytes = new ImageConvert(image(10, 10)).setImageFormat(format)
                        .setQuality(quality).convert();
                BufferedImage result = ImageIO.read(new ByteArrayInputStream(bytes));
                assertNotNull(result, format + ": " + quality);
                assertEquals(10, result.getWidth());
            }
        }
    }

    @Test
    void invalidQualityIsRejected() {
        ImageConvert convert = new ImageConvert(image(2, 2));
        for (float quality : new float[]{-1, 1.1f, Float.NaN, Float.POSITIVE_INFINITY})
            assertThrows(IllegalArgumentException.class, () -> convert.setQuality(quality));
    }

    private TextEmbedded text() {
        TextEmbedded text = new TextEmbedded();
        text.setText("MMMMMMMMMM");
        text.setFont(new Font("Dialog", Font.PLAIN, 24));
        text.setY(0);
        text.setDrawY(24);
        text.setWidth(300);
        text.setHeight(30);
        return text;
    }

    @Test
    void textFitsWithoutChangingItsInput() {
        TextEmbedded text = text();
        text.setAutoFitMaxWidth(35);
        text.setAutoFitMinFontSize(8);
        BufferedImage canvas = new BufferedImage(350, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        try {
            new TextPainter().draw(g, text, 350);
            for (int x = 36; x < canvas.getWidth(); x++)
                for (int y = 0; y < canvas.getHeight(); y++)
                    assertEquals(0, canvas.getRGB(x, y) >>> 24, "文字不能越过最大宽度");
            assertTrue(hasInk(canvas, 0, 35, 0, 50));
            assertEquals("MMMMMMMMMM", text.getText());
            assertEquals(24, text.getFont().getSize());
            assertEquals(300, text.getWidth().intValue());
        } finally {
            g.dispose();
        }
    }

    @Test
    void truncationHandlesWidthsSmallerThanTheEllipsis() {
        TextEmbedded text = text();
        text.setAutoFitMinFontSize(24);
        text.setAutoFitMaxWidth(1);
        BufferedImage canvas = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        try {
            assertDoesNotThrow(() -> new TextPainter().draw(g, text, 40));
            assertFalse(hasInk(canvas, 0, 40, 0, 40));
        } finally {
            g.dispose();
        }
    }

    @Test
    void textRepeatsOnDifferentRowsAndKeepsCoordinates() {
        TextEmbedded text = text();
        text.setText("I");
        text.setWidth(20);
        text.setHeight(30);
        BufferedImage canvas = new BufferedImage(40, 90, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        try {
            new TextPainter().drawRepeat(g, text, 40, 90);
            assertTrue(hasInk(canvas, 0, 20, 0, 30));
            assertTrue(hasInk(canvas, 0, 20, 30, 60));
            assertTrue(hasInk(canvas, 0, 20, 60, 90));
            assertEquals(0, text.getY());
            assertEquals(24, text.getDrawY().intValue());
        } finally {
            g.dispose();
        }
    }

    @Test
    void rectangleAndImageCoordinatesDoNotDrift() {
        RectangleEmbedded rectangle = new RectangleEmbedded();
        rectangle.setX(50);
        rectangle.setY(10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.setDirection(Direction.RightLeft);
        assertStable(new RectanglePainter(), rectangle);

        ImageEmbedded image = new ImageEmbedded();
        image.setImage(image(10, 10));
        image.setZoomMode(ZoomMode.Origin);
        image.setX(50);
        image.setY(10);
        image.setDirection(Direction.RightLeft);
        assertStable(new ImagePainter(), image);

        TextEmbedded text = text();
        text.setX(50);
        text.setDirection(Direction.RightLeft);
        assertStable(new TextPainter(), text);
    }

    private <T extends EmbeddedElement> void assertStable(Painter<T> painter, T element) {
        int x = element.getX(), y = element.getY();
        BufferedImage first = new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB);
        BufferedImage second = new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g1 = first.createGraphics(), g2 = second.createGraphics();
        try {
            painter.draw(g1, element, 120);
            painter.draw(g2, element, 120);
            assertEquals(x, element.getX());
            assertEquals(y, element.getY());
            assertPixelsEqual(first, second);
        } finally {
            g1.dispose();
            g2.dispose();
        }
    }

    @Test
    void repeatKeepsElementPositionAndAppliesRowOffset() {
        RectangleEmbedded rectangle = new RectangleEmbedded();
        rectangle.setX(5);
        rectangle.setY(5);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.setRepeatPaddingHorizontal(10);
        rectangle.setRepeatPaddingVertical(10);
        rectangle.setRepeatRowOffset(3);
        BufferedImage canvas = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        try {
            new RectanglePainter().drawRepeat(g, rectangle, 60, 60);
            assertEquals(5, rectangle.getX());
            assertEquals(5, rectangle.getY());
            assertNotEquals(0, canvas.getRGB(2, 6) >>> 24);
            assertEquals(0, canvas.getRGB(2, 26) >>> 24);
        } finally {
            g.dispose();
        }
    }

    @Test
    void thumbnailFitsTargetWithoutCroppingOutsideTheSource() {
        for (int[] original : new int[][]{{100, 400}, {400, 100}}) {
            for (int[] target : new int[][]{{200, 100}, {500, 100}, {100, 200}}) {
                BufferedImage result = new ImageEffect(image(original[0], original[1]))
                        .thumbnail(target[0], target[1], 2).getOutput();
                assertTrue(result.getWidth() <= target[0]);
                assertTrue(result.getHeight() <= target[1]);
                assertEquals((double) target[0] / target[1],
                        (double) result.getWidth() / result.getHeight(), 0.05);
            }
        }
    }

    @Test
    void regionDecodeHasRequestedDimensionsAndPixels() throws Exception {
        BufferedImage original = new BufferedImage(100, 80, BufferedImage.TYPE_3BYTE_BGR);
        original.setRGB(10, 10, Color.BLUE.getRGB());
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        ImageIO.write(original, "png", encoded);
        for (ImageTypeSpecifier type : new ImageTypeSpecifier[]{null,
                ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_3BYTE_BGR)}) {
            BufferedImage region = new ImageTypeHandler(encoded.toByteArray(), "png")
                    .read(new Rectangle(10, 10, 20, 15), type);
            assertEquals(20, region.getWidth());
            assertEquals(15, region.getHeight());
            assertEquals(Color.BLUE.getRGB(), region.getRGB(0, 0));
        }
    }

    @Test
    void rotationUsesActualSizeForOriginalAndSingleDimensionZoom() {
        ImageEmbedded element = new ImageEmbedded();
        element.setImage(image(20, 10));
        element.setZoomMode(ZoomMode.Origin);
        element.setRotate(90);
        element.setX(30);
        element.setY(30);
        assertStable(new ImagePainter(), element);
        BufferedImage origin = render(element);
        element.setZoomMode(ZoomMode.WidthHeight);
        element.setWidth(20);
        element.setHeight(10);
        assertPixelsEqual(origin, render(element));

        element.setWidth(40);
        element.setHeight(20);
        BufferedImage explicitSize = render(element);
        element.setHeight(null);
        element.setZoomMode(ZoomMode.Width);
        assertPixelsEqual(explicitSize, render(element));
        element.setWidth(null);
        element.setHeight(20);
        element.setZoomMode(ZoomMode.Height);
        assertPixelsEqual(explicitSize, render(element));
    }

    private BufferedImage render(ImageEmbedded element) {
        BufferedImage canvas = new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        try {
            new ImagePainter().draw(g, element, 120);
        } finally {
            g.dispose();
        }
        return canvas;
    }

    @Test
    void bgrProducesCorrectBytesAndUnsupportedFormatThrows() {
        ImageMatrix matrix = new ImageMatrix(image(1, 1));
        assertArrayEquals(new byte[]{0, 0, (byte) 255}, matrix.getMatrix(MatType.BGR).getMatrix());
        assertThrows(UnsupportedOperationException.class, () -> matrix.getMatrix(MatType.NV21));
    }

    @Test
    void scalePreservesTransparentPixels() {
        BufferedImage original = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        original.setRGB(2, 2, 0x80ff0000);
        BufferedImage scaled = new ImageEffect(original).scale(1).getOutput();
        assertTrue(scaled.getColorModel().hasAlpha());
        assertEquals(0, scaled.getRGB(0, 0) >>> 24);
        assertEquals(0x80, scaled.getRGB(2, 2) >>> 24);
    }

    private boolean hasInk(BufferedImage image, int left, int right, int top, int bottom) {
        for (int y = top; y < bottom; y++)
            for (int x = left; x < right; x++)
                if ((image.getRGB(x, y) >>> 24) != 0)
                    return true;
        return false;
    }

    private void assertPixelsEqual(BufferedImage expected, BufferedImage actual) {
        assertEquals(expected.getWidth(), actual.getWidth());
        assertEquals(expected.getHeight(), actual.getHeight());
        assertArrayEquals(expected.getRGB(0, 0, expected.getWidth(), expected.getHeight(), null, 0, expected.getWidth()),
                actual.getRGB(0, 0, actual.getWidth(), actual.getHeight(), null, 0, actual.getWidth()));
    }
}
