---
title: DataReader/ 教程
description: DataReader/ 类提供了流操作和转换的实用方法
tags:
  - 流操作
  - I/O
  - Java
layout: layouts/aj-util-cn.njk
---

# DataReader 使用教程

DataReader 是一个用于从输入流读取数据的读取器类。它支持从文件、网络套接字或内存缓冲区等源读取原始字节数据，同时提供了处理文本和二进制数据的功能。

## 主要功能特性

1. **多种数据读取方式**：支持字节流、文本行、完整字符串等多种读取方式
2. **字符编码支持**：可指定字符编码，默认使用 UTF-8
3. **流式处理**：支持大文件的分块处理，避免内存溢出
4. **函数式编程**：使用函数式接口处理读取的数据

## 基本使用方法

### 1. 创建实例

```java
// 使用默认 UTF-8 编码创建实例
InputStream inputStream = new FileInputStream("example.txt");
DataReader reader = new DataReader(inputStream);

// 指定字符编码创建实例
DataReader readerWithEncoding = new DataReader(inputStream, StandardCharsets.UTF_8);
```


### 2. 读取字节数据

```java
// 以字节形式读取流数据
InputStream inputStream = new FileInputStream("large-file.bin");
DataReader reader = new DataReader(inputStream);

reader.readStreamAsBytes(8192, (readSize, buffer) -> {
    // 处理读取到的数据
    System.out.println("读取到 " + readSize + " 字节数据");
    // 可以在这里处理 buffer 中的数据
});
```


### 3. 读取文本行

```java
// 逐行读取文本数据
InputStream inputStream = new FileInputStream("text-file.txt");
DataReader reader = new DataReader(inputStream);

reader.readAsLineString(line -> {
    System.out.println("读取到一行: " + line);
    // 处理每一行文本
});
```


### 4. 读取完整字符串

```java
// 读取整个输入流为字符串
InputStream inputStream = new FileInputStream("text-file.txt");
DataReader reader = new DataReader(inputStream);

String content = reader.readAsString();
System.out.println("文件内容: " + content);
```


### 5. 读取字节数组

```java
// 读取整个输入流为字节数组
InputStream inputStream = new FileInputStream("binary-file.bin");
DataReader reader = new DataReader(inputStream);

byte[] bytes = reader.readAsBytes();
System.out.println("字节数组长度: " + bytes.length);
```

### 注意事项

1. readStreamAsBytes 方法会在读取完成后自动关闭输入流
2. 所有方法在发生 IO 错误时都会抛出 `UncheckedIOException`
3. 对于大文件处理，推荐使用 readStreamAsBytes 方法进行分块处理
4. readAsString 方法会在每行末尾添加换行符


# DataWriter 使用教程

DataWriter 是一个用于向输出流写入数据的写入器类。它支持向文件、套接字或缓冲区等目标写入原始字节数据，并提供了缓冲功能以提高写入效率。

## 主要功能特性

1. **多种数据写入方式**：支持从输入流复制数据、写入字节数组等
2. **缓冲支持**：默认启用缓冲功能，提高写入性能
3. **灵活的字节数组写入**：支持写入完整字节数组或指定范围的数据
4. **不自动关闭流**：不会自动关闭输出流，由调用者负责管理

## 基本使用方法

### 1. 创建实例

```java
// 创建 DataWriter 实例
OutputStream outputStream = new FileOutputStream("output.txt");
DataWriter writer = new DataWriter(outputStream);
```


### 2. 从输入流复制数据

```java
// 从输入流复制数据到输出流
InputStream inputStream = new FileInputStream("input.txt");
OutputStream outputStream = new FileOutputStream("output.txt");
DataWriter writer = new DataWriter(outputStream);

writer.write(inputStream); // 复制数据
// 注意：需要手动关闭流
inputStream.close();
outputStream.close();
```


### 3. 写入字节数组

```java
// 写入完整字节数组
byte[] data = "Hello World".getBytes();
OutputStream outputStream = new FileOutputStream("output.txt");
DataWriter writer = new DataWriter(outputStream);

writer.write(data);
outputStream.close();
```


### 4. 写入字节数组指定范围

```java
// 写入字节数组的指定范围
byte[] data = "Hello World".getBytes();
OutputStream outputStream = new FileOutputStream("output.txt");
DataWriter writer = new DataWriter(outputStream);

// 写入从偏移量2开始的5个字节
writer.write(data, 2, 5); // 写入 "llo W"
outputStream.close();
```



## 配置选项

```java
// 禁用缓冲功能（不推荐）
OutputStream outputStream = new FileOutputStream("output.txt");
DataWriter writer = new DataWriter(outputStream);
writer.setBuffered(false);
```


### 注意事项

1. DataWriter 不会自动关闭输出流，需要调用者手动管理流的关闭
2. 默认启用缓冲功能，禁用缓冲会给出警告提示
3. 所有方法在发生IO错误时都会抛出 `UncheckedIOException`
4. 当偏移量和长度都为0时，`write(byte[], int, int)` 方法会写入整个字节数组