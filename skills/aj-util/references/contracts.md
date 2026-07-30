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
- Describe the XML builder as applying security restrictions; do not say that its security features are disabled.
- Never log or embed the complete XML input in parsing exceptions.
- Nodes may lack an attribute map; attribute lookup must remain null-safe.
- XML serialization must define behavior for null map values rather than dereferencing them.
- Lazy publication of the shared JSON engine must remain thread-safe.

## Date and time

- A pure date string must parse without requiring a time component.
- `DateTools.object2Date` returns `null` for null, blank, or unsupported inputs, but malformed non-blank date strings throw a parsing exception.
- ISO instant formatting preserves available fractional seconds but does not guarantee the system clock's microsecond or nanosecond resolution.
- Epoch timestamp `0` is a valid value.
- Conversions requiring a missing date, time zone, or offset must use an explicit documented policy; do not silently substitute system "today".
- Detect DST gaps and overlaps instead of silently normalizing ambiguous or nonexistent local times.
- Apply a `Calendar` object's time zone during conversion.
- Do not silently discard the original zone or offset of `ZonedDateTime` and `OffsetDateTime` when the requested target can preserve it.

## Reflection

- Class-hierarchy traversal must terminate safely for interfaces.
- Traverse inherited interfaces recursively and use `Set<Class<?>>` to avoid duplicate visits and cycles.
- The typed `Clazz.getClassByName` overload must validate assignability to its requested target type.
- `Types.type2class` resolves a `ParameterizedType` through its raw type.
- `Types.getActualClass` throws `IllegalArgumentException` when no parameterized superclass or resolvable first class argument exists.
- Reflection lookup must not convert a `SecurityException` into a missing-member result.
- Use `Methods.findCompatibleMethod` as the main runtime-argument lookup API. It traverses superclass
  and inherited-interface relationships with duplicate-safe type traversal.
- `Methods.findPublicExactMethod` is the value-based public fast path. It returns `null` when any value is
  `null`, allowing compatible lookup to decide among reference parameters.
- `Methods.findPublicExactMethodByTypes` searches public methods using explicit exact types and rejects null
  elements. It is the appropriate entry point for distinctions such as `int.class` versus `Integer.class`.
- `Methods.findDeclaredMethodByTypes(String, Class<?>...)` searches the configured class and its superclasses,
  excluding `Object`, and uses explicit exact parameter types.
- `Methods.findDeclaredMethod(String, Object...)` uses exact runtime types and returns `null` when a value is
  `null`; callers must use `findDeclaredMethodByTypes` when the declared type of a null argument is known.
- Name-based `Methods.execute` invokes public methods only: runtime values use exact lookup followed by compatible
  fallback, while the explicit-type overload uses exact public lookup. Invoke a non-public method only by resolving
  it explicitly with `findDeclaredMethodByTypes` and passing the resulting `Method` to `execute`.
- `Methods.execute` preserves a legitimate target `null` return, throws `IllegalArgumentException` for a missing
  method, and propagates target invocation failures.
- `Fields.getUnderLayerErr` returns a cause-less wrapper unchanged and rejects a null input explicitly.
- Compatible member resolution must account for primitive/wrapper compatibility, numeric primitive widening,
  null arguments, varargs, overload ambiguity, bridge methods, and accessibility.
- `NewInstance` intentionally searches public constructors only; do not treat lack of private-constructor support
  as a defect. Compatible public-constructor matching remains tracked in `to_fix.md`.

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
