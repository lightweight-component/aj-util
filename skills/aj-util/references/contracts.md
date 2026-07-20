# Behavioral and security contracts

Use these as review checkpoints. Verify current source before relying on exact exception types or overloads.

## Strings and parsers

- `StrUtil.charCount` counts matches at index zero. Its current stepping rule determines whether matches overlap; preserve and document that rule.
- `StrUtil.simpleTpl` treats replacement values literally. `$` and `\` in values must not become regex replacement syntax.
- `StrUtil.leftPad` prepends only the required padding and must not rewrite whitespace already present in the input.
- Query and key/value parsers split each pair on the first `=` only. A single pair is valid; later `=` characters belong to the value.
- `BytesHelper.parseHexStr2Byte` rejects odd-length hexadecimal input rather than truncating it. Check current empty-input semantics before documenting them.
- `ConvertBasicValue` returns an enum constant for name or ordinal conversion, not the ordinal integer. Numeric detection includes negative decimals.

## XML and JSON

- All XML entry points must use the hardened `XmlHelper` builder with DTDs and external entities disabled. This includes `MapTool.xmlToMap`.
- Never log or embed the complete XML input in parsing exceptions.
- Nodes may lack an attribute map; attribute lookup must remain null-safe.
- XML serialization must define behavior for null map values rather than dereferencing them.
- Lazy publication of the shared JSON engine must remain thread-safe.

## Date and time

- A pure date string must parse without requiring a time component.
- Epoch timestamp `0` is a valid value.
- Conversions requiring a missing date, time zone, or offset must use an explicit documented policy; do not silently substitute system "today".
- Detect DST gaps and overlaps instead of silently normalizing ambiguous or nonexistent local times.
- Apply a `Calendar` object's time zone during conversion.
- Do not silently discard the original zone or offset of `ZonedDateTime` and `OffsetDateTime` when the requested target can preserve it.

## Reflection

- Class-hierarchy traversal must terminate safely for interfaces.
- Traverse inherited interfaces recursively and use `Set<Class<?>>` to avoid duplicate visits and cycles.
- Account for primitive/wrapper compatibility, null arguments, overload ambiguity, bridge methods, and accessibility when resolving members.

## I/O and ZIP

- Reject non-positive caller-supplied buffer sizes before reading.
- Require `FileHelper.setTarget` before `copyTo`/`moveTo` and report a clear state error.
- Do not swallow `IOException` or return a partial checksum after a failed read.
- Calculate CRC with buffered reads.
- ZIP extraction must reject path traversal and enforce entry-count, per-entry size, total-size, and compression-ratio limits.
- Directory compression must have explicit symbolic-link behavior. Never follow links implicitly when that can escape the source tree or create cycles.
- Clarify stream ownership: a method should close only resources it opens unless its contract explicitly says otherwise.

## Cryptography

- Never include private keys, plaintext secrets, passwords, or complete ciphertext payloads in exceptions or logs.
- Keep cipher failures accurately categorized and retain the original cause.
- RSA key generation must reject sizes below 2048 bits.
- Prefer authenticated encryption and modern password-based derivation. Legacy DES/3DES and legacy PBE are compatibility-only and must be labeled unsafe for new designs.
- Validate signing/verifying state before cryptographic operations: key, input data, and signature must be present and valid.
- Use `SecureRandom` for security-sensitive randomness; do not substitute general-purpose random generators.
