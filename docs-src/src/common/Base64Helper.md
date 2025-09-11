---
title: Base64Helper Tutorial
description: Provides a fluent interface for Base64 encoding and decoding operations
date: 2025-09-11
tags:
  - encoding
  - decoding
  - base64
layout: layouts/aj-util.njk
---

# Base64Helper Tutorial

This tutorial provides an overview of the `Base64Helper` class, which is part of the `lightweight-component/aj-util` library. The `Base64Helper` class provides a fluent interface for Base64 encoding and decoding operations.

## Introduction

The `Base64Helper` class offers a type-safe, configurable way to perform Base64 encoding and decoding, with support for URL-safe encoding and padding control.

## Main Features

- Fluent/chained method calls
- Support for both standard and URL-safe Base64
- Optional padding removal
- Type-safe output (String or byte[])
- UTF-8 by default with custom charset support

## Methods

### 1. Initialization

1. `encode()` - Create instance in encode mode (factory method)
2. `decode()` - Create instance in decode mode (factory method)
3. `encoder()` - Set to encode mode
4. `decoder()` - Set to decode mode

### 2. Input Methods

1. `input(String input)` - Set input string (UTF-8)
2. `input(String input, Charset charset)` - Set input string with custom charset
3. `input(byte[] input)` - Set input bytes

### 3. Configuration

1. `withoutPadding()` - Remove padding from encoded output
2. `urlSafe()` - Use URL-safe Base64 variant

### 4. Output Methods

1. `getString()` - Get result as UTF-8 string
2. `getString(Charset charset)` - Get result as string with custom charset
3. `getBytes()` - Get result as byte array

## Usage Examples

### Basic Encoding
```java
String encoded = Base64Helper.encode()
    .input("Hello World")
    .getString();

String decoded = Base64Helper.decode()
    .input(encoded)
    .getString();
```

### URL-Safe Encoding
```java
String encoded = Base64Helper.encode()
    .input("data to encode")
    .urlSafe()
    .getString();
```

### Without Padding
```java
String encoded = Base64Helper.encode()
    .input("data")
    .withoutPadding()
    .getString();
```

### Custom Charset
```java
String encoded = Base64Helper.encode()
    .input("数据", StandardCharsets.UTF_16)
    .getString(StandardCharsets.UTF_16);
```

### Byte Array Handling
```java
byte[] data = {1, 2, 3, 4};
byte[] encoded = Base64Helper.encode()
    .input(data)
    .getBytes();
```

## Conclusion

The `Base64Helper` class provides a flexible and type-safe way to perform Base64 encoding and decoding operations with support for various configurations and output formats.