package com.ajaxjs.image.utils;

import com.ajaxjs.image.model.constant.ImageFormat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * 将源图片编码为指定格式的图片文件数据。
 * <p>先设置目标格式，可选设置压缩质量，再调用 convert 或 outputAsFile。
 * 转换直接读取 source，不依赖 ImageBase.output；返回字节不是原始像素。</p>
 * <p>遇到目标编码器不支持的源像素类型时，会尝试转为不透明 RGB 后重试，
 * 因此转换为 JPG 等格式可能丢失透明度。</p>
 */
public class ImageConvert extends ImageBase {
    /**
     * 直接使用内存图片创建处理器。
     *
     * @param source 已读取的图片，不应为 null；不会自动复制像素
     */
    public ImageConvert(BufferedImage source) {
        super(source);
    }

    /**
     * 从图片文件字节创建处理器，ImageIO 根据文件头识别编码格式。
     *
     * @param imageBytes PNG、JPEG 等完整编码数据，不是原始像素矩阵
     * @throws java.io.UncheckedIOException 解码发生 I/O 错误时抛出
     */
    public ImageConvert(byte[] imageBytes) {
        super(imageBytes);
    }

    /**
     * 从本地图片文件创建处理器。
     *
     * @param file 图片文件，须存在且具有受支持的编码格式
     * @throws IllegalArgumentException     无法识别图片格式时抛出
     * @throws java.io.UncheckedIOException 文件读取失败时抛出
     */
    public ImageConvert(File file) {
        super(file);
    }

    /**
     * 从本地路径或以 http 开头的网络地址创建处理器。
     * <p>网络读取使用 ImageIO 的默认连接行为；本构造方法不提供超时或身份认证参数。</p>
     *
     * @param file 本地文件路径或 HTTP/HTTPS 图片地址
     * @throws RuntimeException 图片读取失败时抛出
     */
    public ImageConvert(String file) {
        super(file);
    }

    /**
     * 目标编码格式，没有默认值；转换前必须通过 setImageFormat 设置。
     */
    ImageFormat format;

    /**
     * 可选编码质量；null 使用编码器默认值，非 null 的合法范围为 0 到 1。
     * <p>0 也是明确的质量值，不代表使用默认值。</p>
     */
    Float quality;

    /**
     * 设置输出编码格式。
     *
     * @param format 输出格式，不应为 null
     * @return 当前处理器，便于继续设置质量或执行转换
     */
    public ImageConvert setImageFormat(ImageFormat format) {
        this.format = format;

        return this;
    }

    /**
     * 设置可选的编码质量。
     * <p>不同编码器对质量的解释不同；不支持压缩的编码器会忽略质量。
     * 传入 null 使用默认设置，传入 0 表示明确选择最低质量。</p>
     *
     * @param quality null 或 0 到 1 范围内的有限浮点数
     * @return 当前处理器
     * @throws IllegalArgumentException 数值越界、为 NaN 或无穷大时抛出
     */
    public ImageConvert setQuality(Float quality) {
        if (quality != null && (Float.isNaN(quality) || quality < 0 || quality > 1))
            throw new IllegalArgumentException("quality must be between 0 and 1");

        this.quality = quality;

        return this;
    }

    /**
     * 将源图片编码到内存。
     * <p>转换前需设置目标格式，源图片保持原尺寸。</p>
     *
     * @return 指定格式的完整图片文件字节
     * @throws UncheckedIOException 编码或写入内存流发生 I/O 错误时抛出
     */
    public byte[] convert() {
        return ImageEncoder.encode(source, format, quality);
    }

    /**
     * 把源图片以目标格式写入调用方的输出流。
     * <p>不负责关闭传入流；透明图片转为不支持透明度的格式时，
     * 会尝试转成不透明 RGB 后重试。</p>
     *
     * @param output 接收编码后图片内容的输出流，由调用方管理生命周期
     * @throws IllegalArgumentException 找不到适用的图片编码器时抛出
     * @throws UncheckedIOException     图片编码或输出失败时抛出
     */
    public void convert(OutputStream output) {
        ImageEncoder.write(source, format, quality, output);
    }

    /**
     * 转换源图片并写入本地文件。
     * <p>目标格式由 setImageFormat 设置，不根据路径扩展名自动推断。
     * 父目录须存在；先写临时文件，成功后替换同名目标，编码或临时写入失败不会清空旧文件。</p>
     *
     * @param filePath 目标路径，扩展名应与设置的格式一致
     * @throws UncheckedIOException 文件创建、编码或写入发生 I/O 错误时抛出
     */
    public void outputAsFile(String filePath) {
        // 编码成功后才开始文件写入，失败不会提前清空已有目标。
        ImageEncoder.writeFile(convert(), filePath);
    }
}
