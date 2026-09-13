---
title: Base64Utils
description: Base64Utils
layout: layouts/aj-util.njk
lang: en
alternate: /common/Base64Utils-cn/
---

# Base64Utils

`com.ajaxjs.util.Base64Utils` wraps Java's standard and URL-safe Base64 codecs. Base64 is encoding, not encryption.

## Input and output

Construct with `byte[]`, `String` (UTF-8), or `String, Charset`. Inputs and the constructor charset must be non-null. The byte array is retained, not copied.

`setUrlSafe(true)` selects the URL alphabet for encoding **and decoding**. `setWithoutPadding(true)` only removes encoding padding. Both setters return the instance.

`encode()` and `decode()` return bytes; `encodeAsString()` returns ASCII Base64. `decodeAsString()` uses UTF-8; its `Charset` overload controls decoded text. Invalid Base64 raises `IllegalArgumentException`.

```java
import com.ajaxjs.util.Base64Utils;
import java.nio.charset.StandardCharsets;

String encoded = new Base64Utils("你好", StandardCharsets.UTF_16)
        .setUrlSafe(true).setWithoutPadding(true).encodeAsString();
String decoded = new Base64Utils(encoded).setUrlSafe(true)
        .decodeAsString(StandardCharsets.UTF_16);
```

`formatPemBase64(String)` inserts a newline every 64 characters, with no trailing newline or PEM boundaries. Decode Base64 text using the default string constructor, even when the plaintext used UTF-16.
