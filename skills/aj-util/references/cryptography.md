# Refactored cryptography contracts

Authority: `aj-util/src/main/java/com/ajaxjs/util/cryptography/` and matching tests. Documentation retains `docs-src/src/aj-util/cryptography/{intro,flow,Cryptography,Rsa}[-cn].md` URLs, but not obsolete APIs.

## API and lifecycle

- `new DoCipher(String transformation, Key key)`; call `doCipher(int mode, byte[]/String data, AlgorithmParameterSpec spec, byte[] aad)` or `doCipherFromBase64(int, String, spec, aad)`. String means UTF-8 plaintext, not encoded ciphertext. No old data/mode setters or no-arg operation.
- Each call creates a new JCE Cipher. Only configuration/key is retained. Missing configuration/key/byte data or unsupported mode raises IllegalStateException; null String/Base64 input raises NullPointerException. Transformation/key/parameters/block/padding/authentication errors become IllegalArgumentException with cause.
- `CipherResult(byte[])`: `getResult`, `toUtf8`, `toBase64`, `toHex` (uppercase). Use UTF-8 only for known text plaintext. Arrays are not copied. `AesCipherResult` extends it with mutable `CipherResult nonce`, initially null in a manually constructed holder. Do not assert blanket immutability/thread safety.
- `DoCipher.randomBytes(positiveLength)` uses SecureRandom via RandomTools.RANDOM. Do not substitute ThreadLocalRandom string/number helpers. There is no key destruction/rotation/envelope management.

## AES

| Class | Construction and calls | Format and requirements |
| --- | --- | --- |
| `aes.AesGcm` | Raw byte[] or Base64 String key; encrypt(String), encrypt(String, aad), encrypt(String, nonce, aad); decrypt(base64Ciphertext, nonceBytes, aad) | AES/GCM/NoPadding; exactly 12-byte nonce; 128-bit tag; result is ciphertext+tag, nonce separate in getNonce(). Two-argument encrypt's array is AAD, not nonce. |
| `aes.AesCbc` | Raw/Base64 key; encrypt(String), encrypt(String, ivBytes); decrypt(base64Ciphertext, base64Iv) | AES/CBC/PKCS5Padding; 16-byte IV in getNonce(); no authentication. |
| `aes.AesPbe` | `(String password, byte[] salt, int iterations)`; encrypt(String) -> byte[]; decrypt(byte[]) -> String | PBKDF2WithHmacSHA256 derives AES-128; random 12-byte nonce prepended to ciphertext+16-byte tag. Salt/iterations not included. |
| `aes.AesLegacy` | `(String keyString)`; encrypt(String) -> hex; decrypt(hex) -> UTF-8 | Actual transformation AES/ECB/PKCS5Padding; AES-128 via seeded-random legacy key generation, not a portable KDF. |

GCM: never reuse a nonce for a key; persist exact AAD or its reconstruction rule. Default encrypt generates random nonces but does not track uniqueness. Tamper/wrong key/AAD must abort processing, never fall back to CBC/ECB.

PBE: salt >=16 bytes, iterations >=100,000, non-null/nonempty password (whitespace is accepted), ciphertext >=28 bytes. Minimum iterations are a floor, not a current universal recommendation. Choose a work factor for deployment, fresh random salt per independent derivation, and persist salt/iterations/KDF/version externally. The String password is not erased; only PBEKeySpec is cleared. No AAD convenience overload.

CBC/ECB are compatibility-only. CBC requires independent integrity protection within an established protocol; padding success is not authentication. DES/3DES/old PBE helpers in TestCryptographyLegacy are test-local, not exported APIs.

## SecretKeyMgr

- `getSecretKey(algorithm, bits, SecureRandom)` generates a key; positive bits initialize size, zero or negative falls through to provider defaults in current body. `getSecretKey(SecureRandom)` uses AES-128.
- `getSecretKey(algorithm, KeySpec)` delegates to SecretKeyFactory; `getSecretKey(KeySpec)` selects PBKDF2WithHmacSHA256. For raw AES use SecretKeySpec(bytes, "AES") or an AES wrapper.
- `getSecretKeyAsStr(algorithm, bits, random)` generates a new key then Base64-encodes it; not an encoder for an existing key. Non-exportable keys need different handling.
- `getRandom([algorithm,] text)` supplements SecureRandom state using UTF-8; default SHA1PRNG. It is not deterministic password derivation across providers/runtimes.

## RSA, signatures and encodings

- `rsa.Rsa.generateKeyPair(bits)` allows 2048/3072/4096 only; imported keys do not undergo the same strength gate.
- `new Rsa(byte[])` copies input; String constructor encodes UTF-8 and does not Base64-decode. Main encryptWithPublicKey[ToBase64]/decryptWithPrivateKey/decryptToString methods accept key objects or PEM/Base64 keys. OAEP-SHA256 explicitly uses MGF1-SHA256 and empty label. Limit: modulusBytes-66 (190 bytes for 2048 bits). No chunking; use hybrid encryption for large data.
- Static encryptOAEP(message, certificate)/decryptOAEP(base64Ciphertext, privateKey) use separate OAEP-SHA1 compatibility scheme; not interchangeable with the main API and not trust validation.
- `DoSignature(PrivateKey/String privateKey)` and `DoVerify(PublicKey/String publicKey)` default to SHA256withRSA (not PSS). All-args constructors are (algorithm, key). Single String signer constructor is a key, not algorithm. sign/signToBase64 take data; verify takes data and signature (raw or Base64); no chainable setters. Fresh Signature per call. Mismatch normally false; malformed signatures can throw IllegalStateException, invalid Base64/algorithms/keys IllegalArgumentException. Null contracts differ by overload: inspect bodies.
- RestoreKey.restorePublicKey accepts X.509 SubjectPublicKeyInfo, not a certificate; restorePrivateKey accepts unencrypted PKCS#8. Both allow Base64 or expected PEM boundaries and whitespace. PKCS#1 RSA PUBLIC/PRIVATE KEY and encrypted private PEM are unsupported. loadPrivateKey(InputStream[, charsetName]) defaults UTF-8 and closes input via DataWriter/DataReader; String overload is a file path.
- PemUtils encodes exported key bytes to Base64 or PUBLIC KEY/PRIVATE KEY PEM (64-column wrapping). String overloads wrap, not validate/convert DER. Never log private key material.

## Certificates and source mismatches

CertificateUtils.getCert(path/InputStream) parses X.509 and checks dates; closes input. Not trust-chain, hostname, revocation or usage validation. deserializeToCerts takes a 32-UTF-8-byte API v3 key and a data list of encrypt_certificate Maps (Base64 ciphertext, UTF-8 nonce/AAD), decrypts via GCM and indexes parsed certificates by serial BigInteger. Missing fields and wrong nested types can throw; trust remains external.

AesLegacy comments still describe bare AES, but body explicitly selects ECB. SecretKeyMgr comments suggest invalid sizes rejected, but negative sizes use defaults. Some signature null exception comments differ from bodies. Certificate fixtures are time-sensitive. Preserve Java source for documentation-only tasks and report these mismatches rather than repairing them implicitly.

Tests: TestDoCipher, TestCipherResult, TestSecretKeyMgr, aes/TestAes, aes/TestAesCipherResult, rsa/TestRsa, rsa/TestVerify, rsa/TestRestoreKey, rsa/TestPemUtils, TestCertificateUtils. Round trips/fixed test keys do not establish production security.
