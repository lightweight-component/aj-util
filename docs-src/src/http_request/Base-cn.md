---
title: Base HTTP Request API
subTitle: 
description: Base HTTP Request API
date: 
tags:
  - Base API
layout: layouts/aj-util-cn.njk
---

HTTP客户端系统在aj-http模块内提供了一个全面的HTTP通信层。它提供了两种互补的编程范式来发起HTTP请求：一种是使用动态代理的声明式基于注解的API，另一种是用于直接编程控制的命令式基于类的API。这两种方法都抽象了Java的`HttpURLConnection`，同时提供了自动JSON/表单编码、可配置超时、自定义头部和结构化响应处理等功能。

本文档提供了HTTP客户端架构和两种API风格的概述。有关实现细节，请参见：

- 基础请求生命周期：基础请求框架
- 具体的HTTP方法类：HTTP方法
- 动态代理和注解：注解驱动API
- 连接初始化和头部：请求配置
- 响应解析和错误处理：响应处理
- 文件上传和下载：文件操作
- 
# Get 类使用教程

Get 类是 HTTP GET 请求的实现类，用于从服务器获取资源。它扩展了基础的 Request 类，提供了多种便捷的方法来与 API 交互并以不同格式获取数据。

### 主要功能特性

1. **多种构造方式**：支持基本 URL 构造和带连接初始化的构造
2. **文本响应获取**：直接获取纯文本响应
3. **JSON 响应处理**：自动解析 JSON 响应为 Map 或 Java 对象
4. **XML 响应处理**：解析 XML 响应为 Map
5. **认证支持**：内置 Bearer Token 认证支持
6. **自定义连接配置**：支持自定义 HTTP 连接配置

### 基本使用方法

#### 1. 创建实例

```java
// 基本构造方式
Get getRequest = new Get("https://api.example.com/data");

// 带连接初始化的构造方式
Get getRequest = new Get("https://api.example.com/data", conn -> {
    conn.setRequestProperty("User-Agent", "MyApp/1.0");
});
```


#### 2. 获取文本响应

```java
// 简单的文本获取
String response = Get.text("https://api.example.com/data");

// 带自定义连接配置的文本获取
String response = Get.text("https://api.example.com/data", conn -> {
    conn.setRequestProperty("Accept", "text/plain");
});
```


#### 3. 获取 JSON 响应

```java
// 获取 JSON 响应为 Map
Map<String, Object> jsonResponse = Get.api("https://api.example.com/data");

// 带认证令牌的 JSON 获取
Map<String, Object> jsonResponse = Get.api("https://api.example.com/data", "your-token");

// 带自定义连接配置的 JSON 获取
Map<String, Object> jsonResponse = Get.api("https://api.example.com/data", conn -> {
    conn.setRequestProperty("Accept", "application/json");
});
```


#### 4. 映射 JSON 响应到 Java 对象

```java
// 映射到指定类类型
MyDataClass data = Get.api("https://api.example.com/data", MyDataClass.class);

// 带认证令牌的映射
MyDataClass data = Get.api("https://api.example.com/data", MyDataClass.class, "your-token");

// 带自定义连接配置的映射
MyDataClass data = Get.api("https://api.example.com/data", MyDataClass.class, conn -> {
    conn.setRequestProperty("Accept", "application/json");
});
```


#### 5. 获取 XML 响应

```java
// 获取 XML 响应为 Map
Map<String, String> xmlResponse = Get.apiXml("https://api.example.com/data.xml", conn -> {
    conn.setRequestProperty("Accept", "application/xml");
});
```


### 参数说明

- `url`: 请求的目标 URL 地址
- `initConnection`: 用于配置 HTTP 连接的函数式接口
- `token`: Bearer 认证令牌
- `clz`: 目标 Java 类类型，用于 JSON 响应映射

### 注意事项

1. 所有静态方法都会自动执行请求并返回结果
2. 带 `initConnection` 参数的方法允许自定义 HTTP 请求头和其他连接属性
3. JSON 响应可以自动映射到 Java 对象，对象字段需要与 JSON 键匹配
4. XML 响应解析为简单的键值对 Map