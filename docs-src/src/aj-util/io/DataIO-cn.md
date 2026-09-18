---
title: DataReader 与 DataWriter
description: 流读取与字节写入工具
layout: layouts/aj-util-cn.njk
lang: zh-CN
alternate: /io/DataIO/
---

# DataReader 与 DataWriter

`DataReader` 用于消费 `InputStream`，`DataWriter` 用于向 `OutputStream` 写入字节或输入流。

```java
String text = new DataReader(inputStream).readAsString();

ByteArrayOutputStream output = new ByteArrayOutputStream();
new DataWriter(output).write("hello".getBytes(StandardCharsets.UTF_8));
```

`DataReader` 在 `readStreamAsBytes`、`readAsLineString`、`readAsString` 和 `readAsBytes` 完成后会关闭输入流，因此输入流不能再复用。`DataWriter` 会刷新但不会关闭输出流，适合调用方持有的响应流、文件流或内存流；默认缓冲可通过 `setBuffered(boolean)` 修改。

`readStreamAsBytes(int, BiConsumer<Integer, byte[]>)` 会传入有效字节数和可复用缓冲区；每次回调只能处理 `[0, readSize)` 范围，下一次回调会复用该数组。

## 读取文本、字节和分块数据

应按数据的大小与形态选择读取 API。`readAsBytes()` 会把整个流收集到内存，适合小资源，但不适合无限制上传或大文件。
`readAsString()` 使用指定字符集（默认 UTF-8）逐行读取，并在每一行后追加 `System.lineSeparator()`。来源不是 UTF-8
时，使用 `DataReader(InputStream, Charset)` 构造器。

```java
String csv = new DataReader(input, StandardCharsets.ISO_8859_1).readAsString();

new DataReader(input).readAsLineString(line -> {
    if (!line.isEmpty())
        consumeCsvLine(line);
});
```

流式处理时，每轮回调拿到的是同一个缓冲数组。如果数据需要在回调之后继续保留，请自行复制；否则应立即处理。
最后一块数据可能小于请求的缓冲区大小。

```java
MessageDigest digest = MessageDigest.getInstance("SHA-256");
new DataReader(input).readStreamAsBytes(8 * 1024,
        (size, buffer) -> digest.update(buffer, 0, size));

// 对 "abcdefghij" 使用大小为 4 的缓冲区时，回调大小依次为 4、4、2。
```

缓冲区大小必须为正数。I/O 失败会包装为 `UncheckedIOException`，关闭流失败则会被记录日志。

## 复制到输出流

`new DataWriter(out).write(in)` 以 1 KiB 块复制输入流并刷新输出流。默认会为输出增加缓冲包装；仅当目标已具备合适
缓冲或需要严格控制刷新行为时才考虑关闭它。

```java
try (InputStream in = Files.newInputStream(source);
     OutputStream out = Files.newOutputStream(target)) {
    new DataWriter(out).write(in);
}

ByteArrayOutputStream memory = new ByteArrayOutputStream();
new DataWriter(memory).write("hello".getBytes(StandardCharsets.UTF_8));
```

`DataWriter` 从不关闭调用方的输出流。实例方法 `write(InputStream)` 委托给 `DataReader`，复制结束时会关闭输入流，
调用方应把该输入流视为已经消费完毕。静态方法 `DataWriter.write(OutputStream, InputStream)` 更底层：只复制字节，
不刷新，也不关闭任一流。

对于 `write(byte[], off, length)`，`(0, 0)` 是一个特殊简写，含义是**写入整个数组**，并不是写入零字节。写片段时请
传入明确的非零长度，例如 `write(bytes, 7, 5)`。

## 实现原理与取舍

两个类都是对 JDK 流的小型适配。`DataReader` 仅在逐行读取文本时增加缓冲字符读取器；字节传输则复用一个数组，避免
每一块都分配新数组。`DataWriter` 刷新而不关闭，便于调用方组合多次写入或继续拥有 HTTP 响应流。对于不需要 Java 8
兼容的 Java 9+ 项目，简单的流复制可直接使用 JDK 的 `InputStream.transferTo`。

