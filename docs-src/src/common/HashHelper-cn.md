---
title: HashHelper
description: HashHelper
layout: layouts/aj-util-cn.njk
lang: zh-CN
alternate: /common/HashHelper/
---

# HashHelper

`com.ajaxjs.util.HashHelper` 计算摘要与 HMAC。哈希不是可逆加密；MD5、SHA-1 属于旧摘要算法，不适合需要抗碰撞的安全判断。普通哈希不是密码存储方案。

构造器接收算法名称和 `String`（UTF-8）或 `byte[]`。`hash()` 将以区分大小写的 `Hmac` 开头的名称分派给 `getMac()`，其余交给 `getMessageDigest()`。`hashAsStr()` 输出小写十六进制；`hashAsBase64()` 保留填充，`hashAsBase64(true)` 去掉填充。

## HMAC 密钥

`setKey(String)` 使用 UTF-8 密钥字节；`setKeyBase64(String)` 解码密钥材料，均返回实例。未设置密钥抛出 `IllegalStateException`，不会生成随机密钥。不支持的算法会失败而非返回 null。

```java
import com.ajaxjs.util.HashHelper;

String digest = HashHelper.sha256("hello");
String mac = new HashHelper(HashHelper.HMAC_SHA256, "hello")
        .setKey("example-only-key").hashAsBase64();
String sameMac = HashHelper.hmacSHA256("hello", "example-only-key", false);
```

## 便捷方法

`md5(String)`、`md5(byte[])`、`md5(InputStream)`、`sha1(String)`、`sha256(String)` 返回小写十六进制。`hmacMD5`、`hmacSHA1`、`hmacSHA256` 接收 `(String data, String key, boolean isWithoutPadding)`，返回 Base64。

**流所有权：** 当前 `md5(InputStream)` 通过 `DataReader` 关闭输入流，与源码 Javadoc 的说法不同，调用后不要复用。它分块处理而非加载整个文件。请保护真实 HMAC 密钥；示例密钥不是生产秘密。
