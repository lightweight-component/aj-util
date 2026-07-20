---
title: UrlEncode 教程
description: 提供URL编码/解码和Base64编码/解码的工具类
tags:
  - URL编码
  - Base64
  - 工具类
layout: layouts/aj-util-cn.njk
---
# UrlEncode 使用教程

UrlEncode 是一个 URL 编码和解码的工具类，提供了对 URL 进行编码和解码的方法，处理 URL 中的中文字符，并能将查询字符串解析为 Map。

### 主要功能特性

1. **URL 编码和解码**：支持不同编码策略和字符集规范
2. **中文字符处理**：专门处理 URL 中的中文乱码问题
3. **查询字符串解析**：将格式为 `xxx=xxx&xxx=xxx` 的字符串转换为 Map
4. **多种编码模式**：支持普通编码、GET 请求优化编码和安全编码

### 基本使用方法

#### 1. 创建实例

```java
// 使用默认 UTF-8 字符集创建实例
UrlEncode encoder = new UrlEncode("Hello World");

// 使用指定字符集创建实例
UrlEncode encoder = new UrlEncode("Hello World", StandardCharsets.UTF_8);
```


#### 2. URL 编码

```java
// 基本 URL 编码
String encoded = new UrlEncode("Hello World").encode();
// 输出: Hello+World

// GET 请求优化编码（将 + 替换为 %20）
String queryEncoded = new UrlEncode("Hello World").encodeQuery();
// 输出: Hello%20World

// 安全编码（处理特殊字符）
String safeEncoded = new UrlEncode("Hello*World~Test").encodeSafe();
// 输出: Hello%2AWorld%7ETest
```


#### 3. URL 解码

```java
// URL 解码
String decoded = new UrlEncode("Hello%20World").decode();
// 输出: Hello World
```


#### 4. 链式调用

由于使用了 `@Accessors(chain = true)` 注解，可以进行链式调用：

```java
String result = new UrlEncode("Hello World")
    .setCharset(StandardCharsets.UTF_8)
    .encodeQuery();
```


### 静态工具方法

#### 1. 中文字符处理

```java
// 处理 URL 中的中文乱码
String chineseStr = "你好世界";
String processed = UrlEncode.urlChinese(chineseStr);
```


#### 2. 查询字符串转 Map

```java
// 将查询字符串转换为 Map
String queryString = "name=张三&age=25&city=北京";
Map<String, String> paramMap = UrlEncode.parseStringToMap(queryString);

// 结果:
// name -> 张三
// age -> 25
// city -> 北京
```

解析器支持只有一个参数的查询串，例如 `a=b`。每个字段只在第一个 `=` 处分隔，因此包含额外 `=` 的 Base64、JWT 和签名值不会被截断。键和值默认按 UTF-8 做 URL 解码。



### 注意事项

1. 如果 Tomcat 过滤器已经正确处理 UTF-8，不应再次调用 `urlChinese` 重复转码。
2. 编码和解码操作均支持自定义字符集。
