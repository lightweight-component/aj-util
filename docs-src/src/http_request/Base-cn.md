---
title: Base HTTP Request API
subTitle: 
description: Base HTTP Request API
date: 
tags:
  - Base API
layout: layouts/aj-util-cn.njk
---
# HTTP 请求 Base 教程

本教程提供了 HTTP 请求包中 `Base` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`Base` 类为 Java 应用程序中的 HTTP 请求提供了基础功能。

## 简介

`Base` 类使用 Java 内置的 `HttpURLConnection` 提供了 HTTP 请求的核心功能。它处理连接初始化、配置和 HTTP 请求的执行。该类作为包中其他 HTTP 请求类（如 `Get`、`Post`、`Delete` 等）的基础。

## 主要特性

- 简单的 HTTP 请求接口
- 连接超时和读取超时处理
- 错误处理和日志记录
- 支持自定义 HTTP 头
- 响应处理

## 方法

### 1. `printErrorLog(String httpMethod, String url, String httpCode, String returnText)`

打印格式化的错误日志，包含请求方法、URL、HTTP状态码和返回文本。

* **参数：**
  * `httpMethod`: HTTP方法
  * `url`: 请求URL
  * `httpCode`: HTTP状态码
  * `returnText`: 返回的错误文本

### 2. `initHttpConnection(String url, String method)`

为指定的 URL 和 HTTP 方法初始化 `HttpURLConnection`。

* **参数：**
  * `url`：HTTP 请求的目标 URL。
  * `method`：HTTP 方法（GET、POST、PUT、DELETE 等）。
* **返回值：** 初始化的 `HttpURLConnection` 对象。

**示例：**

```java
HttpURLConnection conn = Base.initHttpConnection("https://api.example.com/data", "GET");
```

### 2. `connect(HttpURLConnection conn)`

使用提供的连接执行 HTTP 请求并返回响应。

* **参数：**
  * `conn`：用于请求的 `HttpURLConnection` 对象。
* **返回值：** 包含响应详情的 `ResponseEntity` 对象。

**示例：**

```java
HttpURLConnection conn = Base.initHttpConnection("https://api.example.com/data", "GET");
ResponseEntity response = Base.connect(conn);
```

### 3. `connect(String url, String method, Consumer<HttpURLConnection> fn)`

一个便捷方法，将初始化和连接合并为一个调用，并可选择自定义连接的函数。

* **参数：**
  * `url`：HTTP 请求的目标 URL。
  * `method`：HTTP 方法（GET、POST、PUT、DELETE 等）。
  * `fn`：用于自定义连接的可选函数（例如，添加头信息）。
* **返回值：** 包含响应详情的 `ResponseEntity` 对象。

**示例：**

```java
ResponseEntity response = Base.connect("https://api.example.com/data", "GET", conn -> {
    conn.setRequestProperty("Accept", "application/json");
    conn.setRequestProperty("Authorization", "Bearer token123");
});
```

## 连接配置

`Base` 类设置了默认的连接参数：

- **连接超时**：10 秒（10,000 毫秒）
- **读取超时**：15 秒（15,000 毫秒）

这些超时确保在网络问题时请求不会无限期挂起。

## 错误处理

`Base` 类包含全面的错误处理：

1. 对于 HTTP 响应代码 >= 400，它：
   - 在 `ResponseEntity` 中将 `ok` 标志设置为 `false`
   - 检索错误流
   - 记录错误详情

2. 对于连接异常，它：
   - 将 `ok` 标志设置为 `false`
   - 在 `ResponseEntity` 中存储异常
   - 记录异常详情

## ResponseEntity

`connect` 方法返回包含以下内容的 `ResponseEntity` 对象：

- HTTP 响应代码
- 响应体（作为文本或输入流）
- 使用的 URL 和 HTTP 方法
- 请求时间信息
- 成功/失败状态
- 发生的任何异常

## 使用示例

以下是使用 `Base` 类进行 GET 请求的完整示例：

```java
try {
    ResponseEntity response = Base.connect("https://api.example.com/data", "GET", conn -> {
        conn.setRequestProperty("Accept", "application/json");
    });
    
    if (response.isOk()) {
        // 处理成功的响应
        String responseText = StreamHelper.copyToString(response.getIn());
        System.out.println("响应: " + responseText);
    } else {
        // 处理错误
        System.err.println("错误: " + response.getHttpCode() + " - " + response.getResponseText());
    }
} catch (Exception e) {
    e.printStackTrace();
}
```

## 常量

### `ERR_MSG`

错误消息的键名，值为"errMsg"。

## 结论

`Base` 类为 Java 应用程序中的 HTTP 请求提供了坚实的基础。它处理连接管理、错误处理和响应处理的底层细节，使您能够专注于应用程序的业务逻辑。对于特定的 HTTP 方法，请考虑使用基于 `Base` 构建的专用类，如 `Get`、`Post` 等。