---
title: UnzipHelper
description: 带资源限制与安全保护的 ZIP 解压
layout: layouts/aj-util-cn.njk
lang: zh-CN
alternate: /io/UnzipHelper/
---

# UnzipHelper

`UnzipHelper` 专门负责安全解压 ZIP，包含 Zip Slip、符号链接、条目数量、解压大小和压缩比保护；它与 `ZipHelper` 的压缩职责分离。

```java
import com.ajaxjs.util.io.UnzipHelper;

new UnzipHelper("C:/downloads/archive.zip").extract();
```

未指定目标目录时，`archive.zip` 默认解压到同级的 `archive` 目录。也可指定目标目录：

```java
new UnzipHelper("C:/downloads/archive.zip", "C:/work/extracted").extract();
```

## 限制与旧编码文件名

默认限制为 10,000 个条目、单条目 1 GB、总量 10 GB、最大压缩比 100:1。可信压缩包可按实际需要配置限制：

```java
UnzipHelper.ExtractionLimits limits =
        new UnzipHelper.ExtractionLimits(2_000, 100L * 1024 * 1024, 500L * 1024 * 1024, 50.0);
new UnzipHelper("archive.zip", "output", limits).extract();
```

ZIP 文件名默认按 UTF-8 读取。对于使用 GBK 的旧 ZIP，在解压前设置字符集：

```java
UnzipHelper helper = new UnzipHelper("legacy.zip");
helper.setCharset(java.nio.charset.Charset.forName("GBK"));
helper.extract();
```

单个条目会先写入临时文件，成功后再移动到目标位置。若压缩包在后续条目失败，之前已完成的条目仍会保留；需要全有或全无语义时，应先解压到新的临时目录。

