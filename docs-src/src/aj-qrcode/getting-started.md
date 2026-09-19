---
title: Getting started
layout: layouts/aj-util.njk
alternate: /aj-qrcode/getting-started-cn/
---

# Getting started

`QrCode.encodeText()` is the default entry point. It selects the smallest QR version that fits and can raise the requested error-correction level when doing so does not increase the version.

```java
import com.ajaxjs.qr_code.Ecc;
import com.ajaxjs.qr_code.QrCode;
import com.ajaxjs.qr_code.Utils;

QrCode qr = QrCode.encodeText("https://example.com/welcome?from=qr", Ecc.MEDIUM);
Utils.writePng(Utils.toImage(qr, 10, 4), "welcome.png");
```

`QrCode` is immutable. Its public `version`, `size`, `errorCorrectionLevel`, and `mask` fields describe the output. The symbol width in modules is `version * 4 + 17`, from 21 to 177. Call `getModule(x, y)` to draw the black/white grid in another UI toolkit; out-of-bounds coordinates return `false` (light), so it is not a bounds validator.

## Error correction

| Level | Approximate recovery | Typical use |
| --- | ---: | --- |
| `Ecc.LOW` | 7% | Clean, short-lived digital display |
| `Ecc.MEDIUM` | 15% | General links and normal printing |
| `Ecc.QUARTILE` | 25% | Less controlled print/scan conditions |
| `Ecc.HIGH` | 30% | Small physical labels or carefully tested overlays |

Higher levels reduce capacity or increase the selected version. The percentages describe approximate codeword recovery, not a safe logo-covering allowance. Test the actual scanner, print process, background, and viewing distance; error correction cannot compensate for inadequate contrast, quiet zone, or module size.

Continue with [PNG and SVG rendering](/aj-qrcode/rendering/), or learn about [segments](/aj-qrcode/segments/).
