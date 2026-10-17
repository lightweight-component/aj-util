package com.ajaxjs.image;

import com.ajaxjs.image.model.ImageEmbedded;
import com.ajaxjs.image.model.RectangleEmbedded;
import com.ajaxjs.image.model.TextEmbedded;
import com.ajaxjs.image.model.constant.ImageFormat;
import com.ajaxjs.image.model.constant.ZoomMode;
import com.ajaxjs.image.utils.ImageEffect;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class TestImageContainer {
    @Test
    void stringBackgroundReadsLocalAndHttpImagesAndPreservesStateOnFailure(
            @org.junit.jupiter.api.io.TempDir java.nio.file.Path directory) throws Exception {
        BufferedImage source = solid(6, 4, Color.RED);
        java.nio.file.Path path = directory.resolve("background.png");
        ImageIO.write(source, "png", path.toFile());
        byte[] bytes = java.nio.file.Files.readAllBytes(path);
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(
                new java.net.InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/background", exchange -> {
            try {
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            } finally {
                exchange.close();
            }
        });
        server.start();
        try {
            for (String location : new String[]{path.toString(),
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/background"}) {
                ImageContainer container = new ImageContainer();
                container.setBackgroundImage(location, ZoomMode.WidthHeight, 9, 7);
                assertEquals(9, container.combine().getWidth());
                assertEquals(7, container.combine().getHeight());
                assertEquals(Color.RED.getRGB(), container.combine().getRGB(8, 6));
                java.nio.file.Path invalid = directory.resolve("invalid.png");
                java.nio.file.Files.write(invalid, new byte[]{1, 2, 3});
                assertThrows(IllegalArgumentException.class,
                        () -> container.setBackgroundImage(invalid.toString(), ZoomMode.Origin, 0, 0));
                assertThrows(java.io.UncheckedIOException.class,
                        () -> container.setBackgroundImage(directory.resolve("missing.png").toString(), ZoomMode.Origin, 0, 0));
                assertEquals(9, container.combine().getWidth());
                assertEquals(Color.RED.getRGB(), container.combine().getRGB(8, 6));
            }
        } finally {
            server.stop(0);
        }
    }

    private BufferedImage solid(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(color);
            g.fillRect(0, 0, width, height);
        } finally {
            g.dispose();
        }
        return image;
    }

    private RectangleEmbedded rectangle(Color color) {
        RectangleEmbedded element = new RectangleEmbedded();
        element.setX(2);
        element.setY(2);
        element.setWidth(4);
        element.setHeight(4);
        element.setColor(color);
        return element;
    }

    @Test
    void backgroundModesDetermineCanvasDimensions() {
        int[][] sizes = {{6, 4}, {9, 6}, {10, 7}, {9, 7}};
        for (ZoomMode mode : ZoomMode.values()) {
            ImageContainer container = new ImageContainer();
            container.setBackgroundImage(solid(6, 4, Color.RED), mode, 9, 7);
            BufferedImage image = container.combine();
            assertEquals(sizes[mode.ordinal()][0], image.getWidth());
            assertEquals(sizes[mode.ordinal()][1], image.getHeight());
            assertEquals(Color.RED.getRGB(), image.getRGB(image.getWidth() - 1, image.getHeight() - 1));
        }
    }

    @Test
    void elementsAreDrawnInOrderAndDispatchToTheirPainters() {
        ImageContainer container = new ImageContainer();
        container.setBackgroundImage(solid(30, 30, Color.RED), ZoomMode.Origin, 30, 30);
        container.addElement(rectangle(Color.BLUE));
        ImageEmbedded image = new ImageEmbedded();
        image.setImage(solid(4, 4, Color.GREEN));
        image.setX(4);
        image.setY(4);
        image.setZoomMode(ZoomMode.Origin);
        container.addElement(image);
        TextEmbedded text = new TextEmbedded();
        text.setText("MMMM");
        text.setFont(new Font("Monospaced", Font.PLAIN, 12));
        text.setX(0);
        text.setDrawY(25);
        container.addElement(text);
        BufferedImage result = container.combine();
        assertEquals(Color.RED.getRGB(), result.getRGB(0, 0));
        assertEquals(Color.BLUE.getRGB(), result.getRGB(3, 3));
        assertEquals(Color.GREEN.getRGB(), result.getRGB(5, 5));
        boolean hasText = false;
        for (int y = 12; y < 26; y++)
            for (int x = 0; x < 30; x++)
                hasText |= result.getRGB(x, y) == Color.BLACK.getRGB();
        assertTrue(hasText);
    }

    @Test
    void repeatFlagSelectsTilingInsteadOfDrawingOnlyOnce() {
        ImageContainer container = new ImageContainer();
        container.setBackgroundImage(solid(10, 10, Color.RED), ZoomMode.Origin, 10, 10);
        RectangleEmbedded tile = rectangle(Color.BLUE);
        tile.setX(0);
        tile.setY(0);
        tile.setWidth(2);
        tile.setHeight(2);
        tile.setRepeatPaddingHorizontal(2);
        tile.setRepeatPaddingVertical(2);
        tile.setRepeat(true);
        container.addElement(tile);
        BufferedImage result = container.combine();
        assertEquals(Color.BLUE.getRGB(), result.getRGB(0, 0));
        assertEquals(Color.BLUE.getRGB(), result.getRGB(4, 4));
        assertEquals(Color.RED.getRGB(), result.getRGB(2, 0));
    }

    @Test
    void changedElementPropertiesTakeEffectOnlyAfterRecombining() throws Exception {
        ImageContainer container = new ImageContainer();
        container.setOutputFormat(ImageFormat.PNG);
        container.setBackgroundImage(solid(10, 10, Color.RED), ZoomMode.Origin, 10, 10);
        RectangleEmbedded element = rectangle(Color.BLUE);
        container.addElement(element);
        container.combine();
        element.setColor(Color.GREEN);
        try (InputStream previous = container.getCombinedImageStream()) {
            assertEquals(Color.BLUE.getRGB(), ImageIO.read(previous).getRGB(3, 3));
        }
        assertEquals(Color.GREEN.getRGB(), container.combine().getRGB(3, 3));
    }

    @Test
    void pngRetainsTransparentBackgroundAndWholeImageRoundCorners() {
        ImageContainer container = new ImageContainer();
        container.setOutputFormat(ImageFormat.PNG);
        container.setBackgroundImage(new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB), ZoomMode.Origin, 20, 20);
        assertEquals(0, container.combine().getRGB(0, 0) >>> 24);
        container.setBackgroundImage(solid(20, 20, Color.RED), ZoomMode.Origin, 20, 20);
        container.setCanvasRoundCorner(12);
        BufferedImage rounded = container.combine();
        assertEquals(0, rounded.getRGB(0, 0) >>> 24);
        assertEquals(Color.RED.getRGB(), rounded.getRGB(10, 10));
        container.setCanvasRoundCorner(null);
        assertEquals(Color.RED.getRGB(), container.combine().getRGB(0, 0));
    }

    @Test
    void wholeImageRoundCornerRejectsOpaqueOutputFormat() {
        assertThrows(IllegalArgumentException.class, () -> new ImageContainer().setCanvasRoundCorner(5));
    }

    @Test
    void staticRoundCornerScalesToRequestedSizeWithoutChangingSource() {
        BufferedImage original = solid(10, 10, Color.RED);
        BufferedImage result = ImageContainer.makeRoundCorner(original, 20, 30, 12);
        assertEquals(20, result.getWidth());
        assertEquals(30, result.getHeight());
        assertEquals(0, result.getRGB(0, 0) >>> 24);
        assertEquals(Color.RED.getRGB(), result.getRGB(10, 15));
        assertEquals(Color.RED.getRGB(), original.getRGB(0, 0));
    }

    @Test
    void delegatedRoundCornersPreservePreviousPixelsForScalingAndTransparency() {
        BufferedImage source = solid(9, 7, new Color(255, 0, 0, 128));
        source.setRGB(4, 3, Color.BLUE.getRGB());
        int[] original = source.getRGB(0, 0, 9, 7, null, 0, 9);
        for (int[] size : new int[][]{{9, 7}, {18, 10}, {3, 2}}) {
            for (int radius : new int[]{0, 4, 100}) {
                // 保留旧版算法作为参考，验证重构没有改变圆角边缘及透明像素。
                BufferedImage expected = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = expected.createGraphics();
                try {
                    g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g.fillRoundRect(0, 0, size[0], size[1], radius, radius);
                    g.setComposite(java.awt.AlphaComposite.SrcIn);
                    g.drawImage(source, 0, 0, size[0], size[1], null);
                } finally {
                    g.dispose();
                }
                BufferedImage actual = ImageContainer.makeRoundCorner(source, size[0], size[1], radius);
                assertArrayEquals(expected.getRGB(0, 0, size[0], size[1], null, 0, size[0]),
                        actual.getRGB(0, 0, size[0], size[1], null, 0, size[0]));
                assertArrayEquals(original, source.getRGB(0, 0, 9, 7, null, 0, 9));
            }
        }
    }

    @Test
    void containerKeepsPositiveSizeContractWhileEffectRetainsOriginalSizeShortcut() {
        BufferedImage source = solid(9, 7, Color.RED);
        for (int[] size : new int[][]{{0, 0}, {0, 7}, {9, 0}, {-1, 7}, {9, -1}})
            assertThrows(IllegalArgumentException.class,
                    () -> ImageContainer.makeRoundCorner(source, size[0], size[1], 4));
        BufferedImage originalSize = new ImageEffect(source).roundCorner(0, 0, 4).getOutput();
        assertEquals(9, originalSize.getWidth());
        assertEquals(7, originalSize.getHeight());
    }

    @Test
    void backgroundColorAppendsAFullCanvasLayer() {
        ImageContainer container = new ImageContainer();
        container.setBackgroundImage(solid(10, 10, Color.RED), ZoomMode.Origin, 10, 10);
        container.addElement(rectangle(Color.BLUE));
        container.setBgColor(Color.GREEN);
        BufferedImage result = container.combine();
        assertEquals(Color.GREEN.getRGB(), result.getRGB(0, 0));
        assertEquals(Color.GREEN.getRGB(), result.getRGB(3, 3));
        assertEquals(Color.GREEN.getRGB(), result.getRGB(9, 9));
    }

    @Test
    void invalidLifecycleCallsFailInsteadOfReturningEmptyOutput() {
        ImageContainer container = new ImageContainer();
        assertThrows(IllegalStateException.class, container::getCombinedImageStream);
        assertThrows(IllegalArgumentException.class, container::combine);
        assertThrows(IndexOutOfBoundsException.class, () -> container.setBackgroundBlur(1));
    }
}
