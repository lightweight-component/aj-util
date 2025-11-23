---
title: FileHelper 教程
description: FileHelper 类提供了简化文件系统操作的实用方法
tags:
  - 文件操作
  - I/O
  - Java
layout: layouts/aj-util-cn.njk
---

## FileHelper 使用教程

FileHelper 是一个文件操作工具类，提供了读取、写入、复制、移动、删除和操作文件及目录的方法。该类使用 Java NIO Path 和 Files API 实现高效的文件操作，所有方法在发生 IO 错误时都会抛出 UncheckedIOException。

### 主要功能特性

1. **多种构造方式**：支持 Path、File、String 路径创建实例
2. **文件读写操作**：支持文本和字节内容的读取与写入
3. **文件管理操作**：复制、移动、删除、创建目录
4. **目录操作**：列出目录内容、获取文件大小
5. **文件分片处理**：支持大文件分片和合并
6. **链式调用**：支持方法链式调用

## 基本使用方法

### 1. 创建实例

```java
// 通过路径字符串创建
FileHelper helper1 = new FileHelper("path/to/file.txt");

// 通过 Path 对象创建
Path path = Paths.get("path/to/file.txt");
FileHelper helper2 = new FileHelper(path);

// 通过 File 对象创建
File file = new File("path/to/file.txt");
FileHelper helper3 = new FileHelper(file);
```


### 2. 文件读取

```java
// 读取文件文本内容
String content = new FileHelper("example.txt").getFileContent();

// 读取文件字节内容
byte[] bytes = new FileHelper("example.txt").readFileBytes();
```


### 3. 文件写入

```java
// 写入字符串内容到文件
new FileHelper("output.txt").writeFileContent("Hello World");
```


### 4. 文件删除

```java
// 删除文件或目录（递归删除目录）
new FileHelper("file-or-directory").delete();
```


### 5. 目录操作

```java
// 列出目录内容
List<String> contents = new FileHelper("directory").listDirectoryContents();

// 创建目录（支持多级目录）
new FileHelper("new/directory/path").createDirectory();

// 获取文件或目录大小
long size = new FileHelper("file-or-directory").getFileSize();
```


### 6. 文件复制和移动

```java
// 复制文件或目录
new FileHelper("source.txt")
    .setTarget("destination.txt")
    .copyTo();

// 移动文件或目录
new FileHelper("old-location.txt")
    .setTarget("new-location.txt")
    .moveTo();
```


### 7. 文件分片处理

```java
// 将文件分片（每片1MB）
new FileHelper("large-file.zip").chunkFile(1024 * 1024);

// 合并分片文件
Path[] chunks = {Paths.get("file-1"), Paths.get("file-2")};
new FileHelper("merged-file").mergeFile(chunks);
```


## 链式调用示例

```java
// 链式调用示例
new FileHelper("input.txt")
    .setTarget("backup.txt")
    .copyTo()
    .setTarget("moved.txt")
    .moveTo();
```


## 异常处理

所有 IO 操作都会在出错时抛出 UncheckedIOException，可以使用 try-catch 进行处理：

```java
try {
    String content = new FileHelper("nonexistent.txt").getFileContent();
} catch (UncheckedIOException e) {
    System.err.println("文件操作失败: " + e.getMessage());
}
```


## 注意事项

1. 复制和移动操作前必须设置目标路径
2. 文件分片使用零拷贝技术，提高大文件处理效率
3. 目录删除采用反向遍历，确保正确删除所有子目录和文件

