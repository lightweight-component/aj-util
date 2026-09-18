---
title: ZipHelper
description: Create ZIP archives from a directory, one file, or multiple files
layout: layouts/aj-util.njk
lang: en
alternate: /io/ZipHelper-cn/
---

# ZipHelper

`ZipHelper` creates ZIP archives. Construct it with a directory, a single `File`, or an array of files, then call
`zip()`. ZIP extraction is provided separately by [UnzipHelper](/aj-util/io/UnzipHelper/).

```java
import com.ajaxjs.util.io.ZipHelper;

ZipHelper backup = new ZipHelper("C:/data", "C:/backup.zip");
backup.zip();
```

## Sources and compression mode

Directory sources are traversed recursively; relative paths and empty directories are preserved. Symbolic links and
non-regular files are rejected. The destination ZIP must be outside the source directory.

```java
ZipHelper single = new ZipHelper(new File("report.csv"), "report.zip");
single.zip();

ZipHelper files = new ZipHelper(new File[]{new File("a.txt"), new File("b.txt")}, "files.zip");
files.setUseStore(true); // STORED: no compression
files.zip();
```

The default mode is `DEFLATED`. `setUseStore(true)` selects `STORED`; it calculates each source file's size and CRC32
before writing, so source files must not change during the operation. Multiple files must have distinct file names,
because they are stored at the archive root.

The archive is first written to a temporary sibling file, then replaces the target only after a successful write.
I/O failures are reported as `UncheckedIOException`.
