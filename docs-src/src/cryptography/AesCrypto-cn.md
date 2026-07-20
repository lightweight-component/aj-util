---
title: AES/DES 加密解密
subTitle: 2025-02-23 by Frank Cheung
description: AesCrypto
date: 2025-02-23
tags:
  - AES/DES 加密解密
layout: layouts/aj-util-cn.njk
---

# AES/DES 加密解密

## AES
对称加密多基于`javax.crypto`包进行封装，封装在类`com.ajaxjs.util.cryptography`中。先看看 AES 的，
```java
final String key = "abc";
final String word = "123";

@Test
void testAES() {
    String encWord = Cryptography.AES_encode(word, key);
    assertEquals(word, Cryptography.AES_decode(encWord, key));
}
```
`Cryptography.AES_encode()` 等静态方法是对已配置 `Cryptography` 实例的便捷封装，其底层流程等价于：
```java
public static String AES_encode(String data, String key) {
    Cryptography cryptography = new Cryptography(Constant.AES, Cipher.ENCRYPT_MODE);
    cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.AES, 128, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
    cryptography.setDataStr(data);

    return cryptography.doCipherAsHexStr();
}
```
静态方法只是调用上的便利，不会改变算法本身及其密钥要求。

## DES/TripleDES
DES 和 Triple DES 的 API 形式类似，但它们属于旧算法。新应用应优先使用带认证的 AES-GCM。
```java
@Test
void testDES() {
    String encWord = Cryptography.DES_encode(word, key);
    assertEquals(word, Cryptography.DES_decode(encWord, key));
}

@SuppressWarnings("restriction")
@Test
void test3DES() {
    // JDK 已提供所需算法时通常不需要额外注册 Provider
//		Security.addProvider(new com.sun.crypto.provider.SunJCE());
    // byte 数组(用来生成密钥的)
    final byte[] keyBytes = {0x11, 0x22, 0x4F, 0x58, (byte) 0x88, 0x10, 0x40, 0x38, 0x28, 0x25, 0x79, 0x51, (byte) 0xCB, (byte) 0xDD, 0x55, 0x66, 0x77, 0x29, 0x74,
            (byte) 0x98, 0x30, 0x40, 0x36, (byte) 0xE2};
    String word = "This is a 3DES test. 测试";

    byte[] encoded = Cryptography.tripleDES_encode(word, keyBytes);

    assertEquals(word, Cryptography.tripleDES_decode(encoded, keyBytes));
}
```
## PBE
当前口令加密 API 使用 `PBKDF2WithHmacSHA256` 派生 AES 密钥，并通过 AES-GCM 加密。盐值至少需要 16 字节，迭代次数至少为 100,000。
```java
byte[] salt = Cryptography.initSalt();
int iterations = Cryptography.MIN_PBE_ITERATIONS;
byte[] encData = Cryptography.PBE_encode(word, key, salt, iterations);

assertEquals(word, Cryptography.PBE_decode(encData, key, salt, iterations));
```

`PBE_legacy_decode` 仅用于解密旧版 `PBEWithMD5AndDES` 数据，新数据不得继续使用旧算法。
