//package com.ajaxjs.image;
//
//import com.ajaxjs.image.constant.ImageFormat;
//import com.ajaxjs.image.utils.ImageHandler;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.awt.*;
//import java.awt.image.BufferedImage;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@DisplayName("ImageHandler.makeRoundCorner 方法测试")
//class ImageHandlerMakeRoundCornerTest {
//    private BufferedImage testImage;
//    private ImageHandler imageHandler;
//
//    @BeforeEach
//    void setUp() {
//        // 创建一个 100x50 的红色测试图片
//        testImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = testImage.createGraphics();
//        g.setColor(Color.RED);
//        g.fillRect(0, 0, 100, 50);
//        g.dispose();
//
//        imageHandler = new ImageHandler(testImage);
//    }
//
//    @Test
//    @DisplayName("测试正常参数 - 标准圆角")
//    void testMakeRoundCornerWithNormalParameters() {
//        BufferedImage result = imageHandler.makeRoundCorner(100, 50, 10);
//
//        assertNotNull(result, "返回的图片不应为 null");
//        assertEquals(100, result.getWidth(), "宽度应为 100");
//        assertEquals(50, result.getHeight(), "高度应为 50");
//        assertEquals(BufferedImage.TYPE_INT_ARGB, result.getType(), "图片类型应为 ARGB");
//
//        imageHandler.outputAsFile(ImageFormat.PNG, 0.8f, "c:/temp/temp.png");
//    }
//
//    @Test
//    @DisplayName("测试大圆角半径 - 接近半圆")
//    void testMakeRoundCornerWithLargeRadius() {
//        BufferedImage result = imageHandler.makeRoundCorner(100, 50, 50);
//
//        assertNotNull(result);
//        assertEquals(100, result.getWidth());
//        assertEquals(50, result.getHeight());
//        imageHandler.outputAsFile(ImageFormat.PNG, 0.8f, "c:/temp/temp.png");
//    }
//
//    @Test
//    @DisplayName("测试不同宽高比 - 横向矩形")
//    void testMakeRoundCornerWithWideRectangle() {
//        BufferedImage wideImage = new BufferedImage(200, 50, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = wideImage.createGraphics();
//        g.setColor(Color.GREEN);
//        g.fillRect(0, 0, 200, 50);
//        g.dispose();
//
//        ImageHandler handler = new ImageHandler(wideImage);
//        BufferedImage result = handler.makeRoundCorner(200, 50, 15);
//
//        assertNotNull(result);
//        assertEquals(200, result.getWidth());
//        assertEquals(50, result.getHeight());
//
//        handler.outputAsFile(ImageFormat.PNG, 0.8f, "c:/temp/temp.png");
//    }
//
//    @Test
//    @DisplayName("测试不同宽高比 - 纵向矩形")
//    void testMakeRoundCornerWithTallRectangle() {
//        BufferedImage tallImage = new BufferedImage(50, 200, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = tallImage.createGraphics();
//        g.setColor(Color.YELLOW);
//        g.fillRect(0, 0, 50, 200);
//        g.dispose();
//
//        ImageHandler handler = new ImageHandler(tallImage);
//        BufferedImage result = handler.makeRoundCorner(50, 200, 10);
//
//        assertNotNull(result);
//        assertEquals(50, result.getWidth());
//        assertEquals(200, result.getHeight());
//    }
//
//    @Test
//    @DisplayName("测试负数宽度 - 应抛出异常")
//    void testMakeRoundCornerWithNegativeWidth() {
//        assertThrows(IllegalArgumentException.class, () -> {
//            imageHandler.makeRoundCorner(-100, 50, 10);
//        }, "负数宽度应抛出 IllegalArgumentException");
//    }
//
//    @Test
//    @DisplayName("测试透明度 - 返回图片应支持透明通道")
//    void testMakeRoundCornerTransparency() {
//        BufferedImage result = imageHandler.makeRoundCorner(100, 50, 10);
//
//        assertEquals(BufferedImage.TYPE_INT_ARGB, result.getType(), "返回的图片类型应为 ARGB，支持透明通道");
//
//        // 检查角落是否有透明像素（圆角效果）
//        int cornerPixel = result.getRGB(0, 0);
//        int alpha = (cornerPixel >> 24) & 0xff;
//        assertTrue(alpha < 255, "角落像素应该有透明度（圆角效果）");
//    }
//
//    @Test
//    @DisplayName("测试抗锯齿渲染")
//    void testMakeRoundCornerAntiAliasing() {
//        BufferedImage result = imageHandler.makeRoundCorner(100, 50, 10);
//
//        assertNotNull(result);
//
//        // 检查边缘是否有平滑过渡（抗锯齿效果）
//        boolean hasSmoothEdge = false;
//        for (int x = 0; x < 10 && !hasSmoothEdge; x++) {
//            for (int y = 0; y < 10 && !hasSmoothEdge; y++) {
//                int pixel = result.getRGB(x, y);
//                int alpha = (pixel >> 24) & 0xff;
//                if (alpha > 0 && alpha < 255) {
//                    hasSmoothEdge = true;
//                }
//            }
//        }
//
//        assertTrue(hasSmoothEdge, "圆角边缘应该有抗锯齿的平滑过渡");
//    }
//
//    @Test
//    @DisplayName("测试多次调用的一致性")
//    void testMakeRoundCornerConsistency() {
//        BufferedImage result1 = imageHandler.makeRoundCorner(100, 50, 10);
//        BufferedImage result2 = imageHandler.makeRoundCorner(100, 50, 10);
//
//        assertEquals(result1.getWidth(), result2.getWidth());
//        assertEquals(result1.getHeight(), result2.getHeight());
//        assertEquals(result1.getType(), result2.getType());
//
//        // 比较所有像素是否相同
//        for (int x = 0; x < result1.getWidth(); x++) {
//            for (int y = 0; y < result1.getHeight(); y++) {
//                assertEquals(result1.getRGB(x, y), result2.getRGB(x, y), String.format("像素 (%d, %d) 应该相同", x, y));
//            }
//        }
//    }
//
//    @Test
//    @DisplayName("测试源图为 null 的情况")
//    void testMakeRoundCornerWithNullSource() {
//        ImageHandler handler = new ImageHandler((BufferedImage) null);
//
//        assertThrows(NullPointerException.class, () -> {
//            handler.makeRoundCorner(100, 50, 10);
//        }, "源图为 null 时应抛出 NullPointerException");
//    }
//
//    @Test
//    @DisplayName("测试极大圆角半径")
//    void testMakeRoundCornerWithVeryLargeRadius() {
//        BufferedImage result = imageHandler.makeRoundCorner(100, 50, 1000);
//
//        assertNotNull(result);
//        assertEquals(100, result.getWidth());
//        assertEquals(50, result.getHeight());
//    }
//}
