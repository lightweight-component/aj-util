---
title: HashHelper Tutorial
description: Utility methods for message digest algorithms
tags:
  - cryptography
  - hash
  - Java
layout: layouts/aj-util.njk
---


# HashHelper Tutorial

`HashHelper` is a utility class for cryptographic hash operations, supporting various hash algorithms (MD5, SHA-1, SHA-256) and HMAC operations. It provides methods for generating hash values from strings and byte arrays, with support for hexadecimal and Base64 output formats.

### Main Features

1. **Multiple Hash Algorithms Support**: MD5, SHA-1, SHA-256
2. **HMAC Message Authentication Code Support**: HmacMD5, HmacSHA1, HmacSHA256
3. **Multiple Output Formats**: Hexadecimal strings, Base64 encoding
4. **Chainable API Design**
5. **File MD5 Calculation**: Supports efficient processing of large files

### Basic Usage

#### 1. Creating Instances

```java
// Create instance with specified algorithm and byte array
HashHelper helper = new HashHelper("MD5", "Hello World".getBytes());

// Create instance with specified algorithm and string (automatically converted to UTF-8 bytes)
HashHelper helper = new HashHelper("SHA-256", "Hello World");
```


#### 2. Basic Hash Calculation

```java
// Get hash value as byte array
byte[] hashBytes = new HashHelper("MD5", "Hello World").hash();

// Get hash value as hexadecimal string (lowercase)
String hexHash = new HashHelper("SHA-256", "Hello World").hashAsStr();

// Get Base64 encoded hash value
String base64Hash = new HashHelper("SHA-1", "Hello World").hashAsBase64();
String base64HashNoPadding = new HashHelper("SHA-1", "Hello World").hashAsBase64(true);
```


#### 3. HMAC Calculation

```java
// Set key and calculate HMAC
byte[] hmacBytes = new HashHelper("HmacSHA256", "Hello World")
    .setKey("secret-key")
    .hash();

// Get HMAC as hexadecimal string
String hmacHex = new HashHelper("HmacMD5", "Hello World")
    .setKey("secret-key")
    .hashAsStr();

// Get HMAC as Base64 encoded string
String hmacBase64 = new HashHelper("HmacSHA1", "Hello World")
    .setKey("secret-key")
    .hashAsBase64();
```


#### 4. Chainable Calls

Thanks to the `@Accessors(chain = true)` annotation, chainable calls are supported:

```java
String result = new HashHelper("HmacSHA256", "Hello World")
    .setKey("my-secret-key")
    .hashAsBase64(true);
```


#### 5. Static Convenience Methods

```java
// MD5 hash (most commonly used)
String md5Hash = HashHelper.md5("Hello World");

// SHA-1 hash
String sha1Hash = HashHelper.getSHA1("Hello World");

// SHA-256 hash
String sha256Hash = HashHelper.getSHA256("Hello World");

// HMAC-MD5
HashHelper hmacMd5 = HashHelper.getHmacMD5("Hello World", "secret-key");

// HMAC-SHA256 returning Base64
String hmacSha256Base64 = HashHelper.getHmacSHA256("Hello World", "secret-key", false);
```


#### 6. File MD5 Calculation

```java
// Calculate file MD5 from input stream
try (InputStream inputStream = new FileInputStream("example.txt")) {
    String fileMd5 = HashHelper.calcFileMD5(inputStream);
}

// Calculate file MD5 from byte array
byte[] fileBytes = Files.readAllBytes(Paths.get("example.txt"));
String fileMd5 = HashHelper.calcFileMD5(fileBytes);
```


### Constants Definition

- [MD5](file://D:\code\ajaxjs\aj-util\aj-util\src\main\java\com\ajaxjs\util\HashHelper.java#L177-L177): MD5 hash algorithm constant
- [SHA1](file://D:\code\ajaxjs\aj-util\aj-util\src\main\java\com\ajaxjs\util\HashHelper.java#L182-L182): SHA-1 hash algorithm constant
- [SHA256](file://D:\code\ajaxjs\aj-util\aj-util\src\main\java\com\ajaxjs\util\HashHelper.java#L187-L187): SHA-256 hash algorithm constant
- [HMAC_SHA1](file://D:\code\ajaxjs\aj-util\aj-util\src\main\java\com\ajaxjs\util\HashHelper.java#L234-L234): HMAC-SHA1 algorithm constant
- [HMAC_SHA256](file://D:\code\ajaxjs\aj-util\aj-util\src\main\java\com\ajaxjs\util\HashHelper.java#L239-L239): HMAC-SHA256 algorithm constant

### Notes

1. When using HMAC operations without setting a key, a random key will be automatically generated
2. All hash results are returned as lowercase hexadecimal strings by default
3. Base64 encoding includes padding characters by default, controllable via parameters
4. Large file processing uses chunked reading to avoid memory overflow



