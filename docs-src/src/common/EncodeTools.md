---
title: UrlEncode
description: Methods for URL encoding, decoding, and Base64 operations
tags:
  - URL encoding
  - Base64
  - utilities
layout: layouts/aj-util.njk
---

# UrlEncode Tutorial

UrlEncode is a URL encoding and decoding utility class that provides methods for encoding and decoding URLs, handling Chinese characters in URLs, and parsing query strings into Maps.

### Main Features

1. **URL Encoding and Decoding**: Supports different encoding strategies and charset specifications
2. **Chinese Character Handling**: Special handling for Chinese characters in URLs
3. **Query String Parsing**: Converts strings in `xxx=xxx&xxx=xxx` format to Map
4. **Multiple Encoding Modes**: Supports regular encoding, GET request optimized encoding, and safe encoding

### Basic Usage

#### 1. Creating Instances

```java
// Create instance with default UTF-8 charset
UrlEncode encoder = new UrlEncode("Hello World");

// Create instance with specified charset
UrlEncode encoder = new UrlEncode("Hello World", StandardCharsets.UTF_8);
```


#### 2. URL Encoding

```java
// Basic URL encoding
String encoded = new UrlEncode("Hello World").encode();
// Output: Hello+World

// GET request optimized encoding (replaces + with %20)
String queryEncoded = new UrlEncode("Hello World").encodeQuery();
// Output: Hello%20World

// Safe encoding (handles special characters)
String safeEncoded = new UrlEncode("Hello*World~Test").encodeSafe();
// Output: Hello%2AWorld%7ETest
```


#### 3. URL Decoding

```java
// URL decoding
String decoded = new UrlEncode("Hello%20World").decode();
// Output: Hello World
```


#### 4. Chainable Calls

Thanks to the `@Accessors(chain = true)` annotation, chainable calls are supported:

```java
String result = new UrlEncode("Hello World")
    .setCharset(StandardCharsets.UTF_8)
    .encodeQuery();
```


### Static Utility Methods

#### 1. Chinese Character Processing

```java
// Handle Chinese characters in URLs
String chineseStr = "你好世界";
String processed = UrlEncode.urlChinese(chineseStr);
```


#### 2. Query String to Map Conversion

```java
// Convert query string to Map
String queryString = "name=张三&age=25&city=北京";
Map<String, String> paramMap = UrlEncode.parseStringToMap(queryString);

// Result:
// name -> 张三
// age -> 25
// city -> 北京
```

The parser accepts a single pair such as `a=b`. It splits each field at the first `=` only, preserving Base64, JWT, and signature values that contain additional `=` characters. Keys and values are URL-decoded with UTF-8 by default.

### Notes

1. If a Tomcat filter already handles UTF-8 correctly, `urlChinese` should not be applied again.
2. Encoding and decoding operations support custom charsets.
