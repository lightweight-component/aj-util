---
title: HashHelper
description: HashHelper
layout: layouts/aj-util.njk
lang: en
alternate: /common/HashHelper-cn/
---

# HashHelper

`com.ajaxjs.util.HashHelper` computes digests and HMACs. Hashing is not reversible encryption; MD5 and SHA-1 are legacy digests, unsuitable for collision-resistant security decisions. Plain hashes are not password-storage schemes.

Construct with an algorithm name and `String` (UTF-8) or `byte[]`. `hash()` dispatches names beginning exactly with `Hmac` to `getMac()`, otherwise to `getMessageDigest()`. `hashAsStr()` returns lowercase hex; `hashAsBase64()` returns padded Base64, and `hashAsBase64(true)` omits padding.

## HMAC keys

`setKey(String)` uses UTF-8 key bytes; `setKeyBase64(String)` decodes key material. They return the instance. A missing key throws `IllegalStateException`; no random key is generated. Unsupported algorithms fail rather than returning null.

```java
import com.ajaxjs.util.HashHelper;

String digest = HashHelper.sha256("hello");
String mac = new HashHelper(HashHelper.HMAC_SHA256, "hello")
        .setKey("example-only-key").hashAsBase64();
String sameMac = HashHelper.hmacSHA256("hello", "example-only-key", false);
```

## Convenience methods

`md5(String)`, `md5(byte[])`, `md5(InputStream)`, `sha1(String)` and `sha256(String)` return lowercase hex. `hmacMD5`, `hmacSHA1` and `hmacSHA256` take `(String data, String key, boolean isWithoutPadding)` and return Base64.

**Stream ownership:** current `md5(InputStream)` uses `DataReader` and closes the input stream, despite its Javadoc saying otherwise. Do not reuse it afterward. It processes chunks instead of loading the entire file. Protect real HMAC keys; the example key is not a production secret.
