---
title: Introduction to Encryption and Decryption Package
description: Introduction to Encryption and Decryption Package
tags:
  - Encryption
  - Decryption
  - Utilities
layout: layouts/aj-util.njk
---

# Encryption and Decryption

`aj-util` wraps Java 8 JCE and security APIs for symmetric encryption, RSA key management, and signatures. The main entry points are [Cryptography](Cryptography.md) and [RSA, keys, and signatures](Rsa.md).

- Symmetric encryption uses one shared secret for encryption and decryption. It is appropriate for ordinary data payloads.
- RSA uses a public/private key pair. It is normally used to exchange or protect a small symmetric key, and for signatures—not for large payloads.
- Prefer AES-GCM or the PBE helper for newly designed storage and protocol data. DES, 3DES, MD5, and SHA-1 are retained only for compatibility with legacy systems.

## Source Code

The final code
is [here](https://gitcode.com/lightweight-component/aj-util/tree/main/src/main/java/com/ajaxjs/util/cryptography).

Usage examples are also available in the module unit tests.

Maven dependency reference, requires Java 8+

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>ajaxjs-util</artifactId>
    <version>1.3.8</version>
</dependency>
```

The module is compatible with Java 8 and has no mandatory third-party runtime dependency for its cryptographic APIs.
