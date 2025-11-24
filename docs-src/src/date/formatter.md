---
title: Date Formatting
description: Date Formatting
tags:
  - Date Formatting
layout: layouts/aj-util.njk
---
# Date Formatting

The new version recommends using `DateTimeFormatter` instead of `SimpleDateFormat` to achieve thread safety and a clearer API. Since `DateTimeFormatter.ofPattern()` instantiation has certain overhead, especially for custom patterns (such as "yyyy-MM-dd HH:mm:ss"), we can optimize by caching `DateTimeFormatter` instances.

Formatter usage is as follows:

```java
new Formatter(TemporalAccessor temporal).format();
new Formatter(TemporalAccessor temporal).format(String format)
```


The `TemporalAccessor` interface is implemented by common date types, so we can pass them in the constructor and then specify the date format string.

# Utility Functions

Mainly utility functions for `now()` that return the current time as a string.

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
 * Request timestamp, format must conform to RFC1123 date format
 *
 * @return The current time
 */
public static String nowGMTDate() {
    return Formatter.GMT_FORMATTER.format(Instant.now());
}

/**
 * Request timestamp. Following ISO8601 standard representation, using UTC time, format is yyyy-MM-ddTHH:mm:ssZ
 *
 * @return The current time
 */
public static String newISO8601Date() {
    return Formatter.ISO8601_FORMATTER.format(Instant.now());
}
```


Additionally, there is a function for converting strings to dates, which mainly uses regular expressions to match whether the string is a date string and then performs the conversion.