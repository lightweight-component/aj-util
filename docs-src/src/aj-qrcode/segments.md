---
title: Text, bytes, and segments
layout: layouts/aj-util.njk
alternate: /aj-qrcode/segments-cn/
---

# Text, bytes, and segments

`encodeText(CharSequence, Ecc)` uses one convenient mode: numeric for digits, alphanumeric for the QR alphanumeric set, otherwise UTF-8 byte mode. It accepts any Unicode text. `encodeBinary(byte[], Ecc)` always uses byte mode and is appropriate for non-text payloads.

```java
QrCode numeric = QrCode.encodeText("20260319", Ecc.LOW);
QrCode unicode = QrCode.encodeText("订单 2026-031", Ecc.MEDIUM);
QrCode binary = QrCode.encodeBinary(new byte[] {0x01, 0x02, (byte) 0xFF}, Ecc.HIGH);
```

For compact mixed input, construct a segment list. Numeric mode accepts only `0`–`9`; alphanumeric mode accepts uppercase `A`–`Z`, digits, space, and `$%*+-./:`. Invalid content throws `IllegalArgumentException`.

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

An ECI segment may precede byte data when the receiver requires an explicit character-set assignment. Assignment `26` is commonly UTF-8.

```java
QrCode qr = QrCode.encodeSegments(Arrays.asList(
        QrSegment.makeEci(26),
        QrSegment.makeBytes("中文内容".getBytes(StandardCharsets.UTF_8))
), Ecc.MEDIUM);
```

ECI is a compatibility declaration: it does not transcode bytes and only helps scanners/applications that understand the selected assignment. For automatic multi-mode optimization including Kanji, see [advanced encoding](/aj-qrcode/advanced/).
