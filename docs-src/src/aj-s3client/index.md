---
title: aj-s3client
layout: layouts/aj-util.njk
---

# aj-s3client

`aj-s3client` is a small Java 8+ client for a focused set of S3-compatible object-storage operations. It signs requests with either Signature Version 4 or the legacy Signature Version 2 scheme, depending on the selected provider client.

> The module is not a general AWS SDK. It does not implement multipart upload, presigned URLs, object metadata/ACL management (apart from Scaleway's upload ACL), pagination, or a download API that returns or saves object bytes.

## Add the dependency

The module's current POM coordinates are `com.ajaxjs:aj-s3client:1.6`. It brings in `com.ajaxjs:aj-net:2.1` for HTTP transport.

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-s3client</artifactId>
    <version>1.6</version>
</dependency>
```

## Configure a client

Choose a provider implementation, then give it a `Config`. The endpoint must be an HTTPS origin: a scheme is optional, but paths, credentials, query strings, and fragments are rejected. Do not commit or log the access or secret key.

```java
import com.ajaxjs.s3client.Config;
import com.ajaxjs.s3client.factory.CloudflareR2;

Config config = new Config();
config.setEndPoint("<account-id>.r2.cloudflarestorage.com");
config.setAccessKey(System.getenv("R2_ACCESS_KEY_ID"));
config.setSecretKey(System.getenv("R2_SECRET_ACCESS_KEY"));
config.setBucketName("reports");
// Config defaults region to "auto", which is suitable for R2.

CloudflareR2 client = new CloudflareR2();
client.setConfig(config);
```

`bucketName` is used by the overloads that accept only an object key. Bucket names must not contain `/` or `\\`; object keys must be nonblank and cannot contain control characters. Object keys are path-encoded before they are signed and sent.

## Upload, list, and remove objects

The common operations return `true` only for an HTTP 2xx response. Read the source bytes before calling `putObject`; the client does not own a stream or implement `Closeable`.

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

Use the two-argument overloads when the target is not the configured default bucket:

```java
boolean uploaded = client.putObject("archive", "exports/report.csv", csvBytes);
boolean deleted = client.deleteObject("archive", "exports/report.csv");
```

`listBucket()` returns the provider's raw XML response; `listBucketXml()` converts the XML to a `Map<String, String>`. `getObject(...)` sends a signed GET and reports only whether its response was successful—it does not return the object body or save a file. Creating and deleting buckets are available through `createBucket(name)` and `deleteBucket(name)`.

## Provider clients and signing

| Client | Signing | Notes |
| --- | --- | --- |
| `CloudflareR2` | SigV4 | Uses path-style object URLs; `region` defaults to `auto`. |
| `Backblaze` | SigV4 | Adds `Host` to the signed headers; configure the provider's region. |
| `Scaleway` | SigV4 | Adds the signed `x-amz-acl: public-read` header to every upload. Configure the provider's region. |
| `AliyunOSS` | SigV2 | Requires `config.setRemark("OSS")`; uses virtual-hosted-style URLs. |
| `NeteaseOSS` | SigV2 | Legacy compatibility client; requires `config.setRemark("NOS")` and uses virtual-hosted-style URLs. |

For SigV4 clients, set `region` to the provider-required value when it is not `auto`. For SigV2 clients, `remark`, access key, secret key, endpoint, and region must still be nonblank; use the prefix shown above. Provider support is limited to these implementations and their request behavior—test credentials, endpoint format, bucket policy, and regional requirements with the chosen service before production use.

## Documentation map

- [Signature Version 4: request construction and signing](/aj-s3client/signature-v4/)
- [Signature Version 2: legacy provider behavior](/aj-s3client/signature-v2/)
- [Provider factories](/aj-s3client/factories/)
- [API, URL styles, results, and limitations](/aj-s3client/api/)

## Security and operational notes

- HTTPS is enforced for all configured endpoints; do not weaken TLS certificate validation in the underlying transport.
- Keep credentials in a secret manager or environment variables. The `Config` object is mutable, so avoid sharing and logging it.
- `Scaleway.putObject` makes uploaded objects publicly readable. Use `CloudflareR2` or another appropriate client if that is not the intended policy.
- An ETag is not generally guaranteed to be an MD5 digest. Only the legacy NetEase upload path verifies a single-part, unencrypted ETag as MD5.
- Live provider tests are disabled because they require private credentials and remote resources. The unit tests for signing and URL encoding run without them.

## Resources

- [Source code](https://github.com/lightweight-component/aj-util/tree/main/aj-s3client)
