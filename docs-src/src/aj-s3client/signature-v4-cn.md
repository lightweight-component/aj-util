---
title: Signature Version 4
layout: layouts/aj-util-cn.njk
alternate: /signature-v4/
---

# Signature Version 4

`CloudflareR2`、`Backblaze` 和 `Scaleway` 均继承 `BaseS3ClientSigV4`，使用**路径式** URL。例如，`reports` 存储桶中键名为 `2026/a b.txt` 的对象地址为 `https://endpoint/reports/2026/a%20b.txt`；存储桶位于路径中，而不是主机名中。

## 必需配置

SigV4 需要端点、访问密钥、私钥和区域。`Config` 的 `region` 默认是 `auto`，适合 Cloudflare R2，但并非所有服务都接受该值。使用 Backblaze、Scaleway 或其他兼容端点时，应设置厂商要求的签名区域。

```java
Config config = new Config();
config.setEndPoint("s3.example-provider.com");
config.setAccessKey(System.getenv("S3_ACCESS_KEY"));
config.setSecretKey(System.getenv("S3_SECRET_KEY"));
config.setRegion("provider-region");
config.setBucketName("reports");

CloudflareR2 client = new CloudflareR2();
client.setConfig(config);
```

端点会被规范化为 `https://…`，且必须仅是 origin。`https://host/prefix` 这类带路径的端点会被拒绝；本客户端不支持端点路径前缀。

## 签名内容

每个操作都会生成 `x-amz-date` 与 `x-amz-content-sha256`。上传时对传入字节计算 SHA-256；GET、DELETE、创建/删除存储桶和根路径列举使用空正文的 SHA-256。随后由以下内容构成规范请求：

1. HTTP 方法。
2. 规范路径与已排序的规范查询字符串。
3. 规范化后的参与签名请求头。
4. 正文 SHA-256。

`SignBuilder` 为 `date/region/s3/aws4_request` 派生 AWS4 密钥，并生成 `Authorization: AWS4-HMAC-SHA256 …`。请求头名称不区分大小写并排序；值会去首尾空白并合并连续空白。包含 CR 或 LF 的请求头值会被拒绝。

基础客户端参与签名的请求头是 `x-amz-date` 与 `x-amz-content-sha256`。`Backblaze` 还会签名 `Host`；`Scaleway` 上传时会添加参与签名的 `x-amz-acl`。所有参与签名的请求头都必须以相同值实际发送，仅加入签名而未发送是不够的。

## 编码与对象键

键名按 S3 规范使用 UTF-8 百分号编码：空格变为 `%20`，绝不变为 `+`；`/` 保留为路径分隔符；查询参数名和参数值分别编码、排序。不要预先编码键名：传入 `folder/a%20b.txt` 会将 `%` 当作数据再次编码，应传入 `folder/a b.txt`。

```java
client.putObject("reports", "exports/September report.csv", csvBytes);
```

客户端允许以 `/` 分隔的键名，但拒绝空键名和控制字符。对 S3 签名不会规范化 `.` 或 `..` 路径段，因为它们可能是有意义的对象键字节。

## 排查签名失败

- 确认端点不带路径，并且是服务的 S3 API 端点，而不是公开对象 URL。
- 使用厂商要求的区域；错误区域会改变 credential scope 和签名。
- 不要让代理或自定义传输修改参与签名的请求头。
- 检查系统时间。SigV4 使用当前 UTC 时间戳，服务端会拒绝时钟偏差过大的请求。
- 将存储桶策略、访问密钥权限与签名问题分别排查；签名正确仍可能得到 403。

公开 API 对非 2xx 响应只返回 `false`，不会暴露错误正文。若需要诊断，请使用厂商侧请求日志，或以底层源码为基础扩展响应处理。
