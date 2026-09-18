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

## Archive layout examples

Directory input is represented relative to the selected source directory, rather than including the source directory
name itself. All entry separators are `/`, including when the archive is created on Windows.

| Source | Entry in archive |
| --- | --- |
| `C:/work/source/root.txt` | `root.txt` |
| `C:/work/source/a/b/nested.txt` | `a/b/nested.txt` |
| `C:/work/source/empty` | `empty/` |
| `new File[]{a.txt, b.txt}` | `a.txt`, `b.txt` |

```java
Path source = Paths.get("C:/work/source");
new ZipHelper(source.toString(), "C:/out/source.zip").zip();

// Creates a root-level archive of the two files; duplicate base names are rejected.
new ZipHelper(new File[]{new File("logs/app.log"), new File("report.csv")},
        "C:/out/files.zip").zip();
```

The directory walker records directory entries explicitly, so an empty directory remains visible after extraction.
This matters for application templates and backup archives where directory structure is meaningful.

## Compression choice

`DEFLATED` is the normal choice: the ZIP writer compresses while it writes. `STORED` writes raw file bytes; ZIP
requires the exact size and CRC32 in the entry header before those bytes are written. `ZipHelper` therefore reads each
file once to calculate metadata and again to archive it.

Use `STORED` only when no compression is desired (for example, an already-compressed media file and a compatible
consumer). Do not modify input files between those two passes. A changed file can make the recorded CRC or length
inconsistent with the archived bytes.

## Safety and failure behavior

The helper intentionally rejects symbolic links and non-regular files, avoiding accidental inclusion of a file outside
the selected tree. It also rejects a destination placed within the source tree, which would otherwise risk recursively
adding the archive to itself. The destination parent directory is created when needed.

Writing to a temporary sibling archive before replacing the destination reduces the chance that a failed operation
leaves a half-written target. It is not a transaction across every filesystem: a process crash or an unsupported atomic
move can still leave normal filesystem-level recovery considerations. Handle `UncheckedIOException` when the caller
needs to surface or retry a failed backup.

## Implementation notes

`ZipHelper` is a creation-only API; it does not make extraction safe by itself. When consuming untrusted ZIP files,
use [UnzipHelper](/aj-util/io/UnzipHelper/) and keep its destination validation in place to defend against path
traversal entries such as `../../outside.txt`.
