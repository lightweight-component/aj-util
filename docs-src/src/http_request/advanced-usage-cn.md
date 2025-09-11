---
title: 高级用法
subTitle: 
description: HTTP请求的高级用法
date: 
tags:
  - 响应处理器
  - 连接配置
  - SSL证书
  - 批量下载
layout: layouts/aj-util-cn.njk
---
# 响应处理器工具

提供处理HTTP响应的方法，支持多种格式。

## 功能特性

- 响应内容转换为文本
- 响应内容转换为JSON/XML
- 文件下载处理
- GZip响应处理
- 详细日志记录

## 使用示例

```java
// 将响应转换为JSON
Map<String, Object> json = ResponseHandler.toJson(response);

// 将响应转换为XML
Map<String, String> xml = ResponseHandler.toXML(response);

// 从响应下载文件
String filePath = ResponseHandler.download(response, "/downloads", "file.pdf");
```

# 连接配置工具

提供HTTP连接的常见配置方法。

## 功能特性

- Cookie处理
- Referer设置
- 超时配置
- User-Agent配置
- GZip支持
- 请求头映射

## 使用示例

```java
// 设置自定义User-Agent
connectionConsumer = SetConnection.SET_USER_AGENT.andThen(conn -> conn.setRequestProperty("User-Agent", "MyApp/1.0"));

// 设置Cookies
Map<String, String> cookies = Map.of("session", "12345");
SetConnection.SET_COOKIES.accept(connection, cookies);
```

# 批量下载工具

提供多文件并发下载功能。

## 功能特性

- 使用多线程并发下载
- 可自定义文件名
- 通过CountDownLatch跟踪进度

## 使用示例

```java
String[] urls = {
    "https://example.com/file1.pdf",
    "https://example.com/file2.pdf"
};

BatchDownload downloader = new BatchDownload(urls, "/downloads", 
    () -> "custom_" + System.currentTimeMillis());
    
downloader.start();
```

# SSL证书工具

提供处理SSL证书验证的方法。

## 功能特性

- 跳过SSL证书验证
- 加载自定义证书
- 全局SSL上下文配置

## 使用示例

```java
// 全局跳过SSL验证
SkipSSL.init();

// 为单个连接跳过SSL验证
SkipSSL.setSSL_Ignore((HttpsURLConnection)connection);

// 加载自定义证书
KeyManager[] kms = SkipSSL.loadCert("/path/to/cert.p12", "password");
SSLSocketFactory sf = SkipSSL.getSocketFactory(kms);
```
