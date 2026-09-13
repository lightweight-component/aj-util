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

