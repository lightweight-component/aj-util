---
title: WebUtils Tutorial
description: Utility methods for web-related operations in Java applications
tags:
  - Web utilities
  - IP address
  - Cookie handling
layout: layouts/aj-util.njk
---

# WebUtils Tutorial

This tutorial provides an overview of the `WebUtils` class, which is part of the `lightweight-component/aj-util` library. The `WebUtils` class provides utility methods for web-related operations in Java applications.

## Introduction

The `WebUtils` class contains static methods for common web operations such as IP address retrieval and cookie handling.

## Main Features

- Get local IP address
- Get client IP address from HTTP request
- Retrieve cookies from HTTP request

## Methods

### 1. `getLocalIp()`

Retrieves the local IP address of the server.

* **Returns:** The local IP address as a String, or null if the address cannot be determined.

**Example:**
```java
String ip = WebUtils.getLocalIp();
```

### 2. `getClientIp(HttpServletRequest request)`

Retrieves the client's IP address from an HTTP request, handling various proxy headers.

* **Parameters:**
  * `request`: The HttpServletRequest object
* **Returns:** The client IP address as a String

**Example:**
```java
String clientIp = WebUtils.getClientIp(request);
```

### 3. `getCookie(HttpServletRequest request, String cookieName)`

Retrieves a cookie value by name from an HTTP request.

* **Parameters:**
  * `request`: The HttpServletRequest object
  * `cookieName`: The name of the cookie to retrieve
* **Returns:** The cookie value as a String, or null if the cookie doesn't exist

**Example:**
```java
String sessionId = WebUtils.getCookie(request, "JSESSIONID");
```

## Usage Example

Here's a complete example of using the WebUtils class:

```java
import javax.servlet.http.HttpServletRequest;

public class Example {
    public void processRequest(HttpServletRequest request) {
        // Get server IP
        String serverIp = WebUtils.getLocalIp();
        System.out.println("Server IP: " + serverIp);

        // Get client IP
        String clientIp = WebUtils.getClientIp(request);
        System.out.println("Client IP: " + clientIp);

        // Get session cookie
        String sessionId = WebUtils.getCookie(request, "JSESSIONID");
        System.out.println("Session ID: " + sessionId);
    }
}
```

## Conclusion

The `WebUtils` class provides simple utility methods for common web operations, making it easier to handle IP addresses and cookies in Java web applications.