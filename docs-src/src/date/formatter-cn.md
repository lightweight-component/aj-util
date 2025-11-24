---
title: 日期格式化
subTitle: 2025-02-23 by Frank Cheung
description: 日期格式化
date: 2025-02-23
tags:
  - 日期格式化
layout: layouts/aj-util-cn.njk
---

# 日期格式化
新版推荐使用`DateTimeFormatter`替代`SimpleDateFormat`，使之线程安全与 API 清晰。同时`DateTimeFormatter.ofPattern()`实例有一定开销，尤其是自定义模式（如 "yyyy-MM-dd HH:mm:ss"），于是我们可以缓存 DateTimeFormatter 实例的方式来优化。

Formatter 用法如下：

```java
new Formatter(TemporalAccessor temporal).format();
new Formatter(TemporalAccessor temporal).format(String format)
```
接口`TemporalAccessor`实现都是常见的那些日期类型，于是我们在构造器传入即可，然后指定日期格式的字符串。

# 工具函数
主要是`now()`返回当前时间字符串的工具函数。

```java
/**
 * Obtain current time by specified format.
 *
 * @param formatter The formatter object
 * @return The current time
 */
public static String now(DateTimeFormatter formatter) {
    return LocalDateTime.now().format(formatter);
}

/**
 * Obtain current time by specified format.
 *
 * @param format The format string
 * @return The current time
 */
public static String now(String format) {
    return now(Formatter.getDateFormatter(format));
}

/**
 * Obtain current time, which is formatted by default like "yyyy-MM-dd HH:mm:ss".
 *
 * @return The current time
 */
public static String now() {
    return now(Formatter.getDateTimeFormatter());
}

/**
 * Obtain current time, which is formatted like "yyyy-MM-dd HH:mm".
 *
 * @return The current time
 */
public static String nowShort() {
    return now(Formatter.getDateTimeShortFormatter());
}

/**
 * 请求的时间戳，格式必须符合 RFC1123 的日期格式
 *
 * @return The current time
 */
public static String nowGMTDate() {
    return Formatter.GMT_FORMATTER.format(Instant.now());
}

/**
 * 请求的时间戳。按照 ISO8601 标准表示，并需要使用 UTC 时间，格式为 yyyy-MM-ddTHH:mm:ssZ
 *
 * @return The current time
 */
public static String newISO8601Date() {
    return Formatter.ISO8601_FORMATTER.format(Instant.now());
}
```
另外还包括一个字符串转日期的函数，其主要使用正则来匹配是否日期字符串然后进行转换。