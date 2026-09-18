---
title: RandomTools
description: Random numbers, strings, secure random bytes, and UUID version 7 strings.
tags:
  - random
  - UUIDv7
layout: layouts/aj-util.njk
---

# RandomTools

`RandomTools` generates ordinary random numbers and strings, exposes a shared `SecureRandom`, and creates UUID version 7 strings.

```java
int code = RandomTools.generateNumber();          // six digits, 100000–999999
String text = RandomTools.generateRandomString(8); // eight letters/digits

String compactId = RandomTools.uuidV7();          // 32 lowercase hex characters
String standardId = RandomTools.uuidV7(true);     // 8-4-4-4-12 format
Date createdAt = RandomTools.showTime(compactId);
```

`generateNumber(int)` accepts 1 through 9 digits, and `generateRandomString(int)` requires a positive length. Both use `ThreadLocalRandom` and are not suitable for passwords, authentication codes, tokens, or cryptographic keys.

`uuidV7` uses `RandomTools.RANDOM` (`SecureRandom`) for its random portion and embeds the current epoch millisecond. UUIDv7 is time-ordered by that timestamp but is not guaranteed monotonic inside the same millisecond or if the system clock moves backward. It is an identifier, not a secret token. `showTime` accepts hyphenated or 32-character UUIDv7 input and rejects other UUID versions.

Use `RandomTools.RANDOM` or `DoCipher.randomBytes()` when cryptographically strong random bytes are needed.

## UUIDv7 format and validation

UUIDv7 includes a 48-bit Unix-millisecond timestamp, version bits, RFC UUID variant bits, and 74 random bits. The
default `uuidV7()` is equivalent to `uuidV7(false)`; it returns a compact 32-character lowercase value. Choose the
hyphenated form when interoperating with APIs that expect `UUID.toString()` format.

```java
String id = RandomTools.uuidV7(true);
Date timestamp = RandomTools.showTime(id);

// Either form is accepted by showTime:
Date sameTimestamp = RandomTools.showTime(id.replace("-", ""));
```

`showTime` does not trim whitespace and throws `IllegalArgumentException` for malformed input or a UUID that is not
version 7. The returned `Date` is the timestamp embedded in the identifier; it is not independent proof of the actual
creation time.

## Randomness choices

| API | Generator | Suitable for |
| --- | --- | --- |
| `generateNumber`, `generateRandomString` | `ThreadLocalRandom` | UI samples, non-security identifiers |
| `uuidV7` | shared `SecureRandom` random portion | ordinary unique identifiers |
| `RandomTools.RANDOM` / `DoCipher.randomBytes` | `SecureRandom` | nonces, salts, key material |

None of these APIs keeps a uniqueness registry. Store and enforce uniqueness where the application requires it.

## Implementation notes

The numeric and alphanumeric helpers use `ThreadLocalRandom`, avoiding contention on a shared pseudo-random generator
in ordinary concurrent application code. UUIDv7 takes a different path: it fills 16 bytes from a shared
`SecureRandom`, then overwrites the timestamp, version, and variant positions according to UUIDv7 layout before
formatting with `UUID`. The timestamp extraction method reverses that layout from the UUID most-significant bits.

This design gives sortable identifiers and a strong random component, but it does not coordinate calls made in the
same millisecond. If strict database ordering is required, store a separate sequence or ordering column.
