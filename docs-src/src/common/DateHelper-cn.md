---
title: DateHelper 教程
description: 为 Java 应用程序中的日期和时间处理提供实用方法
tags:
  - 日期
  - 时间
  - Java
layout: layouts/aj-util-cn.njk
---

# DateHelper 教程

本教程提供了 `DateHelper` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`DateHelper` 类为 Java 应用程序中的日期和时间处理提供了实用工具方法。

## 简介

`DateHelper` 类包含使用 Java 8 的 java.time API 的静态方法，用于常见的日期/时间操作，包括格式化、解析和不同日期/时间类型之间的转换。

## 主要特性

- 线程安全的日期/时间格式化和解析
- 传统 Date 和 java.time 类型之间的转换
- 支持多种标准日期/时间格式
- RFC1123 和 ISO8601 时间戳生成
- 对象到日期的转换工具

## 标准格式

1. `TIME` - "HH:mm:ss"
2. `DATETIME` - "yyyy-MM-dd HH:mm:ss"
3. `DATETIME_SHORT` - "yyyy-MM-dd HH:mm"
4. `DATE` - "yyyy-MM-dd"

## 方法

### 1. 格式化方法

1. `formatDate(LocalDate date)` - 将 LocalDate 格式化为字符串
2. `formatTime(LocalTime time)` - 将 LocalTime 格式化为字符串
3. `formatDateTime(LocalDateTime dateTime)` - 将 LocalDateTime 格式化为字符串
4. `formatDateTime(Date dateTime)` - 将 Date 格式化为字符串

### 2. 解析方法

1. `parseDate(String dateStr)` - 将字符串解析为 LocalDate
2. `parseTime(String timeStr)` - 将字符串解析为 LocalTime
3. `parseDateTime(String dateTimeStr)` - 将字符串解析为 LocalDateTime
4. `parseDateTimeShort(String dateTimeStr)` - 将短格式字符串解析为 LocalDateTime

### 3. 类型转换

1. `localDateTime2Date(LocalDateTime localDateTime)` - 将 LocalDateTime 转换为 Date
2. `localDate2Date(LocalDate localDate)` - 将 LocalDate 转换为 Date
3. `toLocalDate(Date date)` - 将 Date 转换为 LocalDate
4. `toLocalDateTime(Date date)` - 将 Date 转换为 LocalDateTime

### 4. 标准时间戳

1. `getGMTDate()` - 获取 RFC1123 格式的当前时间
2. `getISO8601Date()` - 获取 ISO8601 格式的当前时间

### 5. 实用方法

1. `object2Date(Object obj)` - 将各种对象类型转换为 Date
2. `nowDateTime()` - 获取当前 Date
3. `now()` - 获取格式化字符串形式的当前时间
4. `now(String format)` - 获取自定义格式的当前时间
5. `nowShort()` - 获取短格式的当前时间

## 使用示例

### 格式化日期
```java
LocalDateTime now = LocalDateTime.now();
String formatted = DateHelper.formatDateTime(now); // "2023-01-15 14:30:45"
```

### 解析日期
```java
LocalDate date = DateHelper.parseDate("2023-01-15");
LocalDateTime dateTime = DateHelper.parseDateTime("2023-01-15 14:30:45");
```

### 类型转换
```java
Date date = new Date();
LocalDateTime ldt = DateHelper.toLocalDateTime(date);
Date newDate = DateHelper.localDateTime2Date(ldt);
```

### 标准时间戳
```java
String rfc1123 = DateHelper.getGMTDate(); // "Mon, 15 Jan 2023 14:30:45 GMT"
String iso8601 = DateHelper.getISO8601Date(); // "2023-01-15T14:30:45Z"
```

### 对象转换
```java
Date fromLong = DateHelper.object2Date(1673793045000L);
Date fromString = DateHelper.object2Date("2023-01-15");
Date fromLocal = DateHelper.object2Date(LocalDateTime.now());
```

## 结论

`DateHelper` 类提供了全面的实用方法，用于处理日期和时间，使在 Java 应用程序中格式化、解析和转换不同的日期/时间表示更加容易。