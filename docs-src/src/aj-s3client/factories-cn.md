---
title: 厂商 Factory
layout: layouts/aj-util-cn.njk
alternate: /factories/
---

# 厂商 Factory

`com.ajaxjs.s3client.factory` 包放置的是具体客户端，而不是 Factory Method API。应直接实例化厂商类，创建 `Config` 后调用 `setConfig`。

| 类 | 基类 | URL 风格 | 厂商特有行为 |
| --- | --- | --- | --- |
| `CloudflareR2` | `BaseS3ClientSigV4` | 路径式 | 通用 SigV4 实现；R2 通常使用 `auto` 区域。 |
| `Backblaze` | `CloudflareR2` | 路径式 | 在签名和实际请求头中启用 `Host`。 |
| `Scaleway` | `CloudflareR2` | 路径式 | 每次对象上传都发送并签名 `x-amz-acl: public-read`。 |
| `AliyunOSS` | `BaseS3ClientSigV2` | 虚拟主机式 | 使用带 `OSS` 前缀的 HMAC-SHA1/Base64 授权。 |
| `NeteaseOSS` | `BaseS3ClientSigV2` | 虚拟主机式 | 使用带 `NOS` 前缀的旧版 HMAC-SHA256 授权，以及 MD5/ETag 上传校验。 |

## CloudflareR2

该类实现模块默认的路径式 SigV4 流程。所有存储桶方法使用 `https://endpoint/bucket`，对象方法在其后添加编码后的键名；没有厂商专属 ACL 或 Host 覆盖行为。

## Backblaze

`Backblaze` 是启用 `isSetHost` 的 `CloudflareR2`。签名构建器会加入 `host`，请求回调也会实际发送该头。应显式设置厂商区域。除非已验证目标服务精确的签名要求，不要仅为强制 `Host` 请求头而将其他服务套用此子类。

## Scaleway

`Scaleway.putObject` 与其他 SigV4 上传有实质差异：它总会设置 `x-amz-acl: public-read`，将该头加入规范签名，并在厂商支持 canned ACL 时使上传对象公开可读。此策略针对每次上传，不能通过 `Config` 关闭；不接受公开读取时请改用其他客户端。

## AliyunOSS 与 NeteaseOSS

这两个类采用存储桶作为主机名的寻址方式，因此配置端点会转为 `https://{bucket}.{endpoint}`。`remark` 必须设置为精确前缀（`OSS` 或 `NOS`）。`NeteaseOSS` 是明确标注的旧兼容代码；其严格 MD5 ETag 校验仅适用于此处实现的单段、未加密请求路径。

## 选择客户端

应依据服务及其所需请求模型选择，而不是只看“S3 兼容”声明。至少确认签名版本、端点 URL 形状、必需区域、Host 请求头行为、canned ACL 策略，以及虚拟主机式存储桶 DNS 是否可用。本包不含通用 AWS S3 客户端，也不提供自动识别厂商的能力。
