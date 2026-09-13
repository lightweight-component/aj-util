---
title: UnzipHelper
description: Secure ZIP extraction with resource limits
layout: layouts/aj-util.njk
lang: en
alternate: /io/UnzipHelper-cn/
---

# UnzipHelper

`UnzipHelper` extracts ZIP archives with path-traversal, symbolic-link, entry-count, size, and compression-ratio
protection. It is intentionally separate from `ZipHelper`.

```java
import com.ajaxjs.util.io.UnzipHelper;

new UnzipHelper("C:/downloads/archive.zip").extract();
```

Without a target directory, `archive.zip` is extracted into a sibling `archive` directory. Supply a target when the
destination is explicit:

```java
new UnzipHelper("C:/downloads/archive.zip", "C:/work/extracted").extract();
```

## Limits and legacy filenames

Default limits allow 10,000 entries, 1 GB per entry, 10 GB in total, and a 100:1 compression ratio. Trusted archives
with different operational requirements can use custom limits:

```java
UnzipHelper.ExtractionLimits limits =
        new UnzipHelper.ExtractionLimits(2_000, 100L * 1024 * 1024, 500L * 1024 * 1024, 50.0);
new UnzipHelper("archive.zip", "output", limits).extract();
```

ZIP filenames normally use UTF-8. For a legacy GBK archive, configure the charset before extracting:

```java
UnzipHelper helper = new UnzipHelper("legacy.zip");
helper.setCharset(java.nio.charset.Charset.forName("GBK"));
helper.extract();
```

Entries are written to temporary files and moved only after each entry succeeds. Extraction can still leave files from
entries completed before a later archive error; callers needing all-or-nothing behaviour should extract to a fresh
temporary directory first.

