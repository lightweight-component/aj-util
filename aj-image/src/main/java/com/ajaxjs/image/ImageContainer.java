package com.ajaxjs.image;

import com.ajaxjs.image.model.EmbeddedElement;
import com.ajaxjs.image.model.ImageEmbedded;
import com.ajaxjs.image.model.RectangleEmbedded;
import com.ajaxjs.image.model.TextEmbedded;
import com.ajaxjs.image.model.constant.ImageFormat;
import com.ajaxjs.image.model.constant.ZoomMode;
import com.ajaxjs.image.painter.ImagePainter;
import com.ajaxjs.image.painter.Painter;
import com.ajaxjs.image.painter.RectanglePainter;
import com.ajaxjs.image.painter.TextPainter;
import com.ajaxjs.image.utils.ImageEffect;
import com.ajaxjs.image.utils.ImageEncoder;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 按添加顺序将背景图、文字、图片和矩形合成为一张图片。
 * <p>先通过接收 {@link BufferedImage} 的背景图方法确定画布尺寸，再添加前景元素，
 * 调用 {@link #combine()} 绘制，最后输出到文件或内存流。后添加的元素覆盖在先添加的元素上方。</p>
 * <p>默认输出 JPG；透明背景和整图圆角应使用 PNG。实例保存可变的元素和合成结果，
 * 不适合在多个线程中同时使用。</p>
 */
@RequiredArgsConstructor
public class ImageContainer {
    /**
     * 待绘制的元素列表，按添加顺序从底层画到顶层。
     * <p>保存元素本身的引用；调用方在合成前修改元素属性，会影响下一次合成。</p>
     */
    private final List<EmbeddedElement> combineElements = new ArrayList<>();

    /**
     * 最近一次成功合成的图片；尚未合成成功时为 {@code null}，失败合成不替换此值。
     */
    private BufferedImage combinedImage;

    /**
     * 画布宽度，单位为像素；由背景图和缩放模式计算，合成前必须大于零。
     */
    private int canvasWidth;

    /**
     * 画布高度，单位为像素；由背景图和缩放模式计算，合成前必须大于零。
     */
    private int canvasHeight;

    /**
     * 图片输出格式，默认 {@link ImageFormat#JPG}。
     * <p>此设置同时影响合成时是否建立透明画布，应在合成前确定。</p>
     */
    @Setter
    private ImageFormat outputFormat = ImageFormat.JPG;

    /**
     * 整张图片圆角的弧宽和弧高，单位为像素；{@code null} 表示不裁剪圆角。
     * <p>沿用 Java2D 圆角矩形参数含义，并非几何半径。</p>
     */
    private Integer roundCorner;

    /**
     * 编码质量，范围为 {@code 0.0f} 到 {@code 1.0f}，默认 {@code 1.0f}。
     * <p>质量的具体含义由图片编码器决定，不影响内存中的合成像素。</p>
     */
    @Setter
    private float quality = 1f;

    /**
     * 最后设置的背景颜色。
     * <p>设置颜色会追加一个覆盖当前画布大小的矩形；绘制顺序以追加位置为准。</p>
     */
    private Color bgColor;

    /**
     * 追加一个覆盖当前画布的纯色矩形。
     * <p>请先设置背景图以确定画布尺寸。矩形追加到当前列表末尾，会盖住先添加的元素；
     * 需要保留透明背景时不要调用此方法。</p>
     *
     * @param bgColor 填充颜色，不应为 null
     */
    public void setBgColor(Color bgColor) {
        RectangleEmbedded bgElement = new RectangleEmbedded();
        bgElement.setX(0);
        bgElement.setY(0);
        bgElement.setWidth(canvasWidth);
        bgElement.setHeight(canvasHeight);
        bgElement.setColor(bgColor);
        this.bgColor = bgColor;

        combineElements.add(bgElement);
    }

    /**
     * 将图片、文字或矩形加入待绘制列表。
     * <p>保存引用而非副本，后添加的元素会绘制在更上层；此方法不立即执行绘制。</p>
     *
     * @param element 待绘制元素，应为受支持的具体子类且不为 null
     */
    public void addElement(EmbeddedElement element) {
        combineElements.add(element);
    }

    /**
     * 从本地路径或 HTTP/HTTPS 地址读取背景图，再按指定模式设置画布。
     * <p>读取成功后才添加背景；失败时保留已有元素。网络读取使用默认连接行为，
     * 不提供超时或身份认证设置，请勿直接传入不可信的地址。</p>
     *
     * @param bgImage  本地文件路径或 HTTP/HTTPS 图片地址
     * @param zoomMode 画布尺寸计算方式
     * @param width    目标宽度，单位为像素
     * @param height   目标高度，单位为像素
     * @throws IllegalArgumentException 无法识别图片格式时抛出
     * @throws UncheckedIOException     图片读取失败时抛出
     */
    public void setBackgroundImage(String bgImage, ZoomMode zoomMode, int width, int height) {
        try {
            BufferedImage image = bgImage.startsWith("http://") || bgImage.startsWith("https://")
                    ? ImageIO.read(new URL(bgImage)) : ImageIO.read(new File(bgImage));

            if (image == null)
                throw new IllegalArgumentException("Unable to read background image: " + bgImage);

            setBackgroundImage(image, zoomMode, width, height);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read background image: " + bgImage, e);
        }
    }

    /**
     * 根据背景图和缩放模式设置画布尺寸，并将缩放后的背景追加到元素列表。
     * <p>Origin 使用原图尺寸；Width 或 Height 按原图比例计算另一边；
     * WidthHeight 直接使用指定宽高。已有元素不会被清空。</p>
     *
     * @param bgImage  已读取的背景图，不应为 null
     * @param zoomMode 画布尺寸计算方式，不应为 null
     * @param width    目标宽度，Width 和 WidthHeight 模式使用，应大于零
     * @param height   目标高度，Height 和 WidthHeight 模式使用，应大于零
     */
    public void setBackgroundImage(BufferedImage bgImage, ZoomMode zoomMode, int width, int height) {
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

        ImageEmbedded imageElement = new ImageEmbedded();
        imageElement.setImage(bgImage);
        imageElement.setX(0);
        imageElement.setY(0);
        imageElement.setWidth(canvasWidth);
        imageElement.setHeight(canvasHeight);
        imageElement.setZoomMode(ZoomMode.WidthHeight);
        addElement(imageElement);
    }

    /**
     * 设置列表中第一张背景图片的模糊半径。
     * <p>要求第一个元素为图片；通常应在设置背景图后、添加其他底层元素前调用。</p>
     *
     * @param blur 模糊半径，小于 1 时不执行模糊
     * @throws IndexOutOfBoundsException 元素列表为空时抛出
     * @throws ClassCastException        第一个元素不是图片时抛出
     */
    public void setBackgroundBlur(int blur) {
        ImageEmbedded bgElement = (ImageEmbedded) combineElements.get(0);
        bgElement.setBlur(blur);
    }

    /**
     * 设置整张合成图片的圆角。
     * <p>请先将输出格式设为 PNG，以保留圆角外侧的透明区域。
     * 参数对应 Java2D 圆角矩形的弧宽和弧高。</p>
     *
     * @param roundCorner 圆角弧尺寸，单位为像素；null 表示取消整图圆角
     * @throws IllegalArgumentException 输出格式不是 PNG 时抛出
     */
    public void setCanvasRoundCorner(Integer roundCorner) {
        if (outputFormat != ImageFormat.PNG)
            throw new IllegalArgumentException("整图圆角，输出格式必须设置为 PNG");

        this.roundCorner = roundCorner;
    }

    /**
     * 按列表顺序重新绘制整张图片，全部成功后才更新合成结果。
     * <p>绘制失败时保留上次成功结果；首次合成失败时仍不可输出图片。</p>
     * <p>每次调用都会创建新的画布；必须先通过背景图确定有效尺寸。
     * 图片、文字和矩形分别交给对应 Painter 处理，元素的 repeat 属性决定是否平铺。</p>
     *
     * @return 本次合成的内存图片，返回实际对象而非副本
     * @throws IllegalArgumentException      画布宽高不合法时抛出
     * @throws UnsupportedOperationException 存在不支持的元素类型时抛出
     */
    public BufferedImage combine() {
        // 本次绘制只操作局部结果，成功后才发布，失败时保留上一次完整结果。
        BufferedImage result = new BufferedImage(canvasWidth, canvasHeight,
                outputFormat == ImageFormat.PNG ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();

        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.white);
            for (EmbeddedElement element : combineElements) {
                if (element instanceof TextEmbedded)
                    drawElement(g, (TextEmbedded) element, new TextPainter());
                else if (element instanceof ImageEmbedded)
                    drawElement(g, (ImageEmbedded) element, new ImagePainter());
                else if (element instanceof RectangleEmbedded)
                    drawElement(g, (RectangleEmbedded) element, new RectanglePainter());
                else
                    throw new UnsupportedOperationException("Unsupported Type: " + element);
            }
        } finally {
            g.dispose();
        }

        if (roundCorner != null)
            result = makeRoundCorner(result, canvasWidth, canvasHeight, roundCorner);

        combinedImage = result;
        return result;
    }

    /**
     * 把具体元素交给类型匹配的画笔，选择普通绘制或平铺。
     *
     * @param <T>     元素的具体类型，必须与 Painter 的类型参数一致
     * @param g       当前画布的绘图上下文
     * @param element 要绘制的元素
     * @param painter 能处理该元素类型的画笔
     */
    private <T extends EmbeddedElement> void drawElement(Graphics2D g, T element, Painter<T> painter) {
        if (element.isRepeat())
            painter.drawRepeat(g, element, canvasWidth, canvasHeight);
        else
            painter.draw(g, element, canvasWidth);
    }

    /**
     * 缩放源图至指定尺寸，并以圆角矩形遮罩保留内部像素。
     * <p>圆角外部为透明像素，不会修改源图。</p>
     * <p>实际处理复用 ImageEffect；此入口仍要求正尺寸，不采用宽高同时为 0 的原图尺寸约定。</p>
     *
     * @param srcImage 源图片
     * @param width    结果宽度，单位为像素，应大于零
     * @param height   结果高度，单位为像素，应大于零
     * @param radius   圆角弧宽和弧高，单位为像素，并非几何半径
     * @return 新建的带透明通道的圆角图片
     * @throws IllegalArgumentException 宽度或高度不大于零时抛出
     * @see ImageEffect#roundCorner(int, int, int)
     */
    public static BufferedImage makeRoundCorner(BufferedImage srcImage, int width, int height, int radius) {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("图片宽高必须大于 0");

        return new ImageEffect(srcImage).roundCorner(width, height, radius).getOutput();
    }

    /**
     * 将最近一次合成结果按当前格式和质量编码到内存流。
     * <p>需要先成功调用 combine；返回的是完整编码后的文件内容，并非原始像素。
     * 调用方负责关闭返回流。</p>
     *
     * @return 可从头读取图片文件内容的输入流
     * @throws RuntimeException 尚未合成成功或图片编码失败时抛出，编码原异常保留为 cause
     */
    public InputStream getCombinedImageStream() {
        if (combinedImage == null)
            throw new IllegalStateException("尚未成功完成图片合成，无法输出文件流");

        try {
            return new ByteArrayInputStream(ImageEncoder.encode(combinedImage, outputFormat, quality));
        } catch (RuntimeException e) {
            throw new RuntimeException("合成图片编码失败，无法输出文件流", e);
        }
    }

    /**
     * 将最近一次合成结果按当前格式和质量写入本地文件。
     * <p>需要先成功调用 combine，父目录须已存在；写临时文件成功后才替换目标文件。
     * 修改元素配置后，需再次 combine 才会更新保存的像素。</p>
     *
     * @param filePath 目标文件路径，扩展名应与输出格式一致
     * @throws RuntimeException 尚未合成或文件写入失败时抛出
     */
    public void save(String filePath) {
        if (combinedImage == null)
            throw new IllegalStateException("尚未成功完成图片合成，无法保存文件");

        ImageEncoder.writeFile(ImageEncoder.encode(combinedImage, outputFormat, quality), filePath);
    }
}
