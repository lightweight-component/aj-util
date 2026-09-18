---
title: DataReader and DataWriter
description: Read streams and write byte data
layout: layouts/aj-util.njk
lang: en
alternate: /io/DataIO-cn/
---

# DataReader and DataWriter

`DataReader` consumes an `InputStream`; `DataWriter` writes bytes or an input stream to an `OutputStream`.

```java
String text = new DataReader(inputStream).readAsString();

ByteArrayOutputStream output = new ByteArrayOutputStream();
new DataWriter(output).write("hello".getBytes(StandardCharsets.UTF_8));
```

`DataReader` closes its input stream after `readStreamAsBytes`, `readAsLineString`, `readAsString`, or `readAsBytes`.
Do not reuse the input afterward. `DataWriter` flushes but does not close its output stream; this makes it suitable for
writing to a caller-owned response, file, or in-memory stream. Its default buffering can be changed with
`setBuffered(boolean)`.

`readStreamAsBytes(int, BiConsumer<Integer, byte[]>)` passes the number of valid bytes together with a reusable buffer;
consume only the range `[0, readSize)` before the next callback.

