---
title: RsaCrypto
subTitle: 2025-02-23 by Frank Cheung
description: RsaCrypto
date: 2025-02-23
tags:
  - RsaCrypto
layout: layouts/aj-util-cn.njk
---

# RsaCrypto 教程

本教程提供了 `RsaCrypto` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`RsaCrypto` 提供了一系列使用 RSA 算法进行非对称加密、解密和数字签名的实用方法。

## 简介

`RsaCrypto` 类提供了生成 RSA 密钥对、使用公钥和私钥加密和解密数据以及创建和验证数字签名的方法。RSA 是一种非对称加密算法，使用一对密钥：公钥用于加密，私钥用于解密。

## 方法

### 1. `init()`

初始化并返回一个密钥长度为 1024 位的 RSA 密钥对。

* **返回值：** 包含公钥和私钥的字节数组映射。

**示例：**

```java
Map<String, byte[]> keyPair = RsaCrypto.init();
```

### 2. `getPublicKey(Map<String, byte[]> map)` 和 `getPrivateKey(Map<String, byte[]> map)`

从密钥对映射中检索公钥或私钥，并以 Base64 编码的字符串形式返回。

* **参数：**
  * `map`：包含密钥对的映射。
* **返回值：** 密钥的 Base64 编码字符串表示。

**示例：**

```java
Map<String, byte[]> keyPair = RsaCrypto.init();
String publicKey = RsaCrypto.getPublicKey(keyPair);
String privateKey = RsaCrypto.getPrivateKey(keyPair);
```

### 3. `sign(String privateKey, byte[] data)`

使用私钥为提供的数据创建数字签名。

* **参数：**
  * `privateKey`：Base64 编码的私钥。
  * `data`：要签名的数据。
* **返回值：** Base64 编码的数字签名。

**示例：**

```java
Map<String, byte[]> keyPair = RsaCrypto.init();
String privateKey = RsaCrypto.getPrivateKey(keyPair);
String signature = RsaCrypto.sign(privateKey, "你好，世界！".getBytes());
```

### 4. `verify(byte[] data, String publicKey, String sign)`

使用公钥验证数字签名。

* **参数：**
  * `data`：原始数据。
  * `publicKey`：Base64 编码的公钥。
  * `sign`：要验证的 Base64 编码签名。
* **返回值：** 如果签名有效则返回 `true`，否则返回 `false`。

**示例：**

```java
Map<String, byte[]> keyPair = RsaCrypto.init();
String publicKey = RsaCrypto.getPublicKey(keyPair);
String privateKey = RsaCrypto.getPrivateKey(keyPair);

byte[] data = "你好，世界！".getBytes();
String signature = RsaCrypto.sign(privateKey, data);

boolean isValid = RsaCrypto.verify(data, publicKey, signature);
// isValid 应该为 true
```

### 5. 公钥操作：`encryptByPublicKey(byte[] data, String key)` 和 `decryptByPublicKey(byte[] data, String key)`

使用公钥加密或解密数据。

* **参数：**
  * `data`：要加密/解密的数据。
  * `key`：Base64 编码的公钥。
* **返回值：** 加密/解密后的数据字节数组。

**示例：**

```java
Map<String, byte[]> keyPair = RsaCrypto.init();
String publicKey = RsaCrypto.getPublicKey(keyPair);

byte[] data = "你好，世界！".getBytes();
byte[] encrypted = RsaCrypto.encryptByPublicKey(data, publicKey);
```

### 6. 私钥操作：`encryptByPrivateKey(byte[] data, String key)` 和 `decryptByPrivateKey(byte[] data, String key)`

使用私钥加密或解密数据。

* **参数：**
  * `data`：要加密/解密的数据。
  * `key`：Base64 编码的私钥。
* **返回值：** 加密/解密后的数据字节数组。

**示例：**

```java
Map<String, byte[]> keyPair = RsaCrypto.init();
String privateKey = RsaCrypto.getPrivateKey(keyPair);
String publicKey = RsaCrypto.getPublicKey(keyPair);

// 使用公钥加密
byte[] data = "你好，世界！".getBytes();
byte[] encrypted = RsaCrypto.encryptByPublicKey(data, publicKey);

// 使用私钥解密
byte[] decrypted = RsaCrypto.decryptByPrivateKey(encrypted, privateKey);
// decrypted 应该等于原始数据
```

## 结论

`RsaCrypto` 类提供了一套全面的 RSA 非对称加密、解密和数字签名工具。它提供了密钥对生成、使用公钥和私钥进行数据加密/解密以及签名创建/验证的方法。请记住安全地存储私钥，并遵循加密实现的最佳实践。