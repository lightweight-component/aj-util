---
title: CommonUtil
description: 整个库中使用的常见加密实用工具
tags:
  - 加密
  - 解密
  - 实用工具
layout: layouts/aj-util-cn.njk
---

# CommonUtil 教程

本教程提供了 `CommonUtil` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`CommonUtil` 类提供了整个库中使用的常见加密实用工具。

## 简介

`CommonUtil` 类包含用于各种加密操作的静态方法，包括：

- 通用加密操作（加密/解密）
- XOR 加密/解密
- 私钥加载和管理
- 证书反序列化

## 主要特性

- 统一的加密操作处理
- 简单的 XOR 加密/解密
- 从各种来源加载私钥
- 证书反序列化和验证
- 支持多种加密算法

## 方法

### 1. 加密操作

1. `doCipher(String algorithmName, int mode, Key key, byte[] data)` - 基本加密操作
2. `doCipher(String algorithmName, int mode, Key key, byte[] data, AlgorithmParameterSpec spec)` - 带参数的加密
3. `doCipher(String algorithmName, int mode, byte[] keyData, AlgorithmParameterSpec spec, String cipherText, byte[] associatedData)` - 带关联数据的 AES 加密

### 2. XOR 加密

1. `XOR_encode(String res, String key)` - 简单 XOR 加密
2. `XOR_decode(String res, String key)` - XOR 解密
3. `XOR(int res, String key)` - 整数 XOR 操作

### 3. 密钥管理

1. `loadPrivateKeyByPath(String privateKeyPath)` - 从文件加载私钥
2. `loadPrivateKey(String privateKey)` - 从字符串加载私钥
3. `loadPrivateKey(InputStream inputStream)` - 从流加载私钥

### 4. 证书处理

1. `deserializeToCerts(String apiV3Key, Map<String, Object> pMap)` - 反序列化和解密证书

## 使用示例

### 加密操作
```java
byte[] encrypted = CommonUtil.doCipher("AES/CBC/PKCS5Padding", 
    Cipher.ENCRYPT_MODE, key, data, ivSpec);

String decrypted = CommonUtil.doCipher("AES/GCM/NoPadding",
    Cipher.DECRYPT_MODE, keyBytes, gcmSpec, cipherText, aad);
```

### XOR 加密
```java
String encoded = CommonUtil.XOR_encode("secret", "password");
String decoded = CommonUtil.XOR_decode(encoded, "password");
```

### 密钥加载
```java
PrivateKey key = CommonUtil.loadPrivateKeyByPath("keys/private.pem");
PrivateKey key2 = CommonUtil.loadPrivateKey(privateKeyString);
```

### 证书处理
```java
Map<BigInteger, X509Certificate> certs = 
    CommonUtil.deserializeToCerts(apiV3Key, responseMap);
```

## 实现说明

- 处理各种加密异常
- 支持多种密钥格式
- 包含基本证书验证
- 提供简单的 XOR 加密用于基本混淆

## 结论

`CommonUtil` 类提供了基础的加密实用工具，被库中其他安全相关类使用，提供了一致的常见加密操作处理方式。