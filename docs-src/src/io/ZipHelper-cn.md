---
title: ZipHelper 教程
description: 为 ZIP 压缩和解压缩操作提供实用方法
tags:
  - 压缩
  - 解压缩
  - Java
layout: layouts/aj-util-cn.njk
---

# ZipHelper 教程

本教程提供了 `ZipHelper` 类的概述，该类是 `lightweight-component/aj-util` 库的一部分。`ZipHelper` 类为 ZIP 压缩和解压缩操作提供了实用工具方法。

## 简介

`ZipHelper` 类包含用于处理 ZIP 文件的静态方法，包括压缩、解压缩和文件类型检测。

## 主要特性

- 带目录处理的 ZIP 文件解压缩
- 文件和目录压缩（支持 STORED 或 DEFLATED 选项）
- 递归目录压缩
- ZIP 文件检测（魔数检查）
- CRC32 校验和计算
- 自动目录创建

## 方法

### 1. 解压缩

1. `unzip(String save, String zipFile)` - 将 ZIP 文件解压缩到目标目录

### 2. 压缩

1. `zipFile(File[] fileContent, String saveZip, boolean useStore)` - 将文件压缩为 ZIP
2. `zipDirectory(String sourceDir, String saveZip, boolean useStore)` - 将目录压缩为 ZIP

### 3. 实用方法

1. `initFolder(File file)` - 确保文件的父目录存在
2. `initFolder(String file)` - initFolder 的字符串版本
3. `isZipFile(String filePath)` - 检查文件是否为有效的 ZIP 文件
4. `getFileCRCCode(File file)` - 计算文件的 CRC32 校验和

## 使用示例

### 解压缩
```java
ZipHelper.unzip("C:/extracted", "C:/archive.zip");
```

### 文件压缩
```java
File[] files = {new File("file1.txt"), new File("file2.txt")};
ZipHelper.zipFile(files, "archive.zip", false); // 使用 DEFLATED 压缩
```

### 目录压缩
```java
ZipHelper.zipDirectory("C:/data", "backup.zip", true); // 使用 STORED（无压缩）
```

### ZIP 文件检测
```java
boolean isZip = ZipHelper.isZipFile("unknown.zip");
```

### 目录创建
```java
ZipHelper.initFolder("C:/new/path/file.txt"); // 如果需要则创建 C:/new/path
```

## 压缩方法

该类支持两种压缩方法：
1. `DEFLATED`（默认） - 标准 ZIP 压缩
2. `STORED` - 无压缩，文件按原样存储

## 错误处理

这些方法处理常见的 IO 错误，并在压缩/解压缩过程中出现问题时记录警告。

## 结论

`ZipHelper` 类提供了全面的实用方法，用于处理 ZIP 文件，使 Java 应用程序中的压缩和解压缩操作更加方便。