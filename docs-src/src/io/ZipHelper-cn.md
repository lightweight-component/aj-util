---
title: ZipHelper
description: 将目录、单个文件或多个文件打包为 ZIP
layout: layouts/aj-util-cn.njk
lang: zh-CN
alternate: /io/ZipHelper/
---

# ZipHelper

`ZipHelper` 用于创建 ZIP 文件。通过目录、单个 `File` 或 `File[]` 构造对象后调用 `zip()`。解压功能由独立的 [UnzipHelper](/io/UnzipHelper-cn/) 提供。

```java
import com.ajaxjs.util.io.ZipHelper;

ZipHelper backup = new ZipHelper("C:/data", "C:/backup.zip");
backup.zip();
```

## 数据源与压缩模式

目录会被递归遍历，保留相对路径和空目录；符号链接与非普通文件会被拒绝。目标 ZIP 不能位于源目录内部。

```java
ZipHelper single = new ZipHelper(new File("report.csv"), "report.zip");
single.zip();

ZipHelper files = new ZipHelper(new File[]{new File("a.txt"), new File("b.txt")}, "files.zip");
files.setUseStore(true); // STORED：不压缩
files.zip();
```

默认使用 `DEFLATED`。调用 `setUseStore(true)` 后使用 `STORED` 模式；该模式会先计算文件大小与 CRC32，因此打包过程中源文件不应发生变化。多个文件会放在 ZIP 根目录，文件名必须互不重复。

生成过程先写入目标文件同目录下的临时文件，成功后再替换目标文件。I/O 失败会抛出 `UncheckedIOException`。
