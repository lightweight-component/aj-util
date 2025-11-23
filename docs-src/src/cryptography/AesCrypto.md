---
title: AesCrypto
subTitle: 2025-02-23 by Frank Cheung
description: AesCrypto
date: 2025-02-23
tags:
  - AesCrypto
layout: layouts/aj-util.njk
---
# AES/DES Encryption and Decryption

## AES
Symmetric encryption is mostly encapsulated based on the `javax.crypto` package, encapsulated in the class `com.ajaxjs.util.cryptography`. Let's first look at AES:

```java
final String key = "abc";
final String word = "123";

@Test
void testAES() {
    String encWord = Cryptography.AES_encode(word, key);
    assertEquals(word, Cryptography.AES_decode(encWord, key));
}
```


Hey~ Why are these still static methods? Oh—right, we encapsulated them with static methods like [Cryptography.AES_encode()](file://D:\code\ajaxjs\aj-util\aj-cryptography\src\main\java\com\ajaxjs\util\cryptography\Cryptography.java#L107-L113), but the essence is:

```java
public static String AES_encode(String data, String key) {
    Cryptography cryptography = new Cryptography(Constant.AES, Cipher.ENCRYPT_MODE);
    cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.AES, 128, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
    cryptography.setDataStr(data);

    return cryptography.doCipherAsHexStr();
}
```


Speaking of instantiating objects each time, it certainly consumes more resources than static methods. However, with today's Java compiler optimizations, this additional consumption can be negligible.

## DES/TripleDES
The rest DES/TripleDES follow the same pattern, just with different algorithms~

```java
@Test
void testDES() {
    String encWord = Cryptography.DES_encode(word, key);
    assertEquals(word, Cryptography.DES_decode(encWord, key));
}

@SuppressWarnings("restriction")
@Test
void test3DES() {
    // Add new security algorithms, if using JCE it needs to be added
    // The addProvider method here adds a new encryption algorithm provider (personal understanding, no good answer found, need补充)
//		Security.addProvider(new com.sun.crypto.provider.SunJCE());
    // byte array (used to generate the key)
    final byte[] keyBytes = {0x11, 0x22, 0x4F, 0x58, (byte) 0x88, 0x10, 0x40, 0x38, 0x28, 0x25, 0x79, 0x51, (byte) 0xCB, (byte) 0xDD, 0x55, 0x66, 0x77, 0x29, 0x74,
            (byte) 0x98, 0x30, 0x40, 0x36, (byte) 0xE2};
    String word = "This is a 3DES test. 测试";

    byte[] encoded = Cryptography.tripleDES_encode(word, keyBytes);

    assertEquals(word, Cryptography.tripleDES_decode(encoded, keyBytes));
}
```

## PBE
Here let's talk about the PBE algorithm. PBE is an enhancement of DES, adding a Salt value to make it more secure.

```java
byte[] salt = Cryptography.initSalt();
byte[] encData = Cryptography.PBE_encode(word, key, salt);

assertEquals(word, Cryptography.PBE_decode(encData, key, salt));
```