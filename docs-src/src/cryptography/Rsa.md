---
title: RSA Keys and Signatures
description: Generate RSA keys, encrypt small values, and create or verify signatures.
tags:
  - Cryptography
  - RSA
layout: layouts/aj-util.njk
---

# RSA Keys and Signatures

`KeyMgr` generates and restores RSA key pairs. Supported generated key sizes are 2048, 3072, and 4096 bits.

```java
import com.ajaxjs.util.cryptography.rsa.KeyMgr;

KeyMgr keys = new KeyMgr(KeyMgr.RSA, 2048);
keys.generateKeyPair();

String publicKeyPem = keys.getPublicToPem();
String privateKeyPem = keys.getPrivateToPem();
```

Keys can be exported as Base64 or PEM and restored with the corresponding `restore...` methods. `KeyMgr` also supplies public-key and private-key encryption/decryption helpers.

RSA has a strict payload-size limit. Encrypt a small session key with RSA and encrypt ordinary data with AES instead.

## Sign and verify

`DoSignature` and `DoVerify` provide a chainable API for signatures such as `SHA256withRSA`.

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
