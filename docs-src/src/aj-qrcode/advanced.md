---
title: Advanced encoding and limits
layout: layouts/aj-util.njk
alternate: /aj-qrcode/advanced-cn/
---

# Advanced encoding and limits

`QrSegmentAdvanced.makeSegmentsOptimally()` searches numeric, alphanumeric, UTF-8 byte, and Kanji modes to minimize encoded bits within a requested ECL and version range. It is helpful for long mixed Japanese/ASCII data.

```java
import com.ajaxjs.qr_code.QrSegmentAdvanced;
import java.util.List;

List<QrSegment> segments = QrSegmentAdvanced.makeSegmentsOptimally(
        "A-2026 漢字 12345", Ecc.MEDIUM, 1, 20);
QrCode qr = QrCode.encodeSegments(segments, Ecc.MEDIUM, 1, 20, -1, true);
```

| `encodeSegments()` parameter | Meaning |
| --- | --- |
| `minVersion`, `maxVersion` | Allowed versions within 1–40; the smallest fitting version is chosen. |
| `mask` | `-1` evaluates all eight masks and selects the lowest penalty; `0`–`7` forces reproducible output. |
| `boostEcl` | Raises ECL only if this does not require a larger version. |

Manual masks and low-level codewords are specialized interoperability/testing features. Prefer automatic masks and high-level methods unless an external format requires otherwise.

## Fast encoder

`com.ajaxjs.qr_code.fast` has parallel classes such as `QrCodeFast`. They store module/template data compactly and memoize templates and Reed–Solomon generators for repeated workloads. Use the regular API by default: `Utils.toImage()` and `Utils.toSvgString()` accept `QrCode`, not `QrCodeFast`. Profile before adopting the fast package, render its `getModule(x, y)` data yourself, and never mix normal and fast segment types.

## Failure and security boundaries

- Data that does not fit the chosen version range/ECL throws `DataTooLongException`. Shorten data, lower ECL, or allow a larger version; never silently truncate URLs, tokens, or business data.
- Invalid ranges, masks, borders, scales, and segment content throw `IllegalArgumentException`; `null` generally throws `NullPointerException`.
- QR content is readable by every scanner. Do not encode passwords, persistent bearer tokens, private personal data, or unprotected internal URLs. Enforce expiry and authorization on the server; QR encoding is not encryption.
