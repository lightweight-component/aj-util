---
title: Request Utility
subTitle: 
description: Utility class for making HTTP GET requests with various response handling options
date: 
tags:
  - HTTP
  - GET/POST/PUT/DELETE/HEAD
  - Request
layout: layouts/aj-util.njk
---

# GET Request Utility

Provides methods for making HTTP GET requests with different response handling options.

## Features

- Simple GET requests returning text
- GET API requests returning JSON (Map or List)
- GET API requests returning XML
- File downloads

## Usage Examples

```java
// Simple GET request
String html = Get.simpleGET("https://example.com");

// GET API returning JSON
Map<String, Object> json = Get.api("https://api.example.com/data");

// Download file
String filePath = Get.download("https://example.com/file.pdf", "/downloads");
```

# POST Request Utility

Provides methods for making HTTP POST and PUT requests with different payload types.

## Features

- POST/PUT requests with form data, JSON, or raw bytes
- Multipart file uploads
- JSON API requests
- XML API requests
- File downloads via POST

## Usage Examples

```java
// Simple POST with form data
ResponseEntity response = Post.post("https://api.example.com", Map.of("key", "value"));

// POST JSON API
Map<String, Object> json = Post.api("https://api.example.com/data", Map.of("id", 123));

// Multipart file upload
ResponseEntity uploadResp = Post.multiPOST("https://api.example.com/upload", 
    Map.of("file", new File("document.pdf")));
```
# DELETE Request Utility

Provides methods for making HTTP DELETE requests.

## Features

- Simple DELETE requests
- DELETE API requests returning JSON

## Usage Examples

```java
// Simple DELETE request
ResponseEntity response = Delete.del("https://api.example.com/resource/123");

// DELETE API returning JSON
Map<String, Object> json = Delete.api("https://api.example.com/resource/123");
```
# HEAD Request Utility

Provides methods for making HTTP HEAD requests and checking resources.

## Features

- HEAD requests
- Check for 404 status
- Get file size
- Get redirect locations
- OAuth helper methods

## Usage Examples

```java
// Check if resource exists
boolean exists = !Head.is404("https://example.com/resource");

// Get file size
long size = Head.getFileSize("https://example.com/file.pdf");

// Get redirect location
String redirectUrl = Head.get302redirect("https://example.com/old");
```