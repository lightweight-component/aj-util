---
title: WeiXinCrypto
description: Encryption and decryption utilities for WeChat/Weixin operations
tags:
  - WeChat
  - Weixin
  - Cryptography
layout: layouts/aj-util.njk
---

# WeiXinCrypto Tutorial

This tutorial provides an overview of the `WeiXinCrypto` class, which is part of the `lightweight-component/aj-util` library. The `WeiXinCrypto` class provides encryption and decryption utilities for WeChat/Weixin related operations.

## Introduction

The `WeiXinCrypto` class contains static methods for handling WeChat/Weixin encryption and decryption operations, including:

- AES-256-GCM decryption
- Mini-program phone number decryption
- RSA-OAEP encryption/decryption
- RSA signature generation

## Main Features

- AES-256-GCM decryption for API v3
- Mini-program encrypted data decryption
- RSA-OAEP encryption with certificates
- RSA signature generation
- Certificate validation

## Methods

### 1. AES Decryption

1. `aesDecryptToString(byte[] aesKey, byte[] associatedData, byte[] nonce, String cipherText)` - AEAD_AES_256_GCM decryption
2. `aesDecryptPhone(String iv, String cipherText, String sessionKey)` - Mini-program phone number decryption

### 2. RSA Encryption/Decryption

1. `encryptOAEP(String message, X509Certificate certificate)` - RSA-OAEP encryption
2. `decryptOAEP(String cipherText, PrivateKey privateKey)` - RSA-OAEP decryption
3. `rsaEncrypt(String message, String certPath)` - RSA encryption with certificate file

### 3. Signatures

1. `rsaSign(PrivateKey privateKey, byte[] data)` - RSA signature generation

## Usage Examples

### AES Decryption
```java
String decrypted = WeiXinCrypto.aesDecryptToString(
    apiKey.getBytes(), 
    associatedData.getBytes(),
    nonce.getBytes(),
    cipherText
);

String phone = WeiXinCrypto.aesDecryptPhone(iv, cipherText, sessionKey);
```

### RSA Encryption
```java
String encrypted = WeiXinCrypto.encryptOAEP(message, certificate);
String encryptedFromFile = WeiXinCrypto.rsaEncrypt(message, "path/to/cert.pem");
```

### RSA Decryption
```java
String decrypted = WeiXinCrypto.decryptOAEP(cipherText, privateKey);
```

### Signature Generation
```java
String signature = WeiXinCrypto.rsaSign(privateKey, data.getBytes());
```

## Implementation Notes

- Uses Java Cryptography Architecture (JCA)
- Supports both file-based and direct certificate input
- Handles Base64 encoding/decoding internally
- Validates certificate expiration

## Conclusion

The `WeiXinCrypto` class provides comprehensive utilities for handling WeChat/Weixin related cryptographic operations, making integration with WeChat services more convenient.