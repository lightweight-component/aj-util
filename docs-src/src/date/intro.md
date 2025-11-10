---
title: CommonUtil
description: Common cryptographic utilities used throughout the library
tags:
  - Encryption
  - Decryption
  - Utilities
layout: layouts/aj-util.njk
---

# CommonUtil Tutorial

This tutorial provides an overview of the `CommonUtil` class, which is part of the `lightweight-component/aj-util` library. The `CommonUtil` class provides common cryptographic utilities used throughout the library.

## Introduction

The `CommonUtil` class contains static methods for various cryptographic operations including:

- General cipher operations (encryption/decryption)
- XOR encryption/decryption
- Private key loading and management
- Certificate deserialization

## Main Features

- Unified cipher operation handling
- Simple XOR encryption/decryption
- Private key loading from various sources
- Certificate deserialization and validation
- Support for multiple cryptographic algorithms

## Methods

### 1. Cipher Operations

1. `doCipher(String algorithmName, int mode, Key key, byte[] data)` - Basic cipher operation
2. `doCipher(String algorithmName, int mode, Key key, byte[] data, AlgorithmParameterSpec spec)` - Cipher with parameters
3. `doCipher(String algorithmName, int mode, byte[] keyData, AlgorithmParameterSpec spec, String cipherText, byte[] associatedData)` - AES cipher with associated data

### 2. XOR Encryption

1. `XOR_encode(String res, String key)` - Simple XOR encryption
2. `XOR_decode(String res, String key)` - XOR decryption
3. `XOR(int res, String key)` - Integer XOR operation

### 3. Key Management

1. `loadPrivateKeyByPath(String privateKeyPath)` - Load private key from file
2. `loadPrivateKey(String privateKey)` - Load private key from string
3. `loadPrivateKey(InputStream inputStream)` - Load private key from stream

### 4. Certificate Handling

1. `deserializeToCerts(String apiV3Key, Map<String, Object> pMap)` - Deserialize and decrypt certificates

## Usage Examples

### Cipher Operations
```java
byte[] encrypted = CommonUtil.doCipher("AES/CBC/PKCS5Padding", 
    Cipher.ENCRYPT_MODE, key, data, ivSpec);

String decrypted = CommonUtil.doCipher("AES/GCM/NoPadding",
    Cipher.DECRYPT_MODE, keyBytes, gcmSpec, cipherText, aad);
```

### XOR Encryption
```java
String encoded = CommonUtil.XOR_encode("secret", "password");
String decoded = CommonUtil.XOR_decode(encoded, "password");
```

### Key Loading
```java
PrivateKey key = CommonUtil.loadPrivateKeyByPath("keys/private.pem");
PrivateKey key2 = CommonUtil.loadPrivateKey(privateKeyString);
```

### Certificate Handling
```java
Map<BigInteger, X509Certificate> certs = 
    CommonUtil.deserializeToCerts(apiV3Key, responseMap);
```

## Implementation Notes

- Handles various cryptographic exceptions
- Supports multiple key formats
- Includes basic certificate validation
- Provides simple XOR encryption for basic obfuscation

## Conclusion

The `CommonUtil` class provides foundational cryptographic utilities that are used by other security-related classes in the library, offering consistent handling of common cryptographic operations.