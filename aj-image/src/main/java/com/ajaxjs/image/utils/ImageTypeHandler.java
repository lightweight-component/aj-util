package com.ajaxjs.image.utils;

import com.ajaxjs.image.constant.ImageInfo;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Iterator;

/**
 * 图像格式处理和信息提取工具类 - 提供对图像文件的高效解析、格式转换和区域解码功能
 * 主要作用
 * 1. 图像信息获取
 *
 *     无需完全解码图像即可获取基本信息
 *     提取图像格式、尺寸、MIME类型等元数据
 *     支持各种常见图像格式（JPEG、PNG、GIF等）
 *
 * 2. 智能图像解码
 *
 *     支持按指定区域解码图像（只解码感兴趣的部分）
 *     可以转换目标图像类型（如转为特定的 BufferedImage.TYPE）
 *     节省内存，提高处理效率
 *
 * 3. 格式转换支持
 *
 *     在解码过程中可以指定目标图像类型
 *     支持将图像转换为不同的颜色模型和数据格式
 *     便于后续的图像处理操作
 *
 * 4. 资源管理
 *
 *     自动管理图像读取器资源
 *     正确关闭输入流，防止内存泄漏
 *     实现了高效的缓存机制
 *
 * 应用场景
 *
 *     大图像处理：只需处理图像部分区域时，避免全图加载
 *     格式标准化：将不同格式图像统一转换为目标格式
 *     图像预览：快速获取图像基本信息用于显示
 *     批量处理：高效处理大量图像文件
 *
 * 技术优势
 *
 *     使用 Java I/O API 的高级特性，性能优异
 *     支持渐进式解码，内存占用低
 *     提供灵活的解码参数配置
 *
 * 这是一个专门为图像处理优化的工具类，特别适合需要高性能图像处理的应用场景。
 */
public class ImageTypeHandler {
    ImageInfo imageInfo;

    byte[] imgBytes;

    private final ImageReader imageReader;

    private MemoryCacheImageInputStream imgInput;

    public ImageTypeHandler(byte[] imgBytes, String suffix) {
        this.imgBytes = imgBytes;
        // 取图像基本信息，检查图像数据有效性
        Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(suffix);

        if (it.hasNext())
            imageReader = it.next();
        else
            throw new IllegalStateException(String.format("invalid suffix %s", suffix));

        if (imageReader.getInput() == null) {
            imgInput = new MemoryCacheImageInputStream(new ByteArrayInputStream(imgBytes));
            imageReader.setInput(imgInput, true, true);
        }

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
    private Rectangle getRectangle() {
        return new Rectangle(0, 0, imageInfo.getWidth(), imageInfo.getHeight());
    }
}