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

## Digest or HMAC?

A message digest is an unkeyed fingerprint: the same bytes always produce the same result, so it is useful for an
integrity check where the expected digest comes from a trusted source. HMAC combines data with a shared secret and is
used to authenticate a message. HMAC is not selected by a separate method family internally: the exact algorithm name
must begin with `Hmac` (for example `HmacSHA256`).

```java
String sha256 = new HashHelper(HashHelper.SHA256, "abc").hashAsStr();
// ba7816bf8f01cfea414140de5dae2223...

String signature = new HashHelper(HashHelper.HMAC_SHA256, "data")
        .setKey("shared-secret")
        .hashAsBase64(true);
```

The key must be supplied before an HMAC call. Use `setKey(String)` only when the protocol defines a UTF-8 textual
secret. If a provider supplies random key bytes as Base64, use `setKeyBase64(String)` so those bytes are not mistakenly
treated as the literal Base64 characters.

```java
String keyFromConfig = "c2lnbmluZy1rZXktYnl0ZXM=";
String mac = new HashHelper(HashHelper.HMAC_SHA256, payloadBytes)
        .setKeyBase64(keyFromConfig)
        .hashAsBase64();
```

## Output formats and interoperability

`hashAsStr()` is lowercase hexadecimal and is usually convenient for logs or a checksum file. `hashAsBase64()` is
shorter for a binary digest and retains `=` padding by default. Choose the format required by the remote protocol;
changing hex letter case or Base64 padding can cause a signature comparison to fail even when the underlying bytes are
identical.

`String` data is consistently converted with UTF-8. For a protocol that signs a canonical byte sequence, construct
the helper with `byte[]` after producing that exact sequence. Avoid signing a Java object string representation unless
the protocol explicitly specifies it.

## Stream checksums

`md5(InputStream)` updates a `MessageDigest` per chunk, so it does not retain the entire file in memory:

```java
try (InputStream in = Files.newInputStream(Paths.get("release.zip"))) {
    String checksum = HashHelper.md5(in);
    verifyExpectedChecksum(checksum);
}
```

The method closes the stream itself. The surrounding try-with-resources is harmless but redundant; use it when the
same code path may change to another stream API later.

## Security guidance and implementation notes

Prefer SHA-256 or HMAC-SHA-256 for new designs. MD5 and SHA-1 may remain necessary for legacy interoperability or a
non-security checksum, but do not use them for password storage, signatures, or collision-resistant identity checks.
For passwords, use a dedicated adaptive password-hashing scheme supplied by an appropriate security library.

Each operation asks the JDK security provider for a `MessageDigest` or `Mac`, initializes it, and returns its result;
instances do not cache a mutable digest state. Unsupported algorithms and invalid HMAC keys fail explicitly instead of
silently returning an empty value.
