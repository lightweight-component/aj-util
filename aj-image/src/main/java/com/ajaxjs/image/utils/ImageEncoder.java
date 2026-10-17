package com.ajaxjs.image.utils;

import com.ajaxjs.image.model.constant.ImageFormat;
import lombok.experimental.UtilityClass;

import javax.imageio.*;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

/**
 * 图片处理器共用的编码与文件保存工具。
 * <p>统一选择图片编码器、设置压缩类型和质量，并释放编码器及内部缓存流。
 * 保存文件时先写同目录临时文件，再替换目标，避免编码或临时写入失败时破坏旧文件。</p>
 */
@UtilityClass
public class ImageEncoder {
    /**
     * 将图片编码为完整的文件字节。
     *
     * @param image   待编码的图片，不应为 null
     * @param format  目标文件格式，不应为 null
     * @param quality 编码质量，null 使用默认值，非 null 必须在 0 到 1 之间
     * @return 编码后的图片文件内容
     * @throws UncheckedIOException 图片编码失败时抛出
     */
    public static byte[] encode(BufferedImage image, ImageFormat format, Float quality) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(image, format, quality, output);

        return output.toByteArray();
    }

    /**
     * 编码图片到调用方的输出流，保留该流的所有权。
     * <p>目标格式不支持源像素类型时，尝试以不透明 RGB 副本编码。
     * 对带透明通道的 JPG、BMP 输入主动使用 RGB 副本，避免不同 JDK 的编码器判断差异。
     * 内部缓存流会关闭并刷新，但不会关闭传入的 output。</p>
     *
     * @param image   待编码的图片
     * @param format  目标编码格式
     * @param quality 可选质量，范围为 0 到 1
     * @param output  调用方持有的目标流
     * @throws IllegalArgumentException 质量非法或找不到适用编码器时抛出
     * @throws UncheckedIOException     编码或写入失败时抛出，原始异常保留为 cause
     */
    public static void write(BufferedImage image, ImageFormat format, Float quality, OutputStream output) {
        Objects.requireNonNull(image, "image");
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(output, "output");

        if (quality != null && (Float.isNaN(quality) || quality < 0 || quality > 1))
            throw new IllegalArgumentException("quality must be between 0 and 1");

        String name = format.name().toLowerCase(Locale.ROOT);
        Iterator<ImageWriter> writers = ImageIO.getImageWriters(ImageTypeSpecifier.createFromRenderedImage(image), name);

        // Java 8 的 JPEG Writer 可能接受带 alpha 的输入并写出四通道 JPEG。
        // 对明确不支持透明度的格式主动转为 RGB，不能仅依赖 Writer 的类型判断。
        if ((image.getColorModel().hasAlpha() && (format == ImageFormat.JPG || format == ImageFormat.BMP)) || !writers.hasNext()) {
            BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();

            try {
                g.drawImage(image, 0, 0, null);
            } finally {
                g.dispose();
            }

            image = rgb;
            writers = ImageIO.getImageWriters(ImageTypeSpecifier.createFromRenderedImage(image), name);
        }

        if (!writers.hasNext())
            throw new IllegalArgumentException("No image writer for " + name);

        ImageWriter writer = writers.next();

        // dispose 必须包住 setOutput、参数设置及写入，不能只处理正常路径。
        try {
            try (MemoryCacheImageOutputStream stream = new MemoryCacheImageOutputStream(output)) {
                writer.setOutput(stream);
                ImageWriteParam param = writer.getDefaultWriteParam();

                if (quality != null && param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    String[] types = param.getCompressionTypes();

                    if (types != null && types.length > 0 && param.getCompressionType() == null)
                        param.setCompressionType(types[0]);

                    param.setCompressionQuality(quality);
                }

                writer.write(null, new IIOImage(image, null, null), param);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to encode " + name + " image", e);
        } finally {
            writer.dispose();
        }
    }

    /**
     * 将已编码的数据写入临时文件，完成后替换目标文件。
     * <p>目标父目录必须存在；优先使用原子替换，不支持时回退到普通替换。
     * 失败时尝试删除临时文件，清理失败会作为附加异常保留。普通替换的故障保证由文件系统决定。</p>
     *
     * @param bytes    已完整编码的图片内容
     * @param filePath 最终保存路径
     * @throws UncheckedIOException 创建、写入或替换文件失败时抛出
     */
    public static void writeFile(byte[] bytes, String filePath) {
        Objects.requireNonNull(bytes, "bytes");
        Path target = Paths.get(filePath).toAbsolutePath();
        Path temporary = null;

        try {
            temporary = Files.createTempFile(target.getParent(), "aj-image-", ".part");
            Files.write(temporary, bytes);

            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | RuntimeException | Error failure) {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException | RuntimeException cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
            }

            if (failure instanceof IOException)
                throw new UncheckedIOException("Unable to save image to " + target, (IOException) failure);

            if (failure instanceof RuntimeException)
                throw (RuntimeException) failure;

            throw (Error) failure;
        }
    }
}
