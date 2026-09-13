---
title: RSA 密钥与签名
description: 生成 RSA 密钥、加密小数据，以及创建或验证签名。
tags:
  - 加密
  - RSA
layout: layouts/aj-util-cn.njk
---

# RSA 密钥与签名

`KeyMgr` 用于生成和恢复 RSA 密钥对。支持生成 2048、3072、4096 位密钥。

```java
import com.ajaxjs.util.cryptography.rsa.KeyMgr;

KeyMgr keys = new KeyMgr(KeyMgr.RSA, 2048);
keys.generateKeyPair();

String publicKeyPem = keys.getPublicToPem();
String privateKeyPem = keys.getPrivateToPem();
```

密钥可导出为 Base64 或 PEM，也可通过相应的 `restore...` 方法恢复。`KeyMgr` 还提供公钥/私钥加解密方法。

RSA 对可加密的数据长度有严格限制。实际业务中应以 RSA 加密较小的会话密钥，再用 AES 加密普通数据。

## 签名和验签

`DoSignature` 和 `DoVerify` 提供链式 API，适用于 `SHA256withRSA` 等签名算法。

```java
import com.ajaxjs.util.cryptography.rsa.DoSignature;
import com.ajaxjs.util.cryptography.rsa.DoVerify;

String signature = new DoSignature("SHA256withRSA")
        .setStrData("message")
        .setPrivateKeyStr(privateKeyPem)
        .signToString();

boolean valid = new DoVerify("SHA256withRSA")
        .setStrData("message")
        .setSignatureBase64(signature)
        .setPublicKeyStr(publicKeyPem)
        .verify();
```
