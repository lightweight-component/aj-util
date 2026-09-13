---
title: 对称加密
description: 使用 Cryptography 进行对称加密和基于密码的加密。
tags:
  - 加密
  - AES
layout: layouts/aj-util-cn.njk
---

# 对称加密

`Cryptography` 是对 `javax.crypto.Cipher` 的有状态封装。每个独立操作应创建一个新实例；同一实例不能在线程间共享。

## 通用 Cipher API

指定转换算法、加解密模式、密钥材料和源数据后，可以获得字节、文本、Base64 或十六进制结果。

```java
import com.ajaxjs.util.cryptography.Cryptography;
import javax.crypto.Cipher;

Cryptography cipher = new Cryptography("AES", Cipher.ENCRYPT_MODE);
cipher.setKeyData(keyBytes);
cipher.setDataStr("hello");
String encrypted = cipher.doCipherAsBase64Str();
```

解密时使用 `Cipher.DECRYPT_MODE` 创建对象，设置相同密钥后，以 `setDataStrBase64()` 载入密文。

类中也提供 AES、DES、3DES 的便捷方法。DES 和 3DES 只应在对接遗留系统时使用。

## 基于密码的加密

`PBE_encode()` 与 `PBE_decode()` 使用 PBKDF2-HMAC-SHA256 派生 AES-GCM 密钥。每条加密数据都应生成独立的随机盐值，并将盐值与密文一同保存。

```java
byte[] salt = Cryptography.initSalt();
byte[] encrypted = Cryptography.PBE_encode("secret", password, salt, 100_000);
String plain = Cryptography.PBE_decode(encrypted, password, salt, 100_000);
```

迭代次数不得低于 `100_000`。AES-GCM 会验证密文完整性；密码、盐值或随机数不匹配，或密文被篡改时，解密将失败。
