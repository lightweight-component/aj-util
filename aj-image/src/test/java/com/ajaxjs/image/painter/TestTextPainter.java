package com.ajaxjs.image.painter;

import com.ajaxjs.image.model.TextEmbedded;
import com.ajaxjs.image.model.constant.BaseLine;
import com.ajaxjs.image.model.constant.Direction;
import com.ajaxjs.image.model.constant.LineAlign;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.util.Arrays;
import java.util.Collections;

import static com.ajaxjs.image.painter.PainterTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 通过同环境 Java2D 参考绘制验证文字布局，不依赖操作系统的固定字体截图。
 */
class TestTextPainter {
    private final TextPainter painter = new TextPainter();
    private final Font font = new Font("Monospaced", Font.PLAIN, 18);

    private TextEmbedded text(String content) {
        TextEmbedded element = new TextEmbedded();
        element.setText(content);
        element.setFont(font);
        element.setX(20);
        element.setY(40);
        element.setDrawY(40);
        return element;
    }

    private FontMetrics metrics(Font font) {
        Graphics2D g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
        try {
            return g.getFontMetrics(font);
        } finally {
            g.dispose();
        }
    }

    private BufferedImage draw(TextEmbedded element) {
        return render(160, 160, g -> painter.draw(g, element, 160));
    }

    private BufferedImage reference(String content, Font usedFont, Color color, int x, int baseline) {
        return render(160, 160, g -> {
            g.setFont(usedFont);
            g.setColor(color);
            g.drawString(content, x, baseline);
        });
    }

    @Test
    void plainTextUsesConfiguredFontColorAndBaselineWithoutCachedSize() {
        TextEmbedded element = text("Hello");
        element.setColor(Color.BLUE);
        assertPixelsEqual(reference("Hello", font, Color.BLUE, 20, 40), draw(element));
        assertNull(element.getWidth());
        assertNull(element.getHeight());
        assertEquals(20, element.getX());
        assertEquals(40, element.getY());
    }

    @Test
    void eachBaselineModePositionsTextRelativeToY() {
        FontMetrics metrics = metrics(font);
        for (BaseLine baseline : BaseLine.values()) {
            TextEmbedded element = text("Hello");
            element.setDrawY(null);
            element.setY(60);
            element.setBaseLine(baseline);
            int expected;
            switch (baseline) {
                case Top:
                    expected = 60 + metrics.getAscent();
                    break;
                case Middle:
                    expected = 60 + (metrics.getAscent() - metrics.getDescent()) / 2;
                    break;
                case Bottom:
                    expected = 60 - metrics.getDescent();
                    break;
                default:
                    expected = 60;
            }
            assertPixelsEqual(reference("Hello", font, Color.BLACK, 20, expected), draw(element));
            assertNull(element.getDrawY());
        }
    }

    @Test
    void explicitDrawYOverridesYAndBaselineMode() {
        TextEmbedded element = text("Hello");
        element.setY(100);
        element.setDrawY(35);
        element.setBaseLine(BaseLine.Bottom);
        assertPixelsEqual(reference("Hello", font, Color.BLACK, 20, 35), draw(element));
    }

    @Test
    void directionAnchorsToMeasuredWidthRatherThanConfiguredWidth() {
        int width = metrics(font).stringWidth("Hello");
        for (Direction direction : Direction.values()) {
            TextEmbedded element = text("Hello");
            element.setX(100);
            element.setWidth(999);
            element.setDirection(direction);
            int expectedX = direction == Direction.LeftRight ? 100
                    : direction == Direction.RightLeft ? 100 - width : 100 - width / 2;
            assertPixelsEqual(reference("Hello", font, Color.BLACK, expectedX, 40), draw(element));
            assertEquals(100, element.getX());
        }
    }

    @Test
    void centeringOverridesConfiguredDirectionAndX() {
        TextEmbedded element = text("Hello");
        element.setCenter(true);
        element.setDirection(Direction.RightLeft);
        int expectedX = (160 - metrics(font).stringWidth("Hello")) / 2;
        assertPixelsEqual(reference("Hello", font, Color.BLACK, expectedX, 40), draw(element));
        assertEquals(20, element.getX());
    }

    @Test
    void opacityAndRotationMatchReferenceDrawing() {
        TextEmbedded element = text("Hello");
        element.setRotate(30);
        element.setAlpha(0.5f);
        BufferedImage expected = render(160, 160, g -> {
            g.setFont(font);
            g.setColor(Color.BLACK);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g.rotate(Math.toRadians(30), 20 + metrics(font).stringWidth("Hello") / 2.0, 40);
            g.drawString("Hello", 20, 40);
        });
        assertPixelsEqual(expected, draw(element));
    }

    @Test
    void strikeThroughAddsTheRequestedDecoration() {
        TextEmbedded element = text("MMMM");
        BufferedImage plain = draw(element);
        element.setStrikeThrough(true);
        BufferedImage expected = render(160, 160, g -> {
            g.setColor(Color.BLACK);
            AttributedString value = new AttributedString("MMMM");
            value.addAttribute(TextAttribute.FONT, font);
            value.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            g.drawString(value.getIterator(), 20, 40);
        });
        BufferedImage decorated = draw(element);
        assertPixelsEqual(expected, decorated);
        assertTrue(inkCount(decorated) > inkCount(plain));
    }

    @Test
    void fittingShrinksTextWithoutChangingOriginalTextOrFont() {
        TextEmbedded element = text("MMMMMMMMMM");
        element.setFont(font.deriveFont(30f));
        Font originalFont = element.getFont();
        int limit = metrics(font).stringWidth(element.getText());
        element.setAutoFitMaxWidth(limit);
        element.setAutoFitMinFontSize(10);
        BufferedImage result = draw(element);
        assertFalse(inkBounds(result).isEmpty());
        assertTrue(inkBounds(result).getMaxX() <= 20 + limit);
        assertEquals(originalFont, element.getFont());
        assertEquals("MMMMMMMMMM", element.getText());
    }

    @Test
    void minimumFontSizeFallsBackToEllipsisWithoutChangingInput() {
        TextEmbedded element = text("abcdefghijk");
        element.setAutoFitMinFontSize(font.getSize());
        element.setAutoFitMaxWidth(metrics(font).stringWidth("ab..."));
        assertPixelsEqual(reference("ab...", font, Color.BLACK, 20, 40), draw(element));
        assertEquals("abcdefghijk", element.getText());
    }

    @Test
    void widthsTooSmallForEllipsisAndEmptyDecoratedTextDrawNothing() {
        TextEmbedded element = text("abcdef");
        element.setAutoFitMinFontSize(font.getSize());
        element.setAutoFitMaxWidth(0);
        assertTrue(inkBounds(draw(element)).isEmpty());
        element.setText("");
        element.setStrikeThrough(true);
        assertTrue(inkBounds(draw(element)).isEmpty());
    }

    @Test
    void multilineAlignmentUsesFirstLineAndKeepsLineCoordinates() {
        TextEmbedded first = text("MMMM");
        TextEmbedded second = text("I");
        second.setX(90);
        second.setDrawY(80);
        second.setColor(Color.BLUE);
        TextEmbedded group = new TextEmbedded();
        group.setAutoBreakLine(true);
        group.setBreakLineElements(Arrays.asList(first, second));
        int firstWidth = metrics(font).stringWidth("MMMM"), secondWidth = metrics(font).stringWidth("I");
        for (LineAlign align : LineAlign.values()) {
            group.setLineAlign(align);
            int offset = align == LineAlign.Left ? 0
                    : align == LineAlign.Center ? (firstWidth - secondWidth) / 2 : firstWidth - secondWidth;
            BufferedImage expected = render(160, 160, g -> {
                g.setFont(font);
                g.setColor(Color.BLACK);
                g.drawString("MMMM", 20, 40);
                g.setColor(Color.BLUE);
                g.drawString("I", 20 + offset, 80);
            });
            assertPixelsEqual(expected, draw(group));
            assertEquals(20, first.getX());
            assertEquals(90, second.getX());
            assertEquals(80, second.getDrawY().intValue());
        }
    }

    @Test
    void repeatingTextUsesMeasuredDimensionsLineHeightOrExplicitSize() {
        for (int mode = 0; mode < 3; mode++) {
            TextEmbedded element = text("I");
            element.setX(0);
            element.setY(0);
            element.setDrawY(18);
            element.setRepeatPaddingHorizontal(3);
            element.setRepeatPaddingVertical(4);
            if (mode == 1)
                element.setLineHeight(30);
            if (mode == 2) {
                element.setWidth(20);
                element.setHeight(25);
                element.setLineHeight(70); // height 优先于 lineHeight。
            }
            int stepX = (mode == 2 ? 20 : metrics(font).stringWidth("I")) + 3;
            int stepY = (mode == 2 ? 25 : mode == 1 ? 30 : metrics(font).getHeight()) + 4;
            BufferedImage expected = render(100, 100, g -> {
                g.setFont(font);
                g.setColor(Color.BLACK);
                for (int x = 0; x < 100; x += stepX)
                    for (int y = 0; y < 100; y += stepY)
                        g.drawString("I", x, y + 18);
            });
            assertPixelsEqual(expected, render(100, 100, g -> painter.drawRepeat(g, element, 100, 100)));
            assertEquals(0, element.getX());
            assertEquals(0, element.getY());
            assertEquals(18, element.getDrawY().intValue());
        }
    }

    @Test
    void invalidConfigurationsFailClearlyAndEmptyMultilineIsAllowed() {
        TextEmbedded element = text(null);
        assertThrows(IllegalArgumentException.class, () -> draw(element));
        element.setText("hello");
        element.setFont(null);
        assertThrows(IllegalArgumentException.class, () -> draw(element));
        element.setFont(font);
        element.setAutoFitMaxWidth(-1);
        assertThrows(IllegalArgumentException.class, () -> draw(element));
        element.setAutoFitMaxWidth(null);
        element.setAutoBreakLine(true);
        assertThrows(IllegalArgumentException.class, () -> draw(element));
        element.setBreakLineElements(Collections.emptyList());
        assertTrue(inkBounds(draw(element)).isEmpty());
    }

    @Test
    void successfulAndFailedDrawingPreserveCallerGraphics() {
        TextEmbedded element = text("Hello");
        element.setRotate(20);
        assertGraphicsPreserved(g -> painter.draw(g, element, 120));
        element.setAlpha(-1);
        assertGraphicsPreserved(g -> assertThrows(IllegalArgumentException.class,
                () -> painter.draw(g, element, 120)));
    }
}
