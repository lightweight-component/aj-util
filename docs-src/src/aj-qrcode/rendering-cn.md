---
title: PNG 与 SVG 渲染
layout: layouts/aj-util-cn.njk
alternate: /aj-qrcode/rendering/
---

# PNG 与 SVG 渲染

`Utils.toImage(qr, scale, border)` 将 `QrCode` 渲染为 `BufferedImage`。`scale` 是每模块像素边长，必须大于零；`border` 是四周浅色安静区的模块数，必须非负。通常建议使用四模块边框。

```java
QrCode qr = QrCode.encodeText("Invoice #2026-031", Ecc.QUARTILE);
BufferedImage image = Utils.toImage(qr, 8, 4, 0xFFFFFF, 0x17365D);
Utils.writePng(image, "invoice-qr.png");
```

颜色使用 `0xRRGGBB` 形式的 24 位值，应保证足够对比度。`writePng()` 内部调用 `ImageIO.write()`，可能抛出 `IOException`；输出路径应由应用自行选择、校验和授权。

## SVG

`toSvgString()` 返回 XML 字符串而不是自动写文件。SVG 可在任意缩放下保持模块锐利，适合文档和浏览器资源。

```java
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

String svg = Utils.toSvgString(qr, 4, "#FFFFFF", "#111827");
Files.write(Paths.get("invoice-qr.svg"), svg.getBytes(StandardCharsets.UTF_8));
```

该方法会把 CSS 颜色字符串直接放进 SVG；请使用固定可信值，不要直接使用请求参数。工具方法不嵌入 Logo，也不会改变编码内容。Canvas、PDF 或 `QrCodeFast` 结果请通过 `getModule(x, y)` 由调用方自行渲染。

返回[快速开始](/aj-qrcode/getting-started-cn/)，或阅读[高级编码](/aj-qrcode/advanced-cn/)。
