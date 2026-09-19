---
title: 高级编码与限制
layout: layouts/aj-util-cn.njk
alternate: /aj-qrcode/advanced/
---

# 高级编码与限制

`QrSegmentAdvanced.makeSegmentsOptimally()` 会在数字、字母数字、UTF-8 字节和 Kanji 模式之间搜索，在指定纠错等级、版本范围内最小化编码位数。它适合较长的日文/ASCII 混合内容。

```java
import com.ajaxjs.qr_code.QrSegmentAdvanced;
import java.util.List;

List<QrSegment> segments = QrSegmentAdvanced.makeSegmentsOptimally(
        "A-2026 漢字 12345", Ecc.MEDIUM, 1, 20);
QrCode qr = QrCode.encodeSegments(segments, Ecc.MEDIUM, 1, 20, -1, true);
```

| `encodeSegments()` 参数 | 含义 |
| --- | --- |
| `minVersion`、`maxVersion` | 可选版本范围，必须为 1–40；会选择容纳数据的最小版本。 |
| `mask` | `-1` 评估全部 8 种掩码并选择惩罚分最低者；`0`–`7` 强制指定以获得可复现输出。 |
| `boostEcl` | 仅在不增大版本时提高纠错等级。 |

手工掩码和底层码字属于特殊互操作/测试能力。除非外部格式明确要求，否则应使用自动掩码和高层 API。

## fast 编码器

`com.ajaxjs.qr_code.fast` 包提供 `QrCodeFast` 等平行实现，以更紧凑的方式保存模块/模板，并缓存模板和 Reed–Solomon 生成器，适合反复生成。默认应使用常规 API：`Utils.toImage()`、`Utils.toSvgString()` 接收 `QrCode`，不接收 `QrCodeFast`。应先性能分析，再使用 fast 包；其结果通过 `getModule(x, y)` 自行绘制，且不要混用两套分段类型。

## 异常和安全边界

- 数据放不进当前版本范围/纠错等级时会抛出 `DataTooLongException`。应缩短数据、降低纠错或允许更大版本，不能静默截断 URL、令牌或业务内容。
- 非法版本范围、掩码、边框、缩放值和分段内容会抛出 `IllegalArgumentException`；`null` 通常抛出 `NullPointerException`。
- 二维码内容可被任何扫码器读取。不要编码密码、长期 Bearer Token、私密信息或未保护的内网 URL。有效期和授权必须由服务端控制；二维码编码不是加密。
