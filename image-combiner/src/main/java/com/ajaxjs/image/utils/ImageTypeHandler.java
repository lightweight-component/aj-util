package com.ajaxjs.image.utils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Iterator;

public class ImageTypeHandler {
    ImageInfo imageInfo;

    byte[] imgBytes;

    private ImageReader imageReader;

    private MemoryCacheImageInputStream imgInput;

    public ImageTypeHandler(byte[] imgBytes, String suffix) {
        this.imgBytes = imgBytes;
        initImgReader(suffix);// 取图像基本信息，检查图像数据有效性

        try {
            imageInfo = new ImageInfo(); // 可以在不将图像全部解码加载到内存而获取图像的基本信息
            imageInfo.setSuffix(imageReader.getFormatName().trim().toLowerCase());
            imageInfo.setMime(URLConnection.guessContentTypeFromName("x." + suffix));
            imageInfo.setWidth(imageReader.getWidth(0));
            imageInfo.setHeight(imageReader.getHeight(0));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ImageInfo getImageInfo() {
        return imageInfo;
    }

    void initImgReader(String suffix) {
        Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(suffix);

        if (it.hasNext())
            imageReader = it.next();
        else
            throw new IllegalStateException(String.format("invalid suffix %s", suffix));

        if (imageReader.getInput() == null) {
            imgInput = new MemoryCacheImageInputStream(new ByteArrayInputStream(imgBytes));
            imageReader.setInput(imgInput, true, true);
        }
    }

    /**
     * @param param 图像读取参数对象
     */
    public BufferedImage read(ImageReadParam param) {
        BufferedImage b;

        try {
            b = imageReader.read(0, param == null ? imageReader.getDefaultReadParam() : param);

            if (imgInput != null)
                imgInput.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            imageReader.dispose();
        }

        return b;
    }

    /**
     * 对图像数据指定的区域解码
     *
     * @param rect            解码区域对象, 默认({@code null})全图解码<br>
     *                        参见 {@link ImageReadParam#setSourceRegion(Rectangle)}
     * @param destinationType 目标图像的所需图像类型,默认为 null, <br>
     *                        例如用此参数可以在解码时指定输出的图像类型为 RGB,<br>
     *                        如下代码生成 destinationType 参数对象：<br>
     *                        {@code
     *                        ImageTypeSpecifier destinationType=ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_3BYTE_BGR);
     *                        }<br>
     *                        用上面的 ImageTypeSpecifier 对象来调用此方法返回的 BufferedImage 对象中的 raster 成员（通过 BufferedImage.getData() 获取）
     *                        的 getDataElements() 方法返回的就是包含 RGB 数据 byte[] 类型的数组<br>
     *                        而直接用 BufferedImage.getRGB() 方式只能获取 ARGB 类型的 int[] 数组 参见
     */
    public BufferedImage read(Rectangle rect, ImageTypeSpecifier destinationType) {
        ImageReadParam param = imageReader.getDefaultReadParam();

        if (rect != null && !rect.equals(getRectangle()))
            param.setSourceRegion(rect);

        param.setDestinationType(destinationType);

        if (destinationType != null)
            param.setDestination(destinationType.createBufferedImage(imageInfo.getWidth(), imageInfo.getHeight()));

        return read(param);
    }

    /**
     * 获取图像矩形对象
     *
     * @return rectangle
     */
    public Rectangle getRectangle() {
        return new Rectangle(0, 0, imageInfo.getWidth(), imageInfo.getHeight());
    }

    byte[] matrix;

    /**
     * 从RGB格式图像矩阵数据创建一个 BufferedImage
     *
     * @param matrixRGB RGB 格式图像矩阵数据,为 null 则创建一个指定尺寸的空图像
     * @param width     宽度
     * @param height    高度
     * @return {@link BufferedImage}对象
     */
    public BufferedImage createRGBImage(int width, int height) {
        int bytePerPixel = 3;
//        Assert.isTrue(null == matrixRGB || matrixRGB.length == width * height * bytePerPixel, "invalid image argument");
        DataBufferByte dataBuffer = null == matrix ? null : new DataBufferByte(matrix, matrix.length);
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] bOffs = {0, 1, 2};
        ComponentColorModel colorModel = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        WritableRaster raster = null != dataBuffer
                ? Raster.createInterleavedRaster(dataBuffer, width, height, width * bytePerPixel, bytePerPixel, bOffs, null)
                : Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, width * bytePerPixel, bytePerPixel, bOffs, null);

        return new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
    }

    /**
     * 从RGBA格式图像矩阵数据创建一个BufferedImage<br>
     * 该方法删除了alpha通道
     *
     * @param matrixRGBA RGBA 格式图像矩阵数据,为 null 则创建一个指定尺寸的空图像
     * @param width      宽度
     * @param height     高度
     * @return {@link BufferedImage}对象
     */
    public BufferedImage createRGBAImage(int width, int height) {
        int bytePerPixel = 4;
//        Assert.isTrue(null == matrixRGBA || matrixRGBA.length == width * height * bytePerPixel, "invalid image argument");
        DataBufferByte dataBuffer = null == matrix ? null : new DataBufferByte(matrix, matrix.length);
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] bOffs = {0, 1, 2};
        ComponentColorModel colorModel = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        WritableRaster raster = null != dataBuffer
                ? Raster.createInterleavedRaster(dataBuffer, width, height, width * bytePerPixel, bytePerPixel, bOffs, null)
                : Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, width * bytePerPixel, bytePerPixel, bOffs, null);

        return new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
    }

    static void assertContains(Rectangle parent, String argParent, Rectangle sub, String argSub) {
        if (!parent.contains(sub))
            throw new IllegalArgumentException(String.format("the %s(X%d,Y%d,W%d,H%d) not contained by %s(X%d,Y%d,W%d,H%d)",
                    argSub, sub.x, sub.y, sub.width, sub.height, argParent, parent.x, parent.y, parent.width, parent.height));
    }

    /**
     * 从 3byte(RGB/BGR) 图像矩阵中截取 rect 指定区域的子矩阵
     *
     * @param matrixRect 矩阵尺寸
     * @param rect       截取区域
     * @return 截取的图像矩阵数据
     */
    public byte[] cutMatrix(Rectangle matrixRect, Rectangle rect) {
        if ((rect == null || rect.equals(matrixRect))) // 解码区域为 null 或与图像尺寸相等时直接返回 matrix
            return matrix;

        // 如果指定的区域超出图像尺寸，则抛出异常
        assertContains(matrixRect, "srcRect", rect, "rect");
        byte[] dstArray = new byte[rect.width * rect.height * 3];

        // 从 matrix 中复制指定区域的图像数据返回
        for (int dstIndex = 0, srcIndex = (rect.y * matrixRect.width + rect.x) * 3, y = 0;
             y < rect.height;
             ++y, srcIndex += matrixRect.width * 3, dstIndex += rect.width * 3)
            System.arraycopy(matrix, srcIndex, dstArray, dstIndex, rect.width * 3);  // 调用 System.arrayCopy每次复制一行数据

        return dstArray;
    }
}
