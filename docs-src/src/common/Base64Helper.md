---
title: Base64Utils Tutorial
description: Provides a fluent interface for Base64 encoding and decoding operations
date: 2025-09-11
tags:
  - encoding
  - decoding
  - base64
layout: layouts/aj-util.njk
---

# Base64Utils Tutorial

This tutorial provides an overview of the `Base64Utils` class, which is part of the `lightweight-component/aj-util` library. The `Base64Utils` class provides a fluent interface for Base64 encoding and decoding operations.

## Introduction

The `Base64Utils` class offers a type-safe, configurable way to perform Base64 encoding and decoding, with support for URL-safe encoding and padding control.

## Main Features

- Fluent/chained method calls
- Support for both standard and URL-safe Base64
- Optional padding removal
- Type-safe output (String or byte[])
- UTF-8 by default with custom charset support

## Methods

### 1. Construction

1. `new Base64Utils(String input)` - Use UTF-8 for string input
2. `new Base64Utils(String input, Charset charset)` - Use a specified charset
3. `new Base64Utils(byte[] input)` - Use binary input

### 3. Configuration

1. `setWithoutPadding(true)` - Remove padding from encoded output
2. `setUrlSafe(true)` - Use the URL-safe Base64 variant

### 4. Output Methods

1. `encode()` / `decode()` - Return a byte array
2. `encodeAsString()` - Return encoded ASCII text
3. `decodeAsString()` / `decodeAsString(Charset)` - Decode text

## Usage Examples

### Basic Encoding
```java
String encoded = new Base64Utils("Hello World").encodeAsString();
String decoded = new Base64Utils(encoded).decodeAsString();
```

### URL-Safe Encoding
```java
String encoded = new Base64Utils("data to encode").setUrlSafe(true).encodeAsString();
```

### Without Padding
```java
String encoded = new Base64Utils("data").setWithoutPadding(true).encodeAsString();
```

### Custom Charset
```java
String encoded = new Base64Utils("数据", StandardCharsets.UTF_16).encodeAsString();
```

### Byte Array Handling
```java
byte[] data = {1, 2, 3, 4};
byte[] encoded = new Base64Utils(data).encode();
```

## Conclusion

The `Base64Utils` class provides a flexible and type-safe way to perform Base64 encoding and decoding operations with support for various configurations and output formats.
