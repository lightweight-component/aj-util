---
title: BoxLogger 教程
description: 为创建带格式和对齐的盒状日志消息提供了实用工具
date: 2025-09-11
tags:
  - 日志
  - 格式化
  - 工具
layout: layouts/aj-util-cn.njk
---

# BoxLogger 教程

本教程提供了 `BoxLogger` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`BoxLogger` 类为创建带格式和对齐的盒状日志消息提供了实用工具。

## 简介

`BoxLogger` 类包含用于创建视觉格式化的盒状日志消息的静态方法，支持 ANSI 颜色和正确处理宽字符（如 CJK）。

## 主要特性

- 盒状日志消息格式化
- ANSI 颜色支持
- 带省略号的字符串截断
- 显示宽度计算（处理宽字符）
- 字符串重复实用工具

## 常量

1. `ANSI_GREEN` - ANSI 绿色代码
2. `ANSI_YELLOW` - ANSI 黄色代码
3. `ANSI_BLUE` - ANSI 蓝色代码
4. `ANSI_RED` - ANSI 红色代码
5. `ANSI_RESET` - ANSI 重置代码
6. `NONE` - 常量 "none"
7. `TRACE_KEY` - 默认跟踪键名
8. `BIZ_ACTION` - 默认业务操作键名

## 方法

### 1. 盒状格式化

1. `boxLine(char left, char fill, char right, String title)` - 创建盒状边框线
2. `boxContent(String key, String value)` - 在盒内创建内容行

### 2. 字符串实用工具

1. `repeat(char c, int n)` - 重复字符 n 次
2. `truncate(String s, int maxDisplayLen)` - 带省略号的字符串截断
3. `getDisplayWidth(String s)` - 计算显示宽度（宽字符=2）
4. `isWideChar(char ch)` - 检查字符是否为宽字符（CJK）

## 使用示例

### 创建盒状日志
```java
System.out.println(BoxLogger.boxLine('┌', '─', '┐', " 日志开始 "));
System.out.println(BoxLogger.boxContent("用户: ", "张三"));
System.out.println(BoxLogger.boxLine('└', '─', '┘', " 日志结束 "));
```

### ANSI 颜色
```java
System.out.println(BoxLogger.ANSI_RED + "错误!" + BoxLogger.ANSI_RESET);
```

### 字符串实用工具
```java
String repeated = BoxLogger.repeat('-', 10); // "----------"
String truncated = BoxLogger.truncate("这是一个长字符串", 10); // "这是一个..."
int width = BoxLogger.getDisplayWidth("中文"); // 4
```

## 实现说明

- 盒宽度固定为 97 个字符
- 正确处理 CJK 和全角字符
- 包含基本的 ANSI 颜色支持
- 提供带省略号的干净截断

## 结论

`BoxLogger` 类提供了有用的实用工具，用于创建视觉格式化的日志消息，特别适用于 CLI 应用程序或调试输出。