---
title: aj-qrcode
layout: layouts/aj-util.njk
alternate: /aj-qrcode/cn/
---

# aj-qrcode

`aj-qrcode` is a dependency-free, Java 8 compatible encoder for QR Code Model 2. It is based on [Nayuki's QR Code generator](https://github.com/nayuki/QR-Code-generator), supports versions 1–40 and all four error-correction levels, and offers both a normal API and a compact `fast` encoder.

The module generates symbols only. It does not scan/decode QR codes, create web responses, write SVG files automatically, or overlay logos.

## Dependency

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-qrcode</artifactId>
    <version>1.4</version>
</dependency>
```

## Documentation map

| Guide | What it covers |
| --- | --- |
| [Getting started](/aj-qrcode/getting-started/) | Create a first QR code, select error correction, and inspect the result. |
| [PNG and SVG rendering](/aj-qrcode/rendering/) | Raster/vector output, colors, quiet zones, and safe file writing. |
| [Text, bytes, and segments](/aj-qrcode/segments/) | Byte payloads, numeric/alphanumeric modes, ECI, and efficient mixed text. |
| [Advanced encoding and limits](/aj-qrcode/advanced/) | Optimized segmentation, version/mask controls, fast implementation, and failures. |

For a basic PNG, begin with [getting started](/aj-qrcode/getting-started/). For a server-rendered document or browser asset, see [rendering](/aj-qrcode/rendering/).

[Source code](https://github.com/lightweight-component/aj-util/tree/main/aj-qrcode) · [Javadoc](https://javadoc.io/doc/com.ajaxjs/aj-qrcode)
