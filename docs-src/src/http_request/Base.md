---
title: Base HTTP Request API
subTitle: 
description: Base HTTP Request API
date: 
tags:
  - Base API
layout: layouts/aj-util.njk
---
# HTTP Request Base Tutorial

The HTTP Client System provides a comprehensive HTTP communication layer within the aj-http module. It offers two complementary programming paradigms for making HTTP requests: a declarative annotation-based API using dynamic proxies, and an imperative class-based API for direct programmatic control. Both approaches abstract Java's `HttpURLConnection` while providing features like automatic JSON/form encoding, configurable timeouts, custom headers, and structured response handling.

This document provides an overview of the HTTP client architecture and the two API styles. For implementation details, see:

- Base request lifecycle: Base Request Framework
- Concrete HTTP method classes: HTTP Methods
- Dynamic proxy and annotations: Annotation-Driven API
- Connection initialization and headers: Request Configuration
- Response parsing and error handling: Response Processing
- File uploads and downloads: File Operations



# Get Class Tutorial

The `Get` class is an HTTP GET request implementation used to retrieve resources from a server. It extends the base `Request` class and provides various convenient methods for API interaction and data retrieval in different formats.

### Main Features

1. **Multiple Construction Methods**: Supports basic URL construction and construction with connection initialization
2. **Text Response Retrieval**: Direct retrieval of plain text responses
3. **JSON Response Processing**: Automatic parsing of JSON responses to Map or Java objects
4. **XML Response Processing**: Parsing XML responses to Map
5. **Authentication Support**: Built-in Bearer Token authentication support
6. **Custom Connection Configuration**: Support for custom HTTP connection configuration

### Basic Usage

#### 1. Creating Instances

```java
// Basic construction method
Get getRequest = new Get("https://api.example.com/data");

// Construction with connection initialization
Get getRequest = new Get("https://api.example.com/data", conn -> {
    conn.setRequestProperty("User-Agent", "MyApp/1.0");
});
```


#### 2. Retrieving Text Response

```java
// Simple text retrieval
String response = Get.text("https://api.example.com/data");

// Text retrieval with custom connection configuration
String response = Get.text("https://api.example.com/data", conn -> {
    conn.setRequestProperty("Accept", "text/plain");
});
```


#### 3. Retrieving JSON Response

```java
// Retrieve JSON response as Map
Map<String, Object> jsonResponse = Get.api("https://api.example.com/data");

// JSON retrieval with authentication token
Map<String, Object> jsonResponse = Get.api("https://api.example.com/data", "your-token");

// JSON retrieval with custom connection configuration
Map<String, Object> jsonResponse = Get.api("https://api.example.com/data", conn -> {
    conn.setRequestProperty("Accept", "application/json");
});
```


#### 4. Mapping JSON Response to Java Object

```java
// Map to specified class type
MyDataClass data = Get.api("https://api.example.com/data", MyDataClass.class);

// Mapping with authentication token
MyDataClass data = Get.api("https://api.example.com/data", MyDataClass.class, "your-token");

// Mapping with custom connection configuration
MyDataClass data = Get.api("https://api.example.com/data", MyDataClass.class, conn -> {
    conn.setRequestProperty("Accept", "application/json");
});
```


#### 5. Retrieving XML Response

```java
// Retrieve XML response as Map
Map<String, String> xmlResponse = Get.apiXml("https://api.example.com/data.xml", conn -> {
    conn.setRequestProperty("Accept", "application/xml");
});
```


### Parameter Descriptions

- `url`: The target URL address for the request
- `initConnection`: Functional interface for configuring HTTP connection
- `token`: Bearer authentication token
- `clz`: Target Java class type for JSON response mapping

### Notes

1. All static methods automatically execute the request and return the result
2. Methods with `initConnection` parameter allow custom HTTP headers and other connection properties
3. JSON responses can be automatically mapped to Java objects, object fields need to match JSON keys
4. XML responses are parsed into simple key-value pair Map
