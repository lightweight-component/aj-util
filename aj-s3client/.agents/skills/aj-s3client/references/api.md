# aj-s3client API Reference

## Configuration

Create a `Config` and set:

- `endPoint`: S3-compatible HTTPS origin, with or without the `https://` scheme;
- `accessKey`: public credential identifier;
- `secretKey`: signing secret;
- `bucketName`: default bucket for convenience operations;
- `region`: SigV4 region, defaulting to `auto`;
- `remark`: SigV2 authorization prefix such as `OSS` or `NOS`.

Assign the configuration with the Lombok-generated `setConfig` method. Do not log `Config` because its generated
representation may contain the secret key.

## Client operations

`S3Client` exposes:

- `listBucket()` for the raw XML listing response;
- `listBucketXml()` for the ajaxjs-util XML-to-map form;
- `createBucket(bucketName)` and `deleteBucket(bucketName)`;
- `putObject(bucketName, objectName, bytes)`;
- `getObject(bucketName, objectName)`;
- `deleteObject(bucketName, objectName)`;
- overloads using `Config.bucketName` for object operations.

The current `getObject` contract returns only whether the HTTP request succeeded; it does not expose or save the
response body as a downloaded file.

Example:

```java
Config config = new Config();
config.setEndPoint("account.r2.cloudflarestorage.com");
config.setAccessKey(accessKey);
config.setSecretKey(secretKey);
config.setBucketName("assets");
config.setRegion("auto");

CloudflareR2 client = new CloudflareR2();
client.setConfig(config);
boolean uploaded = client.putObject("images/logo.png", bytes);
```

## Class hierarchy

- `BaseS3Client` owns configuration validation, URL construction, common object-operation overloads, HTTP success
  checks, and ETag checks.
- `BaseS3ClientSigV2` implements shared date headers, canonical resources, authorization formatting, and
  virtual-hosted-style URLs.
- `BaseS3ClientSigV4` initializes `SignBuilder`, canonical requests, and signed request headers.
- Provider classes select the signature scheme and encode vendor-specific behavior.

## SigV4 model

`CanonicalRequest` stores the method, raw path, and raw query. It preserves S3 path semantics, decodes percent escapes
once, re-encodes with AWS rules, and sorts encoded query names and values.

`CanonicalHeaders` validates, normalizes, sorts, and freezes the signed headers. Header values containing CR or LF are
invalid.

`SignBuilder` is immutable: `header(name, value)` returns a new instance. Add `X-Amz-Date`, `x-amz-content-sha256`, Host
when required, and any provider-specific headers before calling `getS3Signature`.

`S3SigV4Utils` supplies UTC timestamps, HMAC-SHA256, SHA-256 payload hashing, and lowercase hexadecimal output.
`URLEncoding` preserves path slashes but encodes slashes inside query components.

## Provider differences

| Provider      | Signing | URL/header behavior                                                       |
|---------------|---------|---------------------------------------------------------------------------|
| Cloudflare R2 | SigV4   | Shared path-style implementation                                          |
| Backblaze B2  | SigV4   | Explicit Host header participates in signing                              |
| Scaleway      | SigV4   | Upload includes signed `x-amz-acl: public-read`                           |
| Aliyun OSS    | SigV2   | HMAC-SHA1 and `OSS` authorization prefix                                  |
| NetEase NOS   | SigV2   | HMAC-SHA256, `NOS` prefix, Content-MD5, and single-part ETag verification |

## Testing

Use `TestS3Core`, `TestSignerInternals`, `TestURLEncoding`, and `TestAjUtil136Migration` as the offline regression
baseline. Provider integration tests are disabled by default because they read an uncommitted `application.yml` and may
access paid or destructive remote resources.
