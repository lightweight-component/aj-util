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

## HTTP, dates and reflection

- Request/Response and HTTP enums/constants live under httpremote.model.
- Request requires init before connect, considers status < 400 successful, and modifies response text via readAsString plus trim. BatchDownload has unbounded per-URL threads and incomplete timeout/error reporting.
- SkipSSL changes global JVM TLS defaults; do not recommend it for production. HTTP and JSON logs need sensitive-data review.
- Date parsing is restricted to years 1900–2099. Variable-width parsing and skipped start-of-day policy remain unresolved; state charset, timezone and DST policy explicitly.
- NewInstance searches public constructors by exact runtime types, not general compatible overloads. Methods compatibility does not resolve all ambiguities, numeric widening or varargs. executeDefault is currently incompatible with the tested JDK 17 private Lookup constructor.
- Declared-method traversal intentionally excludes Object.

## Cryptography

Prefer explicit authenticated encryption with Cryptography's GCM facilities after checking current signatures. Legacy AES/DES/3DES convenience methods and bare RSA transformations retain unsafe or provider-dependent defaults; successful round-trip tests do not prove security.
PBE uses PBKDF2 and AES-GCM, but salt and iteration settings must be stored externally alongside nonce/ciphertext/tag.
Mutable crypto objects and array accessors should not be shared across threads or requests.
Certificate parsing and validity-date checks are not trust-chain or hostname verification. getCert(InputStream) closes the supplied stream.
Never log keys, passwords, plaintext or full sensitive payloads.
