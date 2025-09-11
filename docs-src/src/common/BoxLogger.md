---
title: BoxLogger Tutorial
description: Provides utilities for creating boxed log messages with proper formatting
date: 2025-09-11
tags:
  - logging
  - formatting
  - utilities
layout: layouts/aj-util.njk
---

# BoxLogger Tutorial

This tutorial provides an overview of the `BoxLogger` class, which is part of the `lightweight-component/aj-util` library. The `BoxLogger` class provides utilities for creating boxed log messages with proper formatting and alignment.

## Introduction

The `BoxLogger` class contains static methods for creating visually formatted log messages in boxes, with support for ANSI colors and proper handling of wide characters (like CJK).

## Main Features

- Boxed log message formatting
- ANSI color support
- String truncation with ellipsis
- Display width calculation (handling wide characters)
- String repetition utility

## Constants

1. `ANSI_GREEN` - ANSI green color code
2. `ANSI_YELLOW` - ANSI yellow color code
3. `ANSI_BLUE` - ANSI blue color code
4. `ANSI_RED` - ANSI red color code
5. `ANSI_RESET` - ANSI reset code
6. `NONE` - Constant "none"
7. `TRACE_KEY` - Default trace key name
8. `BIZ_ACTION` - Default business action key name

## Methods

### 1. Box Formatting

1. `boxLine(char left, char fill, char right, String title)` - Create a box border line
2. `boxContent(String key, String value)` - Create a content line within box

### 2. String Utilities

1. `repeat(char c, int n)` - Repeat character n times
2. `truncate(String s, int maxDisplayLen)` - Truncate string with ellipsis
3. `getDisplayWidth(String s)` - Calculate display width (wide chars = 2)
4. `isWideChar(char ch)` - Check if character is wide (CJK)

## Usage Examples

### Create Boxed Log
```java
System.out.println(BoxLogger.boxLine('┌', '─', '┐', " LOG START "));
System.out.println(BoxLogger.boxContent("User: ", "张三"));
System.out.println(BoxLogger.boxLine('└', '─', '┘', " LOG END "));
```

### ANSI Colors
```java
System.out.println(BoxLogger.ANSI_RED + "Error!" + BoxLogger.ANSI_RESET);
```

### String Utilities
```java
String repeated = BoxLogger.repeat('-', 10); // "----------"
String truncated = BoxLogger.truncate("This is a long string", 10); // "This is..."
int width = BoxLogger.getDisplayWidth("中文"); // 4
```

## Implementation Notes

- Box width is fixed at 97 characters
- Properly handles CJK and fullwidth characters
- Includes basic ANSI color support
- Provides clean truncation with ellipsis

## Conclusion

The `BoxLogger` class provides useful utilities for creating visually formatted log messages, especially useful for CLI applications or debugging output.