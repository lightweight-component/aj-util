---
title: Cryptography Overview
layout: layouts/aj-util.njk
---

# Cryptography

AJ Util wraps JCA/JCE with explicit keys and per-operation inputs. Current source replaces the former monolithic `Cryptography` and `rsa.KeyMgr` APIs; the existing documentation URLs are retained for bookmarks.

| Package | Current APIs | Use |
| --- | --- | --- |
| `com.ajaxjs.util.cryptography` | `DoCipher`, `CipherResult`, `SecretKeyMgr`, `CertificateUtils` | Generic cipher operations, encodings, symmetric keys, certificate parsing |
| `com.ajaxjs.util.cryptography.aes` | `AesGcm`, `AesCbc`, `AesPbe`, `AesLegacy`, `AesCipherResult` | Authenticated encryption, compatibility modes, password-derived keys |
| `com.ajaxjs.util.cryptography.rsa` | `Rsa`, `DoSignature`, `DoVerify`, `RestoreKey`, `PemUtils` | Small-value RSA-OAEP, signatures, key restoration and PEM encoding |

- Prefer **AES-GCM** for new encrypted payloads. Keep the nonce and exact AAD with the ciphertext/tag; never reuse a nonce with the same key.
- Use **AesPbe** when deriving a key from a password. Persist salt, iteration count and format/version metadata separately from its nonce-prefixed ciphertext.
- RSA encrypts small values, such as a session key, not large files. Signature verification authenticates data; it does not decrypt it.
- Hashes (`HashHelper`) and Base64/hex are not encryption. Hashes can collide; MD5/SHA-1 are not recommended for new security designs. Do not use fast hashes alone for password storage.
- CBC is unauthenticated. Legacy ECB is for historical interoperability only. DES/3DES and old PBE examples in `TestCryptographyLegacy` are test-local helpers, not current library convenience APIs.

Read [cipher flow and key handling](/aj-util/cryptography/flow/), [AES/PBE](/aj-util/cryptography/Cryptography/) and [RSA, PEM and certificates](/aj-util/cryptography/Rsa/).

## Dependency and evidence

Current `aj-util/pom.xml` coordinates (not a claim of latest published version), Java 8 baseline:

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>ajaxjs-util</artifactId>
    <version>1.3.8</version>
</dependency>
```

[Source](https://github.com/lightweight-component/aj-util/tree/main/aj-util/src/main/java/com/ajaxjs/util/cryptography) and tests under `aj-util/src/test/java/com/ajaxjs/util/cryptography/` are the authority. Relevant tests include `TestDoCipher`, `TestCipherResult`, `TestSecretKeyMgr`, `aes/TestAes`, `aes/TestAesCipherResult`, `rsa/TestRsa`, `rsa/TestVerify`, `rsa/TestPemUtils` and `rsa/TestRestoreKey`. Round trips are not a security audit; provider support and deployed JDK policy still matter.
