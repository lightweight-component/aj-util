---
title: A Clean and Clear Java Date API
description: A Clean and Clear Java Date API
tags:
  - Date handling
layout: layouts/aj-util.njk
---
# A Clean and Clear Java Date API

When it comes to date types, they are relatively more tricky to handle compared to other primitive data types (int/long/boolean/string), and there are multiple reasons for this. Firstly, date is a broad concept - year-month-day can be called a date, year-month-day-hour-minute-second can also be called a date, or even just hour-minute-second alone can be considered a date. So which date are you actually referring to? The traditional Java date type `java.util.Date` does exactly this - one type encompasses all the above date concepts, which is the root cause of the混乱 in date APIs. Secondly, the way dates are expressed, also known as "format", can be `2025-12-22 11:05`, or `2025-12-22 11-05` or even `3 Jun 2025 11:05`, with many varieties. Finally, dates also have the concept of time zones, such as `2024-12-03T10:15:30+01:00[Europe/Paris]`, `Tue, 3 Jun 2025 11:05:30 GMT`, etc.

## About the `Date` Type

In early Java versions, the `java.util.Date` type dominated everything, with many date processing methods built around it (before Java 1.1). In later version refactoring, many of these methods were deprecated and replaced by `java.util.Calendar` and `java.text.DateFormat`. Did Calendar solve the tricky date problems? Not really. Although Calendar handles dates more flexibly, it is still overly complex to use, and setting, comparing, and formatting remain troublesome and verbose. Therefore, the community came up with a replacement - Joda-Time, which was adopted by many projects. This excellent open-source project gradually became part of the Java standard, known as JSR310, and was finally integrated into Java 8 as `java.time`!

In summary, the date types we advocate using are `Instant` and `LocalDateTime` from `java.time`. `Instant` represents a moment in time, roughly corresponding to a timestamp, and can support nanosecond precision. `LocalDateTime` might be more commonly used by us, along with its other derived precise types.

### 📅 New Java Common Date/Time Types

| Type | Package | Java Version | Description | Recommended |
|------|---------|--------------|-------------|-------------|
| `Instant` | `java.time.Instant` | 8+ | Represents an instantaneous point on the timeline (UTC), precise to nanoseconds | ✅ Recommended for timestamps |
| `LocalDate` | `java.time.LocalDate` | 8+ | **Date only** (year-month-day), such as `2025-03-08` | ✅ Strongly recommended for "pure date" scenarios (such as train departure dates) |
| `LocalTime` | `java.time.LocalTime` | 8+ | **Time only** (hour:minute:second.nanosecond), such as `14:30:00` | ✅ Recommended for "pure time" scenarios (such as departure times) |
| `LocalDateTime` | `java.time.LocalDateTime` | 8+ | Date + Time (no time zone), such as `2025-03-08T14:30:00` | ✅ Recommended for local time (such as train schedules) |
| `ZonedDateTime` | `java.time.ZonedDateTime` | 8+ | Date/time with time zone, supports daylight saving time, etc. | ✅ Recommended for cross-timezone systems |
| `OffsetDateTime` | `java.time.OffsetDateTime` | 8+ | Time with offset (such as `+08:00`) | ✅ Suitable for storing database times |
| `Duration` | `java.time.Duration` | 8+ | Represents a time period (seconds, nanoseconds), such as 2 hours | ✅ Recommended for calculating time differences |
| `Period` | `java.time.Period` | 8+ | Represents a time period (years, months, days), such as 1 month later | ✅ Recommended for calendar period calculations |
| `DateTimeFormatter` | `java.time.format.DateTimeFormatter` | 8+ | Thread-safe formatting/parsing tool | ✅ Replacement for `SimpleDateFormat` |

So is the Date type completely obsolete and unused? ——Not exactly. Although the related APIs around Date were deprecated as early as after Java 1.1, this doesn't mean the Date type itself is deprecated. If it were deprecated, why wouldn't you find the `@Deprecated` annotation in the new JDK17? This indicates that the type is still retained. Using Date as a value object (Value Object) itself isn't problematic. For example, two constructor usages are still retained: `new Date()` and `new Date(long timestamp)`.

## API Introduction

This date API encapsulated by the author mainly has three functions:

1. Date type conversion. Facing such a variety of date types: Date, Instant, Int/Long/String, LocalDate/LocalDateTime/LocalTime, ZonedDateTime, etc., providing methods that can convert between each other, i.e., input `T returned date = input(Object anyType).to(Class<T> expected type)`.
2. Universal date formatting.
3. `now()` function that returns the current date and other utility functions.

Source code at: [https://gitee.com/lightweight-components/aj-util/tree/main/aj-util/src/main/java/com/ajaxjs/util/date](https://gitee.com/lightweight-components/aj-util/tree/main/aj-util/src/main/java/com/ajaxjs/util/date).