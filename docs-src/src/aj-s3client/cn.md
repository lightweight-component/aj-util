---
title: aj-s3client
layout: layouts/aj-util-cn.njk
---

# aj-s3client

`aj-s3client` 是一个面向 Java 8 及以上环境的轻量级 S3 兼容对象存储客户端，提供一组精简的存储桶和对象操作。它会根据所选厂商客户端使用 Signature Version 4（SigV4）或旧版 Signature Version 2（SigV2）对请求签名。

> 本模块不是通用 AWS SDK；不实现分片上传、预签名 URL、对象元数据或 ACL 管理（Scaleway 上传 ACL 除外）、分页，也没有返回或保存对象字节的下载 API。

## 添加依赖

当前 POM 坐标为 `com.ajaxjs:aj-s3client:1.6`，并依赖 `com.ajaxjs:aj-net:2.1` 作为 HTTP 传输层。

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-s3client</artifactId>
    <version>1.6</version>
</dependency>
```

## 配置客户端

选择厂商实现后，为它设置 `Config`。端点必须是 HTTPS origin：可省略协议名，但不允许路径、用户信息、查询参数或片段。请勿提交或记录访问密钥与私钥。

```java
import com.ajaxjs.s3client.Config;
import com.ajaxjs.s3client.factory.CloudflareR2;

Config config = new Config();
config.setEndPoint("<account-id>.r2.cloudflarestorage.com");
config.setAccessKey(System.getenv("R2_ACCESS_KEY_ID"));
config.setSecretKey(System.getenv("R2_SECRET_ACCESS_KEY"));
config.setBucketName("reports");
// Config 默认 region 为 "auto"，适合 R2。

CloudflareR2 client = new CloudflareR2();
client.setConfig(config);
```

`bucketName` 用于只传对象键的便捷重载方法。存储桶名称不能包含 `/` 或 `\\`；对象键不能为空，且不能含控制字符。对象键会在签名和发送前按路径规则编码。

## 上传、列举和删除对象

通用操作只有在 HTTP 响应状态为 2xx 时才返回 `true`。请先读取待上传的字节；客户端不持有流，也未实现 `Closeable`。

```java
import java.nio.charset.StandardCharsets;

boolean uploaded = client.putObject(
        "2026-09/summary.txt",
        "completed".getBytes(StandardCharsets.UTF_8));
if (!uploaded) {
    throw new IllegalStateException("Object upload was rejected");
}

String listXml = client.listBucket();
boolean removed = client.deleteObject("2026-09/summary.txt");
```

目标不是默认存储桶时，使用包含存储桶名称的重载：

```java
boolean uploaded = client.putObject("archive", "exports/report.csv", csvBytes);
boolean deleted = client.deleteObject("archive", "exports/report.csv");
```

`listBucket()` 返回厂商的原始 XML；`listBucketXml()` 将 XML 转换为 `Map<String, String>`。`getObject(...)` 只发送已签名的 GET 请求并报告响应是否成功，**不会**返回对象内容或将其写入文件。可通过 `createBucket(name)` 和 `deleteBucket(name)` 创建、删除存储桶。

## 厂商客户端与签名方式

| 客户端 | 签名 | 说明 |
| --- | --- | --- |
| `CloudflareR2` | SigV4 | 使用路径式对象 URL；`region` 默认值为 `auto`。 |
| `Backblaze` | SigV4 | 会将 `Host` 加入签名请求头；需配置厂商要求的区域。 |
| `Scaleway` | SigV4 | 每次上传都会添加并签名 `x-amz-acl: public-read`；需配置厂商要求的区域。 |
| `AliyunOSS` | SigV2 | 必须设置 `config.setRemark("OSS")`；使用虚拟主机式 URL。 |
| `NeteaseOSS` | SigV2 | 为兼容保留的旧客户端；必须设置 `config.setRemark("NOS")`，使用虚拟主机式 URL。 |

对于 SigV4 客户端，若厂商不接受 `auto`，请将 `region` 设为其要求的值。SigV2 客户端同样要求 `remark`、访问密钥、私钥、端点和区域均为非空，并使用上表所列的前缀。支持范围仅限这些实现及其当前请求行为；上线前请针对所选服务验证凭据、端点格式、存储桶策略与区域要求。

## 文档目录

- [Signature Version 4：请求构建与签名](/aj-s3client/signature-v4-cn/)
- [Signature Version 2：旧版厂商行为](/aj-s3client/signature-v2-cn/)
- [厂商 Factory](/aj-s3client/factories-cn/)
- [API、URL 风格与功能限制](/aj-s3client/api-cn/)

## 安全与运行说明

- 所有配置端点均强制使用 HTTPS；不要在底层传输层放宽 TLS 证书校验。
- 应将凭据放在密钥管理服务或环境变量中。`Config` 是可变对象，应避免共享和日志输出。
- `Scaleway.putObject` 会使上传的对象公开可读；如不希望公开，请选用 `CloudflareR2` 或其他符合策略的客户端。
- ETag 通常不保证是 MD5。只有旧版 NetEase 上传路径会将单段、未加密对象的 ETag 按 MD5 校验。
- 厂商联调测试因需要私有凭据和远程资源而被禁用；签名与 URL 编码的单元测试可离线运行。

## 相关资源

- [源代码](https://github.com/lightweight-component/aj-util/tree/main/aj-s3client)
