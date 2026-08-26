package com.ajaxjs.image.utils;

import com.ajaxjs.image.constant.MatType;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.util.Arrays;

public class ImageMatrix extends ImageBase {
    public ImageMatrix(BufferedImage source) {
        super(source);
    }

    public ImageMatrix(byte[] imageBytes) {
        super(imageBytes);
    }

    public ImageMatrix(File file) {
        super(file);
    }

    public ImageMatrix(String file) {
        super(file);
    }

    /**
     * 图像的矩阵数据
     */
    byte[] matrix;

    public byte[] getMatrix() {
        return matrix;
    }

    /**
     * 对图像解码返回指定的格式矩阵数据
     *
     * @return 指定的格式矩阵数据
     */
    public ImageMatrix getMatrix(MatType target) {
        int width = source.getWidth(), height = source.getHeight();

        switch (target) {
            case RGB:
                if (equalBandOffsetWith3Byte(source, new int[]{2, 1, 0}))
                    matrix = bufferedImageToBytes(source, width, height);
                else
                    matrix = to(BufferedImage.TYPE_3BYTE_BGR, width, height);
                break;
            case RGBA:
                if (source.getType() == BufferedImage.TYPE_4BYTE_ABGR || source.getType() == BufferedImage.TYPE_4BYTE_ABGR_PRE)
                    matrix = bufferedImageToBytes(source, width, height);
                else
                    matrix = to(BufferedImage.TYPE_4BYTE_ABGR, width, height);
                break;
            case GRAY:
                if (source.getType() == BufferedImage.TYPE_BYTE_GRAY)
                    matrix = bufferedImageToBytes(source, width, height);
                else
                    matrix = to(BufferedImage.TYPE_BYTE_GRAY, width, height);
                break;
        }

        return this;
    }

    /**
     * 对图像解码返回 BGR 格式矩阵数据
     *
     * @return BGR 格式矩阵数据
     */
    public byte[] getMatrixBGR() {
        int width = source.getWidth(), height = source.getHeight();

        if (equalBandOffsetWith3Byte(source, new int[]{0, 1, 2}))
            matrix = bufferedImageToBytes(source, width, height);
        else {
            // ARGB 格式图像数据
            int[] intRGB = source.getRGB(0, 0, width, height, null, 0, width);
            matrix = new byte[width * height * 3];

            for (int i = 0, j = 0; i < intRGB.length; ++i, j += 3) { // ARGB 转 BGR 格式
                matrix[j] = (byte) (intRGB[i] & 0xff);
                matrix[j + 1] = (byte) ((intRGB[i] >> 8) & 0xff);
                matrix[j + 2] = (byte) ((intRGB[i] >> 16) & 0xff);
            }
        }

        return matrix;
    }

    /**
     * 判断给定的 BufferedImage 是否为 3 字节 BGR 类型，并且其通道偏移量与指定的 bandOffset 数组相匹配
     *
     * @param image      要检查的 BufferedImage 对象
     * @param bandOffset 用于判断通道顺序的偏移量数组，例如 [0, 1, 2] 表示 BGR 顺序，[2, 1, 0] 表示 RGB 顺序
     * @return 如果图像类型为 TYPE_3BYTE_BGR 且其通道偏移量与提供的 bandOffset 匹配则返回 true，否则返回 false
     */
    private static boolean equalBandOffsetWith3Byte(BufferedImage image, int[] bandOffset) {
        // 检查图像类型是否为 3 字节 BGR 格式 (TYPE_3BYTE_BGR)
        // TYPE_3BYTE_BGR 是一种存储格式，每个像素用 3 个字节表示，按蓝色、绿色、红色顺序排列
        if (image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            // 检查图像的数据采样模型是否为 ComponentSampleModel
            // ComponentSampleModel 是 SampleModel 的子类，用于表示组件样本模型
            if (image.getData().getSampleModel() instanceof ComponentSampleModel) {
                // 将采样模型转换为 ComponentSampleModel 以便访问其特定方法
                ComponentSampleModel sampleModel = (ComponentSampleModel) image.getData().getSampleModel();

                // 获取图像的实际通道偏移量数组并与传入的 bandOffset 进行比较
                // 通道偏移量定义了颜色分量在内存中的排列顺序
                // 例如：对于 BGR 图像，通常偏移量为 [0, 1, 2]，表示蓝色在第 0 位，绿色在第 1 位，红色在第 2 位
                return Arrays.equals(sampleModel.getBandOffsets(), bandOffset);
            }
        }

        return false; // 如果图像不是 TYPE_3BYTE_BGR 类型或不是 ComponentSampleModel 实例，则返回 false
    }


    /**
     * 将 BufferedImage 对象转换为字节数组
     *
     * @param source 要转换的 BufferedImage 源图像
     * @param width  要提取数据的区域宽度（通常应与图像宽度相同）
     * @param height 要提取数据的区域高度（通常应与图像高度相同）
     * @return 包含图像像素数据的字节数组，字节顺序和格式取决于图像的 SampleModel
     */
    private static byte[] bufferedImageToBytes(BufferedImage source, int width, int height) {
        /*
         从图像的 Raster 数据中获取原始像素数据元素
         getDataElements 方法将指定矩形区域的像素数据复制到字节数组中
         参数说明：
         - x=0, y=0: 起始坐标位置（左上角）
         - width, height: 要获取数据的矩形区域尺寸
         - obj=null: 输出对象，null 表示创建新的字节数组来存储数据

         返回值包含的是原始像素数据，其格式和顺序取决于图像的 ColorModel 和 SampleModel
         例如，对于 TYPE_3BYTE_BGR 类型的图像，每个像素会包含 B、G、R 三个字节
         */
        return (byte[]) source.getData().getDataElements(0, 0, width, height, null);
    }

    /**
     * 将源图像转换为指定类型并返回其原始像素数据的字节数组
     *
     * @param imageType 目标图像类型（如 BufferedImage.TYPE_INT_RGB, TYPE_3BYTE_BGR 等）
     * @param width     目标图像的宽度
     * @param height    目标图像的高度
     * @return 包含转换后图像像素数据的字节数组
     */
    private byte[] to(int imageType, int width, int height) {
        // 创建一个新的 BufferedImage 对象，具有指定的宽度、高度和图像类型
        // 这个新图像将作为颜色转换的目标图像
        BufferedImage img = new BufferedImage(width, height, imageType);

        // 创建一个 ColorConvertOp 对象，用于执行颜色空间转换操作
        // ColorConvertOp 是一个颜色转换操作类，可以执行颜色空间转换和渲染提示
        // 这里将源图像的颜色空间转换为 sRGB 颜色空间
        // ColorSpace.getInstance(ColorSpace.CS_sRGB) 获取标准 sRGB 颜色空间实例
        // 第二个参数 null 表示使用默认的 RenderingHints
        new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), null).filter(source, img);

        // 从转换后的图像中提取原始像素数据
        // getDataElements 方法获取指定矩形区域（整个图像）的原始数据元素
        // 返回值是一个包含像素数据的字节数组，格式由目标图像类型决定
        return bufferedImageToBytes(img, width, height);
    }

    private static void assertContains(Rectangle parent, String argParent, Rectangle sub, String argSub) {
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

        assertContains(matrixRect, "srcRect", rect, "rect");// 如果指定的区域超出图像尺寸，则抛出异常
        byte[] dstArray = new byte[rect.width * rect.height * 3];

        // 从 matrix 中复制指定区域的图像数据返回
        for (int dstIndex = 0, srcIndex = (rect.y * matrixRect.width + rect.x) * 3, y = 0;
             y < rect.height;
             ++y, srcIndex += matrixRect.width * 3, dstIndex += rect.width * 3)
            System.arraycopy(matrix, srcIndex, dstArray, dstIndex, rect.width * 3);  // 调用 System.arrayCopy 每次复制一行数据

        return dstArray;
    }

    /**
     * 从 RGB 格式图像矩阵数据创建一个 BufferedImage
     * matrix 此时应为 RGB 格式图像矩阵数据,为 null 则创建一个指定尺寸的空图像

     * @param width     宽度
     * @param height    高度
     */
    public ImageMatrix createRGBImage(int width, int height) {
        int bytePerPixel = 3;
//        Assert.isTrue(null == matrixRGB || matrixRGB.length == width * height * bytePerPixel, "invalid image argument");
        DataBufferByte dataBuffer = null == matrix ? null : new DataBufferByte(matrix, matrix.length);
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] bOffs = {0, 1, 2};
        ComponentColorModel colorModel = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        WritableRaster raster = null != dataBuffer
                ? Raster.createInterleavedRaster(dataBuffer, width, height, width * bytePerPixel, bytePerPixel, bOffs, null)
                : Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, width * bytePerPixel, bytePerPixel, bOffs, null);

        output = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);

        return this;
    }

    /**
     * 从 RGBA 格式图像矩阵数据创建一个 BufferedImage
     * 该方法删除了 alpha 通道
     * matrix 此时应为 RGBA 格式图像矩阵数据，为 null 则创建一个指定尺寸的空图像
     *
     * @param width  宽度
     * @param height 高度
     */
    public ImageMatrix createRGBAImage(int width, int height) {
        int bytePerPixel = 4;
//        Assert.isTrue(null == matrixRGBA || matrixRGBA.length == width * height * bytePerPixel, "invalid image argument");
        DataBufferByte dataBuffer = matrix == null ? null : new DataBufferByte(matrix, matrix.length);
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] bOffs = {0, 1, 2};
        ComponentColorModel colorModel = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        WritableRaster raster = dataBuffer != null
                ? Raster.createInterleavedRaster(dataBuffer, width, height, width * bytePerPixel, bytePerPixel, bOffs, null)
                : Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, width * bytePerPixel, bytePerPixel, bOffs, null);

        output = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);

        return this;
    }
}
