---
title: Base64Utils 教程
description: 为 Base64 编码和解码操作提供了流畅的接口
date: 2025-09-11
tags:
  - 编码
  - 解码
  - base64
layout: layouts/aj-util-cn.njk
---

# Base64Utils 教程

本教程提供了 `Base64Utils` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`Base64Utils` 类为 Base64 编码和解码操作提供了流畅的接口。

## 简介

`Base64Utils` 类提供了一种类型安全、可配置的方式来执行 Base64 编码和解码，支持 URL-safe 编码和填充控制。

## 主要特性

- 流畅/链式方法调用
- 支持标准和 URL-safe Base64
- 可选填充移除
- 类型安全的输出（字符串或字节数组）
- 默认 UTF-8 编码，支持自定义字符集

## 方法

### 1. 构造实例

1. `new Base64Utils(String input)` - 字符串输入默认使用 UTF-8
2. `new Base64Utils(String input, Charset charset)` - 指定字符串字符集
3. `new Base64Utils(byte[] input)` - 使用二进制输入

### 3. 配置

1. `setWithoutPadding(true)` - 移除编码输出中的填充字符
2. `setUrlSafe(true)` - 使用 URL-safe Base64 变体

### 4. 输出方法

1. `encode()` / `decode()` - 返回字节数组
2. `encodeAsString()` - 返回 Base64 文本
3. `decodeAsString()` / `decodeAsString(Charset)` - 解码为文本

## 使用示例

### 基本编码
```java
String encoded = new Base64Utils("Hello World").encodeAsString();
String decoded = new Base64Utils(encoded).decodeAsString();
```

### URL-Safe 编码
```java
String encoded = new Base64Utils("data to encode").setUrlSafe(true).encodeAsString();
```

### 无填充
```java
String encoded = new Base64Utils("data").setWithoutPadding(true).encodeAsString();
```

### 自定义字符集
```java
String encoded = new Base64Utils("数据", StandardCharsets.UTF_16).encodeAsString();
```

### 字节数组处理
```java
byte[] data = {1, 2, 3, 4};
byte[] encoded = new Base64Utils(data).encode();
```

## 结论

`Base64Utils` 类提供了一种灵活且类型安全的方式来执行 Base64 编码和解码操作，支持各种配置和输出格式。
