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

This tutorial provides an overview of the `Base` class in the HTTP request package, which is part of the `lightweight-component/aj-util` library. The `Base` class serves as the foundation for making HTTP requests in Java applications.

## Introduction

The `Base` class provides the core functionality for making HTTP requests using Java's built-in `HttpURLConnection`. It handles connection initialization, configuration, and execution of HTTP requests. This class serves as the foundation for other HTTP request classes in the package, such as `Get`, `Post`, `Delete`, etc.

## Key Features

- Simple interface for making HTTP requests
- Connection timeout and read timeout handling
- Error handling and logging
- Support for custom HTTP headers
- Response processing

## Methods

### 1. `initHttpConnection(String url, String method)`

Initializes an `HttpURLConnection` for the specified URL and HTTP method.

* **Parameters:**
  * `url`: The target URL for the HTTP request.
  * `method`: The HTTP method (GET, POST, PUT, DELETE, etc.).
* **Returns:** An initialized `HttpURLConnection` object.

**Example:**

```java
HttpURLConnection conn = Base.initHttpConnection("https://api.example.com/data", "GET");
```

### 2. `connect(HttpURLConnection conn)`

Executes the HTTP request using the provided connection and returns the response.

* **Parameters:**
  * `conn`: The `HttpURLConnection` object to use for the request.
* **Returns:** A `ResponseEntity` object containing the response details.

**Example:**

```java
HttpURLConnection conn = Base.initHttpConnection("https://api.example.com/data", "GET");
ResponseEntity response = Base.connect(conn);
```

### 3. `connect(String url, String method, Consumer<HttpURLConnection> fn)`

A convenience method that combines initialization and connection in one call, with an optional function for customizing the connection.

* **Parameters:**
  * `url`: The target URL for the HTTP request.
  * `method`: The HTTP method (GET, POST, PUT, DELETE, etc.).
  * `fn`: An optional function for customizing the connection (e.g., adding headers).
* **Returns:** A `ResponseEntity` object containing the response details.

**Example:**

```java
ResponseEntity response = Base.connect("https://api.example.com/data", "GET", conn -> {
    conn.setRequestProperty("Accept", "application/json");
    conn.setRequestProperty("Authorization", "Bearer token123");
});
```

## Connection Configuration

The `Base` class sets default connection parameters:

- **Connection Timeout**: 10 seconds (10,000 ms)
- **Read Timeout**: 15 seconds (15,000 ms)

These timeouts ensure that requests don't hang indefinitely if there are network issues.

## Error Handling

The `Base` class includes comprehensive error handling:

1. For HTTP response codes >= 400, it:
   - Sets the `ok` flag to `false` in the `ResponseEntity`
   - Retrieves the error stream
   - Logs the error details

2. For connection exceptions, it:
   - Sets the `ok` flag to `false`
   - Stores the exception in the `ResponseEntity`
   - Logs the exception details

## ResponseEntity

The `connect` methods return a `ResponseEntity` object that contains:

- HTTP response code
- Response body (as text or input stream)
- URL and HTTP method used
- Request timing information
- Success/failure status
- Any exceptions that occurred

## Usage Example

Here's a complete example of using the `Base` class to make a GET request:

```java
try {
    ResponseEntity response = Base.connect("https://api.example.com/data", "GET", conn -> {
        conn.setRequestProperty("Accept", "application/json");
    });
    
    if (response.isOk()) {
        // Process the successful response
        String responseText = StreamHelper.copyToString(response.getIn());
        System.out.println("Response: " + responseText);
    } else {
        // Handle the error
        System.err.println("Error: " + response.getHttpCode() + " - " + response.getResponseText());
    }
} catch (Exception e) {
    e.printStackTrace();
}
```

## Conclusion

The `Base` class provides a solid foundation for making HTTP requests in Java applications. It handles the low-level details of connection management, error handling, and response processing, allowing you to focus on the business logic of your application. For specific HTTP methods, consider using the specialized classes built on top of `Base`, such as `Get`, `Post`, etc.