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

## 使用摘要还是 HMAC？

消息摘要是不带密钥的指纹：相同字节总会得到相同结果，适合在预期摘要来自可信来源时进行完整性校验。HMAC 将数据与共享
秘密结合，用于认证消息。内部不会通过另一套方法自动判断 HMAC，算法名称必须以精确大小写的 `Hmac` 开头，例如
`HmacSHA256`。

```java
String sha256 = new HashHelper(HashHelper.SHA256, "abc").hashAsStr();
// ba7816bf8f01cfea414140de5dae2223...

String signature = new HashHelper(HashHelper.HMAC_SHA256, "data")
        .setKey("shared-secret")
        .hashAsBase64(true);
```

调用 HMAC 前必须设置密钥。只有当协议将秘密定义为 UTF-8 文本时才使用 `setKey(String)`；如果提供方给出的是随机密钥
字节的 Base64 表示，请使用 `setKeyBase64(String)`，避免把 Base64 字符本身误当作密钥。

```java
String keyFromConfig = "c2lnbmluZy1rZXktYnl0ZXM=";
String mac = new HashHelper(HashHelper.HMAC_SHA256, payloadBytes)
        .setKeyBase64(keyFromConfig)
        .hashAsBase64();
```

## 输出格式与互操作

`hashAsStr()` 输出小写十六进制，适合日志或校验和文件。`hashAsBase64()` 对二进制摘要更短，默认保留 `=` 填充。应选择
远端协议规定的格式；即使底层字节相同，改变十六进制大小写或 Base64 填充也可能导致签名比较失败。

字符串数据一律按 UTF-8 转字节。若协议要求对规范化字节序列签名，请先构造出精确的 `byte[]` 再创建 helper。除非协议
明确规定，否则不要签名 Java 对象的字符串表示。

## 流式校验和

`md5(InputStream)` 每块更新一次 `MessageDigest`，不会将整个文件保留在内存中：

```java
try (InputStream in = Files.newInputStream(Paths.get("release.zip"))) {
    String checksum = HashHelper.md5(in);
    verifyExpectedChecksum(checksum);
}
```

该方法自身会关闭流。外层的 try-with-resources 无害但属于冗余；当同一段代码日后可能换用其他流 API 时，保留它也能让
资源所有权更直观。

## 安全建议与实现原理

新设计应优先 SHA-256 或 HMAC-SHA-256。MD5、SHA-1 可以用于遗留互操作或非安全校验和，但不要用于密码存储、签名或
需要抗碰撞的身份判断。密码应使用合适安全库提供的专门、可调成本的密码哈希算法。

每次操作都会向 JDK 安全提供者获取 `MessageDigest` 或 `Mac`、初始化并返回结果；实例不缓存可变摘要状态。不支持的
算法与非法 HMAC 密钥会明确失败，不会悄悄返回空值。
