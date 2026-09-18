---
title: Cipher Flow and Key Handling
layout: layouts/aj-util.njk
---

# DoCipher and CipherResult

`new DoCipher(transformation, Key)` retains configuration, not input or a JCE cipher engine. Every operation creates a new `Cipher`. There are no mode/data setters or no-argument `doCipher()` in the current API.

```java
import com.ajaxjs.util.cryptography.CipherResult;
import com.ajaxjs.util.cryptography.DoCipher;
import com.ajaxjs.util.cryptography.SecretKeyMgr;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

SecretKey key = SecretKeyMgr.getSecretKey("AES", 128, new SecureRandom());
DoCipher cipher = new DoCipher("AES/GCM/NoPadding", key);
byte[] nonce = DoCipher.randomBytes(12);
byte[] aad = "record:v1".getBytes(StandardCharsets.UTF_8);
GCMParameterSpec parameters = new GCMParameterSpec(128, nonce);
CipherResult encrypted = cipher.doCipher(Cipher.ENCRYPT_MODE, "hello", parameters, aad);
CipherResult decrypted = cipher.doCipherFromBase64(
        Cipher.DECRYPT_MODE, encrypted.toBase64(), parameters, aad);
String plaintext = decrypted.toUtf8();
```

The nonce above belongs to **one encryption only**. Use [AesGcm](/aj-util/cryptography/Cryptography/) for automatic nonce generation. Generic `DoCipher` does not package or return provider-generated IVs; specify required parameters and store them yourself.

## Inputs, outputs and errors

- `doCipher(int, byte[], AlgorithmParameterSpec, byte[])` handles raw bytes; the String overload encodes UTF-8. `doCipherFromBase64(...)` decodes binary input first. Pass null AAD for algorithms without AAD support.
- `CipherResult.getResult()` returns raw bytes; `toBase64()` and uppercase `toHex()` encode binary ciphertext. `toUtf8()` is for decrypted UTF-8 plaintext, not arbitrary ciphertext.
- Result arrays are **not defensively copied**. `AesCipherResult` also has a mutable `CipherResult nonce` property, initially null when constructed manually. Do not mutate shared input/result arrays or use these holders as immutable security records.
- Missing/blank transformation, unsupported mode (anything except ENCRYPT/DECRYPT), null key or null byte input cause `IllegalStateException`. Null String/Base64 input causes `NullPointerException`.
- Unsupported transformations, invalid keys/parameters, block length, padding and GCM authentication failures are wrapped as `IllegalArgumentException` with cause. Authentication failure must abort processing; never accept unauthenticated output or fall back to a weaker mode.

Unlike the old stateful API, `DoCipher` and AES wrappers create a fresh engine per call and retain only key/configuration. Do not infer a blanket thread-safety guarantee for caller-supplied keys, mutable result holders or arrays. There is no automatic key destruction; control key lifetime and do not log secrets.

## SecretKeyMgr

`getSecretKey("AES", bits, secureRandom)` generates a random key. Positive sizes initialize the generator explicitly; zero **or negative** sizes fall through to provider defaults in the current body (do not rely on negative sizes being rejected). `getSecretKey(SecureRandom)` selects AES-128. Normal raw AES key lengths are 16/24/32 bytes, with provider validation during use.

`getSecretKey(algorithm, KeySpec)` delegates to `SecretKeyFactory`; `getSecretKey(KeySpec)` selects `PBKDF2WithHmacSHA256`. For raw AES bytes use `new SecretKeySpec(bytes, "AES")` or an AES wrapper, not a password string pretending to be a key. `getSecretKeyAsStr(algorithm, bits, random)` **generates another new key** and returns its Base64 encoding; it does not encode a previously generated key. Non-exportable keys cannot be encoded this way.

`getRandom(algorithm, text)` / `getRandom(text)` (default `SHA1PRNG`) call `SecureRandom.setSeed` with UTF-8 text. This supplements state: it is **not** a portable deterministic password KDF. Use `AesPbe` for password-based encryption. `DoCipher.randomBytes(length)` uses `RandomTools.RANDOM` (`SecureRandom`) and requires a positive length; unrelated `RandomTools` convenience strings/numbers are not substitutes.

Evidence: `DoCipher`, `CipherResult`, `SecretKeyMgr`, `aes/AesCipherResult`, `TestDoCipher`, `TestCipherResult`, `TestSecretKeyMgr`. Prefer method bodies over stale exception comments (notably negative key-size handling).
