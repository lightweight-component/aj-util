---
title: Signature Version 2
layout: layouts/aj-util-cn.njk
alternate: /signature-v2/
---

# Signature Version 2

`AliyunOSS` 和旧版 `NeteaseOSS` 继承 `BaseS3ClientSigV2`，使用**虚拟主机式** URL。`reports` 存储桶中的对象会请求 `https://reports.endpoint/object-key`；因此配置端点必须是厂商域名本身，不能包含存储桶或路径。

## 必需配置

SigV2 除端点和凭据外，还需要在 `Config.remark` 中设置厂商授权前缀：`AliyunOSS` 使用 `OSS`，`NeteaseOSS` 使用 `NOS`。共享配置校验同样要求区域非空；默认 `auto` 满足该校验，但 SigV2 授权值本身不包含区域 scope。

```java
Config config = new Config();
config.setEndPoint("oss-cn-example.aliyuncs.com");
config.setAccessKey(System.getenv("OSS_ACCESS_KEY"));
config.setSecretKey(System.getenv("OSS_SECRET_KEY"));
config.setRemark("OSS");
config.setBucketName("reports");

AliyunOSS client = new AliyunOSS();
client.setConfig(config);
```

如果厂商要求每个存储桶使用不同域名，只配置公共端点；`BaseS3ClientSigV2` 会自行构建存储桶主机名。

## 签名构建

每个请求都带有 GMT `Date` 请求头。规范资源包含 `/{bucket}/{encoded-object-key}`；对象键仍按 SigV4 相同的 UTF-8 路径规则编码。基类将 Authorization 组织为：

```text
{remark} {accessKey}:{signature}
```

`AliyunOSS` 使用 HMAC-SHA1 并对结果 Base64 编码；`NeteaseOSS` 使用其旧版 HMAC-SHA256 实现。这些是具体 Factory 的实现行为，并不表示任意 S3 SigV2 服务都可直接使用。

## 上传完整性行为

`AliyunOSS.putObject` 以 HTTP 成功状态作为上传结果。`NeteaseOSS.putObject` 还会发送 `Content-MD5`，并且只在响应成功且未加引号的单段 ETag 等于内容 MD5 时返回成功。该检查会拒绝含 `-` 的分片式 ETag，不能推广到加密或经过转换的上传。

两个客户端都没有可由调用方配置的 Content-Type 或元数据 Map；二进制上传使用共享 HTTP 文件类型。

## 运行注意事项

- 虚拟主机式存储桶地址依赖 `bucket.endpoint` 的 DNS 和 TLS 证书，应使用实际厂商验证存储桶名称。
- `getObject` 只发起 GET 并返回是否成功，响应正文会被丢弃，因此它不是下载功能。
- SigV2 仅为已命名的厂商客户端保留。若对象存储服务支持 SigV4 且其请求模型与本模块匹配，应优先选择 SigV4 客户端。
- 不应将 `true` 理解为持久化复制、生命周期处理或业务层确认；它只表示收到的 HTTP 状态为 2xx。
