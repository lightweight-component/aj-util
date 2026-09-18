---
title: RSA, Signatures, PEM and Certificates
layout: layouts/aj-util.njk
---

# RSA keys and encryption

Use `com.ajaxjs.util.cryptography.rsa.Rsa`, not the removed `KeyMgr`. `generateKeyPair(int)` accepts only **2048, 3072 or 4096** bits. This restriction applies to generation, not a minimum-strength check on every restored/imported key.

```java
import com.ajaxjs.util.cryptography.rsa.Rsa;
import java.security.KeyPair;

KeyPair pair = Rsa.generateKeyPair(2048);
byte[] encrypted = new Rsa("small secret").encryptWithPublicKey(pair.getPublic());
String plaintext = new Rsa(encrypted).decryptToString(pair.getPrivate());
```

The main methods use `RSA/ECB/OAEPWithSHA-256AndMGF1Padding` with explicit SHA-256 for **both OAEP and MGF1**, and an empty label. Public keys encrypt; private keys decrypt. This is not the old provider-default RSA/PKCS#1 scheme, nor a private-key “encryption” signature API.

`Rsa(byte[])` copies its input. `Rsa(String)` encodes UTF-8; it **does not Base64-decode ciphertext**. `encryptWithPublicKeyToBase64(...)` returns Base64; decode it to bytes (e.g. `new Base64Utils(text).decode()`) before constructing a decrypting `Rsa`. Encrypt/decrypt methods accept key objects or supported PEM/Base64 key strings. `decryptWithPrivateKey(...)` returns raw bytes; `decryptToString(...)` is only for UTF-8 plaintext.

OAEP-SHA256 input limit is `modulusBytes - 2*32 - 2`: **190 bytes for a 2048-bit key**, not 190 characters. There is no chunking. Use an explicitly designed hybrid protocol: AES-GCM for the payload and RSA for a short symmetric key. Oversized input, mismatched padding or invalid ciphertext/key fails rather than returning partial plaintext.

The separate static `Rsa.encryptOAEP(String, X509Certificate)` / `decryptOAEP(String base64Ciphertext, PrivateKey)` methods use **OAEP-SHA1** for external protocol compatibility (such as WeChat Pay). They are not interchangeable with the main SHA-256 methods and do not validate certificate trust.

## Sign and verify

```java
import com.ajaxjs.util.cryptography.rsa.DoSignature;
import com.ajaxjs.util.cryptography.rsa.DoVerify;
import com.ajaxjs.util.cryptography.rsa.Rsa;
import java.security.KeyPair;

KeyPair pair = Rsa.generateKeyPair(2048);
String signature = new DoSignature(pair.getPrivate()).signToBase64("message");
boolean valid = new DoVerify(pair.getPublic()).verify("message", signature);
if (!valid) {
    throw new SecurityException("Invalid signature");
}
```

Default is `SHA256withRSA` (JCA signature, not RSA cipher and not RSA-PSS). Constructors accept the appropriate key object or a PEM/Base64 key string; generated all-args constructors take `(String algorithmName, PrivateKey/PublicKey)`. `new DoSignature(String)` interprets its String as a **private key**, not an algorithm name.

`sign(byte[]/String)` returns bytes; `signToBase64(byte[]/String)` returns text. `verify` takes data bytes or UTF-8 text plus signature bytes or Base64. Each call creates a new `Signature`; there are no chainable input/result setters. Ordinary mismatches return false, but malformed signatures may raise `IllegalStateException` (provider `SignatureException`), and malformed Base64/algorithms/keys can raise `IllegalArgumentException`. Null behavior differs by overload: signer byte input/missing key is checked during signing with `IllegalStateException`; null signer String input produces `NullPointerException`; verifier byte input/signature validation uses `IllegalArgumentException`. Handle errors as verification failure, not as acceptance.

## RestoreKey and PemUtils

```java
import com.ajaxjs.util.cryptography.rsa.PemUtils;
import com.ajaxjs.util.cryptography.rsa.RestoreKey;
import com.ajaxjs.util.cryptography.rsa.Rsa;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;

KeyPair pair = Rsa.generateKeyPair(2048);
String publicPem = PemUtils.publicKeyToPem(pair.getPublic());
String privatePem = PemUtils.privateKeyToPem(pair.getPrivate());
PublicKey publicKey = RestoreKey.restorePublicKey(publicPem);
PrivateKey privateKey = RestoreKey.restorePrivateKey(privatePem);
```

- `restorePublicKey(String)` accepts X.509 **SubjectPublicKeyInfo** DER in Base64, optionally wrapped in `BEGIN PUBLIC KEY` PEM. This is a key structure, not an X.509 certificate.
- `restorePrivateKey(String)` accepts unencrypted **PKCS#8** DER/Base64 or `BEGIN PRIVATE KEY` PEM. Whitespace is removed. PKCS#1 `RSA PUBLIC KEY` / `RSA PRIVATE KEY` forms are explicitly rejected; encrypted private-key PEM is not supported.
- Invalid formats raise `IllegalArgumentException`, null input `NullPointerException`, unavailable RSA factory `IllegalStateException`.
- `loadPrivateKey(InputStream)` defaults to UTF-8; the charset overload takes a charset name. Its `DataWriter`/`DataReader` path **closes the supplied input**. A String overload loads a file path. Reading failures can throw unchecked I/O exceptions.
- `PemUtils.encodeKeyBase64(Key)` encodes `getEncoded()`. PEM helpers wrap public/private encodings at 64 columns and also accept Base64 Strings; they do not validate DER or convert PKCS#1 to PKCS#8. Non-exportable keys are not supported by these encoding helpers. Private PEM/Base64 is still secret plaintext key material; protect storage and never log it.

## Certificates

`com.ajaxjs.util.cryptography.CertificateUtils.getCert(String path/InputStream)` parses PEM/DER X.509 and calls `checkValidity()`. The supplied stream is closed. Invalid/expired/not-yet-valid certificates produce `RuntimeException`; file/stream I/O failures can produce `UncheckedIOException`. Parsing and date checks **do not** verify the trust chain, revocation, hostname, intended usage or source authenticity. Establish trust separately before using a certificate public key.

`deserializeToCerts(String apiV3Key, Map<String,Object> response)` is a protocol-specific helper: the key must encode to **32 UTF-8 bytes** (not Base64); `data` must be a list of entries with an `encrypt_certificate` Map containing `ciphertext`, `nonce`, `associated_data`. Ciphertext is Base64; nonce and AAD are UTF-8 strings (surrounding quotes are removed), and GCM requires a 12-byte nonce. It decrypts with `AesGcm`, parses/checks certificate dates and returns a Map keyed by the parsed certificate serial number (`BigInteger`). Missing fields/invalid parameters can raise `IllegalArgumentException`; wrong nested types can raise `ClassCastException`. It is not a general certificate trust validator.

Evidence: `Rsa`, `DoSignature`, `DoVerify`, `RestoreKey`, `PemUtils`, `CertificateUtils`; `rsa/TestRsa`, `rsa/TestVerify`, `rsa/TestRestoreKey`, `rsa/TestPemUtils`, `TestCertificateUtils`. Existing certificate fixtures have time-sensitive validity; do not treat an old fixture or network response as a current trusted certificate.
