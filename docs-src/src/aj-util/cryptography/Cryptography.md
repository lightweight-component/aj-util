---
title: AES and Password-Based Encryption
layout: layouts/aj-util.njk
---

# AES and password-based encryption

These classes live under `com.ajaxjs.util.cryptography.aes`. This URL is retained from the old `Cryptography` page; use the current classes below, not removed static convenience methods.

## AesGcm: preferred for new data

```java
import com.ajaxjs.util.cryptography.SecretKeyMgr;
import com.ajaxjs.util.cryptography.aes.AesCipherResult;
import com.ajaxjs.util.cryptography.aes.AesGcm;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

byte[] key = SecretKeyMgr.getSecretKey("AES", 128, new SecureRandom()).getEncoded();
AesGcm aes = new AesGcm(key);
byte[] aad = "record:v1".getBytes(StandardCharsets.UTF_8);
AesCipherResult encrypted = aes.encrypt("secret", aad);
String ciphertextBase64 = encrypted.toBase64();
byte[] nonce = encrypted.getNonce().getResult();
String plaintext = aes.decrypt(ciphertextBase64, nonce, aad);
```

`AesGcm(byte[])` accepts raw AES key bytes; `AesGcm(String)` accepts a **Base64-encoded key**, not a password. Keys are normally 16/24/32 bytes. `encrypt(String)` and `encrypt(String, byte[] associatedData)` generate a secure random **12-byte nonce** each time. The two-argument byte array is AAD, **not the nonce**. `encrypt(String, byte[] nonce, byte[] associatedData)` is for protocols managing a nonce explicitly and checks its length.

The transformation is `AES/GCM/NoPadding` with a **128-bit tag**. `AesCipherResult` contains `[ciphertext][16-byte tag]`; its separate `getNonce()` holds the nonce. Base64 output does **not** include the nonce. Persist/transmit both, plus key identifier, format version and exact AAD (or a reproducible way to obtain it). AAD is authenticated, not encrypted, and is not stored in the result. Nonces are not secret but must never repeat for a key; random generation is not an enforced uniqueness registry.

`decrypt(String base64Ciphertext, byte[] nonce, byte[] associatedData)` returns UTF-8 plaintext. Wrong keys, nonces, AAD or tampering cause `IllegalArgumentException` on authentication failure. Never reuse a fixed demo nonce in production.

## AesPbe: password-derived AES-GCM

This complete method receives the password from its caller; do not hard-code production passwords.

```java
import com.ajaxjs.util.cryptography.DoCipher;
import com.ajaxjs.util.cryptography.aes.AesPbe;

public class PasswordExample {
    public static String roundTrip(String password) {
        byte[] salt = DoCipher.randomBytes(AesPbe.PBE_SALT_LENGTH);
        int iterations = AesPbe.MIN_PBE_ITERATIONS;
        AesPbe pbe = new AesPbe(password, salt, iterations);
        byte[] encrypted = pbe.encrypt("secret");
        return new AesPbe(password, salt, iterations).decrypt(encrypted);
    }
}
```

`AesPbe(String password, byte[] salt, int iterationCount)` derives **AES-128** using `PBKDF2WithHmacSHA256`. It rejects null/empty passwords, salt shorter than **16 bytes**, and iterations below **100,000**. Whitespace-only passwords are not rejected. This minimum is an implementation floor, not a universal current password-hardening recommendation: benchmark and choose an appropriate higher work factor for your deployment.

`encrypt(String)` returns raw `[12-byte nonce][ciphertext][16-byte tag]`; `decrypt(byte[])` expects this format and rejects null or fewer than 28 bytes. It is **not** the `AesGcm` separate-nonce format. Store the random salt, iteration count, KDF/format version and key policy externally. Use a fresh random salt for each independent derivation. Wrong password/salt/iterations or tampering fails authentication. No AAD convenience parameter is exposed by these PBE methods.

The derived key is retained by the instance. The internal `PBEKeySpec` password is cleared, but the caller's immutable String and all key material are not automatically erased.

## AesCbc: interoperability only

```java
import com.ajaxjs.util.cryptography.SecretKeyMgr;
import com.ajaxjs.util.cryptography.aes.AesCbc;
import com.ajaxjs.util.cryptography.aes.AesCipherResult;
import java.security.SecureRandom;

AesCbc aes = new AesCbc(SecretKeyMgr.getSecretKey("AES", 128, new SecureRandom()).getEncoded());
AesCipherResult encrypted = aes.encrypt("legacy protocol content");
String plaintext = aes.decrypt(encrypted.toBase64(), encrypted.getNonce().toBase64());
```

CBC uses `AES/CBC/PKCS5Padding`. Raw/Base64 key constructors mirror GCM. `encrypt(String)` generates a random **16-byte IV**; `encrypt(String, byte[] iv)` requires exactly 16 bytes. Here `AesCipherResult.getNonce()` actually holds the IV. `decrypt(String, String)` expects **Base64 ciphertext and Base64 IV**, unlike GCM's byte-array nonce.

CBC does not authenticate: successful padding/decryption does not prove integrity. Use only under an existing authenticated protocol with a fresh unpredictable IV; do not invent a CBC protocol or expose padding-error distinctions to attackers. Prefer GCM for new work.

## AesLegacy and lifecycle

`new AesLegacy(keyString).encrypt(text)` returns uppercase hex; `decrypt(hex)` returns UTF-8. The actual constructor selects **`AES/ECB/PKCS5Padding`**, despite older comments describing a bare `AES` transformation. It derives AES-128 via the legacy `SecretKeyMgr.getRandom` seeded-random mechanism, not a portable password KDF. Use only for historical data with verified provider compatibility; ECB leaks repeated blocks and has no authentication.

All these wrappers delegate to a new cipher engine per call. Result arrays and the nonce holder are mutable and not copied; see [DoCipher/CipherResult](/aj-util/cryptography/flow/). They do not implement key rotation, secure storage, message envelopes or automatic destruction. Test evidence: `aes/TestAes`, `aes/TestAesCipherResult` and `TestDoCipher` cover parameter rejection, formats, AAD and tamper/wrong-password failures; fixed/zero keys in tests are not production examples.
