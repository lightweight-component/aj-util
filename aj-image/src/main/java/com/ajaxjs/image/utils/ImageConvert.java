package com.ajaxjs.image.utils;

import com.ajaxjs.image.constant.ImageFormat;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.Iterator;

@Slf4j
public class ImageConvert extends ImageBase {
    public ImageConvert(BufferedImage source) {
        super(source);
    }

    public ImageConvert(byte[] imageBytes) {
        super(imageBytes);
    }

    public ImageConvert(File file) {
        super(file);
    }

    public ImageConvert(String file) {
        super(file);
    }

    /**
     * 图像格式
     */
    ImageFormat format;

    /**
     * 压缩质量(0.0~1.0)，超过此范围抛出异常，为 null 使用默认值
     */
    Float quality;

    public ImageConvert setImageFormat(ImageFormat format) {
        this.format = format;

        return this;
    }

    public ImageConvert setQuality(Float quality) {
        this.quality = quality;

        return this;
    }

    /**
     * 将{@link BufferedImage}生成 formatName 指定格式的图像数据
     *
     * @return 指定格式的图像数据
     */
    public byte[] convert() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        convert(output);

        return output.toByteArray();
    }

    /**
     * 将{@link BufferedImage}生成 formatName 指定格式的图像数据
     *
     * @param output 输出流
     */
    public void convert(OutputStream output) {
        Graphics2D g = null;

        try {
            // 对于某些格式的图像(如 png)，直接调用 ImageIO.write 生成 jpeg 可能会失败
            // 所以先尝试直接调用 ImageIO.write,如果失败则用 Graphics 生成新的 BufferedImage 再调用 ImageIO.write
            for (BufferedImage s = source; !convert(s, output, format.toString(), quality); ) {
                if (g != null)
                    throw new IllegalArgumentException(String.format("not found writer for '%s'", format));

                s = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
                g = s.createGraphics();
                g.drawImage(source, 0, 0, null);
            }
        } finally {
            if (g != null)
                g.dispose();
        }
    }

    /**
     * 将原图压缩生成{@code formatName}指定格式的数据<br>
     * 除了可以指定生成的图像质量之外，
     * 其他行为与{@link ImageIO#write(RenderedImage, String, OutputStream)}相同
     *
     * @param source     源图片
     * @param formatName 格式名称
     * @param output     输出流
     * @param quality    指定图像质量,为{@code null}调用{@link ImageIO#write(RenderedImage, String, OutputStream)}
     * @return 压缩成功返回{@code true}否则返回{@code false}
     */
    private static boolean convert(RenderedImage source, OutputStream output, String formatName, float quality) {
        try {
            if (quality == 0)
                return ImageIO.write(source, formatName, output);

            /*
             * Returns <code>ImageWriter</code> instance according to given
             * rendered image and image format or <code>null</code> if there
             * is no appropriate writer.
             */
            Iterator<ImageWriter> i = ImageIO.getImageWriters(ImageTypeSpecifier.createFromRenderedImage(source), formatName);
            ImageWriter writer;

            if (i.hasNext())
                writer = i.next();
            else
                return false;

            ImageOutputStream stream = ImageIO.createImageOutputStream(output);
            writer.setOutput(stream);
            ImageWriteParam param = writer.getDefaultWriteParam();

            try {
                if (param.canWriteCompressed()) {
                    try {
                        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        param.setCompressionQuality(quality);
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
            log.error("Error when converting image.", e);
            throw new UncheckedIOException(e);
        }
    }

    public void outputAsFile(String filePath) {
        try (FileOutputStream file = new FileOutputStream(filePath)) {
            file.write(convert());
        } catch (IOException e) {
            log.error("Error when saving the output image as file.", e);
            throw new UncheckedIOException(e);
        }
    }
}