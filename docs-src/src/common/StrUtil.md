---
title: StrUtil Tutorial
description: Provides utility methods for string manipulation in Java applications
date: 2025-09-11
tags:
  - string
  - utilities
  - manipulation
layout: layouts/aj-util.njk
---

# StrUtil Tutorial

This tutorial provides an overview of the `StrUtil` class, which is part of the `lightweight-component/aj-util` library. The `StrUtil` class provides utility methods for string manipulation in Java applications.

## Introduction

The `StrUtil` class contains static methods for common string operations including validation, concatenation, padding, templating, and encoding conversions.

## Main Features

- String validation and empty checks
- URL path concatenation
- String templating with placeholders
- String padding and formatting
- List/array joining with custom delimiters
- UTF-8 encoding/decoding utilities
- Pattern matching and replacement

## Constants

1. `EMPTY_STRING` - Empty string constant
2. `DELIM_STR` - Default template delimiter "{}"

## Methods

### 1. Validation

1. `hasText(String str)` - Check if string contains actual text
2. `isEmptyText(String str)` - Opposite of hasText()

### 2. Concatenation

1. `concatUrl(String a, String b)` - Smart URL path concatenation
2. `join(T[] arr, String str)` - Join array with delimiter
3. `join(List<T> list, String str)` - Join list with delimiter
4. `join(List<String> list, String tpl, String str)` - Join with template formatting

### 3. Templating

1. `print(String tpl, Object... args)` - Simple {} placeholder replacement
2. `simpleTpl(String template, Map<String, Object> params)` - ${var} replacement
3. `simpleTpl2(String template, Map<String, Object> data)` - #{var} replacement
4. `simpleTpl(String template, Object data)` - JavaBean property replacement

### 4. Utilities

1. `charCount(String str, String _char)` - Count character occurrences
2. `leftPad(String str, int len, String _char)` - Left pad string
3. `isWordOneOfThem(String word, String[] arr)` - Check string in array
4. `getUTF8_Bytes(String str)` - Get UTF-8 bytes
5. `byte2String(byte[] bytes)` - Convert bytes to UTF-8 string
6. `byte2String(String str)` - Re-encode string as UTF-8

## Usage Examples

### Validation
```java
boolean valid = StrUtil.hasText("  test  "); // true
boolean empty = StrUtil.isEmptyText("   "); // true
```

### URL Concatenation
```java
String url = StrUtil.concatUrl("http://example.com", "api"); 
// "http://example.com/api"
```

### Templating
```java
String result = StrUtil.print("Hello {}!", "World"); // "Hello World!"
String tpl = StrUtil.simpleTpl("Name: ${name}", Map.of("name", "John")); 
// "Name: John"
```

### Joining
```java
String joined = StrUtil.join(List.of("a","b","c"), ","); // "a,b,c"
```

### Encoding
```java
byte[] bytes = StrUtil.getUTF8_Bytes("test");
String str = StrUtil.byte2String(bytes); // "test"
```

## Conclusion

The `StrUtil` class provides comprehensive utility methods for string manipulation, making common string operations more convenient in Java applications.