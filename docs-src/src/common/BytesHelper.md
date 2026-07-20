---
title: BytesHelper Tutorial
description: Byte-array concatenation and hexadecimal conversion utilities
tags:
  - byte arrays
  - hexadecimal
  - Java
layout: layouts/aj-util.njk
---

# BytesHelper Tutorial

`BytesHelper` provides small, stateless helpers for byte arrays.

## Concatenating arrays

```java
byte[] result = BytesHelper.concat(new byte[]{1, 2}, new byte[]{3, 4});
// [1, 2, 3, 4]
```

## Hexadecimal encoding

```java
String hex = BytesHelper.bytesToHexStr(new byte[]{0x1A, 0x2B});
// "1A2B"
```

The output uses uppercase hexadecimal characters.

## Hexadecimal decoding

```java
byte[] bytes = BytesHelper.parseHexStr2Byte("1A2B");
```

An empty string returns an empty byte array. Input must contain an even number of characters, and every character must be hexadecimal; otherwise `IllegalArgumentException` is thrown. Odd-length input is never silently truncated.
