---
title: Composition tutorial
layout: layouts/aj-util.njk
alternate: /tutorial-cn/
---

# Image composition tutorial

Start with a simple image-and-text example, then combine a background, product photo, avatar, watermark, and QR image into a poster. See the [home page](/aj-image/) for Maven setup and [image composition](/aj-image/composition/) for supported features.

## Basic workflow

Create an `ImageContainer`, set its output format and background, configure elements, add them with `addElement()`, and call `combine()`. The background determines canvas dimensions. Later elements cover earlier ones.

`combine()` returns a `BufferedImage`; `getCombinedImageStream()` provides encoded file data for your upload service; `save()` writes a local file. Obtaining a stream does not upload it. Close the stream after use.

When configuring elements:

- Use `ImageEmbedded`, `TextEmbedded`, and `RectangleEmbedded` for images, text, and rectangles.
- Create an element, configure it with individual setter calls, then add it with `addElement()`.
- Image elements require an already loaded `BufferedImage` and an explicit `ZoomMode`. Text requires an explicit `Font`.
- The string background overload accepts local paths and HTTP/HTTPS. The example helper additionally handles `file:` URLs; this does not imply every library entry point supports that protocol.
- Automatic text wrapping and character spacing are not implemented. Prepare multiline text and each line's position and style yourself.

## Minimal example

This method shares imports and the `readImage()` and `text()` helpers with the complete class below, which already includes it. Replace placeholder URLs with the corresponding real assets. The background must be tall enough for text at Y=960.

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

## Complete example

The class covers background blur, canvas corners, product scaling, text styles, prepared multiline text, a circular avatar, tiled watermarks, gradient backing, a border, a QR image, and a price.

- The background is scaled to width 1500 using `ZoomMode.Width`. For height-based scaling, use `Height` and supply the target height as the fourth argument. The resulting canvas must fit the elements; the backing extends to Y=2007.
- Whole-canvas corners require PNG. Quality is encoder-specific; PNG is lossless and its quality setting is not a JPEG visual-quality percentage.
- The title uses horizontal centring, not simultaneous right anchoring. Centring takes priority. For right anchoring, disable centring and use `Direction.RightLeft`; X then denotes the right edge.
- Content is prepared as two lines spaced 60 pixels apart by their Y coordinates. For width-based wrapping, calculate the line breaks in application code first.
- The avatar uses square drawing dimensions. Separate border and fill elements keep the backing filled.
- The watermark is added before foreground text, avatar, and QR image so it does not cover them. Tiling covers the canvas rather than starting only after the configured position.
- Keep the QR image separate, without blur or opacity effects. Preserve its quiet zone and verify scanning against the final output.

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

## Before running

Provide the corresponding images, `src/main/resources/font/yahei.ttf`, and the output directory. `text()`, `image()`, `rectangle()`, `readImage()`, and `loadFont()` are example helpers, not library APIs.

The example sets network timeouts in `readImage()`; the library's network-loading entry points do not expose configurable timeouts. Production applications must also restrict accessible URLs, download sizes, and decoded dimensions. Do not accept untrusted URLs directly. Named system fonts depend on the server environment; load a properly licensed font file for consistent Chinese rendering.

The example follows current source signatures. Placeholder URLs, local drive files, and the font must be supplied; successful compilation does not establish their availability.

[Composition and limits](/aj-image/composition/) · [Image utilities and output](/aj-image/utilities/) · [Home](/aj-image/)
