---
title: RsaCrypto
subTitle: 2025-02-23 by Frank Cheung
description: RsaCrypto
date: 2025-02-23
tags:
  - RsaCrypto
layout: layouts/aj-util.njk
---
# RSA Encryption and Decryption

RSA asymmetric encryption involves many tasks, which can be decomposed into the following sub-tasks:

- Signing, encapsulated in `com.ajaxjs.util.cryptography.rsa.DoSignature`
- Signature verification, encapsulated in `com.ajaxjs.util.cryptography.rsa.DoVerify`
- Key management, encapsulated in `com.ajaxjs.util.cryptography.rsa.KeyMgr`
- RSA encryption and decryption itself

Each will be introduced separately below.

## Signing
Input parameters include algorithm, input data, and private key. Execute `sign()` to return the signature. The involved types are as follows:

- Input data, can be `byte[]` or string
- Private key, can be a PrivateKey object or string. If it's a string, it will be restored to a PrivateKey object through `KeyMgr.restoreKey`
- The returned signature data is `byte[]`, and `signToString()` can be called to return a base64-encoded string

```java
// Generate public and private keys
KeyMgr keyMgr = new KeyMgr(Constant.RSA, 1024);
keyMgr.generateKeyPair();
String privateKey = keyMgr.getPrivateKeyStr();

byte[] helloWorlds = new DoSignature(Constant.SHA256_RSA).setStrData("hello world").setPrivateKeyStr(privateKey).sign();
String result = new DoSignature(Constant.SHA256_RSA).setStrData("hello world").setPrivateKeyStr(privateKey).signToString();

assertEquals(EncodeTools.base64EncodeToString(helloWorlds), result);
```


It's worth mentioning that where does the private key come from? You can generate it through the above KeyMgr.

## Signature Verification
Input parameters include algorithm, input data, signature data, and public key. Execute `verify()` to verify the signature. The involved types are as follows:

- Input data, can be `byte[]` or string
- Signature data, can be `byte[]` or Base64 string
- Public key, can be a PublicKey object or string. If it's a string, it will be restored to a PublicKey object through `KeyMgr`
- Whether the returned signature is valid, is `boolean`

```java
// Generate public and private keys
KeyMgr keyMgr = new KeyMgr(Constant.RSA, 1024);
keyMgr.generateKeyPair();
String publicKey = keyMgr.getPublicKeyStr(), privateKey = keyMgr.getPrivateKeyStr();
String result = new DoSignature(Constant.SHA256_RSA).setStrData("hello world").setPrivateKeyStr(privateKey).signToString();
boolean verified = new DoVerify(Constant.SHA256_RSA).setStrData("hello world").setPublicKeyStr(publicKey).setSignatureBase64(result).verify();

assertTrue(verified);
```


It's worth mentioning that where do the public and private keys come from? You can generate them through the above `KeyMgr`.

## RSA Encryption and Decryption
Nothing more to say, directly show the API examples.

```java
// Generate public and private keys
KeyMgr keyMgr = new KeyMgr(Constant.RSA, 1024);
keyMgr.generateKeyPair();
String publicKey = keyMgr.getPublicKeyStr(), privateKey = keyMgr.getPrivateKeyStr();

System.out.println("Public Key: \n\r" + publicKey);
System.out.println("Private Key: \n\r" + privateKey);
//		System.out.println("Public Key Encryption--------Private Key Decryption");

String word = "你好，世界！";

byte[] encWord = KeyMgr.publicKeyEncrypt(word.getBytes(), publicKey);
String decWord = new String(KeyMgr.privateKeyDecrypt(encWord, privateKey));

String eBody = EncodeTools.base64EncodeToString(encWord);
String decWord2 = new String(KeyMgr.privateKeyDecrypt(EncodeTools.base64Decode(eBody), privateKey));
System.out.println("Before Encryption: " + word + "\n\rCiphertext: " + eBody + "\nAfter Decryption: " + decWord2);
assertEquals(word, decWord);

//		System.out.println("Private Key Encryption--------Public Key Decryption");

String english = "Hello, World!";
byte[] encEnglish = KeyMgr.privateKeyEncrypt(english.getBytes(), privateKey);
String decEnglish = new String(KeyMgr.publicKeyDecrypt(encEnglish, publicKey));
//		System.out.println("Before Encryption: " + english + "\n\r" + "After Decryption: " + decEnglish);

assertEquals(english, decEnglish);
//		System.out.println("Private Key Signing——Public Key Signature Verification");

// Generate signature
String sign = new DoSignature(Constant.MD5_RSA).setPrivateKeyStr(privateKey).setData(encEnglish).signToString();
//		System.out.println("Signature:\r" + sign);
// Verify signature
assertTrue(new DoVerify(Constant.MD5_RSA).setPublicKeyStr(publicKey).setData(encEnglish).setSignatureBase64(sign).verify());
```


## Key Management
Some utility methods for keys are in `KeyMgr`, including both public and private keys. Generally, open-source projects like to encapsulate `KeyPair` as a Map, but the author thinks using `KeyPair` itself is sufficient. If not fully satisfied, certain methods can be added, such as `getPublicKeyBytes()`, `getPublicKeyStr()`, `getPublicToPem()`, which are clearer compared to using Map.

Additionally, the keys themselves can also be encrypted and decrypted for higher security.