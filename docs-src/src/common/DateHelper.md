---
title: DateHelper Tutorial
description: Utility methods for working with dates and times in Java applications
tags:
  - date
  - time
  - Java
layout: layouts/aj-util.njk
---

# DateHelper Tutorial

This tutorial provides an overview of the `DateHelper` class, which is part of the `lightweight-component/aj-util` library. The `DateHelper` class provides utility methods for working with dates and times in Java applications.

## Introduction

The `DateHelper` class contains static methods for common date/time operations using Java 8's java.time API, including formatting, parsing, and conversion between different date/time types.

## Main Features

- Thread-safe date/time formatting and parsing
- Conversion between legacy Date and java.time types
- Support for multiple standard date/time formats
- RFC1123 and ISO8601 timestamp generation
- Object to date conversion utilities

## Standard Formats

1. `TIME` - "HH:mm:ss"
2. `DATETIME` - "yyyy-MM-dd HH:mm:ss"
3. `DATETIME_SHORT` - "yyyy-MM-dd HH:mm"
4. `DATE` - "yyyy-MM-dd"

## Methods

### 1. Formatting Methods

1. `formatDate(LocalDate date)` - Format LocalDate to string
2. `formatTime(LocalTime time)` - Format LocalTime to string
3. `formatDateTime(LocalDateTime dateTime)` - Format LocalDateTime to string
4. `formatDateTime(Date dateTime)` - Format Date to string

### 2. Parsing Methods

1. `parseDate(String dateStr)` - Parse string to LocalDate
2. `parseTime(String timeStr)` - Parse string to LocalTime
3. `parseDateTime(String dateTimeStr)` - Parse string to LocalDateTime
4. `parseDateTimeShort(String dateTimeStr)` - Parse short format string to LocalDateTime

### 3. Type Conversion

1. `localDateTime2Date(LocalDateTime localDateTime)` - Convert LocalDateTime to Date
2. `localDate2Date(LocalDate localDate)` - Convert LocalDate to Date
3. `toLocalDate(Date date)` - Convert Date to LocalDate
4. `toLocalDateTime(Date date)` - Convert Date to LocalDateTime

### 4. Standard Timestamps

1. `getGMTDate()` - Get current time in RFC1123 format
2. `getISO8601Date()` - Get current time in ISO8601 format

### 5. Utility Methods

1. `object2Date(Object obj)` - Convert various object types to Date
2. `nowDateTime()` - Get current Date
3. `now()` - Get current time as formatted string
4. `now(String format)` - Get current time with custom format
5. `nowShort()` - Get current time in short format

## Usage Examples

### Formatting Dates
```java
LocalDateTime now = LocalDateTime.now();
String formatted = DateHelper.formatDateTime(now); // "2023-01-15 14:30:45"
```

### Parsing Dates
```java
LocalDate date = DateHelper.parseDate("2023-01-15");
LocalDateTime dateTime = DateHelper.parseDateTime("2023-01-15 14:30:45");
```

### Type Conversion
```java
Date date = new Date();
LocalDateTime ldt = DateHelper.toLocalDateTime(date);
Date newDate = DateHelper.localDateTime2Date(ldt);
```

### Standard Timestamps
```java
String rfc1123 = DateHelper.getGMTDate(); // "Mon, 15 Jan 2023 14:30:45 GMT"
String iso8601 = DateHelper.getISO8601Date(); // "2023-01-15T14:30:45Z"
```

### Object Conversion
```java
Date fromLong = DateHelper.object2Date(1673793045000L);
Date fromString = DateHelper.object2Date("2023-01-15");
Date fromLocal = DateHelper.object2Date(LocalDateTime.now());
```

## Conclusion

The `DateHelper` class provides comprehensive utility methods for working with dates and times, making it easier to format, parse, and convert between different date/time representations in Java applications.