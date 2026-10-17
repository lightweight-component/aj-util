package com.ajaxjs.image.utils;

import com.ajaxjs.image.model.constant.ImageInfo;
import lombok.Getter;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Objects;

/**
 * 读取图片尺寸、格式信息，并按完整区域或指定区域解码第一帧。
 * <p>构造时通过格式名选择 ImageIO 读取器并读取头部，调用 read 时才解码像素。
 * 可指定目标像素类型，但具体支持范围由读取器决定。</p>
 * <p>read 完成后会释放读取器，实例按一次性读取使用；不要复用同一实例多次读取，
 * 也不要在多个线程间共享。只读取元数据时，请使用 try-with-resources 或主动 close。</p>
 */
public class ImageTypeHandler implements Closeable {
    /**
     * 构造阶段从图片头部取得的信息；MIME 类型按传入后缀推测。
     */
    @Getter
    ImageInfo imageInfo;

    /**
     * 输入的完整图片文件字节，保存调用方数组引用，不应在读取期间修改。
     */
    byte[] imgBytes;

    /**
     * 本次图片格式对应的读取器；完成一次 read 后会被释放。
     */
    private final ImageReader imageReader;

    /**
     * 包装输入字节的内存缓存流，供读取器定位图片数据。
     */
    private MemoryCacheImageInputStream imgInput;

    /**
     * 使用已知的图片格式名读取头部信息。
     *
     * @param imgBytes 完整编码图片字节，不是像素矩阵
     * @param suffix   ImageIO 支持的格式名或后缀，例如 png、jpg，不带点号
     * @throws IllegalStateException 找不到对应格式的读取器时抛出
     * @throws RuntimeException      读取图片头部失败时抛出
     */
    public ImageTypeHandler(byte[] imgBytes, String suffix) {
        this.imgBytes = Objects.requireNonNull(imgBytes, "imgBytes");
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(Objects.requireNonNull(suffix, "suffix"));
        if (!readers.hasNext())
            throw new IllegalStateException("invalid suffix " + suffix);
        imageReader = readers.next();

        try {
            imgInput = new MemoryCacheImageInputStream(new ByteArrayInputStream(imgBytes));
            imageReader.setInput(imgInput, true, true);
            imageInfo = new ImageInfo();
            imageInfo.setSuffix(imageReader.getFormatName().trim().toLowerCase(java.util.Locale.ROOT));
            imageInfo.setMime(URLConnection.guessContentTypeFromName("x." + suffix));
            imageInfo.setWidth(imageReader.getWidth(0));
            imageInfo.setHeight(imageReader.getHeight(0));
        } catch (IOException e) {
            UncheckedIOException failure = new UncheckedIOException("Unable to read image header", e);
            closeAfterFailure(failure);
            throw failure;
        } catch (RuntimeException | Error failure) {
            closeAfterFailure(failure);
            throw failure;
        }
    }

    /**
     * 按读取参数解码第一帧，结束后释放读取器。
     * <p>实例仅适合一次读取；需要再次解码时应创建新实例。</p>
     *
     * @param param 读取参数，null 时使用读取器默认值
     * @return 解码后的图片
     * @throws RuntimeException      图片解码或输入流关闭失败时抛出
     * @throws IllegalStateException 已关闭或已经完成一次解码时抛出
     */
    public BufferedImage read(ImageReadParam param) {
        ensureOpen();
        try (ImageTypeHandler ignored = this) {
            return imageReader.read(0, param == null ? imageReader.getDefaultReadParam() : param);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to decode image", e);
        }
    }

    /**
     * 读取第一帧的指定区域，可同时指定输出像素类型。
     * <p>只设置源区域与目标类型，由读取器按实际区域分配结果图片；
     * 不会额外创建原图尺寸的空白画布。目标类型必须被所选读取器支持。</p>
     *
     * @param rect            要读取的源图区域，单位为像素；null 或完整范围表示整图
     * @param destinationType 目标像素类型，null 使用默认类型
     * @return 解码结果，尺寸由有效源区域决定
     * @throws RuntimeException      区域、类型不兼容或解码失败时抛出
     * @throws IllegalStateException 已关闭或已经完成一次解码时抛出
     */
    public BufferedImage read(Rectangle rect, ImageTypeSpecifier destinationType) {
        ensureOpen();
        // 参数配置也可能失败，因此与解码一起放入资源保护范围。
        try (ImageTypeHandler ignored = this) {
            ImageReadParam param = imageReader.getDefaultReadParam();
            if (rect != null && !rect.equals(getRectangle()))
                param.setSourceRegion(rect);
            param.setDestinationType(destinationType);
            return imageReader.read(0, param);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to decode image region", e);
        }
    }

    /**
     * 取得覆盖原始图片的矩形。
     *
     * @return 原点为 0、0，宽高为原图尺寸的矩形
     */
    private Rectangle getRectangle() {
        return new Rectangle(0, 0, imageInfo.getWidth(), imageInfo.getHeight());
    }

    /**
     * 是否已释放读取器和输入流；关闭后只能读取已有元数据。
     */
    private boolean closed;

    /**
     * 释放本实例的读取器及输入流，可重复调用。
     *
     * @throws IOException 输入流关闭失败时抛出
     */
    @Override
    public void close() throws IOException {
        if (closed)
            return;
        closed = true;
        try {
            imageReader.dispose();
        } finally {
            if (imgInput != null)
                imgInput.close();
        }
    }

    /**
     * 拒绝在资源已释放后继续解码。
     *
     * @throws IllegalStateException 实例已关闭时抛出
     */
    private void ensureOpen() {
        if (closed)
            throw new IllegalStateException("Image reader is already closed");
    }

    /**
     * 构造失败时释放部分创建的资源，同时保留最初的异常。
     *
     * @param failure 最初的读取或初始化异常
     */
    private void closeAfterFailure(Throwable failure) {
        try {
            close();
        } catch (IOException | RuntimeException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }
}
