---
title: 一个简洁、清晰的 Java 日期 API
description: 整个库中使用的常见加密实用工具
tags:
  - 加密
  - 解密
  - 实用工具
layout: layouts/aj-util-cn.njk
---

# 一个简洁、清晰的 Java 日期 API
说起日期类型，相对其他原始数据类型（int/long/boolean/string）处理起来是比较棘手的，原因也是多方面的，首先日期是个笼统的概念，年月日可以说成是日期，年月日时分秒也可以说成日期，或者说单独的时分秒也是日期，——那你到底说着哪个日期呢，是不是？Java 传统日期类型`java.util.Date`就是这么干的，一个类型囊括上述所有的日期概念，也是造成日期 API 混乱的根源；其次日期的表达方式，也称之为“格式 Format”，可以`2025-12-22 11:05`，也可以`2025-12-22 11-05`甚至`3 Jun 2025 11:05`，种类繁多；最后日期还有时区的概念，比如`2024-12-03T10:15:30+01:00[Europe/Paris]`，`Tue, 3 Jun 2025 11:05:30 GMT`等等。

## 关于`Date`类型
早期 Java 版本中就是基于`java.util.Date`类型一统天下，很多日期处理方法都是围绕它进行的（Java 1.1 之前）。后来版本重构中废弃了很多的这些方法，改由`java.util.Calendar`与`java.text.DateFormat`完成。Calendar 解决了日期的棘手问题了吗？并没有，虽然 Calendar 处理日期起来更灵活但是使用仍然过于复杂，设置、比较、格式化仍旧比较麻烦、冗长，所以社区又有了代替品 Joda-Time，被许多项目所采用。于是这个优秀的开源项目渐渐地成为 Java 标准的一部分，便是 JSR310，最后集成到 Java 8 中变成`java.time`！

总之，我们主张使用的日期类型就是`java.time`里面的 Instant 及 LocalDateTime，其中 Instant 表示某一时刻的时间，大致对应时间戳，可支持纳秒的级别。LocalDateTime 可能更我们更常用一些，以及它衍生的其他精确类型。

### 📅 新 Java 常用日期时间类型

| 类型 | 所在包 | Java 版本 | 说明 | 是否推荐 |
|------|--------|-----------|------|----------|
| `Instant` | `java.time.Instant` | 8+ | 表示时间线上的一个瞬时点（UTC），精确到纳秒 | ✅ 推荐用于时间戳 |
| `LocalDate` | `java.time.LocalDate` | 8+ | **只包含日期**（年-月-日），如 `2025-03-08` | ✅ 强烈推荐用于“纯日期”场景（如列车出发日） |
| `LocalTime` | `java.time.LocalTime` | 8+ | **只包含时间**（时:分:秒.纳秒），如 `14:30:00` | ✅ 推荐用于“纯时间”场景（如发车时间） |
| `LocalDateTime` | `java.time.LocalDateTime` | 8+ | 日期 + 时间（无时区），如 `2025-03-08T14:30:00` | ✅ 推荐用于本地时间（如列车时刻表） |
| `ZonedDateTime` | `java.time.ZonedDateTime` | 8+ | 带时区的日期时间，支持夏令时等 | ✅ 推荐用于跨时区系统 |
| `OffsetDateTime` | `java.time.OffsetDateTime` | 8+ | 带偏移量的时间（如 `+08:00`） | ✅ 适合存储数据库时间 |
| `Duration` | `java.time.Duration` | 8+ | 表示时间段（秒、纳秒），如 2 小时 | ✅ 推荐用于计算时间差 |
| `Period` | `java.time.Period` | 8+ | 表示时间段（年、月、日），如 1个月后 | ✅ 推荐用于日历周期计算 |
| `DateTimeFormatter` | `java.time.format.DateTimeFormatter` | 8+ | 线程安全的格式化/解析工具 | ✅ 替代 `SimpleDateFormat` |


那么 Date 类型就不用，完全废弃了吗？——并不是，虽然围绕 Date 的相关 API 早在 Java 1.1 之后就被废弃，但是不代表 Date 本身被废弃。如果废弃了，为什么你在新版 JDK17 上找不到`@Deprecated`注解？说明该类型还是保留着的。作为值对象（Value Object）使用 Date 本身没什么问题，例如两种构造器的用法仍保留：`new Date()`以及`new Date(long timestamp)`。

## API 简介
这套笔者封装的 日期 API，主要功能有以下三点：

1. 日期类型的转换。面对如此繁多的日期类型：Date、Instant、Int/Long/String、LocalDate/LocalDateTime/LocalTime、ZonedDateTime 等等，给出一个相互之间都能够转换的方法，即输入`T 返回的日期 = input(Object anyType).to(Class<T> 期望类型)`。
2. 通用的日期格式化。
3. 返回当前日期的`now()`函数及其他工具函数。


源码在：[https://gitee.com/lightweight-components/aj-util/tree/main/aj-util/src/main/java/com/ajaxjs/util/date](https://gitee.com/lightweight-components/aj-util/tree/main/aj-util/src/main/java/com/ajaxjs/util/date)。


