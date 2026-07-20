---
title: Date Type Conversion
description: Converting legacy and Java 8 date/time types safely
tags:
  - Date Conversion
layout: layouts/aj-util.njk
---

# Date Type Conversion

`DateTypeConvert` converts legacy `Date`, `Calendar`, SQL date types, epoch milliseconds, and Java 8 `java.time` values. Supply a `ZoneId` whenever a conversion needs a civil time zone.

```java
Instant instant = Instant.now();
ZoneId zone = ZoneId.of("Asia/Shanghai");

LocalDateTime local = new DateTypeConvert(instant).to(LocalDateTime.class, zone);
Calendar calendar = new DateTypeConvert(instant).to(Calendar.class, zone);
```

## Important semantics

- Epoch value `0` is valid and represents `1970-01-01T00:00:00Z`.
- Date-only strings are parsed as dates instead of being forced through a date-time formatter.
- A `Calendar` result uses the requested zone, not an unrelated system-default calendar.
- `ZonedDateTime` and `OffsetDateTime` inputs are returned unchanged when the requested target is the same type, preserving their original zone or offset.
- Converting `LocalDateTime` to an instant is strict: a daylight-saving gap is rejected, and an overlap is rejected as ambiguous instead of silently choosing an offset.
- `OffsetTime` has no date. It may be converted only to `OffsetTime` or `LocalTime`; instant-based conversions are rejected rather than attaching the system's current date.

```java
Instant epoch = new DateTypeConvert(0L).to(Instant.class, ZoneOffset.UTC);
assertEquals(Instant.EPOCH, epoch);
```

If no input has been set or a conversion is inherently ambiguous, the API throws an exception instead of silently inventing missing context.
