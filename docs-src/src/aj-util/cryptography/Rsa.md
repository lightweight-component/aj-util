---
title: RSA, Signatures, PEM and Certificates
layout: layouts/aj-util.njk
---

# RSA keys and encryption

`com.ajaxjs.util.cryptography.rsa.Rsa` is a **key-bound** RSA cipher. Create one instance with a recipient public key to encrypt, or another with the corresponding private key to decrypt. It replaces both `RsaDefault` and the former stateful `Rsa` API; key-pair generation now belongs to `RestoreKey`.

```java
import com.ajaxjs.util.cryptography.rsa.PemUtils;
import com.ajaxjs.util.cryptography.rsa.RestoreKey;
import com.ajaxjs.util.cryptography.rsa.Rsa;
import java.security.KeyPair;

KeyPair pair = RestoreKey.generateKeyPair(2048);
String publicPem = PemUtils.publicKeyToPem(pair.getPublic());
String privatePem = PemUtils.privateKeyToPem(pair.getPrivate());

String ciphertextBase64 = new Rsa(publicPem, true)
        .encrypt("small secret")
        .toBase64();
String plaintext = new Rsa(privatePem, false).decrypt(ciphertextBase64);
```

The default transformation is `RSA/ECB/OAEPWithSHA-256AndMGF1Padding`. Both the OAEP digest and MGF1 digest are explicitly SHA-256 and the OAEP label is empty. The same transformation and parameters must be used at both ends. A public-key instance can only encrypt and a private-key instance can only decrypt; this API is not a substitute for signing.

`encrypt(String)` UTF-8 encodes text and returns a binary `CipherResult`. Base64 is therefore the normal wire/storage representation: `encrypt(...).toBase64()` is the counterpart of `decrypt(String)`. For binary content, call `encrypt(byte[])` and recover it with `decryptToResult(byte[])`; `CipherResult.getResult()` returns the recovered bytes. `decryptToResult(String, boolean)` is available when a caller must state whether a ciphertext string is Base64. Avoid `decryptRawUtf8Ciphertext(String)`: arbitrary encrypted bytes are not generally valid UTF-8, and that method exists only for a legacy representation.

OAEP-SHA256 accepts at most `modulusBytes - 2*32 - 2` plaintext bytes: **190 bytes with a 2048-bit key**. That is bytes, not characters, and no chunking is performed. For a file or other large payload, use a hybrid design: encrypt the payload with AES-GCM and use RSA only for the short random AES key. Invalid ciphertext, an oversized plaintext, a wrong key, or mismatched padding fails rather than returning partial plaintext.

## Compatibility transformations and certificates

For an integration that explicitly requires OAEP SHA-1, choose the public compatibility constant on **both** instances:

```java
String ciphertextBase64 = new Rsa(Rsa.RSA_CIPHER_SHA1, publicPem, true)
        .encrypt("protocol value")
        .toBase64();
String value = new Rsa(Rsa.RSA_CIPHER_SHA1, privatePem, false)
        .decrypt(ciphertextBase64);
```

This mode explicitly uses SHA-1 for OAEP and MGF1. It is for protocol compatibility, not a preferred setting for a new integration. Other custom-transformation constructors accept only transformations registered by `Rsa`, so they retain matching OAEP parameters; use `DoCipher` directly for a deliberately different JCA transformation or OAEP label.

`new Rsa(X509Certificate)` is convenient when a peer supplies its RSA public key in a certificate. The constructor uses the public key only. Parsing a certificate—or constructing `Rsa` from it—does not establish the certificate's chain of trust, revocation status, hostname, intended usage, or source authenticity.

## Sign and verify

RSA encryption and signatures use distinct JCA operations. Use `DoSignature` with a private key and `DoVerify` with the corresponding public key:

```java
import com.ajaxjs.util.cryptography.rsa.DoSignature;
import com.ajaxjs.util.cryptography.rsa.DoVerify;

String signature = new DoSignature(pair.getPrivate()).signToBase64("message");
boolean valid = new DoVerify(pair.getPublic()).verify("message", signature);
if (!valid) {
    throw new SecurityException("Invalid signature");
}
```

The default signature algorithm is `SHA256withRSA`—a JCA RSA signature, not an RSA cipher and not RSA-PSS. Constructors accept the appropriate key object or PEM/Base64 key text; all-argument constructors use `(String algorithmName, PrivateKey/PublicKey)`. `new DoSignature(String)` interprets its String as a private key, not an algorithm name.

`sign(byte[]/String)` returns raw bytes and `signToBase64(byte[]/String)` returns text. `verify` accepts bytes or UTF-8 text plus a raw or Base64 signature. A normal mismatch returns `false`; malformed signatures, Base64, keys, or algorithms can raise an exception and must never be treated as a valid result.

## RestoreKey and PemUtils

```java
import com.ajaxjs.util.cryptography.rsa.PemUtils;
import com.ajaxjs.util.cryptography.rsa.RestoreKey;
import java.security.PrivateKey;
import java.security.PublicKey;

PublicKey publicKey = RestoreKey.restorePublicKey(publicPem);
PrivateKey privateKey = RestoreKey.restorePrivateKey(privatePem);
```

- `RestoreKey.generateKeyPair(int)` accepts only **2048**, **3072**, or **4096** bits. This constrains generated keys; it is not a minimum-strength check for every restored key.
- `restorePublicKey(String)` accepts Base64 or `BEGIN PUBLIC KEY` PEM containing X.509 **SubjectPublicKeyInfo**. This structure is a public key, not an X.509 certificate.
- `restorePrivateKey(String)` accepts unencrypted Base64 or `BEGIN PRIVATE KEY` PEM containing **PKCS#8**. Whitespace is removed. PKCS#1 `RSA PUBLIC KEY` / `RSA PRIVATE KEY` and encrypted private-key PEM are deliberately unsupported.
- `loadPrivateKey(InputStream)` defaults to UTF-8, and its stream-reading path closes the supplied input. The String overload reads a file path.
- `PemUtils` emits Base64 or 64-column PEM for exportable keys. It wraps text; it does not validate DER or convert PKCS#1 to PKCS#8. PEM/Base64 private-key text is still secret material: protect it at rest and never log it.

## Certificates

`com.ajaxjs.util.cryptography.CertificateUtils.getCert(String path/InputStream)` parses PEM/DER X.509 and calls `checkValidity()`; the supplied stream is closed. Invalid, expired, or not-yet-valid certificates cause an exception. Parsing and date validation do **not** verify a trust chain, revocation, hostname, intended usage, or source authenticity. Establish those properties separately before relying on a certificate public key.

`deserializeToCerts(String apiV3Key, Map<String,Object> response)` is a protocol-specific helper. Its key must encode to **32 UTF-8 bytes** (not Base64), and its `data` must contain `encrypt_certificate` maps with Base64 ciphertext plus UTF-8 nonce and associated data. It decrypts through `AesGcm`, parses/checks certificate dates, and returns certificates indexed by serial number (`BigInteger`). It is not a general certificate trust validator.

Evidence: `Rsa`, `DoSignature`, `DoVerify`, `RestoreKey`, `PemUtils`, `CertificateUtils`; `rsa/TestRsa`, `rsa/TestVerify`, `rsa/TestRestoreKey`, `rsa/TestPemUtils`, `TestCertificateUtils`.
