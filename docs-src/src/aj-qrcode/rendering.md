---
title: PNG and SVG rendering
layout: layouts/aj-util.njk
alternate: /aj-qrcode/rendering-cn/
---

# PNG and SVG rendering

`Utils.toImage(qr, scale, border)` renders a `QrCode` as `BufferedImage`. `scale` is pixels per module and must be positive; `border` is the light quiet-zone width in modules and must be non-negative. A border of four is the usual interoperable choice.

```java
QrCode qr = QrCode.encodeText("Invoice #2026-031", Ecc.QUARTILE);
BufferedImage image = Utils.toImage(qr, 8, 4, 0xFFFFFF, 0x17365D);
Utils.writePng(image, "invoice-qr.png");
```

The colors are 24-bit `0xRRGGBB`. Ensure strong contrast. `writePng()` delegates to `ImageIO.write()` and can throw `IOException`; choose, validate, and authorize output locations in application code.

## SVG

`toSvgString()` returns XML rather than writing a file. SVG preserves sharp module edges at any scale and is usually suitable for documents or browser assets.

```java
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

String svg = Utils.toSvgString(qr, 4, "#FFFFFF", "#111827");
Files.write(Paths.get("invoice-qr.svg"), svg.getBytes(StandardCharsets.UTF_8));
```

The SVG method inserts CSS color strings into its output; use fixed trusted values, not unsanitized request parameters. The helpers do not embed logos or modify encoded data. For custom canvas, PDF, or a `QrCodeFast` result, iterate `getModule(x, y)` and implement rendering in the caller.

Return to [getting started](/aj-qrcode/getting-started/) or read [advanced encoding](/aj-qrcode/advanced/).
