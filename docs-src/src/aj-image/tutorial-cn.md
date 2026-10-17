---
title: 合成教程
layout: layouts/aj-util-cn.njk
alternate: /tutorial/
---

# 图片合成教程

本教程从一个简单的图片加文字示例开始，再逐步展示如何组合背景、商品图、头像、水印和二维码，制作一张海报。依赖配置见[首页](/aj-image/cn/)，功能说明见[图片合成](/aj-image/composition-cn/)。

## 基本使用流程

使用 `ImageContainer`：先设置输出格式和背景，再创建元素并通过 `addElement()` 添加，最后调用 `combine()`。背景决定画布大小，后添加的元素覆盖前面的元素。

`combine()` 返回 `BufferedImage`；`getCombinedImageStream()` 返回编码后的图片文件流，可交给自己的上传服务；`save()` 保存到本地。获取流不等于上传，使用后需要关闭。

配置元素时需要注意：

- 图片、文字和矩形分别使用 `ImageEmbedded`、`TextEmbedded`、`RectangleEmbedded`。
- 先创建元素，再逐项调用 setter 配置，最后通过 `addElement()` 添加到合成器。
- 图片元素需要先读取为 `BufferedImage`，并设置 `ZoomMode`；文字需要明确设置 `Font`。
- 背景字符串入口支持本地路径、HTTP/HTTPS；下面的示例辅助方法另外处理 `file:` URL，不能据此认为所有库入口都支持该协议。
- 自动拆行和字间距尚未实现；多行文字需提前拆好，并设置每行位置和样式。

## 最简单的例子

下面的方法与完整示例共用 `readImage()`、`text()` 辅助方法及 import，已包含在后面的完整类中。网址是占位地址，需要换成对应的真实素材。文字位于 Y=960，背景需足够高。

```java
public void simpleDemo() throws Exception {
    ImageContainer container = new ImageContainer();
    container.setOutputFormat(ImageFormat.JPG);
    container.setBackgroundImage(readImage("http://xxx.com/image/bg.jpg"),
            ZoomMode.Origin, 0, 0);

    ImageEmbedded product = new ImageEmbedded();
    product.setImage(readImage("http://xxx.com/image/product.png"));
    product.setX(0);
    product.setY(300);
    product.setZoomMode(ZoomMode.Origin);
    container.addElement(product);

    TextEmbedded title = text("周末大放送",
            new Font(Font.SANS_SERIF, Font.BOLD, 60), 100, 960);
    container.addElement(title);

    BufferedImage result = container.combine();
    try (InputStream input = container.getCombinedImageStream()) {
        // Pass input to your own upload service here.
    }
    // container.save("d:/image.jpg");
}
```

## 完整示例

完整类展示了背景模糊、整图圆角、商品缩放、文字样式、预拆分多行、圆形头像、平铺水印、渐变底衬、边框、二维码和价格。

- 背景按宽度缩放到 1500，使用 `ZoomMode.Width`；按高度缩放则需要使用 `Height` 并把高度传入第四个参数。缩放后画布需要足够容纳下面的元素（底衬延伸到 Y=2007）。
- 整图圆角使用 PNG。质量参数由编码器解释，PNG 是无损格式，不能将其当作 JPEG 的画质百分比。
- 标题选择水平居中，不同时设置右对齐；居中优先于方向设置。需要右对齐时关闭居中，使用 `Direction.RightLeft`，此时 X 表示右边缘。
- 内容提前分成两行，通过 Y 坐标相差 60 像素控制行距。需要按宽度拆行时，由调用方先计算分行结果。
- 圆形头像采用正方形绘制尺寸；边框与填充分开，避免设置边框后底衬变成空心。
- 水印先于正文、头像和二维码绘制，避免覆盖它们；平铺会覆盖整个画布范围，而不是只出现在给定坐标之后。
- 二维码使用独立素材，不添加模糊或透明效果；保留素材自身留白，并用最终图片实际验证扫码效果。

```java
import com.ajaxjs.image.ImageContainer;
import com.ajaxjs.image.model.ImageEmbedded;
import com.ajaxjs.image.model.RectangleEmbedded;
import com.ajaxjs.image.model.TextEmbedded;
import com.ajaxjs.image.model.constant.GradientDirection;
import com.ajaxjs.image.model.constant.ImageFormat;
import com.ajaxjs.image.model.constant.LineAlign;
import com.ajaxjs.image.model.constant.ZoomMode;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

public class ImageCompositionDemo {
    public void simpleDemo() throws Exception {
        ImageContainer container = new ImageContainer();
        container.setOutputFormat(ImageFormat.JPG);
        container.setBackgroundImage(readImage("http://xxx.com/image/bg.jpg"),
                ZoomMode.Origin, 0, 0);
    
        ImageEmbedded product = new ImageEmbedded();
        product.setImage(readImage("http://xxx.com/image/product.png"));
        product.setX(0);
        product.setY(300);
        product.setZoomMode(ZoomMode.Origin);
        container.addElement(product);
    
        TextEmbedded title = text("周末大放送",
                new Font(Font.SANS_SERIF, Font.BOLD, 60), 100, 960);
        container.addElement(title);
    
        BufferedImage result = container.combine();
        try (InputStream input = container.getCombinedImageStream()) {
            // Pass input to your own upload service here.
        }
        // container.save("d:/image.jpg");
    }

    public void demo() throws Exception {
        String bgImageUrl = "http://xxx.com/image/bg.jpg";
        String productImageUrl = "https://xxx.com/image/product.jpg";
        String qrCodeUrl = "file:///d:/qrCode.png";
        BufferedImage background = readImage(bgImageUrl);
        BufferedImage productImage = readImage(productImageUrl);
        BufferedImage qrCodeImage = readImage(qrCodeUrl);
        BufferedImage waterMark = readImage("https://xxx.com/image/waterMark.jpg");
        BufferedImage avatar = readImage("d:/avatar.jpg");

        // PNG preserves the transparent area outside rounded corners.
        ImageContainer container = new ImageContainer();
        container.setOutputFormat(ImageFormat.PNG);
        container.setBackgroundImage(background, ZoomMode.Width, 1500, 0);
        container.setBackgroundBlur(30);
        container.setCanvasRoundCorner(100);
        container.setQuality(.8f);

        ImageEmbedded product = new ImageEmbedded();
        product.setImage(productImage);
        product.setX(0);
        product.setY(160);
        product.setWidth(837);
        product.setZoomMode(ZoomMode.Width);
        product.setCenter(true);
        product.setRoundCorner(46);
        container.addElement(product);

        ImageEmbedded watermark = new ImageEmbedded();
        watermark.setImage(waterMark);
        watermark.setX(630);
        watermark.setY(1200);
        watermark.setZoomMode(ZoomMode.Origin);
        watermark.setAlpha(.8f);
        watermark.setRotate(45);
        watermark.setBlur(20);
        watermark.setRepeat(true);
        watermark.setRepeatPaddingHorizontal(100);
        watermark.setRepeatPaddingVertical(50);
        container.addElement(watermark);

        TextEmbedded title = text("# 最爱的家居",
                new Font(Font.SANS_SERIF, Font.BOLD, 60), 150, 1400);
        title.setCenter(true);
        title.setAlpha(.8f);
        title.setRotate(45);
        title.setColor(Color.RED);
        title.setAutoFitMaxWidth(200);
        title.setAutoFitMinFontSize(24);
        container.addElement(title);

        // Supply this font file in src/main/resources/font/yahei.ttf.
        Font subtitleFont = loadFont("/font/yahei.ttf", 40);
        container.addElement(text("年度狂欢", subtitleFont, 150, 1450));

        // Prepare each line explicitly: no automatic width-based wrapping.
        Font contentFont = new Font("微软雅黑", Font.BOLD, 40);
        TextEmbedded firstLine = text("苏格拉底说：“如果没有那个桌子，",
                contentFont, 150, 1510);
        firstLine.setStrikeThrough(true);
        TextEmbedded secondLine = text("可能就没有那个水壶”",
                contentFont, 150, 1570);
        secondLine.setStrikeThrough(true);
        TextEmbedded content = new TextEmbedded();
        content.setAutoBreakLine(true);
        content.setBreakLineElements(Arrays.asList(firstLine, secondLine));
        content.setLineAlign(LineAlign.Left);
        container.addElement(content);

        ImageEmbedded avatarElement = image(avatar, 200, 1200, 130, 130);
        avatarElement.setRoundCorner(200);
        container.addElement(avatarElement);

        // Fill and border must be separate elements.
        RectangleEmbedded backing = rectangle(138, 1707, 300, 300);
        backing.setRoundCorner(50);
        backing.setAlpha(.8f);
        backing.setFromColor(Color.YELLOW);
        backing.setToColor(Color.BLUE);
        backing.setGradientDirection(GradientDirection.LeftRight);
        container.addElement(backing);
        RectangleEmbedded border = rectangle(138, 1707, 300, 300);
        border.setRoundCorner(50);
        border.setColor(Color.WHITE);
        border.setBorderSize(5);
        container.addElement(border);

        // Preserve the QR asset's quiet zone; verify scanning after resizing.
        container.addElement(image(qrCodeImage, 195, 1764, 186, 186));

        TextEmbedded price = text("￥1290",
                new Font(Font.SANS_SERIF, Font.PLAIN, 60), 230, 1300);
        price.setColor(Color.RED);
        price.setStrikeThrough(true);
        container.addElement(price);

        BufferedImage result = container.combine();
        try (InputStream input = container.getCombinedImageStream()) {
            // Pass input to your own upload service here.
        }
        container.save("d:/image.png");
    }

    // These helpers belong to this example, not the library's public API.
    private static TextEmbedded text(String value, Font font, int x, int y) {
        TextEmbedded element = new TextEmbedded();
        element.setText(value);
        element.setFont(font);
        element.setX(x);
        element.setY(y);

        return element;
    }

    private static ImageEmbedded image(BufferedImage image, int x, int y,
                                       int width, int height) {
        ImageEmbedded element = new ImageEmbedded();
        element.setImage(image);
        element.setX(x);
        element.setY(y);
        element.setWidth(width);
        element.setHeight(height);
        element.setZoomMode(ZoomMode.WidthHeight);

        return element;
    }

    private static RectangleEmbedded rectangle(int x, int y, int width, int height) {
        RectangleEmbedded element = new RectangleEmbedded();
        element.setX(x);
        element.setY(y);
        element.setWidth(width);
        element.setHeight(height);

        return element;
    }

    private static BufferedImage readImage(String location) throws IOException {
        BufferedImage image;
        if (location.startsWith("http://") || location.startsWith("https://")
                || location.startsWith("file:")) {
            URLConnection connection = new URL(location).openConnection();
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(20_000);
            try (InputStream input = connection.getInputStream()) {
                image = ImageIO.read(input);
            }
        } else {
            image = ImageIO.read(new File(location));
        }
        if (image == null)
            throw new IOException("Unrecognized image: " + location);

        return image;
    }

    private static Font loadFont(String resource, float size) throws Exception {
        try (InputStream input = ImageCompositionDemo.class.getResourceAsStream(resource)) {
            if (input == null)
                throw new IOException("Missing font resource: " + resource);

            return Font.createFont(Font.TRUETYPE_FONT, input).deriveFont(size);
        }
    }
}
```

## 运行前准备

准备对应图片、`src/main/resources/font/yahei.ttf` 字体文件和输出目录。示例中的 `text()`、`image()`、`rectangle()`、`readImage()`、`loadFont()` 都是教程辅助方法，不是库内置 API。

`readImage()` 的网络超时由示例自行设置；库的网络读取入口不提供可配置超时。生产环境还需限制可访问地址、下载大小和解码尺寸，不能直接使用不可信 URL。系统字体名称取决于服务器环境，稳定显示中文时应加载有合法使用权限的字体文件。

示例按当前源码编写；占位网址、D 盘素材及字体需自行准备，编译通过不代表这些资源已经可用。

[图片合成与限制](/aj-image/composition-cn/) · [图片工具与输出](/aj-image/utilities-cn/) · [返回首页](/aj-image/cn/)
