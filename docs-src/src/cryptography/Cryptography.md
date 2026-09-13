---
title: Symmetric Encryption
description: Use Cryptography for symmetric encryption and PBE encryption.
tags:
  - Cryptography
  - AES
layout: layouts/aj-util.njk
---

# Symmetric Encryption

`Cryptography` is a stateful wrapper around `javax.crypto.Cipher`. Create a new instance for each independent operation; do not share one instance between threads.

## Generic Cipher API

Provide the transformation, cipher mode, key material, and source data, then obtain bytes, text, Base64, or hexadecimal output.

```java
import com.ajaxjs.util.cryptography.Cryptography;
import javax.crypto.Cipher;

Cryptography cipher = new Cryptography("AES", Cipher.ENCRYPT_MODE);
cipher.setKeyData(keyBytes);
cipher.setDataStr("hello");
String encrypted = cipher.doCipherAsBase64Str();
```

For decryption, construct the helper with `Cipher.DECRYPT_MODE`, supply the same key, and load the ciphertext using `setDataStrBase64()`.

The class also provides convenience methods for AES, DES, and 3DES. Use DES and 3DES only when communicating with an existing legacy system.

## Password-based encryption

`PBE_encode()` and `PBE_decode()` derive an AES-GCM key using PBKDF2 with HMAC-SHA256. Use a unique random salt per encrypted value and store it alongside the ciphertext.

```java
byte[] salt = Cryptography.initSalt();
byte[] encrypted = Cryptography.PBE_encode("secret", password, salt, 100_000);
String plain = Cryptography.PBE_decode(encrypted, password, salt, 100_000);
```

The iteration count must be at least `100_000`. AES-GCM authenticates the ciphertext: decryption fails if it is altered or the password, salt, or nonce does not match.
