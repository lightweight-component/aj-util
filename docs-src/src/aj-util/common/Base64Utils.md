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

## Standard, URL-safe, and padding modes

Standard Base64 uses `+` and `/`. URL-safe Base64 substitutes `-` and `_`, avoiding characters that would otherwise
need escaping in a URL value. Padding (`=`) completes groups of four output characters. It is optional in formats such
as JWT, but the receiving protocol decides whether omitted padding is accepted.

```java
byte[] tokenBytes = new byte[]{(byte) 0xfb, (byte) 0xff};

String standard = new Base64Utils(tokenBytes).encodeAsString(); // +/8=
String urlToken = new Base64Utils(tokenBytes)
        .setUrlSafe(true).setWithoutPadding(true).encodeAsString(); // -_8

byte[] original = new Base64Utils(urlToken).setUrlSafe(true).decode();
```

The selected `urlSafe` mode must match the encoded alphabet for decoding. `withoutPadding` is an encoder setting;
decoding accepts the representation supported by the JDK decoder, but it does not select the alphabet for you.

## Binary data versus text

Base64 carries bytes. The `String` constructors and `decodeAsString(...)` are convenience boundaries that turn text
into bytes or bytes back into text. Use a `byte[]` constructor for a file, digest, key, or other binary material.

```java
byte[] digest = MessageDigest.getInstance("SHA-256")
        .digest("payload".getBytes(StandardCharsets.UTF_8));
String wireValue = new Base64Utils(digest).encodeAsString();

byte[] restoredDigest = new Base64Utils(wireValue).decode();
```

A `byte[]` supplied to the constructor is retained by reference. Do not mutate it between construction and
`encode()`/`decode()` unless that change is intentional. Base64 is not confidentiality protection: anyone who receives
the encoded text can decode it. It also expands data to roughly four output characters per three input bytes.

## Implementation notes

The implementation delegates to `java.util.Base64` rather than maintaining a custom codec. That gives it the JDK's
validation behavior (`IllegalArgumentException` for malformed input) and keeps standard and URL-safe behavior aligned
with the runtime. `encodeAsString()` explicitly uses US-ASCII because Base64 output is ASCII by definition.
