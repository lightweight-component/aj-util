package com.freewayso.image.combiner;

import com.ajaxjs.image.constant.enums.OutputFormat;
import com.ajaxjs.image.constant.enums.ZoomMode;
import com.freewayso.image.combiner.element.CombineElement;
import com.freewayso.image.combiner.element.ImageElement;
import com.freewayso.image.combiner.element.RectangleElement;
import com.freewayso.image.combiner.element.TextElement;
import com.freewayso.image.combiner.painter.IPainter;
import com.freewayso.image.combiner.painter.PainterFactory;
import lombok.Data;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImageCombiner {
    private final List<CombineElement<?>> combineElements = new ArrayList<>();   //待绘制的元素集合

    /**
     * 合成后的图片对象
     */
    private BufferedImage combinedImage;
    private final int canvasWidth;                                            //画布宽度
    private final int canvasHeight;                                           //画布高度
    private final OutputFormat outputFormat;                                  //输出图片格式
    private Integer roundCorner;                                        //画布圆角（针对整图）

    /**
     * 图片保存质量（0.0 ~ 1.0）
     */
    private Float quality = 1f;

    /**
     * @param canvasWidth  画布宽
     * @param canvasHeight 画布高
     * @param outputFormat 输出图片格式
     */
    public ImageCombiner(int canvasWidth, int canvasHeight, OutputFormat outputFormat) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.outputFormat = outputFormat;
    }

    /**
     * @param canvasWidth  画布宽
     * @param canvasHeight 画布高
     * @param bgColor      画布颜色（如果需要透明背景，不要设这个参数，比方图片边缘是圆角的场景）
     * @param outputFormat 输出图片格式
     */
    public ImageCombiner(int canvasWidth, int canvasHeight, Color bgColor, OutputFormat outputFormat) {
        this(canvasWidth, canvasHeight, outputFormat);
        RectangleElement bgElement = new RectangleElement(0, 0, canvasWidth, canvasHeight);
        bgElement.setColor(bgColor);
        combineElements.add(bgElement);
    }

    /**
     * @param bgImageUrl   背景图片地址（画布以背景图宽高为基准）
     * @param outputFormat 输出图片格式
     */
    public ImageCombiner(String bgImageUrl, OutputFormat outputFormat) {
        this(getBufferedImage(bgImageUrl), outputFormat);
    }

    static BufferedImage getBufferedImage(String url) {
        try {
            return ImageIO.read(new URL(url));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param bgImage      背景图片对象（画布以背景图宽高为基准）
     * @param outputFormat 输出图片格式
     */
    public ImageCombiner(BufferedImage bgImage, OutputFormat outputFormat) {
        this(bgImage.getWidth(), bgImage.getHeight(), outputFormat);
        combineElements.add(new ImageElement(bgImage, 0, 0));
    }

    /**
     * @param bgImageUrl   背景图片地址
     * @param width        背景图宽度
     * @param height       背景图高度
     * @param zoomMode     缩放模式
     * @param outputFormat 输出图片格式
     */
    public ImageCombiner(String bgImageUrl, int width, int height, ZoomMode zoomMode, OutputFormat outputFormat) {
        this(getBufferedImage(bgImageUrl), width, height, zoomMode, outputFormat);
    }

    /**
     * @param bgImage      背景图片对象
     * @param width        背景图宽度
     * @param height       背景图高度
     * @param zoomMode     缩放模式
     * @param outputFormat 输出图片格式
     */
    public ImageCombiner(BufferedImage bgImage, int width, int height, ZoomMode zoomMode, OutputFormat outputFormat) {
        ImageElement bgImageElement = new ImageElement(bgImage, 0, 0, width, height, zoomMode);
        int canvasWidth = 0, canvasHeight = 0;

        switch (zoomMode) {
            case Origin:
                canvasWidth = bgImage.getWidth();
                canvasHeight = bgImage.getHeight();
                break;
            case Width:
                canvasWidth = width;
                canvasHeight = bgImage.getHeight() * canvasWidth / bgImage.getWidth();
                break;
            case Height:
                canvasHeight = height;
                canvasWidth = bgImage.getWidth() * canvasHeight / bgImage.getHeight();
                break;
            case WidthHeight:
                canvasHeight = height;
                canvasWidth = width;
                break;
        }

        this.combineElements.add(bgImageElement);
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.outputFormat = outputFormat;
    }

    /**
     * 合成图片，返回图片对象
     */
    public BufferedImage combine() {
        combinedImage = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = combinedImage.createGraphics();

        if (outputFormat == OutputFormat.PNG) {  // PNG要做透明度处理，否则背景图透明部分会变黑
            combinedImage = g.getDeviceConfiguration().createCompatibleImage(canvasWidth, canvasHeight, Transparency.TRANSLUCENT);
            g = combinedImage.createGraphics();
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // 抗锯齿
        g.setColor(Color.white);

        for (CombineElement<?> element : combineElements) { // 循环绘制各元素
            IPainter painter = PainterFactory.createInstance(element);

            if (element.isRepeat())  //平铺绘制
                painter.drawRepeat(g, element, canvasWidth, canvasHeight);
            else
                painter.draw(g, element, canvasWidth);// 正常绘制
        }

        g.dispose();

        if (roundCorner != null)   // 处理整图圆角
            combinedImage = makeRoundCorner(combinedImage, canvasWidth, canvasHeight, roundCorner);

        return combinedImage;
    }

    /**
     * 设置背景高斯模糊（毛玻璃效果）
     */
    public void setBackgroundBlur(int blur) {
        ImageElement bgElement = (ImageElement) combineElements.get(0);
        bgElement.setBlur(blur);
    }

    /**
     * 设置画布圆角（针对整图）
     *
     * @param roundCorner
     */
    public void setCanvasRoundCorner(Integer roundCorner) {
        if (outputFormat != OutputFormat.PNG)
            throw new IllegalArgumentException("整图圆角，输出格式必须设置为PNG");

        this.roundCorner = roundCorner;
    }

    /**
     * 获取合成后的图片流
     */
    public InputStream getCombinedImageStream() {
        if (combinedImage == null)
            throw new RuntimeException("尚未执行图片合成，无法输出文件流");

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageWriter writer = ImageIO.getImageWritersBySuffix(outputFormat.getName()).next();
            ImageWriteParam param = writer.getDefaultWriteParam();

            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }

            writer.setOutput(new MemoryCacheImageOutputStream(os));
            writer.write(null, new IIOImage(combinedImage, null, null), param);
            writer.dispose();

            return new ByteArrayInputStream(os.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("执行图片合成失败，无法输出文件流");
        }
    }

    /**
     * 保存合成后的图片
     *
     * @param filePath 完整保存路径，如 “d://123.jpg”
     */
    public void save(String filePath) {
        if (combinedImage == null)
            throw new RuntimeException("尚未执行图片合成，无法输出文件流");

        ImageWriter writer = ImageIO.getImageWritersBySuffix(outputFormat.getName()).next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            writer.setOutput(new MemoryCacheImageOutputStream(os));
            writer.write(null, new IIOImage(combinedImage, null, null), param);
            writer.dispose();

            try (FileOutputStream file = new FileOutputStream(filePath)) {
                file.write(os.toByteArray());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加元素（图片或文本）
     *
     * @param element 图片或文本元素
     */
    public void addElement(CombineElement element) {
        combineElements.add(element);
    }

    /**
     * 添加图片元素
     *
     * @param imgUrl 图片url
     * @param x      x坐标
     * @param y      y坐标
     * @return ImageElement
     */
    public ImageElement addImageElement(String imgUrl, int x, int y) {
        ImageElement imageElement = new ImageElement(imgUrl, x, y);
        combineElements.add(imageElement);

        return imageElement;
    }

    /**
     * 添加图片元素
     *
     * @param image 图片对象
     * @param x     x坐标
     * @param y     y坐标
     * @return ImageElement
     */
    public ImageElement addImageElement(BufferedImage image, int x, int y) {
        ImageElement imageElement = new ImageElement(image, x, y);
        combineElements.add(imageElement);

        return imageElement;
    }

    /**
     * 添加图片元素
     *
     * @param imgUrl   图片rul
     * @param x        x坐标
     * @param y        y坐标
     * @param width    宽度
     * @param height   高度
     * @param zoomMode 缩放模式
     * @return ImageElement
     */
    public ImageElement addImageElement(String imgUrl, int x, int y, int width, int height, ZoomMode zoomMode) {
        ImageElement imageElement = new ImageElement(imgUrl, x, y, width, height, zoomMode);
        combineElements.add(imageElement);

        return imageElement;
    }

    /**
     * 添加图片元素
     *
     * @param image    图片对象
     * @param x        x坐标
     * @param y        y坐标
     * @param width    宽度
     * @param height   高度
     * @param zoomMode 缩放模式
     * @return ImageElement
     */
    public ImageElement addImageElement(BufferedImage image, int x, int y, int width, int height, ZoomMode zoomMode) {
        ImageElement imageElement = new ImageElement(image, x, y, width, height, zoomMode);
        combineElements.add(imageElement);

        return imageElement;
    }

    /**
     * 添加文本元素
     *
     * @param text 文本
     * @param font Font对象
     * @param x    x坐标
     * @param y    y坐标
     * @return TextElement
     */
    public TextElement addTextElement(String text, Font font, int x, int y) {
        TextElement textElement = new TextElement(text, font, x, y);
        combineElements.add(textElement);

        return textElement;
    }

    /**
     * 添加文本元素
     *
     * @param text     文本
     * @param fontSize 字体大小
     * @param x        x坐标
     * @param y        y坐标
     * @return textElement
     */
    public TextElement addTextElement(String text, int fontSize, int x, int y) {
        TextElement textElement = new TextElement(text, fontSize, x, y);
        combineElements.add(textElement);

        return textElement;
    }

    /**
     * 添加文本元素
     *
     * @param text      文本
     * @param fontStyle 字体样式
     * @param fontSize  字体大小
     * @param x         x坐标
     * @param y         y坐标
     * @return textElement
     */
    public TextElement addTextElement(String text, int fontStyle, int fontSize, int x, int y) {
        TextElement textElement = new TextElement(text, fontStyle, fontSize, x, y);
        combineElements.add(textElement);

        return textElement;
    }

    /**
     * 添加文本元素
     *
     * @param text           文本
     * @param fontNameOrPath 字体名称
     * @param fontSize       字体大小
     * @param x              x坐标
     * @param y              y坐标
     * @return textElement
     */
    public TextElement addTextElement(String text, String fontNameOrPath, int fontSize, int x, int y) {
        TextElement textElement = new TextElement(text, fontNameOrPath, fontSize, x, y);
        combineElements.add(textElement);

        return textElement;
    }

    /**
     * 添加文本元素
     *
     * @param text           文本
     * @param fontNameOrPath 字体名称
     * @param fontStyle      字体样式
     * @param fontSize       字体大小
     * @param x              x坐标
     * @param y              y坐标
     * @return textElement
     */
    public TextElement addTextElement(String text, String fontNameOrPath, int fontStyle, int fontSize, int x, int y) {
        TextElement textElement = new TextElement(text, fontNameOrPath, fontStyle, fontSize, x, y);
        combineElements.add(textElement);

        return textElement;
    }

    /**
     * 添加矩形元素
     *
     * @param x      x坐标
     * @param y      y坐标
     * @param width  宽度
     * @param height 高度
     * @return RectangleElement
     */
    public RectangleElement addRectangleElement(int x, int y, int width, int height) {
        RectangleElement rectangleElement = new RectangleElement(x, y, width, height);
        combineElements.add(rectangleElement);

        return rectangleElement;
    }

    /**
     * 圆角
     *
     * @param srcImage
     * @param width
     * @param height
     * @param radius
     * @return BufferedImage
     */
    public static BufferedImage makeRoundCorner(BufferedImage srcImage, int width, int height, int radius) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillRoundRect(0, 0, width, height, radius, radius);
        g.setComposite(AlphaComposite.SrcIn);
        g.drawImage(srcImage, 0, 0, width, height, null);
        g.dispose();

        return image;
    }

}