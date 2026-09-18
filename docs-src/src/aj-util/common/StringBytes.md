---
title: StringBytes
description: StringBytes
layout: layouts/aj-util.njk
lang: en
alternate: /common/StringBytes-cn/
---

# StringBytes

`com.ajaxjs.util.StringBytes` converts text and bytes. Choose the constructor matching the direction: `String` for `getBytes(Charset)` / `getUTF8_Bytes()`, and `byte[]` for `getString(Charset)` / `getUTF8_String()`.

The no-argument `getBytes()` / `getString()` and null charset use the platform default, reported by `showPlatformDefaultCharset()`. Prefer an explicit charset in protocols and files.

## Hexadecimal

`bytesToHex(byte[])` returns uppercase hex. `hexToBytes(String)` accepts upper/lowercase hex, returns empty bytes for empty input, and rejects odd length or non-hex characters with `IllegalArgumentException`. Null hex inputs are rejected.

```java
import com.ajaxjs.util.StringBytes;
import com.ajaxjs.util.ObjectHelper;

byte[] utf8 = new StringBytes("你好").getUTF8_Bytes();
String text = new StringBytes(utf8).getUTF8_String();
String hex = StringBytes.bytesToHex(new byte[]{0x1A, 0x2B}); // 1A2B
byte[] restored = StringBytes.hexToBytes(hex);
byte[] combined = ObjectHelper.concat(restored, new byte[]{0x3C});
```

Byte concatenation belongs to `ObjectHelper.concat(byte[], byte[])`. These conversions do not detect encodings or validate application data.
