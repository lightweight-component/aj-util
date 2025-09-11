---
title: Version 教程
description: 提供了环境检测和配置工具
date: 2025-09-11
tags:
  - 环境
  - 配置
  - 版本
layout: layouts/aj-util-cn.njk
---

# Version 教程

本教程提供了 `Version` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`Version` 类提供了环境检测和配置工具。

## 简介

`Version` 类包含用于检测运行时环境特征和配置默认设置的静态方法和字段。

## 主要特性

- 操作系统检测
- 调试模式检测
- 时区配置
- 单元测试检测

## 字段

1. `OS_NAME` - 操作系统名称（小写）
2. `isDebug` - 指示调试/开发模式的标志
3. `isRunningTest` - 单元测试检测的缓存

## 方法

### 1. `isRunningTest()`

通过检查堆栈跟踪中的 JUnit 运行器来检测代码是否在 JUnit 测试环境中运行。

* **返回值:** 如果在测试环境中运行则为 true

## 静态初始化

该类自动：
1. 检查并将默认时区设置为 Asia/Shanghai（如果尚未设置）
2. 根据操作系统确定调试模式（非 Linux = 调试模式）

## 使用示例

### 检查调试模式
```java
if (Version.isDebug) {
    // 开发环境特定代码
}
```

### 检测测试环境
```java
if (Version.isRunningTest()) {
    // 测试特定设置
}
```

### 获取操作系统信息
```java
String os = Version.OS_NAME; // "windows 10", "linux" 等
```

## 结论

`Version` 类提供了简单的工具，用于检测运行时环境特征和配置默认设置（如时区）。