---
title: StreamHelper Tutorial
description: Utility methods for stream operations and conversions
tags:
  - stream operations
  - I/O
  - Java
layout: layouts/aj-util.njk
---


# DataReader Tutorial

DataReader  is a reader class for reading data from an input stream. It supports reading raw byte data from sources such as files, network sockets, or memory buffers, while providing functionality for handling both text and binary data.

## Main Features

1. **Multiple Data Reading Methods**: Supports various reading methods including byte streams, text lines, and complete strings
2. **Character Encoding Support**: Character encoding can be specified, defaults to UTF-8
3. **Streaming Processing**: Supports chunked processing of large files to avoid memory overflow
4. **Functional Programming**: Uses functional interfaces to process read data

## Basic Usage

### 1. Creating Instances

```java
// Create instance with default UTF-8 encoding
InputStream inputStream = new FileInputStream("example.txt");
DataReader reader = new DataReader(inputStream);

// Create instance with specified character encoding
DataReader readerWithEncoding = new DataReader(inputStream, StandardCharsets.UTF_8);
```


### 2. Reading Byte Data

```java
// Read stream data as bytes
InputStream inputStream = new FileInputStream("large-file.bin");
DataReader reader = new DataReader(inputStream);

reader.readStreamAsBytes(8192, (readSize, buffer) -> {
    // Process the read data
    System.out.println("Read " + readSize + " bytes of data");
    // Can process data in buffer here
});
```


### 3. Reading Text Lines

```java
// Read text data line by line
InputStream inputStream = new FileInputStream("text-file.txt");
DataReader reader = new DataReader(inputStream);

reader.readAsLineString(line -> {
    System.out.println("Read a line: " + line);
    // Process each line of text
});
```


### 4. Reading Complete String

```java
// Read entire input stream as string
InputStream inputStream = new FileInputStream("text-file.txt");
DataReader reader = new DataReader(inputStream);

String content = reader.readAsString();
System.out.println("File content: " + content);
```


### 5. Reading Byte Array

```java
// Read entire input stream as byte array
InputStream inputStream = new FileInputStream("binary-file.bin");
DataReader reader = new DataReader(inputStream);

byte[] bytes = reader.readAsBytes();
System.out.println("Byte array length: " + bytes.length);
```



### Notes

1. The `readStreamAsBytes` method automatically closes the input stream when reading is complete
2. All methods throw `UncheckedIOException` when IO errors occur
3. For large file processing, it's recommended to use the `readStreamAsBytes` method for chunked processing
4. The `readAsString` method appends a newline character at the end of each line

# DataWriter Tutorial

DataWriter is a writer class for writing data to an output stream. It supports writing raw byte data to destinations such as files, sockets, or buffers, and provides buffering functionality to improve write efficiency.

## Main Features

1. **Multiple Data Writing Methods**: Supports copying data from input streams, writing byte arrays, etc.
2. **Buffering Support**: Buffering functionality is enabled by default to improve write performance
3. **Flexible Byte Array Writing**: Supports writing complete byte arrays or specified ranges of data
4. **No Automatic Stream Closing**: Does not automatically close output streams, managed by the caller

## Basic Usage

### 1. Creating Instances

```java
// Create DataWriter instance
OutputStream outputStream = new FileOutputStream("output.txt");
DataWriter writer = new DataWriter(outputStream);
```


### 2. Copying Data from Input Stream

```java
// Copy data from input stream to output stream
InputStream inputStream = new FileInputStream("input.txt");
OutputStream outputStream = new FileOutputStream("output.txt");
DataWriter writer = new DataWriter(outputStream);

writer.write(inputStream); // Copy data
// Note: Need to manually close streams
inputStream.close();
outputStream.close();
```


### 3. Writing Byte Arrays

```java
// Write complete byte array
byte[] data = "Hello World".getBytes();
OutputStream outputStream = new FileOutputStream("output.txt");
DataWriter writer = new DataWriter(outputStream);

writer.write(data);
outputStream.close();
```


### 4. Writing Specified Range of Byte Array

```java
// Write specified range of byte array
byte[] data = "Hello World".getBytes();
OutputStream outputStream = new FileOutputStream("output.txt");
DataWriter writer = new DataWriter(outputStream);

// Write 5 bytes starting from offset 2
writer.write(data, 2, 5); // Write "llo W"
outputStream.close();
```




## Configuration Options

```java
// Disable buffering (not recommended)
OutputStream outputStream = new FileOutputStream("output.txt");
DataWriter writer = new DataWriter(outputStream);
writer.setBuffered(false);
```


## Notes

1. DataWriter does not automatically close output streams, the caller needs to manually manage stream closing
2. Buffering is enabled by default, disabling buffering will give a warning prompt
3. All methods throw `UncheckedIOException` when IO errors occur
4. When both offset and length are 0, the `write(byte[], int, int)` method writes the entire byte array