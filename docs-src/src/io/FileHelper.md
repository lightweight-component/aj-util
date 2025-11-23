---
title: FileHelper Tutorial
description: Utility methods for simplifying file system operations
tags:
  - file operations
  - I/O
  - Java
layout: layouts/aj-util.njk
---


# FileHelper Tutorial

FileHelper is a file operation utility class that provides methods for reading, writing, copying, moving, deleting, and manipulating files and directories. This class uses Java NIO Path and Files API for efficient file operations, and all methods throw UncheckedIOException for IO errors.

## Main Features

1. **Multiple Construction Methods**: Supports creating instances with Path, File, and String paths
2. **File Read/Write Operations**: Supports reading and writing text and byte content
3. **File Management Operations**: Copy, move, delete, and create directories
4. **Directory Operations**: List directory contents, get file sizes
5. **File Chunking**: Supports large file chunking and merging
6. **Chainable Calls**: Supports method chaining

## Basic Usage

### 1. Creating Instances

```java
// Create with path string
FileHelper helper1 = new FileHelper("path/to/file.txt");

// Create with Path object
Path path = Paths.get("path/to/file.txt");
FileHelper helper2 = new FileHelper(path);

// Create with File object
File file = new File("path/to/file.txt");
FileHelper helper3 = new FileHelper(file);
```


### 2. File Reading

```java
// Read file text content
String content = new FileHelper("example.txt").getFileContent();

// Read file byte content
byte[] bytes = new FileHelper("example.txt").readFileBytes();
```


### 3. File Writing

```java
// Write string content to file
new FileHelper("output.txt").writeFileContent("Hello World");
```


### 4. File Deletion

```java
// Delete file or directory (recursively delete directories)
new FileHelper("file-or-directory").delete();
```


### 5. Directory Operations

```java
// List directory contents
List<String> contents = new FileHelper("directory").listDirectoryContents();

// Create directory (supports multi-level directories)
new FileHelper("new/directory/path").createDirectory();

// Get file or directory size
long size = new FileHelper("file-or-directory").getFileSize();
```


### 6. File Copying and Moving

```java
// Copy file or directory
new FileHelper("source.txt")
    .setTarget("destination.txt")
    .copyTo();

// Move file or directory
new FileHelper("old-location.txt")
    .setTarget("new-location.txt")
    .moveTo();
```


### 7. File Chunking Operations

```java
// Chunk file (1MB per chunk)
new FileHelper("large-file.zip").chunkFile(1024 * 1024);

// Merge chunk files
Path[] chunks = {Paths.get("file-1"), Paths.get("file-2")};
new FileHelper("merged-file").mergeFile(chunks);
```


## Chainable Call Example

```java
// Chainable call example
new FileHelper("input.txt")
    .setTarget("backup.txt")
    .copyTo()
    .setTarget("moved.txt")
    .moveTo();
```


## Exception Handling

All IO operations throw `UncheckedIOException` when errors occur, which can be handled with try-catch:

```java
try {
    String content = new FileHelper("nonexistent.txt").getFileContent();
} catch (UncheckedIOException e) {
    System.err.println("File operation failed: " + e.getMessage());
}
```


### Notes

1. Target path must be set before copy and move operations
2. File chunking uses zero-copy technology for improved large file processing efficiency
3. Directory deletion uses reverse traversal to ensure proper deletion of all subdirectories and files