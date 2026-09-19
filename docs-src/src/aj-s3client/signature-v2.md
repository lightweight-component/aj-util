---
title: Signature Version 2
layout: layouts/aj-util.njk
alternate: /signature-v2-cn/
---

# Signature Version 2

`AliyunOSS` and the legacy `NeteaseOSS` clients extend `BaseS3ClientSigV2`. They use **virtual-hosted-style** URLs: a key in bucket `reports` is requested from `https://reports.endpoint/object-key`. The configured endpoint must therefore be the provider domain without a bucket name or path.

## Required configuration

In addition to endpoint and credentials, SigV2 needs a provider authorization prefix in `Config.remark`. `AliyunOSS` requires `OSS`; `NeteaseOSS` requires `NOS`. The shared configuration validation also requires a nonblank region; its default value `auto` satisfies that requirement, although SigV2's authorization value does not include a region scope.

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

If a provider requires a different domain pattern for each bucket, configure the common endpoint only; `BaseS3ClientSigV2` constructs the bucket host itself.

## Signature construction

Each request carries a GMT `Date` header. The canonical resource includes `/{bucket}/{encoded-object-key}`; object keys use the same UTF-8 path encoding as SigV4. The base class formats the authorization header as:

```text
{remark} {accessKey}:{signature}
```

`AliyunOSS` applies HMAC-SHA1 and Base64 encodes the result. `NeteaseOSS` applies its legacy HMAC-SHA256 implementation. These details are factory behavior, not a generic promise that an arbitrary S3 SigV2 service will work.

## Upload integrity behavior

`AliyunOSS.putObject` accepts a successful HTTP status as the upload result. `NeteaseOSS.putObject` additionally sends `Content-MD5`, then accepts only a successful response whose unquoted, single-part ETag equals the content MD5. That check deliberately rejects multipart-style ETags (which contain `-`) and cannot be generalized to encrypted or transformed uploads.

Neither client provides a caller-configurable content type or metadata map. Binary uploads use the shared HTTP file content type.

## Operational cautions

- Virtual-hosted bucket addressing depends on DNS and TLS certificates for `bucket.endpoint`; test bucket names with the actual provider.
- `getObject` only performs the GET and returns whether it succeeded. It discards the body, so it is not a download facility.
- SigV2 is retained for the named provider clients. Prefer a SigV4 provider client when your object-storage service supports it and its request model matches this module.
- Do not interpret `true` as a durable replication, lifecycle, or business-level confirmation; it means the received HTTP status was 2xx.
