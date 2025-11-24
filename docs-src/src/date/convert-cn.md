---
title: 万能日期类型转换
subTitle: 2025-02-23 by Frank Cheung
description: 万能日期类型转换
date: 2025-02-23
tags:
  - 基本流程
layout: layouts/aj-util-cn.njk
---

# 万能日期类型转换
起初想到的转换方式是这样的，一个类型对应着一个类型逐个转换。

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
可是这样的方式太累了，代码也啰嗦。于是咨询了一下 AI 的意见，改为下面清爽的代码。

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

主要就是不管什么输入类型，先统一转换到`Instant`类再转为目标类型。


下面是一些用法例子：

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