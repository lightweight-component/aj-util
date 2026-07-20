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

`StrUtil` 提供字符串计数、填充、模板替换、拼接和成员判断等静态方法。

## 主要特性

- 带占位符的字符串模板
- 字符串填充和格式化
- 使用自定义分隔符连接列表/数组

## 方法

### 1. 拼接

1. `join(T[] arr, String str)` - 使用分隔符连接数组
2. `join(List<String> list, String str)` - 使用分隔符连接字符串列表
3. `join(List<String> list, String tpl, String str)` - 格式化并连接字符串列表

### 3. 模板

1. `simpleTpl(String template, Map<String, Object> params)` - `${var}` 替换
2. `simpleTpl2(String template, Map<String, Object> data)` - `#{var}` 替换
3. `simpleTpl(String template, Object data)` - JavaBean 属性替换

替换值按字面量处理，值中包含 `$` 或 `\` 也不会被解释为分组引用。

### 4. 实用工具

1. `charCount(String str, String _char)` - 统计出现次数，支持重叠匹配
2. `leftPad(String str, int len, String padding)` - 精确填充到 `len`，不会改动原字符串已有空白
3. `isWordOneOfThem(String word, String[] arr)` - 检查字符串是否在数组中

## 使用示例

### 模板
```java
Map<String, Object> values = new HashMap<>();
values.put("name", "John");
String tpl = StrUtil.simpleTpl("Name: ${name}", values);
// "Name: John"
```

### 计数与填充
```java
int count = StrUtil.charCount("aaa", "aa"); // 2，重叠匹配也会计数
String padded = StrUtil.leftPad("a b", 6, "$"); // "$$$a b"
```

### 连接
```java
String joined = StrUtil.join(Arrays.asList("a", "b", "c"), ","); // "a,b,c"
```

## 结论

`StrUtil` 类提供了全面的实用方法，用于字符串操作，使 Java 应用程序中的常见字符串操作更加方便。
