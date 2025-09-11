---
title: GET请求工具
subTitle: 
description: 提供多种响应处理方式的HTTP GET请求工具类
date: 
tags:
  - HTTP
  - GET/POST/PUT/DELETE/HEAD
  - 请求
layout: layouts/aj-util-cn.njk
---

# GET请求工具

提供多种响应处理方式的HTTP GET请求方法。

## 功能特性

- 返回文本的简单GET请求
- 返回JSON的API请求（Map或List格式）
- 返回XML的API请求
- 文件下载功能

## 使用示例

```java
// 简单GET请求
String html = Get.simpleGET("https://example.com");

// 返回JSON的API请求
Map<String, Object> json = Get.api("https://api.example.com/data");

// 下载文件
String filePath = Get.download("https://example.com/file.pdf", "/downloads");
```

# POST请求工具

提供多种载荷类型的HTTP POST和PUT请求方法。

## 功能特性

- 支持表单数据、JSON或原始字节的POST/PUT请求
- 多部分文件上传
- JSON API请求
- XML API请求
- 通过POST下载文件

## 使用示例

```java
// 带表单数据的简单POST请求
ResponseEntity response = Post.post("https://api.example.com", Map.of("key", "value"));

// POST JSON API请求
Map<String, Object> json = Post.api("https://api.example.com/data", Map.of("id", 123));

// 多部分文件上传
ResponseEntity uploadResp = Post.multiPOST("https://api.example.com/upload", 
    Map.of("file", new File("document.pdf")));
```

# DELETE请求工具

提供HTTP DELETE请求方法。

## 功能特性

- 简单DELETE请求
- 返回JSON的DELETE API请求

## 使用示例

```java
// 简单DELETE请求
ResponseEntity response = Delete.del("https://api.example.com/resource/123");

// 返回JSON的DELETE API请求
Map<String, Object> json = Delete.api("https://api.example.com/resource/123");
```

# HEAD请求工具

提供HTTP HEAD请求和资源检查方法。

## 功能特性

- HEAD请求
- 检查404状态
- 获取文件大小
- 获取重定向地址
- OAuth辅助方法

## 使用示例

```java
// 检查资源是否存在
boolean exists = !Head.is404("https://example.com/resource");

// 获取文件大小
long size = Head.getFileSize("https://example.com/file.pdf");

// 获取重定向地址
String redirectUrl = Head.get302redirect("https://example.com/old");