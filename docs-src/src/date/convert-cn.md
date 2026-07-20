---
title: 日期类型转换
description: 安全转换传统日期类型与 Java 8 日期时间类型
tags:
  - 日期转换
layout: layouts/aj-util-cn.njk
---

# 日期类型转换

`DateTypeConvert` 可转换传统 `Date`、`Calendar`、SQL 日期类型、毫秒时间戳以及 Java 8 的 `java.time` 类型。凡是涉及本地日期时间的转换，都应明确传入 `ZoneId`。

```java
Instant instant = Instant.now();
ZoneId zone = ZoneId.of("Asia/Shanghai");

LocalDateTime local = new DateTypeConvert(instant).to(LocalDateTime.class, zone);
Calendar calendar = new DateTypeConvert(instant).to(Calendar.class, zone);
```

## 重要语义

- 时间戳 `0` 是合法输入，表示 `1970-01-01T00:00:00Z`。
- 纯日期字符串按日期格式解析，不再强制套用日期时间格式。
- 转换得到的 `Calendar` 会使用调用方指定的时区。
- 输入与目标同为 `ZonedDateTime` 或 `OffsetDateTime` 时会原样返回，保留原始时区或 offset。
- `LocalDateTime` 转换为瞬时时采用严格规则：夏令时缺口会被拒绝，重叠时间也会因歧义被拒绝，不会静默选择某个 offset。
- `OffsetTime` 本身不含日期，只能转换为 `OffsetTime` 或 `LocalTime`；需要日期的瞬时转换会被拒绝，不再隐式补系统“今天”。

```java
Instant epoch = new DateTypeConvert(0L).to(Instant.class, ZoneOffset.UTC);
assertEquals(Instant.EPOCH, epoch);
```

没有输入或转换缺少必要上下文时，API 会抛出异常，而不是静默猜测。
