package com.ajaxjs.image.painter;

import com.ajaxjs.image.model.ImageEmbedded;
import com.ajaxjs.image.model.constant.Direction;
import com.ajaxjs.image.model.constant.ZoomMode;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static com.ajaxjs.image.painter.PainterTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 图片画笔的缩放、定位、旋转和效果测试。
 */
class TestImagePainter {
    private final ImagePainter painter = new ImagePainter();

    private ImageEmbedded element() {
        ImageEmbedded element = new ImageEmbedded();
        element.setImage(solid(20, 10, Color.RED));
        element.setZoomMode(ZoomMode.Origin);
        element.setX(20);
        element.setY(20);
        return element;
    }

    private BufferedImage draw(ImageEmbedded element) {
        return render(120, 120, g -> painter.draw(g, element, 120));
    }

    @Test
    void zoomModesProduceExpectedDrawnDimensions() {
        int[][] sizes = {{20, 10}, {40, 20}, {60, 30}, {40, 30}};
        for (ZoomMode mode : ZoomMode.values()) {
            ImageEmbedded element = element();
            element.setZoomMode(mode);
            element.setWidth(40);
            element.setHeight(30);
            int[] size = sizes[mode.ordinal()];
            assertEquals(new Rectangle(20, 20, size[0], size[1]), inkBounds(draw(element)), mode.toString());
            assertEquals(20, element.getImage().getWidth());
            assertEquals(10, element.getImage().getHeight());
        }
    }

    @Test
    void directionChangesAnchorWithoutMovingElement() {
        int[] starts = {60, 40, 50};
        for (Direction direction : Direction.values()) {
            ImageEmbedded element = element();
            element.setX(60);
            element.setDirection(direction);
            assertEquals(new Rectangle(starts[direction.ordinal()], 20, 20, 10), inkBounds(draw(element)));
            assertEquals(60, element.getX());
        }
    }

    @Test
    void centeringTakesPriorityOverDirection() {
        ImageEmbedded element = element();
        element.setCenter(true);
        element.setDirection(Direction.RightLeft);
        assertEquals(new Rectangle(50, 20, 20, 10), inkBounds(draw(element)));
        assertEquals(20, element.getX());
    }

    @Test
    void transparencyIsAppliedToSourcePixels() {
        ImageEmbedded element = element();
        element.setImage(solid(20, 10, new Color(255, 0, 0, 128)));
        element.setAlpha(0.5f);
        assertEquals(64, alpha(draw(element), 25, 25), 1);
    }

    @Test
    void ninetyDegreeRotationUsesRealImageCenterAndOrientation() {
        ImageEmbedded element = element();
        element.setImage(render(8, 4, g -> {
            g.setColor(Color.RED);
            g.fillRect(0, 0, 4, 4);
            g.setColor(Color.BLUE);
            g.fillRect(4, 0, 4, 4);
        }));
        element.setRotate(90);
        BufferedImage result = draw(element);
        assertEquals(new Rectangle(22, 18, 4, 8), inkBounds(result));
        assertEquals(Color.RED.getRGB(), result.getRGB(23, 19));
        assertEquals(Color.BLUE.getRGB(), result.getRGB(23, 25));
        assertEquals(20, element.getX());
        assertEquals(20, element.getY());
    }

    @Test
    void singleDimensionZoomRotatesLikeItsEquivalentExplicitSize() {
        ImageEmbedded element = element();
        element.setRotate(90);
        element.setZoomMode(ZoomMode.WidthHeight);
        element.setWidth(40);
        element.setHeight(20);
        BufferedImage expected = draw(element);
        element.setZoomMode(ZoomMode.Width);
        element.setHeight(null);
        assertPixelsEqual(expected, draw(element));
        element.setZoomMode(ZoomMode.Height);
        element.setWidth(null);
        element.setHeight(20);
        assertPixelsEqual(expected, draw(element));
    }

    @Test
    void roundedImageKeepsTransparentCornersWithoutChangingSource() {
        ImageEmbedded element = element();
        int[] source = pixels(element.getImage());
        element.setRoundCorner(10);
        BufferedImage result = draw(element);
        assertEquals(0, alpha(result, 20, 20));
        assertEquals(Color.RED.getRGB(), result.getRGB(30, 25));
        assertArrayEquals(source, pixels(element.getImage()));
    }

    @Test
    void blurSpreadsColorWithoutChangingSourceAndIsRepeatable() {
        ImageEmbedded element = element();
        BufferedImage source = new BufferedImage(7, 7, BufferedImage.TYPE_INT_RGB);
        source.setRGB(3, 3, Color.WHITE.getRGB());
        int[] before = pixels(source);
        element.setImage(source);
        element.setBlur(1);
        BufferedImage first = draw(element);
        Color center = new Color(first.getRGB(23, 23));
        Color neighbor = new Color(first.getRGB(22, 23));
        assertTrue(center.getRed() > 0 && center.getRed() < 255);
        assertTrue(neighbor.getRed() > 0);
        assertArrayEquals(before, pixels(source));
        assertPixelsEqual(first, draw(element));
    }

    @Test
    void disabledBlurLeavesPixelsUnchanged() {
        ImageEmbedded element = element();
        BufferedImage expected = draw(element);
        for (int radius : new int[]{0, -1}) {
            element.setBlur(radius);
            assertPixelsEqual(expected, draw(element));
        }
    }

    @Test
    void repeatUsesScaledDimensionsAndConfiguredGaps() {
        ImageEmbedded element = element();
        element.setImage(solid(2, 1, Color.RED));
        element.setX(0);
        element.setY(0);
        element.setZoomMode(ZoomMode.Width);
        element.setWidth(4);
        element.setRepeatPaddingHorizontal(2);
        element.setRepeatPaddingVertical(2);
        BufferedImage result = render(10, 6, g -> painter.drawRepeat(g, element, 10, 6));
        for (Point point : Arrays.asList(new Point(0, 0), new Point(6, 0), new Point(0, 4), new Point(6, 4)))
            assertEquals(Color.RED.getRGB(), result.getRGB(point.x, point.y));
        assertEquals(0, alpha(result, 4, 0));
        assertEquals(0, alpha(result, 0, 2));
        assertEquals(0, element.getX());
        assertEquals(0, element.getY());
        assertNull(element.getHeight());
    }

    @Test
    void successfulAndFailedDrawingPreserveCallerGraphics() {
        ImageEmbedded element = element();
        element.setRotate(20);
        element.setAlpha(0.5f);
        assertGraphicsPreserved(g -> painter.draw(g, element, 120));
        element.setAlpha(2);
        assertGraphicsPreserved(g -> assertThrows(IllegalArgumentException.class,
                () -> painter.draw(g, element, 120)));
    }
}
