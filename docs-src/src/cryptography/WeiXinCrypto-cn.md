# WeiXinCrypto 教程

本教程提供了 `WeiXinCrypto` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`WeiXinCrypto` 类为微信相关操作提供了加密和解密实用工具。

## 简介

`WeiXinCrypto` 类包含用于处理微信加密和解密操作的静态方法，包括：

- AES-256-GCM 解密
- 小程序手机号解密
- RSA-OAEP 加密/解密
- RSA 签名生成

## 主要特性

- API v3 的 AES-256-GCM 解密
- 小程序加密数据解密
- 使用证书的 RSA-OAEP 加密
- RSA 签名生成
- 证书验证

## 方法

### 1. AES 解密

1. `aesDecryptToString(byte[] aesKey, byte[] associatedData, byte[] nonce, String cipherText)` - AEAD_AES_256_GCM 解密
2. `aesDecryptPhone(String iv, String cipherText, String sessionKey)` - 小程序手机号解密

### 2. RSA 加密/解密

1. `encryptOAEP(String message, X509Certificate certificate)` - RSA-OAEP 加密
2. `decryptOAEP(String cipherText, PrivateKey privateKey)` - RSA-OAEP 解密
3. `rsaEncrypt(String message, String certPath)` - 使用证书文件的 RSA 加密

### 3. 签名

1. `rsaSign(PrivateKey privateKey, byte[] data)` - RSA 签名生成

## 使用示例

### AES 解密
```java
String decrypted = WeiXinCrypto.aesDecryptToString(
    apiKey.getBytes(), 
    associatedData.getBytes(),
    nonce.getBytes(),
    cipherText
);

String phone = WeiXinCrypto.aesDecryptPhone(iv, cipherText, sessionKey);
```

### RSA 加密
```java
String encrypted = WeiXinCrypto.encryptOAEP(message, certificate);
String encryptedFromFile = WeiXinCrypto.rsaEncrypt(message, "path/to/cert.pem");
```

### RSA 解密
```java
String decrypted = WeiXinCrypto.decryptOAEP(cipherText, privateKey);
```

### 签名生成
```java
String signature = WeiXinCrypto.rsaSign(privateKey, data.getBytes());
```

## 实现说明

- 使用 Java 加密体系结构 (JCA)
- 支持基于文件和直接证书输入
- 内部处理 Base64 编码/解码
- 验证证书过期时间

## 结论

`WeiXinCrypto` 类提供了全面的实用工具，用于处理微信相关的加密操作，使与微信服务的集成更加方便。