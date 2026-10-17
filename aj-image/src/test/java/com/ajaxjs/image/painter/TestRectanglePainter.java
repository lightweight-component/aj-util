package com.ajaxjs.image.painter;

import com.ajaxjs.image.model.RectangleEmbedded;
import com.ajaxjs.image.model.constant.Direction;
import com.ajaxjs.image.model.constant.GradientDirection;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static com.ajaxjs.image.painter.PainterTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 检查矩形的填充、边框、圆角、渐变和定位是否落到正确的像素上。
 */
class TestRectanglePainter {
    private final RectanglePainter painter = new RectanglePainter();

    private RectangleEmbedded rectangle() {
        RectangleEmbedded element = new RectangleEmbedded();
        element.setX(20);
        element.setY(20);
        element.setWidth(40);
        element.setHeight(30);
        element.setColor(Color.RED);
        return element;
    }

    private BufferedImage draw(RectangleEmbedded element) {
        return render(100, 100, g -> painter.draw(g, element, 100));
    }

    @Test
    void fillsOnlyConfiguredRectangleWithRequestedColor() {
        BufferedImage result = draw(rectangle());
        assertEquals(new Rectangle(20, 20, 40, 30), inkBounds(result));
        assertEquals(Color.RED.getRGB(), result.getRGB(30, 30));
        assertEquals(0, alpha(result, 19, 30));
        assertEquals(0, alpha(result, 60, 30));
    }

    @Test
    void directionControlsAnchorWithoutChangingInputCoordinates() {
        int[] left = {20, -20, 0};
        for (Direction direction : Direction.values()) {
            RectangleEmbedded element = rectangle();
            element.setX(60);
            element.setDirection(direction);
            int expectedX = left[direction.ordinal()] + 40;
            assertEquals(new Rectangle(expectedX, 20, 40, 30), inkBounds(draw(element)));
            assertEquals(60, element.getX());
            assertEquals(20, element.getY());
        }
    }

    @Test
    void horizontalCenterTakesPriorityOverDirection() {
        RectangleEmbedded element = rectangle();
        element.setX(90);
        element.setCenter(true);
        element.setDirection(Direction.RightLeft);
        assertEquals(new Rectangle(30, 20, 40, 30), inkBounds(draw(element)));
        assertEquals(90, element.getX());
    }

    @Test
    void opacityCombinesWithColorAlpha() {
        RectangleEmbedded element = rectangle();
        element.setColor(new Color(255, 0, 0, 128));
        element.setAlpha(0.5f);
        assertEquals(64, alpha(draw(element), 30, 30), 1);
    }

    @Test
    void borderLeavesInteriorTransparentForStraightAndRoundedShapes() {
        for (int corner : new int[]{0, 16}) {
            RectangleEmbedded element = rectangle();
            element.setBorderSize(2);
            element.setRoundCorner(corner);
            BufferedImage result = draw(element);
            assertEquals(0, alpha(result, 40, 35));
            assertTrue(alpha(result, 40, 20) > 0);
            assertTrue(alpha(result, 20, 35) > 0);
        }
    }

    @Test
    void roundedFillClearsCornersAndPreservesCenter() {
        RectangleEmbedded element = rectangle();
        element.setRoundCorner(20);
        BufferedImage result = draw(element);
        assertEquals(0, alpha(result, 20, 20));
        assertEquals(Color.RED.getRGB(), result.getRGB(40, 35));
    }

    @Test
    void eachGradientRunsFromStartColorToEndColor() {
        for (GradientDirection direction : GradientDirection.values()) {
            RectangleEmbedded element = rectangle();
            element.setHeight(40);
            element.setFromColor(Color.RED);
            element.setToColor(Color.BLUE);
            element.setGradientDirection(direction);
            BufferedImage result = draw(element);
            Point start, end;
            switch (direction) {
                case TopBottom:
                    start = new Point(40, 22);
                    end = new Point(40, 57);
                    break;
                case LeftRight:
                    start = new Point(22, 40);
                    end = new Point(57, 40);
                    break;
                case LeftTopRightBottom:
                    start = new Point(22, 22);
                    end = new Point(57, 57);
                    break;
                default:
                    start = new Point(57, 22);
                    end = new Point(22, 57);
            }
            Color first = new Color(result.getRGB(start.x, start.y));
            Color last = new Color(result.getRGB(end.x, end.y));
            assertTrue(first.getRed() > first.getBlue(), direction.toString());
            assertTrue(last.getBlue() > last.getRed(), direction.toString());
        }
    }

    @Test
    void extendingGradientChangesTransitionInsideRectangle() {
        RectangleEmbedded element = rectangle();
        element.setFromColor(Color.RED);
        element.setToColor(Color.BLUE);
        element.setGradientDirection(GradientDirection.LeftRight);
        Color before = new Color(draw(element).getRGB(21, 30));
        element.setFromExtend(20);
        element.setToExtend(20);
        Color after = new Color(draw(element).getRGB(21, 30));
        assertTrue(after.getBlue() > before.getBlue());
        assertTrue(after.getRed() < before.getRed());
    }

    @Test
    void repeatLeavesGapsAndOffsetsAlternateRows() {
        RectangleEmbedded element = rectangle();
        element.setX(0);
        element.setY(0);
        element.setWidth(10);
        element.setHeight(10);
        element.setRepeatPaddingHorizontal(5);
        element.setRepeatPaddingVertical(5);
        element.setRepeatRowOffset(3);
        BufferedImage result = render(40, 40, g -> painter.drawRepeat(g, element, 40, 40));
        assertEquals(Color.RED.getRGB(), result.getRGB(1, 1));
        assertEquals(0, alpha(result, 11, 1));
        assertEquals(0, alpha(result, 1, 11));
        assertEquals(0, alpha(result, 8, 16));
        assertEquals(Color.RED.getRGB(), result.getRGB(13, 16));
        assertEquals(0, element.getX());
        assertEquals(0, element.getY());
    }

    @Test
    void successfulAndFailedDrawingPreserveCallerGraphics() {
        RectangleEmbedded element = rectangle();
        assertGraphicsPreserved(g -> painter.draw(g, element, 100));
        element.setAlpha(2);
        assertGraphicsPreserved(g -> assertThrows(IllegalArgumentException.class,
                () -> painter.draw(g, element, 100)));
    }
}
