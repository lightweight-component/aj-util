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

The `StrUtil` class contains static methods for counting, padding, templating, joining, and membership checks.

## Main Features

- String templating with placeholders
- String padding and formatting
- List/array joining with custom delimiters

## Methods

### 1. Joining

1. `join(T[] arr, String str)` - Join an array with a delimiter
2. `join(List<String> list, String str)` - Join a string list with a delimiter
3. `join(List<String> list, String tpl, String str)` - Format and join a string list

### 3. Templating

1. `simpleTpl(String template, Map<String, Object> params)` - `${var}` replacement
2. `simpleTpl2(String template, Map<String, Object> data)` - `#{var}` replacement
3. `simpleTpl(String template, Object data)` - JavaBean property replacement

Replacement values are treated literally, so `$` and `\` are safe in values.

### 4. Utilities

1. `charCount(String str, String _char)` - Count occurrences, including overlapping matches
2. `leftPad(String str, int len, String padding)` - Pad to exactly `len` characters without changing whitespace already in `str`
3. `isWordOneOfThem(String word, String[] arr)` - Check string in array

## Usage Examples

### Templating
```java
Map<String, Object> values = new HashMap<>();
values.put("name", "John");
String tpl = StrUtil.simpleTpl("Name: ${name}", values);
// "Name: John"
```

### Counting and padding
```java
int count = StrUtil.charCount("aaa", "aa"); // 2: overlapping matches are counted
String padded = StrUtil.leftPad("a b", 6, "$"); // "$$$a b"
```

### Joining
```java
String joined = StrUtil.join(Arrays.asList("a", "b", "c"), ","); // "a,b,c"
```

## Conclusion

The `StrUtil` class provides comprehensive utility methods for string manipulation, making common string operations more convenient in Java applications.
