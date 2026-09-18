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

## Reading text, bytes, and chunks

Choose the reading API by the size and shape of the data. `readAsBytes()` collects the whole stream in memory, which is
convenient for a small resource but unsuitable for an unbounded upload or a large file. `readAsString()` reads line by
line using the configured charset (UTF-8 by default), then appends `System.lineSeparator()` after every line. Use the
`DataReader(InputStream, Charset)` constructor when the source text is not UTF-8.

```java
String csv = new DataReader(input, StandardCharsets.ISO_8859_1).readAsString();

new DataReader(input).readAsLineString(line -> {
    if (!line.isEmpty())
        consumeCsvLine(line);
});
```

For streaming work, the callback sees the same buffer instance on every iteration. Copy a chunk if it must outlive
the callback; otherwise process it immediately. The final chunk can be shorter than the requested buffer size.

```java
MessageDigest digest = MessageDigest.getInstance("SHA-256");
new DataReader(input).readStreamAsBytes(8 * 1024,
        (size, buffer) -> digest.update(buffer, 0, size));

// For "abcdefghij" and a buffer size of 4, callback sizes are 4, 4, 2.
```

The buffer size must be positive. I/O failures are wrapped as `UncheckedIOException`, while close failures are logged.

## Copying to an output stream

`new DataWriter(out).write(in)` copies an input stream in 1 KiB chunks and flushes the output. It uses a buffered
output wrapper by default; turn it off only when the destination already has suitable buffering or the flushing
behavior is significant.

```java
try (InputStream in = Files.newInputStream(source);
     OutputStream out = Files.newOutputStream(target)) {
    new DataWriter(out).write(in);
}

ByteArrayOutputStream memory = new ByteArrayOutputStream();
new DataWriter(memory).write("hello".getBytes(StandardCharsets.UTF_8));
```

`DataWriter` never closes the caller's output stream. Its instance `write(InputStream)` delegates to `DataReader`, so
the input is closed when copying completes; callers should regard that input as consumed. The static
`DataWriter.write(OutputStream, InputStream)` is lower level: it only copies bytes and does not flush or close either
stream.

For `write(byte[], off, length)`, `(0, 0)` is a special shorthand meaning **write the entire array**; it is not a
zero-byte write. Pass an explicit non-zero length for a slice, such as `write(bytes, 7, 5)`.

## Implementation notes

Both classes are deliberately small adapters around JDK streams. `DataReader` adds a buffered character reader only
for line-oriented text, while byte transfer uses one reusable byte array to avoid allocating a new array for each
chunk. `DataWriter` flushes instead of closing so that callers can compose several writes or retain ownership of an
HTTP response stream. For Java 9+ projects that do not need Java 8 compatibility, `InputStream.transferTo` is the JDK
alternative for a simple stream copy.

