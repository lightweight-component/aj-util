# Current contracts and review boundaries

These are current behaviors, not guarantees that all known issues have been fixed.
Consult `aj-util/to_fix.md` before relying on edge cases.

## Encoding, hashes and random values

- UrlCodec accepts non-null text and defaults to UTF-8. encodeForm/decodeForm use form semantics (space as +). encodeQueryValue uses %20 for space; decodeQueryValue preserves literal +. Neither is a whole-URL or path-segment API. concatUrl is simple joining, not URI resolution.
- StringBytes.bytesToHex emits uppercase hex. hexToBytes accepts empty text but rejects odd length and non-hex characters. Default string/byte conversion still uses the platform charset; choose explicit UTF-8 methods for portable data.
- Base64Utils supports byte[] or String input and an explicit Charset constructor. Encoding is not encryption.
- HashHelper requires an explicit key for HMAC: getMac throws IllegalStateException when absent. hash dispatches Hmac-prefixed algorithms to MAC; hashAsStr emits lowercase hex.
- RandomTools.generateNumber accepts 1–9 digits and excludes a leading zero. Random strings require positive length. These ThreadLocalRandom-based helpers are not suitable for passwords or tokens.
- RandomTools.uuidV7() returns 32 hex characters; uuidV7(true) includes hyphens. showTime accepts compact or hyphenated UUIDs and checks version 7, but does not validate variant or enforce canonical formatting. UUIDv7 reveals its timestamp and is not promised to be monotonic.

## Structured data and capacity

- JSON uses a pluggable engine, with Jackson 2 as the default implementation. Parsing failures are wrapped in RuntimeException, not silently returned as null. Error logging can still expose full input.
- XML map conversion belongs to XmlHelper, not MapTool. Invalid XML/DOCTYPE input is rejected with IllegalArgumentException. A missing nodeAsMap XPath returns an empty map.
- ObjectHelper.getInitialCapacity rejects negatives; values below 3 return expectedSize + 1, values below 1 << 30 use the load-factor formula, and larger values return Integer.MAX_VALUE. Do not reuse tests expecting minimum 16 or a 1 << 30 cap without reconciling the policy.
- ConvertBasicValue numeric conversions may truncate or overflow. Do not assume exact conversion.

## I/O and archives

- DataReader closes the supplied input. readAsString currently changes line endings and appends a final newline; it is not lossless. Its byte callback receives a reused buffer: copy data before retaining it.
- Instance DataWriter.write(InputStream) closes input via DataReader and flushes, but does not close output. Static DataWriter.write(OutputStream, InputStream) neither closes nor flushes either stream. They do not have identical ownership contracts.
- The instance three-argument byte write treats (off=0, length=0) as the full array: an unresolved issue.
- FileHelper writes text as UTF-8, but getFileContent still reconstructs lines and loses the original trailing-newline distinction. Merge rejects nonregular/symlink chunks and does not publish partial output on validation failure.
- ResourceHelper supports optional targetClass-relative resources. Use getStream for JAR resources; getPath is filesystem-oriented. Test resources must be in the matching package under src/test/resources.
- ZipHelper(String, String) takes a source directory. For a single file use the File constructor; File[] inputs use flat, unique names. Call zip(); setUseStore(boolean) is a separate void setter. Compression rejects symlinks and publishes through a temporary file.
- UnzipHelper takes a ZIP path, optional target directory and optional ExtractionLimits. Call extract(); setCharset(Charset) is a separate setter for archives such as GBK ZIPs. Null target uses the archive's sibling stem directory.
- Extraction defaults: 10,000 entries, 1 GiB per entry, 10 GiB total, ratio 100. Extraction checks traversal, symlinks and limits, but custom ExtractionLimits lack validation (including NaN ratios). Do not claim arbitrary custom policies are safe.

## HTTP ownership, dates and reflection

- HTTP client APIs moved to aj-http without changing the com.ajaxjs.util.httpremote namespace. Use the aj-http skill and its source/contracts; do not infer Request's package or success semantics from older AJ Util docs.
- JSON logs still need sensitive-data review.
- Date parsing is restricted to years 1900–2099. Variable-width parsing and skipped start-of-day policy remain unresolved; state charset, timezone and DST policy explicitly.
- NewInstance searches public constructors by exact runtime types, not general compatible overloads. Methods compatibility does not resolve all ambiguities, numeric widening or varargs. executeDefault is currently incompatible with the tested JDK 17 private Lookup constructor.
- Declared-method traversal intentionally excludes Object.

## Cryptography

Read [cryptography.md](cryptography.md) for current signatures and format/security boundaries. Prefer AesGcm; DoCipher takes transformation/key at construction and operation inputs per call. AES wrappers create a fresh engine per call, but result arrays/nonce holders are mutable and not copied.
AesPbe uses PBKDF2-HMAC-SHA256 and AES-128-GCM, prepends its nonce, and requires external salt/iteration metadata. Rsa uses explicit OAEP-SHA256/MGF1-SHA256; only its protocol-specific OAEP helpers use SHA-1. AesLegacy explicitly uses AES/ECB/PKCS5Padding and seeded random key generation, not a portable password KDF.
Removed Cryptography/KeyMgr APIs and DES/3DES helpers in test code are not public library APIs. Round trips do not prove security.
Certificate parsing and validity-date checks are not trust-chain or hostname verification. getCert(InputStream) and RestoreKey.loadPrivateKey(InputStream) close the supplied stream.
Never log keys, passwords, plaintext or full sensitive payloads.
