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


The convenience methods such as `Cryptography.AES_encode()` delegate to a configured `Cryptography` instance. The underlying flow is equivalent to:

```java
public static String AES_encode(String data, String key) {
    Cryptography cryptography = new Cryptography(Constant.AES, Cipher.ENCRYPT_MODE);
    cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.AES, 128, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
    cryptography.setDataStr(data);

    return cryptography.doCipherAsHexStr();
}
```


The static methods are convenience wrappers; they do not change the cryptographic operation or key requirements.

## DES/TripleDES
DES and Triple DES follow a similar API shape, but they are legacy algorithms. Prefer authenticated AES-GCM for new applications.

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
    // A provider is normally unnecessary when the JDK already supplies the algorithm.
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
The current password-based encryption API derives an AES key with `PBKDF2WithHmacSHA256` and encrypts with AES-GCM. It requires a salt of at least 16 bytes and at least 100,000 iterations.

```java
byte[] salt = Cryptography.initSalt();
int iterations = Cryptography.MIN_PBE_ITERATIONS;
byte[] encData = Cryptography.PBE_encode(word, key, salt, iterations);

assertEquals(word, Cryptography.PBE_decode(encData, key, salt, iterations));
```

`PBE_legacy_decode` exists only for decrypting data produced by the former `PBEWithMD5AndDES` implementation; do not use the legacy algorithm for new ciphertext.
