---
title: 快速开始
layout: layouts/aj-util-cn.njk
alternate: /aj-qrcode/getting-started/
---

# 快速开始

`QrCode.encodeText()` 是默认入口。它会选择可容纳数据的最小二维码版本；若不增大版本，也可能提高请求的纠错等级。

```java
import com.ajaxjs.qr_code.Ecc;
import com.ajaxjs.qr_code.QrCode;
import com.ajaxjs.qr_code.Utils;

QrCode qr = QrCode.encodeText("https://example.com/welcome?from=qr", Ecc.MEDIUM);
Utils.writePng(Utils.toImage(qr, 10, 4), "welcome.png");
```

`QrCode` 是不可变对象。公共字段 `version`、`size`、`errorCorrectionLevel`、`mask` 描述最终二维码；模块边长公式为 `version * 4 + 17`，范围 21–177。接入其他 UI 工具时可调用 `getModule(x, y)` 读取黑白网格；越界坐标返回 `false`（白色），所以不应把它当作坐标校验。

## 纠错等级

| 等级 | 大致恢复能力 | 常见场景 |
| --- | ---: | --- |
| `Ecc.LOW` | 7% | 干净、短时电子屏展示 |
| `Ecc.MEDIUM` | 15% | 普通链接和常规印刷 |
| `Ecc.QUARTILE` | 25% | 印刷或扫码条件较不稳定 |
| `Ecc.HIGH` | 30% | 小尺寸实体标签或充分测试的遮挡 |

更高纠错会减少容量或增大二维码。百分比只是大致码字恢复能力，不代表可安全遮挡的 Logo 面积。应使用真实扫码器、打印工艺、背景和距离验证；纠错无法弥补低对比度、安静区不足或模块过小。

下一步可阅读 [PNG 与 SVG 渲染](/aj-qrcode/rendering-cn/)，或了解[文本与分段](/aj-qrcode/segments-cn/)。
