---
title: Base64Helper 教程
description: 为 Base64 编码和解码操作提供了流畅的接口
date: 2025-09-11
tags:
  - 编码
  - 解码
  - base64
layout: layouts/aj-util-cn.njk
---

# Base64Helper 教程

本教程提供了 `Base64Helper` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`Base64Helper` 类为 Base64 编码和解码操作提供了流畅的接口。

## 简介

`Base64Helper` 类提供了一种类型安全、可配置的方式来执行 Base64 编码和解码，支持 URL-safe 编码和填充控制。

## 主要特性

- 流畅/链式方法调用
- 支持标准和 URL-safe Base64
- 可选填充移除
- 类型安全的输出（字符串或字节数组）
- 默认 UTF-8 编码，支持自定义字符集

## 方法

### 1. 初始化

1. `encode()` - 创建编码模式实例（工厂方法）
2. `decode()` - 创建解码模式实例（工厂方法）
3. `encoder()` - 设置为编码模式
4. `decoder()` - 设置为解码模式

### 2. 输入方法

1. `input(String input)` - 设置输入字符串（UTF-8）
2. `input(String input, Charset charset)` - 使用自定义字符集设置输入字符串
3. `input(byte[] input)` - 设置输入字节数组

### 3. 配置

1. `withoutPadding()` - 从编码输出中移除填充
2. `urlSafe()` - 使用 URL-safe Base64 变体

### 4. 输出方法

1. `getString()` - 获取 UTF-8 字符串结果
2. `getString(Charset charset)` - 获取自定义字符集的字符串结果
3. `getBytes()` - 获取字节数组结果

## 使用示例

### 基本编码
```java
String encoded = Base64Helper.encode()
    .input("Hello World")
    .getString();

String decoded = Base64Helper.decode()
    .input(encoded)
    .getString();
```

### URL-Safe 编码
```java
String encoded = Base64Helper.encode()
    .input("data to encode")
    .urlSafe()
    .getString();
```

### 无填充
```java
String encoded = Base64Helper.encode()
    .input("data")
    .withoutPadding()
    .getString();
```

### 自定义字符集
```java
String encoded = Base64Helper.encode()
    .input("数据", StandardCharsets.UTF_16)
    .getString(StandardCharsets.UTF_16);
```

### 字节数组处理
```java
byte[] data = {1, 2, 3, 4};
byte[] encoded = Base64Helper.encode()
    .input(data)
    .getBytes();
```

## 结论

`Base64Helper` 类提供了一种灵活且类型安全的方式来执行 Base64 编码和解码操作，支持各种配置和输出格式。