package com.ajaxjs.image.utils;

import com.ajaxjs.image.constant.ImageFormat;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;

@Slf4j
public abstract class ImageBase {
    /**
     * The input image
     */
    BufferedImage source;

    /**
     * The output image
     */
    BufferedImage output;

    public BufferedImage getOutput() {
        return output;
    }

    /**
     * Constructor of image handler
     *
     * @param source The input image
     */
    public ImageBase(BufferedImage source) {
        this.source = source;
    }

    /**
     * Constructor of image handler
     *
     * @param imageBytes The input image bytes
     */
    public ImageBase(byte[] imageBytes) {
        try {// ImageIO.read() 会自动分析字节流的文件头来确定格式
            source = ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (IOException e) {
            log.warn("Error when bytes to buffered image.", e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Constructor of image handler
     *
     * @param file The input image file
     */
    public ImageBase(File file) {
        fileToData(file);
    }

    /**
     * Constructor of image handler
     *
     * @param file The input image file
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
     * Constructor of image handler
     *
     * @param file The input image file
     */
    public ImageBase(String file) {
        if (file.startsWith("http")) {
            try {
                source = ImageIO.read(new URL(file));
            } catch (IOException e) {
                log.warn("Error when reading image from url: {}", file, e);
                throw new RuntimeException(e);
            }
        } else
            fileToData(Paths.get(file).toFile());
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
     * Save the output image as bytes
     *
     * @param format  The format of the image
     * @param quality The quality of the image
     * @return The output image in bytes
     */
    public byte[] output(ImageFormat format, float quality) {
        if (output == null)
            throw new NullPointerException("The output image is not ready");

        ImageWriter writer = ImageIO.getImageWritersBySuffix(format.toString().toLowerCase()).next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            writer.setOutput(new MemoryCacheImageOutputStream(os));
            writer.write(null, new IIOImage(output, null, null), param);
            writer.dispose();

            return os.toByteArray();
        } catch (IOException e) {
            log.error("Error when saving the output image as bytes.", e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Save the output image as file
     *
     * @param format   The format of the image
     * @param quality  The quality of the image
     * @param filePath The path of the output image
     */
    public void outputAsFile(ImageFormat format, float quality, String filePath) {
        try (FileOutputStream file = new FileOutputStream(filePath)) {
            file.write(output(format, quality));
        } catch (IOException e) {
            log.error("Error when saving the output image as file.", e);
            throw new UncheckedIOException(e);
        }
    }
}