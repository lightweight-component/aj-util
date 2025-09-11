---
title: Advanced Usage
subTitle: 
description: Advanced Usage for HTTP
date: 
tags:
  - Response Handler
  - Connection Setup
  - SSL Certificate
  - Batch Download
layout: layouts/aj-util.njk
---
# Response Handler Utility

Provides methods for processing HTTP responses in various formats.

## Features

- Response to text conversion
- Response to JSON/XML conversion
- File download handling
- GZip response handling
- Detailed logging

## Usage Examples

```java
// Convert response to JSON
Map<String, Object> json = ResponseHandler.toJson(response);

// Convert response to XML
Map<String, String> xml = ResponseHandler.toXML(response);

// Download file from response
String filePath = ResponseHandler.download(response, "/downloads", "file.pdf");
```

# Connection Setup Utility

Provides common configurations for HTTP connections.

## Features

- Cookie handling
- Referer setting
- Timeout configuration
- User-Agent configuration
- GZip support
- Header mapping

## Usage Examples

```java
// Set custom User-Agent
connectionConsumer = SetConnection.SET_USER_AGENT.andThen(conn -> conn.setRequestProperty("User-Agent", "MyApp/1.0"));

// Set cookies
Map<String, String> cookies = Map.of("session", "12345");
SetConnection.SET_COOKIES.accept(connection, cookies);
```

# Batch Download Utility

Provides concurrent downloading of multiple files.

## Features

- Concurrent downloads using multiple threads
- Customizable file naming
- Progress tracking via CountDownLatch

## Usage Examples

```java
String[] urls = {
    "https://example.com/file1.pdf",
    "https://example.com/file2.pdf"
};

BatchDownload downloader = new BatchDownload(urls, "/downloads", 
    () -> "custom_" + System.currentTimeMillis());
    
downloader.start();
```

# SSL Certificate Utility

Provides methods for handling SSL certificate verification.

## Features

- Skip SSL certificate verification
- Custom certificate loading
- Global SSL context configuration

## Usage Examples

```java
// Skip SSL verification globally
SkipSSL.init();

// Skip SSL verification for single connection
SkipSSL.setSSL_Ignore((HttpsURLConnection)connection);

// Load custom certificate
KeyManager[] kms = SkipSSL.loadCert("/path/to/cert.p12", "password");
SSLSocketFactory sf = SkipSSL.getSocketFactory(kms);
```
