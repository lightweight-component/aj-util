package com.ajaxjs.image.utils;

import com.ajaxjs.image.model.constant.ImageFormat;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * 图片读取、复制及处理结果输出的公共基类。
 * <p>source 是输入图片，output 是最近一次操作的结果。
 * 两者可能不是同一对象；构造后通常需要先调用具体处理方法，再读取或输出结果。</p>
 * <p>默认保留传入图片引用。实例持有可变状态，不应在多个线程中同时操作。</p>
 */
@Slf4j
public abstract class ImageBase {
    /**
     * 输入图片，供子类操作使用。
     * <p>通常保存传入对象的引用；部分子类操作可能读写其像素，不能假定自动执行深拷贝。</p>
     */
    BufferedImage source;

    /**
     * 最近一次处理结果；尚未处理时通常为 null。
     * <p>getOutput 返回实际图片引用，修改它会影响后续输出。</p>
     */
    @Getter
    BufferedImage output;

    /**
     * 直接使用内存图片创建处理器。
     *
     * @param source 已读取的图片，不应为 null；不会自动复制像素
     */
    public ImageBase(BufferedImage source) {
        this.source = source;
    }

    /**
     * 从图片文件字节创建处理器，ImageIO 根据文件头识别编码格式。
     *
     * @param imageBytes PNG、JPEG 等完整编码数据，不是原始像素矩阵
     * @throws IllegalArgumentException     无法识别图片格式时抛出
     * @throws java.io.UncheckedIOException 解码发生 I/O 错误时抛出
     */
    public ImageBase(byte[] imageBytes) {
        try {// ImageIO.read() 会自动分析字节流的文件头来确定格式
            source = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (source == null)
                throw new IllegalArgumentException("Unable to recognize image bytes");
        } catch (IOException e) {
            log.warn("Error when bytes to buffered image.", e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 从本地图片文件创建处理器。
     *
     * @param file 图片文件，须存在且具有受支持的编码格式
     * @throws IllegalArgumentException     无法识别图片格式时抛出
     * @throws java.io.UncheckedIOException 文件读取失败时抛出
     */
    public ImageBase(File file) {
        fileToData(file);
    }

    /**
     * 读取本地图片并保存到 source。
     *
     * @param file 待解码的本地文件
     * @throws IllegalArgumentException ImageIO 没有识别出可读取的图片时抛出
     * @throws UncheckedIOException     读取文件失败时抛出
     */
    private void fileToData(File file) {// 将数据从文件中读取到内存
        try {
            source = ImageIO.read(file);

            if (source == null)
                throw new IllegalArgumentException("Unable to read image from file: " + file.getPath());
        } catch (IOException e) {
            log.warn("Error when reading image from file: {}", file.getPath(), e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 从本地路径或以 http 开头的网络地址创建处理器。
     * <p>网络读取使用 ImageIO 的默认连接行为；本构造方法不提供超时或身份认证参数。</p>
     *
     * @param file 本地文件路径或 HTTP/HTTPS 图片地址
     * @throws IllegalArgumentException 无法识别图片格式时抛出
     * @throws RuntimeException         图片读取失败时抛出
     */
    public ImageBase(String file) {
        if (file.startsWith("http")) {
            try {
                source = ImageIO.read(new URL(file));
                if (source == null)
                    throw new IllegalArgumentException("Unable to read image from URL: " + file);
            } catch (IOException e) {
                log.warn("Error when reading image from url: {}", file, e);
                throw new RuntimeException(e);
            }
        } else
            fileToData(Paths.get(file).toFile());
    }

    /**
     * 将已加载的图片绘制到指定像素类型的新图片中。
     * <p>结果尺寸与输入相同；目标像素类型不支持透明度时，透明信息会丢失。</p>
     *
     * @param image     已加载、宽高可用的源图片
     * @param imageType BufferedImage 的像素类型常量，例如 TYPE_INT_ARGB
     * @return 像素存储独立的新图片
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
     * 复制源图片为不带透明通道的三字节 BGR 图片。
     * <p>返回副本，但不更新本对象的 output 字段。</p>
     *
     * @return 新建的 TYPE_3BYTE_BGR 图片
     */
    public BufferedImage copy() {
        return copy(source, BufferedImage.TYPE_3BYTE_BGR);
    }

    /**
     * 将最近一次处理结果编码为图片文件字节。
     * <p>应先执行产生 output 的处理操作。格式和压缩支持由 ImageIO 编码器决定；
     * JPG 等不支持透明通道的格式会在必要时使用不透明 RGB 副本编码。</p>
     *
     * @param format  目标图片格式
     * @param quality 编码质量，范围为 0 到 1，具体含义由编码器决定
     * @return 完整编码后的图片字节，可写入文件或网络响应
     * @throws NullPointerException 尚未产生 output 时抛出
     * @throws UncheckedIOException 编码发生 I/O 错误时抛出
     */
    public byte[] output(ImageFormat format, float quality) {
        if (output == null)
            throw new NullPointerException("The output image is not ready");

        return ImageEncoder.encode(output, format, quality);
    }

    /**
     * 将最近一次处理结果编码后写入本地文件。
     * <p>父目录应已存在；先完成编码，再写同目录临时文件，最后替换目标。
     * 编码或临时写入失败时不会清空已有文件。</p>
     *
     * @param format   目标图片格式
     * @param quality  编码质量，范围为 0 到 1
     * @param filePath 输出文件路径，扩展名应与格式一致
     * @throws UncheckedIOException 文件写入或编码发生 I/O 错误时抛出
     */
    public void outputAsFile(ImageFormat format, float quality, String filePath) {
        ImageEncoder.writeFile(output(format, quality), filePath);
    }
}
