---
title: HashHelper 使用教程
description: HashHelper 类提供了消息摘要算法的实用工具方法
tags:
  - 加密
  - 哈希
  - Java
layout: layouts/aj-util-cn.njk
---


# HashHelper 使用教程

`HashHelper` 是一个用于密码学哈希操作的工具类，支持多种哈希算法（MD5、SHA-1、SHA-256）和 HMAC 操作。它提供了从字符串和字节数组生成哈希值的方法，并支持十六进制和 Base64 输出格式。

### 主要功能特性

1. **支持多种哈希算法**: MD5, SHA-1, SHA-256
2. **支持 HMAC 消息认证码**: HmacMD5, HmacSHA1, HmacSHA256
3. **多种输出格式**: 十六进制字符串、Base64 编码
4. **链式调用 API 设计**
5. **文件 MD5 计算**: 支持大文件分块处理

### 基本使用方法

#### 1. 创建实例

```java
// 使用指定算法和字节数组创建实例
HashHelper helper = new HashHelper("MD5", "Hello World".getBytes());

// 使用指定算法和字符串创建实例（自动转为 UTF-8 字节）
HashHelper helper = new HashHelper("SHA-256", "Hello World");
```


#### 2. 基本哈希计算

```java
// 获取字节数组形式的哈希值
byte[] hashBytes = new HashHelper("MD5", "Hello World").hash();

// 获取十六进制字符串形式的哈希值（小写）
String hexHash = new HashHelper("SHA-256", "Hello World").hashAsStr();

// 获取 Base64 编码的哈希值
String base64Hash = new HashHelper("SHA-1", "Hello World").hashAsBase64();
String base64HashNoPadding = new HashHelper("SHA-1", "Hello World").hashAsBase64(true);
```


#### 3. HMAC 计算

```java
// 设置密钥并计算 HMAC
byte[] hmacBytes = new HashHelper("HmacSHA256", "Hello World")
    .setKey("secret-key")
    .hash();

// 获取 HMAC 的十六进制字符串
String hmacHex = new HashHelper("HmacMD5", "Hello World")
    .setKey("secret-key")
    .hashAsStr();

// 获取 HMAC 的 Base64 编码
String hmacBase64 = new HashHelper("HmacSHA1", "Hello World")
    .setKey("secret-key")
    .hashAsBase64();
```


#### 4. 链式调用

由于使用了 `@Accessors(chain = true)` 注解，可以进行链式调用：

```java
String result = new HashHelper("HmacSHA256", "Hello World")
    .setKey("my-secret-key")
    .hashAsBase64(true);
```


#### 5. 静态便捷方法

```java
// MD5 哈希（最常用）
String md5Hash = HashHelper.md5("Hello World");

// SHA-1 哈希
String sha1Hash = HashHelper.getSHA1("Hello World");

// SHA-256 哈希
String sha256Hash = HashHelper.getSHA256("Hello World");

// HMAC-MD5
HashHelper hmacMd5 = HashHelper.getHmacMD5("Hello World", "secret-key");

// HMAC-SHA256 返回 Base64
String hmacSha256Base64 = HashHelper.getHmacSHA256("Hello World", "secret-key", false);
```


#### 6. 文件 MD5 计算

```java
// 从输入流计算文件 MD5
try (InputStream inputStream = new FileInputStream("example.txt")) {
    String fileMd5 = HashHelper.calcFileMD5(inputStream);
}

// 从字节数组计算文件 MD5
byte[] fileBytes = Files.readAllBytes(Paths.get("example.txt"));
String fileMd5 = HashHelper.calcFileMD5(fileBytes);
```


### 常量定义

- MD5: MD5 哈希算法常量
- SHA1: SHA-1 哈希算法常量
- SHA256: SHA-256 哈希算法常量
- HMAC_SHA1: HMAC-SHA1 算法常量
- HMAC_SHA256: HMAC-SHA256 算法常量

### 注意事项

1. 当使用 HMAC 操作且未设置密钥时，会自动生成随机密钥
2. 所有哈希结果默认以小写十六进制字符串返回
3. Base64 编码默认包含填充字符，可通过参数控制
4. 大文件处理采用分块读取方式，避免内存溢出

