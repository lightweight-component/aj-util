package com.ajaxjs.image.utils;

import com.ajaxjs.image.constant.ImageFormat;
import com.ajaxjs.image.constant.MatType;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

//@Slf4j
public class ImageHandler {
    BufferedImage source;

    public ImageHandler(BufferedImage source) {
        this.source = source;
    }

    public ImageHandler(byte[] imageBytes) {
        try {// ImageIO.read() 会自动分析字节流的文件头来确定格式
            source = ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ImageHandler(File file) {
        fileToData(file);
    }

    private void fileToData(File file) {// 将数据从文件中读取到内存
        // TODO file2bytes
    }

    public ImageHandler(String file) {
        if (file.startsWith("http")) {
            try {
                source = ImageIO.read(new URL(file));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else
            fileToData(Paths.get(file).toFile());
    }

    /**
     * 将{@link BufferedImage}生成formatName指定格式的图像数据
     *
     * @param format             图像格式名，图像格式名错误则抛出异常,可用的值 'BMP','PNG','GIF','JPEG'
     * @param compressionQuality 压缩质量(0.0~1.0),超过此范围抛出异常,为null使用默认值
     * @return 指定格式的图像数据
     */
    public byte[] writeBytes(ImageFormat format, float compressionQuality) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(format, compressionQuality, output);

        return output.toByteArray();
    }

    /**
     * 将{@link BufferedImage}生成 formatName 指定格式的图像数据
     *
     * @param format             图像格式名，图像格式名错误则抛出异常,可用的值 'BMP','PNG','GIF','JPEG'
     * @param compressionQuality 压缩质量(0.0~1.0),超过此范围抛出异常,为null使用默认值
     * @param output             输出流
     */
    public void write(ImageFormat format, float compressionQuality, OutputStream output) {
        Graphics2D g = null;

        try {
            // 对于某些格式的图像(如 png)，直接调用 ImageIO.write 生成 jpeg 可能会失败
            // 所以先尝试直接调用 ImageIO.write,如果失败则用 Graphics 生成新的 BufferedImage 再调用 ImageIO.write
            for (BufferedImage s = source; !write(s, format.toString(), output, compressionQuality); ) {
                if (null != g)
                    throw new IllegalArgumentException(String.format("not found writer for '%s'", format));

                s = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
                g = s.createGraphics();
                g.drawImage(source, 0, 0, null);
            }
        } finally {
            if (null != g)
                g.dispose();
        }
    }

    /**
     * 将原图压缩生成{@code formatName}指定格式的数据<br>
     * 除了可以指定生成的图像质量之外，
     * 其他行为与{@link ImageIO#write(RenderedImage, String, OutputStream)}相同
     *
     * @param source             源图片
     * @param formatName         格式名称
     * @param output             输出流
     * @param compressionQuality 指定图像质量,为{@code null}调用{@link ImageIO#write(RenderedImage, String, OutputStream)}
     * @return 压缩成功返回{@code true}否则返回{@code false}
     */
    public static boolean write(RenderedImage source, String formatName, OutputStream output, float compressionQuality) {
        try {
            if (compressionQuality == 0)
                return ImageIO.write(source, formatName, output);

            ImageWriter writer = getImageWriter(source, formatName);

            if (writer == null)
                return false;

            ImageOutputStream stream = ImageIO.createImageOutputStream(output);
            writer.setOutput(stream);
            ImageWriteParam param = writer.getDefaultWriteParam();

            try {
                if (param.canWriteCompressed()) {
                    try {
                        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        param.setCompressionQuality(compressionQuality);
                    } catch (RuntimeException ignored) {
                    }
                }

                writer.write(null, new IIOImage(source, null, null), param);

                return true;
            } finally {
                writer.dispose();
                stream.flush();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Returns <code>ImageWriter</code> instance according to given
     * rendered image and image format or <code>null</code> if there
     * is no appropriate writer.
     */
    private static ImageWriter getImageWriter(RenderedImage im, String formatName) {
        Iterator<ImageWriter> i = ImageIO.getImageWriters(ImageTypeSpecifier.createFromRenderedImage(im), formatName);

        if (i.hasNext())
            return i.next();
        else
            return null;
    }

    /**
     * 从源{@link BufferedImage}对象创建一份拷贝
     *
     * @param imageType 创建的{@link BufferedImage}目标对象类型
     * @return 返回拷贝的对象
     * @see BufferedImage#BufferedImage(int, int, int)
     */
    public static BufferedImage copy(Image image, int imageType) {
        BufferedImage dst = new BufferedImage(image.getWidth(null), image.getHeight(null), imageType);
        Graphics g = dst.getGraphics();

        try {
            g.drawImage(image, 0, 0, null);

            return dst;
        } finally {
            g.dispose();
        }
    }

    /**
     * 创建{@link BufferedImage#TYPE_3BYTE_BGR}类型的拷贝
     *
     * @return 返回拷贝的对象
     */
    public BufferedImage copy() {
        return copy(source, BufferedImage.TYPE_3BYTE_BGR);
    }

    /**
     * 对原图缩放，返回缩放后的新对象
     *
     * @param scale
     * @return 返回缩放后的新对象
     */
    public BufferedImage scale(double scale) {
        if (scale < 0)
            throw new IllegalArgumentException("scale must >0");

        int width = source.getWidth();
        int height = source.getHeight();
        Image image = source.getScaledInstance((int) Math.round(width * scale), (int) Math.round(height * scale), Image.SCALE_SMOOTH);

        return copy(image, BufferedImage.TYPE_3BYTE_BGR);
    }

    /**
     * 对图像进行缩放
     *
     * @param targetWidth  缩放后图像宽度
     * @param targetHeight 缩放后图像高度
     * @param constrain    为true时等比例缩放，targetWidth,targetHeight为缩放图像的限制尺寸
     * @return 缩放后的{@link BufferedImage}对象
     */
    public BufferedImage resize(int targetWidth, int targetHeight, boolean constrain) {
        if (constrain) {
            double aspectRatio = (double) source.getWidth() / source.getHeight();
            double sx = (double) targetWidth / source.getWidth();
            double sy = (double) targetHeight / source.getHeight();

            if (sx > sy)
                targetWidth = (int) Math.round(targetHeight * aspectRatio);
            else
                targetHeight = (int) Math.round(targetWidth / aspectRatio);
        }

        int type = source.getType();
        BufferedImage target;

        if (type == BufferedImage.TYPE_CUSTOM) {
            ColorModel cm = source.getColorModel();
            WritableRaster raster = cm.createCompatibleWritableRaster(targetWidth, targetHeight);
            target = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
        } else
            target = new BufferedImage(targetWidth, targetHeight, type);

        Graphics2D g = target.createGraphics();

        try {
            g.drawImage(source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null);
        } finally {
            g.dispose();
        }

        return target;
    }

    /**
     * 对原图创建缩略图对象<br>
     * 如果原图尺寸小于指定的缩略图尺寸则直接返回原图对象的副本
     *
     * @param thumbnailWidth  缩略图宽度
     * @param thumbnailHeight 缩略图高度
     * @param ratioThreshold  最大宽高比阀值(宽高中较大的值/较小的值)，<此值时对原图等比例缩放，>=此值时从原图切出中间部分图像再等比例缩放
     * @return {@link BufferedImage}对象
     */
    public BufferedImage createThumbnail(int thumbnailWidth, int thumbnailHeight, double ratioThreshold) {
        int w = source.getWidth();
        int h = source.getHeight();

        if (w < thumbnailWidth && h < thumbnailHeight) // 返回原图的副本
            return source.getSubimage(0, 0, w, h);

        double thumAspectRatio = (double) thumbnailWidth / thumbnailHeight;
        double wh_sca = w > h ? (double) w / h : (double) h / w;

        if (wh_sca >= ratioThreshold) {
            if (w > h) {
                int fw = (int) (thumAspectRatio * h);

                if (h <= thumbnailHeight)
                    return source.getSubimage((w - fw) / 2, 0, fw, h);
                else {
                    source = source.getSubimage((w - fw) / 2, 0, fw, h);
                    return resize(thumbnailWidth, thumbnailHeight, true);
                }
            } else {
                int fh = (int) (thumAspectRatio * w);

                if (w <= thumbnailWidth)
                    return source.getSubimage(0, (h - fh) / 2, w, fh);
                else {
                    source = source.getSubimage(0, (h - fh) / 2, w, fh);
                    return resize(thumbnailWidth, thumbnailHeight, true);
                }
            }
        } else
            return resize(thumbnailWidth, thumbnailHeight, true);
    }


    /**
     * @param image
     * @param bandOffset 用于判断通道顺序
     * @return
     */
    private static boolean equalBandOffsetWith3Byte(BufferedImage image, int[] bandOffset) {
        if (image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            if (image.getData().getSampleModel() instanceof ComponentSampleModel) {
                ComponentSampleModel sampleModel = (ComponentSampleModel) image.getData().getSampleModel();

                return Arrays.equals(sampleModel.getBandOffsets(), bandOffset);
            }
        }

        return false;
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
    public ImageHandler getMatrix(MatType target) {
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

    static byte[] bufferedImageToBytes(BufferedImage source, int width, int height) {
        return (byte[]) source.getData().getDataElements(0, 0, width, height, null);
    }

    byte[] to(int imageType, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, imageType);
        new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), null).filter(source, img);

        return (byte[]) img.getData().getDataElements(0, 0, width, height, null);
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

            // ARGB 转 BGR 格式
            for (int i = 0, j = 0; i < intRGB.length; ++i, j += 3) {
                matrix[j] = (byte) (intRGB[i] & 0xff);
                matrix[j + 1] = (byte) ((intRGB[i] >> 8) & 0xff);
                matrix[j + 2] = (byte) ((intRGB[i] >> 16) & 0xff);
            }
        }

        return matrix;
    }

    /**
     * 将{图像上下左右扩充指定的尺寸
     *
     * @param imageType 图片类型
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return 返回扩展尺寸后的新对象
     */
    public BufferedImage growCanvas(int imageType, int left, int top, int right, int bottom) {
        if (left < 0 || top < 0 || right < 0 || bottom < 0)
            throw new IllegalArgumentException("left,top,right,bottom must >=0");

        BufferedImage dst = new BufferedImage(source.getWidth() + left + right, source.getHeight() + top + bottom, imageType);
        Graphics g = dst.getGraphics();

        try {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, dst.getWidth(), dst.getHeight());
            g.drawImage(source, left, top, null);

            return dst;
        } finally {
            g.dispose();
        }
    }

    /**
     * 将图像上下左右扩充指定的尺寸
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return 返回扩展尺寸后的新对象
     */
    public BufferedImage growCanvas(int left, int top, int right, int bottom) {
        return growCanvas(BufferedImage.TYPE_3BYTE_BGR, left, top, right, bottom);
    }

    /**
     * 将{@link Image}图像(向右下)扩充为正文形(尺寸长宽最大边)
     *
     * @return 扩充后的{@link BufferedImage}对象
     */
    public BufferedImage growSquareCanvas() {
        int width = source.getWidth(), height = source.getHeight();
        int size = Math.max(width, height);

        return growCanvas(0, 0, size - width, size - height);
    }

    /**
     * 圆角
     *
     * @param width
     * @param height
     * @param radius
     * @return BufferedImage
     */
    public BufferedImage makeRoundCorner(int width, int height, int radius) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRoundRect(0, 0, width, height, radius, radius);
        g.setComposite(AlphaComposite.SrcIn);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();

        return image;
    }

    public byte[] output(ImageFormat format, float quality) {
        ImageWriter writer = ImageIO.getImageWritersBySuffix(format.toString().toLowerCase()).next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            writer.setOutput(new MemoryCacheImageOutputStream(os));
            writer.write(null, new IIOImage(source, null, null), param);
            writer.dispose();

            return os.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void outputAsFile(ImageFormat format, float quality, String filePath) {
        try (FileOutputStream file = new FileOutputStream(filePath)) {
            file.write(output(format, quality));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
