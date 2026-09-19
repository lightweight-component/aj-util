---
title: API 与限制
layout: layouts/aj-util-cn.njk
alternate: /api/
---

# API 与限制

`S3Client` 是公开操作接口。`BaseS3Client` 提供配置校验、URL 编码、默认存储桶重载与 HTTP 2xx 结果校验；两个签名基类再实现具体 HTTP 操作。

## 操作约定

| 方法 | 请求目标 | 返回结果 |
| --- | --- | --- |
| `listBucket()` | 配置端点根路径 | 厂商原始 XML 响应。 |
| `listBucketXml()` | 配置端点根路径 | 转为 `Map<String, String>` 的厂商 XML。 |
| `createBucket(bucket)` | 指定存储桶 | HTTP 2xx 时为 `true`。 |
| `deleteBucket(bucket)` | 指定存储桶 | HTTP 2xx 时为 `true`。 |
| `putObject(bucket, key, bytes)` | 指定对象 | 上传字节，成功时为 `true`。 |
| `getObject(bucket, key)` | 指定对象 | 发起 GET、丢弃响应内容，并返回是否成功。 |
| `deleteObject(bucket, key)` | 指定对象 | HTTP 2xx 时为 `true`。 |

不带存储桶的重载使用 `Config.bucketName`，未配置默认存储桶时会被拒绝。`listBucket` 不接收存储桶或前缀参数，因此此版本公开 API 只请求服务根路径，并不是针对指定存储桶的对象列举 API。

## 校验与 URL 规则

`Config` 必须提供端点、访问密钥、私钥和区域。端点仅接受 HTTPS，可省略 `https://`，但不能含用户信息、查询参数、片段或非根路径。存储桶名称不能为空，不能含 `/`、`\\` 或控制字符；对象键不能为空，不能含控制字符。

`BaseS3ClientSigV4` 构建路径式 URL，`BaseS3ClientSigV2` 构建虚拟主机式 URL。两者都会由客户端对键名编码一次；应输入原始对象键，而不是已 URL 转义的值。

## 错误模型

配置错误会在发送请求前抛出 `IllegalStateException` 或 `IllegalArgumentException`。底层 HTTP 层无法完成请求时也可能抛出运行时异常。获得响应后，`check(response)` 将 200 至 299 的任意状态视为成功，不解析厂商错误 XML。

因此，若调用方需要响应正文、状态码、错误码、重试策略、超时控制、流式上传/下载或可观测性，应使用或扩展更底层的实现，而不能仅依赖这些布尔方法。

## 不在范围内的能力

- AWS SDK 常见能力：分片上传、拷贝、预签名 URL、版本、标签、生命周期配置和 STS 凭据。
- 按存储桶、前缀、分隔符或 continuation token 列举对象。
- 下载或返回对象内容。
- 单次请求的 Content-Type、元数据、加密选项、自定义 ACL 与通用请求头。
- 自动重试、幂等性处理、并发控制和凭据刷新。

不要因厂商宣称 S3 兼容而假定上述能力可用；它们并不属于本模块 API。
