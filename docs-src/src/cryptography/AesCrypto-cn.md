---
title: AesCrypto
subTitle: 2025-02-23 by Frank Cheung
description: AesCrypto
date: 2025-02-23
tags:
  - AesCrypto
layout: layouts/aj-util-cn.njk
---

# AesCrypto 教程

本教程提供了 `AesCrypto` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`AesCrypto` 提供了一系列使用 AES、DES、3DES 和 PBE 等算法进行对称加密和解密的实用方法。

## 简介

`AesCrypto` 类提供了使用各种对称加密算法对数据进行加密和解密的方法。它实现了单例模式，并提供了一套完整的安全数据加密工具。

## 方法

### 1. `getInstance()`

返回 `AesCrypto` 类的单例实例。

* **返回值：** `AesCrypto` 的单例实例。

**示例：**

```java
AesCrypto crypto = AesCrypto.getInstance();
```

### 2. `getSecretKey(String algorithmName, SecureRandom secure)`

使用提供的安全随机生成器为指定算法生成密钥，并将其作为 Base64 编码的字符串返回。

* **参数：**
  * `algorithmName`：加密算法的名称。
  * `secure`：安全随机生成器。
* **返回值：** 密钥的 Base64 编码字符串表示。

**示例：**

```java
SecureRandom secureRandom = new SecureRandom();
String secretKey = AesCrypto.getSecretKey("AES", secureRandom);
```

### 3. `DES_encode(String res, String key)` 和 `DES_decode(String res, String key)`

使用 DES 算法加密或解密数据。

* **参数：**
  * `res`：要加密/解密的数据。
  * `key`：加密/解密密钥。
* **返回值：** 加密/解密后的数据。

**示例：**

```java
AesCrypto crypto = AesCrypto.getInstance();
String encrypted = crypto.DES_encode("你好，世界！", "mySecretKey");
String decrypted = crypto.DES_decode(encrypted, "mySecretKey");
```

### 4. `AES_encode(String res, String key)` 和 `AES_decode(String res, String key)`

使用 AES 算法和 128 位密钥大小加密或解密数据。

* **参数：**
  * `res`：要加密/解密的数据。
  * `key`：加密/解密密钥。
* **返回值：** 加密/解密后的数据。

**示例：**

```java
AesCrypto crypto = AesCrypto.getInstance();
String encrypted = crypto.AES_encode("你好，世界！", "mySecretKey");
String decrypted = crypto.AES_decode(encrypted, "mySecretKey");
```

### 5. `encryptTripleDES(byte[] key, String data)` 和 `decryptTripleDES(byte[] key, byte[] data)`

使用三重 DES (3DES) 算法加密或解密数据。

* **参数：**
  * `key`：加密/解密密钥（三重 DES 需要 24 字节）。
  * `data`：要加密/解密的数据。
* **返回值：** 加密/解密后的数据。

**示例：**

```java
byte[] key = new byte[24]; // 生成或获取一个 24 字节的密钥
new SecureRandom().nextBytes(key);

byte[] encrypted = AesCrypto.encryptTripleDES(key, "你好，世界！");
String decrypted = AesCrypto.decryptTripleDES(key, encrypted);
```

### 6. `initSalt()`、`encryptPBE(String key, byte[] salt, String data)` 和 `decryptPBE(String key, byte[] salt, byte[] data)`

基于密码的加密 (PBE) 方法。

* **initSalt()**：生成一个随机的 8 字节盐值。
* **encryptPBE()**：使用提供的密钥和盐值通过 PBE 加密数据。
* **decryptPBE()**：使用提供的密钥和盐值通过 PBE 解密数据。

**示例：**

```java
byte[] salt = AesCrypto.initSalt();
String key = "mySecretPassword";

byte[] encrypted = AesCrypto.encryptPBE(key, salt, "你好，世界！");
String decrypted = AesCrypto.decryptPBE(key, salt, encrypted);
```

## 结论

`AesCrypto` 类提供了一套全面的对称加密和解密工具。它支持多种算法（AES、DES、3DES、PBE）并为安全数据处理提供了简单的接口。请记住安全地存储加密密钥，并遵循加密实现的最佳实践。