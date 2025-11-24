---
title: Universal Date Type Conversion
description: Universal Date Type Conversion
tags:
  - Date Conversion
layout: layouts/aj-util.njk
---

# Universal Date Type Conversion

Initially, the conversion approach was like this, converting one type to another type individually.

```java
@SuppressWarnings("unchecked")
public <T> T to(Class<T> clz, ZoneId zoneId) {
    if (input != null) {
        if (clz == LocalDate.class) {
            LocalDate localDate = input.toInstant()
                    .atZone(zoneId == null ? ZoneId.systemDefault() : zoneId)
                    .toLocalDate();

            return (T) localDate;
        }
    }

    if (localDate != null) {
        if (clz == Date.class) {
            Date date = Date.from(localDate.atStartOfDay(zoneId == null ? ZoneId.systemDefault() : zoneId).toInstant());

            return (T) date;
        }
    }

    throw new UnsupportedOperationException("Can not transform this date type to another date type");
}
```


However, this approach was too tedious and the code was verbose. After consulting AI's opinion, the code was changed to the following clean version.

```java
/**
 * Convert the input to the specified date type
 *
 * @param clz    The target date type
 * @param zoneId The time zone. Optional, defaults to system default if passed null
 * @param <T>    The target date type
 * @return The converted date
 */
@SuppressWarnings("unchecked")
public <T> T to(Class<T> clz, ZoneId zoneId) {
    ZoneId zone = zoneId != null ? zoneId : ZoneId.systemDefault();
    Instant baseInstant;

    if (input != null) {/* SB way, actually u can set any values to Instant by constructor or setter method */
        baseInstant = input.toInstant();
    } else if (sqlDate != null) {
        baseInstant = sqlDate.toLocalDate().atStartOfDay(zone).toInstant();
    } else if (sqlTimestamp != null) {
        baseInstant = sqlTimestamp.toInstant();
    } else if (localDate != null) {
        baseInstant = localDate.atStartOfDay(zone).toInstant();
    } else if (localDateTime != null) {
        baseInstant = localDateTime.atZone(zone).toInstant();
    } else if (zonedDateTime != null) {
        baseInstant = zonedDateTime.toInstant();
    } else if (offsetDateTime != null) {
        baseInstant = offsetDateTime.toInstant();
    } else if (offsetTime != null) {
        baseInstant = offsetTime.atDate(LocalDate.now()).toInstant();
    } else if (instant != null) {
        baseInstant = instant;
    } else if (timestamp != 0L) {
        baseInstant = Instant.ofEpochMilli(timestamp);
    } else
        throw new UnsupportedOperationException("No input date/time set.");

    // Convert baseInstant to target
    if (clz == Instant.class) {
        return (T) baseInstant;
    } else if (clz == Date.class) {
        return (T) Date.from(baseInstant);
    } else if (clz == java.sql.Date.class) {
        return (T) java.sql.Date.valueOf(baseInstant.atZone(zone).toLocalDate());
    } else if (clz == Timestamp.class) {
        return (T) Timestamp.from(baseInstant);
    } else if (clz == LocalDate.class) {
        return (T) baseInstant.atZone(zone).toLocalDate();
    } else if (clz == LocalTime.class) {
        return (T) baseInstant.atZone(zone).toLocalTime();
    } else if (clz == LocalDateTime.class) {
        return (T) baseInstant.atZone(zone).toLocalDateTime();
    } else if (clz == ZonedDateTime.class) {
        return (T) baseInstant.atZone(zone);
    } else if (clz == OffsetDateTime.class) {
        return (T) baseInstant.atOffset(zone.getRules().getOffset(baseInstant));
    } else if (clz == OffsetTime.class) {
        return (T) baseInstant.atZone(zone).toOffsetDateTime().toOffsetTime();
    } else if (clz == Calendar.class) {
        Calendar calendar = Calendar.getInstance();
        baseInstant.atZone(zone);
        calendar.setTimeInMillis(baseInstant.toEpochMilli());

        return (T) calendar;
    }

    throw new UnsupportedOperationException("Unsupported target type: " + clz.getName());
}
```


The main idea is that regardless of the input type, first convert it uniformly to the `Instant` class and then convert it to the target type.

Below are some usage examples:

```java
long timestamp = System.currentTimeMillis();
Instant expected = Instant.ofEpochMilli(timestamp);

Instant result = new DateTypeConvert(timestamp).to(Instant.class, null);
assertEquals(expected, result);


Instant instant = Instant.now();
LocalDateTime expected = instant.atZone(zone).toLocalDateTime();

LocalDateTime result = new DateTypeConvert(instant).to(LocalDateTime.class, null);
assertEquals(expected, result);

LocalDate localDate = LocalDate.of(2025, 10, 23);
Date expected = Date.from(localDate.atStartOfDay(zone).toInstant());

Date result = new DateTypeConvert(localDate).to(Date.class, null);
assertEquals(expected, result);
```
