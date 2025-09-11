---
title: SecurityTextType
description: Enums for security-related text encoding and hashing types
tags:
  - Encoding
  - Hashing
  - Cryptography
layout: layouts/aj-util.njk
---

# SecurityTextType Tutorial

This tutorial provides an overview of the `SecurityTextType` interface, which is part of the `lightweight-component/aj-util` library. The `SecurityTextType` interface defines enums for various security-related text encoding and hashing types.

## Introduction

The `SecurityTextType` interface contains nested enums that categorize different types of:

- Text encoding formats
- Digest (hash) algorithms
- Cryptographic operations

## Enums

### 1. Encode

Defines various text encoding formats:

1. `BASE16` - Hexadecimal encoding
2. `BASE32` - Base32 encoding
3. `BASE58` - Base58 encoding (used in Bitcoin)
4. `BASE64` - Base64 encoding
5. `BASE91` - Base91 encoding

### 2. Digest

Defines message digest (hash) algorithms:

1. `Md5` - MD5 hash
2. `Md5WithSalt` - MD5 hash with salt

### 3. Cryptography

Currently empty, reserved for future cryptographic operation types.

## Usage Examples

```java
SecurityTextType.Encode encoding = SecurityTextType.Encode.BASE64;
SecurityTextType.Digest digest = SecurityTextType.Digest.Md5WithSalt;
```

## Conclusion

The `SecurityTextType` interface provides a type-safe way to reference different security-related text encoding and hashing algorithms throughout the application.