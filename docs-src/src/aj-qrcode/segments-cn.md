---
title: 文本、字节与分段
layout: layouts/aj-util-cn.njk
alternate: /aj-qrcode/segments/
---

# 文本、字节与分段

`encodeText(CharSequence, Ecc)` 会选择一种方便的模式：纯数字用数字模式，符合二维码字母数字字符集的文本用字母数字模式，其他内容用 UTF-8 字节模式。它接受任意 Unicode 文本。`encodeBinary(byte[], Ecc)` 始终使用字节模式，适合非文本负载。

```java
QrCode numeric = QrCode.encodeText("20260319", Ecc.LOW);
QrCode unicode = QrCode.encodeText("订单 2026-031", Ecc.MEDIUM);
QrCode binary = QrCode.encodeBinary(new byte[] {0x01, 0x02, (byte) 0xFF}, Ecc.HIGH);
```

混合内容要更节省空间时可以自行组合分段。数字模式只接受 `0`–`9`；字母数字模式接受大写 `A`–`Z`、数字、空格和 `$%*+-./:`。无效内容会抛出 `IllegalArgumentException`。

```java
import com.ajaxjs.qr_code.QrSegment;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

QrCode qr = QrCode.encodeSegments(Arrays.asList(
        QrSegment.makeAlphanumeric("ORDER "),
        QrSegment.makeNumeric("20260319"),
        QrSegment.makeBytes("-北京".getBytes(StandardCharsets.UTF_8))
), Ecc.MEDIUM);
```

## ECI

扫码端需要明确字符集标识时，可在字节数据前加入 ECI 分段。赋值 `26` 通常表示 UTF-8：

```java
QrCode qr = QrCode.encodeSegments(Arrays.asList(
        QrSegment.makeEci(26),
        QrSegment.makeBytes("中文内容".getBytes(StandardCharsets.UTF_8))
), Ecc.MEDIUM);
```

ECI 只是兼容性声明：不会转换字节，也只对支持该分配值的扫码器/应用有帮助。含 Kanji 的自动多模式优化见[高级编码](/aj-qrcode/advanced-cn/)。
