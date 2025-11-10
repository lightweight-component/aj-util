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
咦~怎么还是静态方法？噢——对了，我们通过静态方法`Cryptography.AES_encode()`封装了一层，其实质是：
```java
public static String AES_encode(String data, String key) {
    Cryptography cryptography = new Cryptography(Constant.AES, Cipher.ENCRYPT_MODE);
    cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.AES, 128, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
    cryptography.setDataStr(data);

    return cryptography.doCipherAsHexStr();
}
```
要说每次实例化对象，当然比静态方法耗资源，不过在 Java 编译器优化的今天，这多出了一点的消耗可以忽略不计。

## DES/TripleDES
其余 DES/TripleDES 如此类推，只是算法不同~
```java
@Test
void testDES() {
    String encWord = Cryptography.DES_encode(word, key);
    assertEquals(word, Cryptography.DES_decode(encWord, key));
}

@SuppressWarnings("restriction")
@Test
void test3DES() {
    // 添加新安全算法,如果用 JCE 就要把它添加进去
    // 这里 addProvider 方法是增加一个新的加密算法提供者(个人理解没有找到好的答案,求补充)
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
这里说说 PBE 算法。PBE 是 DES 的加强，增加一个 Salt 盐值使其更安全。
```java
byte[] salt = Cryptography.initSalt();
byte[] encData = Cryptography.PBE_encode(word, key, salt);

assertEquals(word, Cryptography.PBE_decode(encData, key, salt));
```
