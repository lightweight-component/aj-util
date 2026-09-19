---
title: aj-qrcode
layout: layouts/aj-util-cn.njk
alternate: /aj-qrcode/
---

# aj-qrcode

`aj-qrcode` 是一个兼容 Java 8、无第三方运行时依赖的 QR Code Model 2 编码器。实现基于 [Nayuki QR Code generator](https://github.com/nayuki/QR-Code-generator)，支持版本 1–40、四种纠错等级，并提供常规 API 与更紧凑的 `fast` 编码器。

该模块只负责**生成**二维码，不提供扫描/解码、Web 响应输出、自动写入 SVG 文件或 Logo 叠加功能。

## 依赖

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-qrcode</artifactId>
    <version>1.4</version>
</dependency>
```

## 文档导航

| 文档 | 内容 |
| --- | --- |
| [快速开始](/aj-qrcode/getting-started-cn/) | 生成首个二维码、选择纠错等级、读取结果信息。 |
| [PNG 与 SVG 渲染](/aj-qrcode/rendering-cn/) | 位图/矢量输出、颜色、安静区和安全写文件。 |
| [文本、字节与分段](/aj-qrcode/segments-cn/) | 字节负载、数字/字母数字模式、ECI 和混合文本压缩。 |
| [高级编码与限制](/aj-qrcode/advanced-cn/) | 优化分段、版本/掩码控制、fast 实现与异常。 |

生成 PNG 请从[快速开始](/aj-qrcode/getting-started-cn/)阅读；面向网页或文档输出请看 [PNG 与 SVG 渲染](/aj-qrcode/rendering-cn/)。

[源代码](https://github.com/lightweight-component/aj-util/tree/main/aj-qrcode) · [Javadoc](https://javadoc.io/doc/com.ajaxjs/aj-qrcode)
