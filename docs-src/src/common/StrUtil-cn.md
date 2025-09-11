---
title: StrUtil 教程
description: 为 Java 应用程序中的字符串操作提供了实用工具方法
date: 2025-09-11
tags:
  - 字符串
  - 工具
  - 操作
layout: layouts/aj-util-cn.njk
---

# StrUtil 教程

本教程提供了 `StrUtil` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`StrUtil` 类为 Java 应用程序中的字符串操作提供了实用工具方法。

## 简介

`StrUtil` 类包含用于常见字符串操作的静态方法，包括验证、连接、填充、模板化和编码转换。

## 主要特性

- 字符串验证和空检查
- URL 路径连接
- 带占位符的字符串模板
- 字符串填充和格式化
- 使用自定义分隔符连接列表/数组
- UTF-8 编码/解码工具
- 模式匹配和替换

## 常量

1. `EMPTY_STRING` - 空字符串常量
2. `DELIM_STR` - 默认模板分隔符 "{}"

## 方法

### 1. 验证

1. `hasText(String str)` - 检查字符串是否包含实际文本
2. `isEmptyText(String str)` - hasText() 的反向操作

### 2. 连接

1. `concatUrl(String a, String b)` - 智能 URL 路径连接
2. `join(T[] arr, String str)` - 使用分隔符连接数组
3. `join(List<T> list, String str)` - 使用分隔符连接列表
4. `join(List<String> list, String tpl, String str)` - 使用模板格式化连接

### 3. 模板

1. `print(String tpl, Object... args)` - 简单的 {} 占位符替换
2. `simpleTpl(String template, Map<String, Object> params)` - ${var} 替换
3. `simpleTpl2(String template, Map<String, Object> data)` - #{var} 替换
4. `simpleTpl(String template, Object data)` - JavaBean 属性替换

### 4. 实用工具

1. `charCount(String str, String _char)` - 计算字符出现次数
2. `leftPad(String str, int len, String _char)` - 左填充字符串
3. `isWordOneOfThem(String word, String[] arr)` - 检查字符串是否在数组中
4. `getUTF8_Bytes(String str)` - 获取 UTF-8 字节
5. `byte2String(byte[] bytes)` - 将字节转换为 UTF-8 字符串
6. `byte2String(String str)` - 将字符串重新编码为 UTF-8

## 使用示例

### 验证
```java
boolean valid = StrUtil.hasText("  test  "); // true
boolean empty = StrUtil.isEmptyText("   "); // true
```

### URL 连接
```java
String url = StrUtil.concatUrl("http://example.com", "api"); 
// "http://example.com/api"
```

### 模板
```java
String result = StrUtil.print("Hello {}!", "World"); // "Hello World!"
String tpl = StrUtil.simpleTpl("Name: ${name}", Map.of("name", "John")); 
// "Name: John"
```

### 连接
```java
String joined = StrUtil.join(List.of("a","b","c"), ","); // "a,b,c"
```

### 编码
```java
byte[] bytes = StrUtil.getUTF8_Bytes("test");
String str = StrUtil.byte2String(bytes); // "test"
```

## 结论

`StrUtil` 类提供了全面的实用方法，用于字符串操作，使 Java 应用程序中的常见字符串操作更加方便。