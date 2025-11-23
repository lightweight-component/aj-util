---
title: Base64 Utils Tutorial
description: Utility methods for Base64 manipulation
tags:
  - byte arrays
  - Base64
  - Java
layout: layouts/aj-util.njk
---

# Base64 Utils Tutorial

The `Base64Utils` is a utility class for Base64 encoding and decoding operations, providing convenient methods for conversion between byte arrays and strings.

### Main Features

1. **Supports standard and URL-safe Base64 encoding**
2. **Configurable padding omission**
3. **Multiple charset support**
4. **Chainable API design**

### Basic Usage

#### 1. Creating Instances

```java
// Create from byte array
byte[] data = "Hello World".getBytes();
Base64Utils utils = new Base64Utils(data);

// Create from string (default UTF-8)
Base64Utils utils = new Base64Utils("Hello World");

// Create from string (specified charset)
Base64Utils utils = new Base64Utils("Hello World", StandardCharsets.UTF_8);
```


#### 2. Base64 Encoding

```java
// Basic encoding
String encoded = new Base64Utils("Hello World").encodeAsString();
// Output: SGVsbG8gV29ybGQ=

// URL-safe encoding
String urlSafeEncoded = new Base64Utils("Hello World")
    .setUrlSafe(true)
    .encodeAsString();
// Output: SGVsbG8gV29ybGQ=

// Encoding without padding
String noPadding = new Base64Utils("Hello World")
    .setWithoutPadding(true)
    .encodeAsString();
// Output: SGVsbG8gV29ybGQ (without trailing =)
```


#### 3. Base64 Decoding

```java
// Basic decoding
String decoded = new Base64Utils("SGVsbG8gV29ybGQ=").decodeAsString();
// Output: Hello World

// Decoding with specified charset
String decodedWithCharset = new Base64Utils("SGVsbG8gV29ybGQ=")
    .decodeAsString(StandardCharsets.UTF_8);

// URL-safe decoding
String urlDecoded = new Base64Utils("SGVsbG8gV29ybGQ=", true)
    .setUrlSafe(true)
    .decodeAsString();
```


#### 4. Chainable Calls

Due to the `@Accessors(chain = true)` annotation, chainable calls are supported:

```java
String result = new Base64Utils("Hello World")
    .setUrlSafe(true)
    .setWithoutPadding(true)
    .encodeAsString();
```


#### 5. Formatting Base64 Strings

```java
// Format long Base64 string with line break every 64 characters
String longBase64 = "SGVsbG8gV29ybGQhIEkgYW0gYSBsb25nIHN0cmluZyBmb3IgdGVzdGluZyB0aGUgZm9ybWF0dGluZyBmdW5jdGlvbi4=";
String formatted = Base64Utils.formatBase64String(longBase64);
```


### Parameter Description

- [withoutPadding](file://D:\code\ajaxjs\aj-util\aj-util\src\main\java\com\ajaxjs\util\Base64Utils.java#L57-L57): Whether to omit trailing `=` characters in the encoding result
- [urlSafe](file://D:\code\ajaxjs\aj-util\aj-util\src\main\java\com\ajaxjs\util\Base64Utils.java#L63-L63): Whether to use URL-safe Base64 variant (using `-` instead of `+`, `_` instead of `/`)

### Notes

1. Calling encoding or decoding methods without setting input data will throw `IllegalStateException`
2. Encoding results are converted to strings using `ISO_8859_1` charset by default
3. Decoding uses `UTF-8` charset by default

This utility class simplifies Base64 operations in Java, especially suitable for scenarios requiring frequent Base64 encoding and decoding.