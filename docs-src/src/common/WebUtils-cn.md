---
title: WebUtils 教程
description: 提供Java Web应用程序常用工具方法的工具类
tags:
  - Web工具
  - IP地址
  - Cookie处理
layout: layouts/aj-util-cn.njk
---

# WebUtils 教程

本教程提供了 `WebUtils` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`WebUtils` 类为 Java Web 应用程序提供了常用的工具方法。

## 简介

`WebUtils` 类包含用于常见 Web 操作的静态方法，如 IP 地址获取和 Cookie 处理。

## 主要特性

- 获取本地 IP 地址
- 从 HTTP 请求中获取客户端 IP 地址
- 从 HTTP 请求中获取 Cookie

## 方法

### 1. `getLocalIp()`

获取服务器的本地 IP 地址。

* **返回值:** 本地 IP 地址字符串，如果无法确定则返回 null。

**示例:**
```java
String ip = WebUtils.getLocalIp();
```

### 2. `getClientIp(HttpServletRequest request)`

从 HTTP 请求中获取客户端的 IP 地址，处理各种代理头。

* **参数:**
  * `request`: HttpServletRequest 对象
* **返回值:** 客户端 IP 地址字符串

**示例:**
```java
String clientIp = WebUtils.getClientIp(request);
```

### 3. `getCookie(HttpServletRequest request, String cookieName)`

从 HTTP 请求中按名称获取 Cookie 值。

* **参数:**
  * `request`: HttpServletRequest 对象
  * `cookieName`: 要获取的 Cookie 名称
* **返回值:** Cookie 值字符串，如果 Cookie 不存在则返回 null

**示例:**
```java
String sessionId = WebUtils.getCookie(request, "JSESSIONID");
```

## 使用示例

以下是使用 WebUtils 类的完整示例：

```java
import javax.servlet.http.HttpServletRequest;

public class Example {
    public void processRequest(HttpServletRequest request) {
        // 获取服务器 IP
        String serverIp = WebUtils.getLocalIp();
        System.out.println("服务器 IP: " + serverIp);

        // 获取客户端 IP
        String clientIp = WebUtils.getClientIp(request);
        System.out.println("客户端 IP: " + clientIp);

        // 获取会话 Cookie
        String sessionId = WebUtils.getCookie(request, "JSESSIONID");
        System.out.println("会话 ID: " + sessionId);
    }
}
```

## 结论

`WebUtils` 类为常见的 Web 操作提供了简单的工具方法，使在 Java Web 应用程序中处理 IP 地址和 Cookie 更加容易。