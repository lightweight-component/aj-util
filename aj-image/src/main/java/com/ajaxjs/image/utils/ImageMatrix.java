package com.ajaxjs.image.utils;

import com.ajaxjs.image.model.constant.MatType;
import lombok.Getter;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.util.Arrays;

/**
 * 在图片与逐像素字节矩阵之间转换。
 * <p>矩阵按行排列，颜色值通过字节保存；它与 PNG、JPEG 文件字节不是同一种数据。
 * 先提取矩阵，再按需要裁剪或创建图片。内部保存最近一次矩阵和图片结果，不适合并发操作。</p>
 */
public class ImageMatrix extends ImageBase {
    /**
     * 直接使用内存图片创建处理器。
     *
     * @param source 已读取的图片，不应为 null；不会自动复制像素
     */
    public ImageMatrix(BufferedImage source) {
        super(source);
    }

    /**
     * 从图片文件字节创建处理器，ImageIO 根据文件头识别编码格式。
     *
     * @param imageBytes PNG、JPEG 等完整编码数据，不是原始像素矩阵
     * @throws java.io.UncheckedIOException 解码发生 I/O 错误时抛出
     */
    public ImageMatrix(byte[] imageBytes) {
        super(imageBytes);
    }

    /**
     * 从本地图片文件创建处理器。
     *
     * @param file 图片文件，须存在且具有受支持的编码格式
     * @throws IllegalArgumentException     无法识别图片格式时抛出
     * @throws java.io.UncheckedIOException 文件读取失败时抛出
     */
    public ImageMatrix(File file) {
        super(file);
    }

    /**
     * 从本地路径或以 http 开头的网络地址创建处理器。
     * <p>网络读取使用 ImageIO 的默认连接行为；本构造方法不提供超时或身份认证参数。</p>
     *
     * @param file 本地文件路径或 HTTP/HTTPS 图片地址
     * @throws RuntimeException 图片读取失败时抛出
     */
    public ImageMatrix(String file) {
        super(file);
    }

    /**
     * 最近一次提取的像素字节，初始为 null。
     * <p>长度和通道顺序由提取格式决定；getMatrix 返回实际数组，创建图片时可能直接共享此数组。</p>
     */
    @Getter
    byte[] matrix;

    /**
     * 将源图转换为指定的像素矩阵并保存到 matrix。
     * <p>通过无参数 getMatrix 取得结果数组；RGB、BGR、RGBA、GRAY 分别对应
     * 每像素 3、3、4、1 个字节。RGBA 为非预乘颜色，透明度不会提前乘入 RGB。
     * NV21 尚未实现。</p>
     *
     * @param target 目标通道格式，不应为 null
     * @return 当前处理器，供继续读取或处理 matrix
     * @throws UnsupportedOperationException 请求尚未支持的格式时抛出
     */
    public ImageMatrix getMatrix(MatType target) {
        int width = source.getWidth(), height = source.getHeight();

        switch (target) {
            case BGR:
                matrix = getMatrixBGR();
                break;
            case RGB:
                if (equalBandOffsetWith3Byte(source, new int[]{2, 1, 0}))
                    matrix = bufferedImageToBytes(source, width, height);
                else
                    matrix = to(BufferedImage.TYPE_3BYTE_BGR, width, height);
                break;
            case RGBA:
                // getRGB 会把预乘颜色还原成普通颜色，避免调用方再次应用透明度后变暗。
                int[] rgba = source.getRGB(0, 0, width, height, null, 0, width);
                matrix = new byte[rgba.length * 4];
                for (int i = 0; i < rgba.length; i++) {
                    matrix[i * 4] = (byte) (rgba[i] >>> 16);
                    matrix[i * 4 + 1] = (byte) (rgba[i] >>> 8);
                    matrix[i * 4 + 2] = (byte) rgba[i];
                    matrix[i * 4 + 3] = (byte) (rgba[i] >>> 24);
                }
                break;
            case GRAY:
                if (source.getType() == BufferedImage.TYPE_BYTE_GRAY)
                    matrix = bufferedImageToBytes(source, width, height);
                else
                    matrix = to(BufferedImage.TYPE_BYTE_GRAY, width, height);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported matrix format: " + target);
        }

        return this;
    }

    /**
     * 提取蓝、绿、红顺序的三通道像素数据，同时更新 matrix。
     * <p>每个像素占 3 字节，不保留透明度。颜色分量可通过 {@code value & 0xff} 转为无符号整数。</p>
     *
     * @return 按行排列、长度为宽乘高乘 3 的 BGR 字节数组
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
     * 判断源图是否为三字节 BGR 类型且通道偏移与期望值一致。
     * <p>偏移描述各颜色分量在样本存储中的位置，不应直接当作 getDataElements 的输出顺序。</p>
     *
     * @param image      待检查的图片
     * @param bandOffset 期望的通道偏移数组
     * @return 类型和通道偏移均匹配时为 true
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

                /*
                 获取图像的实际通道偏移量数组并与传入的 bandOffset 进行比较
                 通道偏移量定义了颜色分量在内存中的排列顺序
                 例如：对于 BGR 图像，通常偏移量为 [0, 1, 2]，表示蓝色在第 0 位，绿色在第 1 位，红色在第 2 位
                 */
                return Arrays.equals(sampleModel.getBandOffsets(), bandOffset);
            }
        }

        return false; // 如果图像不是 TYPE_3BYTE_BGR 类型或不是 ComponentSampleModel 实例，则返回 false
    }


    /**
     * 提取指定矩形区域的像素数据元素。
     * <p>结果由图像采样模型定义，不能只凭 BufferedImage 类型名称推断原始内存字节顺序。</p>
     *
     * @param source 使用字节型像素存储的源图
     * @param width  从左上角读取的区域宽度，单位为像素
     * @param height 从左上角读取的区域高度，单位为像素
     * @return 由 Raster 导出的像素字节数组
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
     * 将源图颜色转换到指定像素类型，再提取字节数据。
     *
     * @param imageType 目标字节型 BufferedImage 类型
     * @param width     目标宽度，应与源图宽度一致
     * @param height    目标高度，应与源图高度一致
     * @return 转换后的像素数据元素
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

    /**
     * 检查裁剪矩形是否完全位于父矩形内。
     *
     * @param parent    可读取的矩阵范围
     * @param argParent 父范围的名称，用于错误信息
     * @param sub       要裁剪的范围
     * @param argSub    裁剪范围的名称，用于错误信息
     * @throws IllegalArgumentException 裁剪区域未完全包含在父范围内时抛出
     */
    private static void assertContains(Rectangle parent, String argParent, Rectangle sub, String argSub) {
        if (!parent.contains(sub))
            throw new IllegalArgumentException(String.format("the %s(X%d,Y%d,W%d,H%d) not contained by %s(X%d,Y%d,W%d,H%d)",
                    argSub, sub.x, sub.y, sub.width, sub.height, argParent, parent.x, parent.y, parent.width, parent.height));
    }

    /**
     * 从当前 RGB 或 BGR 三通道矩阵中复制一个矩形区域。
     * <p>应先提取每像素 3 字节的矩阵；本方法不能用于灰度或 RGBA 数据。
     * 不更新 matrix，完整区域或 null 裁剪区域会直接返回原数组。</p>
     *
     * @param matrixRect 整个矩阵的尺寸范围，当前索引计算要求原点为 0、0
     * @param rect       裁剪范围，使用原矩阵坐标；null 表示完整区域
     * @return 裁剪字节数组，完整区域时为原数组引用
     * @throws IllegalArgumentException 裁剪范围超出父范围时抛出
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
     * 将当前 RGB 三通道矩阵包装为不透明图片，并更新 output。
     * <p>矩阵为 null 时创建空白图片；非 null 时数组须容纳指定尺寸的像素。
     * 图片可能共享矩阵数组，修改一方的数据会影响另一方。</p>
     *
     * @param width  图片宽度，单位为像素，应大于零
     * @param height 图片高度，单位为像素，应大于零
     * @return 当前处理器，output 为生成的图片
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
     * 以每像素 4 字节的 RGBA 矩阵创建图片，但当前实现丢弃透明度。
     * <p>仅使用红、绿、蓝分量，结果为不透明图片，不能用于透明图片的无损恢复。
     * 矩阵为 null 时分配空白存储，否则可能与生成图片共享数组。</p>
     *
     * @param width  图片宽度，单位为像素，应大于零
     * @param height 图片高度，单位为像素，应大于零
     * @return 当前处理器，output 为生成的不透明图片
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
