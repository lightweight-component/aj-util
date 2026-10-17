package com.ajaxjs.image.painter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 内存画布和像素断言，字体结果在当前运行环境中测量，避免依赖平台截图。
 */
final class PainterTestSupport {
    private PainterTestSupport() {
    }

    static BufferedImage render(int width, int height, Consumer<Graphics2D> action) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            action.accept(g);
        } finally {
            g.dispose();
        }
        return image;
    }

    static BufferedImage solid(int width, int height, Color color) {
        return render(width, height, g -> {
            g.setColor(color);
            g.fillRect(0, 0, width, height);
        });
    }

    static void assertPixelsEqual(BufferedImage expected, BufferedImage actual) {
        assertEquals(expected.getWidth(), actual.getWidth());
        assertEquals(expected.getHeight(), actual.getHeight());
        assertArrayEquals(pixels(expected), pixels(actual));
    }

    static int[] pixels(BufferedImage image) {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

    static Rectangle inkBounds(BufferedImage image) {
        int left = image.getWidth(), top = image.getHeight(), right = -1, bottom = -1;
        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++)
                if (alpha(image, x, y) != 0) {
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                    top = Math.min(top, y);
                    bottom = Math.max(bottom, y);
                }
        return right < left ? new Rectangle() : new Rectangle(left, top, right - left + 1, bottom - top + 1);
    }

    static int alpha(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) >>> 24;
    }

    static int inkCount(BufferedImage image) {
        int count = 0;
        for (int pixel : pixels(image))
            if ((pixel >>> 24) != 0)
                count++;
        return count;
    }

    static void assertGraphicsPreserved(Consumer<Graphics2D> action) {
        BufferedImage result = render(120, 120, g -> {
            g.translate(3, 4);
            g.setClip(2, 3, 80, 70);
            g.setColor(Color.ORANGE);
            g.setPaint(new GradientPaint(0, 0, Color.RED, 30, 30, Color.BLUE));
            g.setStroke(new BasicStroke(3));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g.setFont(new Font("Dialog", Font.ITALIC, 13));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            AffineTransform transform = g.getTransform();
            Rectangle clip = g.getClipBounds();
            Paint paint = g.getPaint();
            Color color = g.getColor();
            Stroke stroke = g.getStroke();
            Composite composite = g.getComposite();
            Font font = g.getFont();
            RenderingHints hints = g.getRenderingHints();
            action.accept(g);
            assertEquals(transform, g.getTransform());
            assertEquals(clip, g.getClipBounds());
            assertEquals(paint, g.getPaint());
            assertEquals(color, g.getColor());
            assertEquals(stroke, g.getStroke());
            assertEquals(composite, g.getComposite());
            assertEquals(font, g.getFont());
            assertEquals(hints, g.getRenderingHints());
            // 验证调用方的画笔仍可继续使用，没有被 Painter 关闭。
            g.setTransform(new AffineTransform());
            g.setClip(null);
            g.setComposite(AlphaComposite.Src);
            g.setColor(Color.GREEN);
            g.fillRect(119, 119, 1, 1);
        });
        assertEquals(Color.GREEN.getRGB(), result.getRGB(119, 119));
    }
}
