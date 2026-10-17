package com.ajaxjs.image.painter;

import com.ajaxjs.image.model.RectangleEmbedded;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ajaxjs.image.painter.PainterTestSupport.assertGraphicsPreserved;
import static com.ajaxjs.image.painter.PainterTestSupport.render;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 独立验证公共平铺算法，不把具体图形的绘制当成前提。
 */
class TestPainter {
    private RectangleEmbedded element() {
        RectangleEmbedded element = new RectangleEmbedded();
        element.setX(3);
        element.setY(5);
        element.setWidth(4);
        element.setHeight(6);
        element.setRepeatPaddingHorizontal(2);
        element.setRepeatPaddingVertical(2);
        element.setRepeatRowOffset(2);
        return element;
    }

    @Test
    void repeatReturnsToEdgesAndTraversesColumnsWithAlternatingRows() {
        RectangleEmbedded element = element();
        RecordingPainter painter = new RecordingPainter();
        render(10, 12, g -> painter.drawRepeat(g, element, 10, 12));
        assertEquals(Arrays.asList(new Point(-3, -3), new Point(-5, 5),
                new Point(3, -3), new Point(1, 5), new Point(9, -3), new Point(7, 5)), painter.positions);
        assertEquals(3, element.getX());
        assertEquals(5, element.getY());
    }

    @Test
    void explicitTileSizeOverridesConfiguredElementSize() {
        RectangleEmbedded element = new RectangleEmbedded();
        element.setWidth(100);
        element.setHeight(100);
        RecordingPainter painter = new RecordingPainter();
        render(8, 6, g -> painter.drawRepeat(g, element, 8, 6, 4, 3));
        assertEquals(Arrays.asList(new Point(0, 0), new Point(0, 3), new Point(4, 0), new Point(4, 3)), painter.positions);
    }

    @Test
    void negativeOriginsAndOverlappingTilesAreSupported() {
        RectangleEmbedded element = new RectangleEmbedded();
        element.setX(-2);
        element.setY(-1);
        element.setWidth(5);
        element.setHeight(4);
        element.setRepeatPaddingHorizontal(-2);
        element.setRepeatPaddingVertical(-1);
        RecordingPainter painter = new RecordingPainter();
        render(4, 3, g -> painter.drawRepeat(g, element, 4, 3));
        assertEquals(Arrays.asList(new Point(-2, -1), new Point(-2, 2), new Point(1, -1), new Point(1, 2)), painter.positions);
    }

    @Test
    void invalidOrOverflowingStepsAreRejectedWithoutLooping() {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            for (int[] sizes : new int[][]{{0, 1}, {-1, 1}, {1, 0}, {1, -1}, {Integer.MAX_VALUE, 1}}) {
                RectangleEmbedded element = new RectangleEmbedded();
                element.setRepeatPaddingHorizontal(sizes[0] == Integer.MAX_VALUE ? 1 : 0);
                render(10, 10, g -> assertThrows(IllegalArgumentException.class,
                        () -> new RecordingPainter().drawRepeat(g, element, 10, 10, sizes[0], sizes[1])));
            }
        });
    }

    @Test
    void hugeStartingCoordinatesReturnToCanvasWithoutOverflow() {
        RectangleEmbedded element = new RectangleEmbedded();
        element.setX(Integer.MAX_VALUE);
        element.setY(Integer.MAX_VALUE);
        element.setWidth(6);
        element.setHeight(6);
        RecordingPainter painter = new RecordingPainter();
        render(2, 2, g -> painter.drawRepeat(g, element, 2, 2));
        assertEquals(Arrays.asList(new Point(-5, -5), new Point(-5, 1), new Point(1, -5), new Point(1, 1)), painter.positions);
    }

    @Test
    void perTileGraphicsChangesDoNotLeakToLaterTilesOrCaller() {
        RecordingPainter painter = new RecordingPainter();
        assertGraphicsPreserved(g -> painter.drawRepeat(g, element(), 10, 12));
        assertEquals(6, painter.positions.size());
        // 调用方平移 3、4；单元内额外的 translate(100,100) 不应累积。
        assertEquals(new Point(0, 1), painter.positions.get(0));
        assertEquals(new Point(-2, 9), painter.positions.get(1));
    }

    @Test
    void drawFailurePropagatesWithoutChangingElementOrCallerGraphics() {
        RectangleEmbedded element = element();
        Painter<RectangleEmbedded> painter = (g, value, width) -> {
            g.translate(100, 100);
            throw new IllegalStateException("draw failed");
        };
        assertGraphicsPreserved(g -> {
            IllegalStateException error = assertThrows(IllegalStateException.class,
                    () -> painter.drawRepeat(g, element, 10, 12));
            assertEquals("draw failed", error.getMessage());
        });
        assertEquals(3, element.getX());
        assertEquals(5, element.getY());
    }

    private static class RecordingPainter implements Painter<RectangleEmbedded> {
        final List<Point> positions = new ArrayList<>();

        @Override
        public void draw(Graphics2D g, RectangleEmbedded element, int canvasWidth) {
            positions.add(new Point((int) (element.getX() + g.getTransform().getTranslateX()),
                    (int) (element.getY() + g.getTransform().getTranslateY())));
            g.translate(100, 100);
            g.setColor(Color.BLUE);
        }
    }
}
