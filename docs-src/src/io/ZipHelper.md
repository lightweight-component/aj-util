---
title: ZipHelper Tutorial
description: Utility methods for ZIP compression and decompression operations
tags:
  - compression
  - decompression
  - Java
layout: layouts/aj-util.njk
---

# ZipHelper Tutorial

This tutorial provides an overview of the `ZipHelper` class, which is part of the `lightweight-component/aj-util` library. The `ZipHelper` class provides utility methods for ZIP compression and decompression operations.

## Introduction

The `ZipHelper` class contains static methods for working with ZIP files, including compression, decompression, and file type detection.

## Main Features

- ZIP extraction with Zip Slip, symbolic-link, entry-count, size, and compression-ratio protection
- File and directory compression (with STORED or DEFLATED options)
- Recursive directory compression
- ZIP file detection (magic number check)
- Buffered CRC32 calculation for STORED entries
- Automatic directory creation

## Methods

### 1. Decompression

1. `unzip(String save, String zipFile)` - Decompress ZIP file to target directory
2. `unzip(String save, String zipFile, ExtractionLimits limits)` - Decompress with custom resource limits
3. `unzipWithChineseFilename(...)` - Read legacy GBK entry names

### 2. Compression

1. `zipFile(File[] fileContent, String saveZip, boolean useStore)` - Compress files to ZIP
2. `zipDirectory(String sourceDir, String saveZip, boolean useStore)` - Compress directory to ZIP
3. `zipSingleFile(String sourceFile, String saveZip, boolean useStore)` - Compress one file

### 3. Utility Methods

1. `initFolder(File file)` - Ensure parent directories exist for a file
2. `initFolder(String file)` - String version of initFolder
3. `isZipFile(String filePath)` - Check if file is a valid ZIP file

## Usage Examples

### Decompression
```java
ZipHelper.unzip("C:/extracted", "C:/archive.zip");
```

Extraction uses conservative default limits. Use the overload with `ExtractionLimits` when trusted archives require different limits.

### File Compression
```java
File[] files = {new File("file1.txt"), new File("file2.txt")};
ZipHelper.zipFile(files, "archive.zip", false); // Use DEFLATED compression
```

### Directory Compression
```java
ZipHelper.zipDirectory("C:/data", "backup.zip", true); // Use STORED (no compression)
```

Directory compression does not follow symbolic links. The destination ZIP must be outside the source directory.

### ZIP File Detection
```java
boolean isZip = ZipHelper.isZipFile("unknown.zip");
```

### Directory Creation
```java
ZipHelper.initFolder("C:/new/path/file.txt"); // Creates C:/new/path if needed
```

## Compression Methods

The class supports two compression methods:
1. `DEFLATED` (default) - Standard ZIP compression
2. `STORED` - No compression, files are stored as-is

## Error Handling

I/O failures are reported as `UncheckedIOException`; invalid arguments use `IllegalArgumentException`. Compression is written to a temporary file and published only after the archive is complete.

## Conclusion

The `ZipHelper` class provides comprehensive utility methods for working with ZIP files, making compression and decompression operations more convenient in Java applications.
