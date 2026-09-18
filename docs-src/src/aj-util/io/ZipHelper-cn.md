---
title: ZipHelper
description: 将目录、单个文件或多个文件打包为 ZIP
layout: layouts/aj-util-cn.njk
lang: zh-CN
alternate: /io/ZipHelper/
---

# ZipHelper

`ZipHelper` 用于创建 ZIP 文件。通过目录、单个 `File` 或 `File[]` 构造对象后调用 `zip()`。解压功能由独立的 [UnzipHelper](/aj-util/io/UnzipHelper-cn/) 提供。

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

## 压缩包目录结构示例

目录输入在 ZIP 中以所选源目录为相对根，而不会包含源目录自身的名字。无论 ZIP 在 Windows 还是其他系统生成，条目
分隔符都统一使用 `/`。

| 源文件 | ZIP 内条目 |
| --- | --- |
| `C:/work/source/root.txt` | `root.txt` |
| `C:/work/source/a/b/nested.txt` | `a/b/nested.txt` |
| `C:/work/source/empty` | `empty/` |
| `new File[]{a.txt, b.txt}` | `a.txt`、`b.txt` |

```java
Path source = Paths.get("C:/work/source");
new ZipHelper(source.toString(), "C:/out/source.zip").zip();

// 两个文件都位于 ZIP 根目录；同名基础文件会被拒绝。
new ZipHelper(new File[]{new File("logs/app.log"), new File("report.csv")},
        "C:/out/files.zip").zip();
```

目录遍历会显式记录目录条目，因此解压后空目录仍然存在。这对于应用模板或目录结构本身具有意义的备份很重要。

## 压缩模式的选择

`DEFLATED` 是常规选择：ZIP 写入时同步压缩。`STORED` 则写入原始文件字节；ZIP 要求在写入前就提供精确大小和
CRC32。因此 `ZipHelper` 会先读取每个文件计算元数据，再读取一次写入压缩包。

只在确实不需要压缩时使用 `STORED`，例如已经压缩过的媒体文件且接收方兼容该模式。两次读取之间不要修改源文件，
否则记录的 CRC 或长度可能与实际归档字节不一致。

## 安全性与失败行为

该工具会拒绝符号链接与非普通文件，避免意外把源目录之外的文件打进包内；也拒绝把目标 ZIP 放在源目录内，避免归档
递归包含自己。目标父目录不存在时会自动创建。

它先写入目标同目录的临时文件，成功后才替换目标，降低失败时留下半成品目标文件的概率。这并不是跨所有文件系统的
事务：进程崩溃或不支持原子移动时，仍应按普通文件系统故障方式处理。需要向用户提示或重试备份时，请捕获
`UncheckedIOException`。

## 实现原理与取舍

`ZipHelper` 只负责创建压缩包，本身并不保证解压安全。处理不可信 ZIP 时请使用
[UnzipHelper](/aj-util/io/UnzipHelper-cn/)，并保留其目标路径校验，以防止 `../../outside.txt` 这类路径穿越条目。
