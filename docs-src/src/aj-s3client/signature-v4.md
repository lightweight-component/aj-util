---
title: Signature Version 4
layout: layouts/aj-util.njk
alternate: /signature-v4-cn/
---

# Signature Version 4

`CloudflareR2`, `Backblaze`, and `Scaleway` extend `BaseS3ClientSigV4`. They use **path-style** addresses: an object in `reports` with key `2026/a b.txt` is addressed as `https://endpoint/reports/2026/a%20b.txt`. The bucket name is part of the path, not the host name.

## Required configuration

SigV4 needs an endpoint, access key, secret key, and region. `Config` defaults `region` to `auto`; this is appropriate for Cloudflare R2, but it is not a universal value. Set the service's required signing region for Backblaze, Scaleway, or any other compatible endpoint.

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

The endpoint is normalized to `https://…` and must be an origin only. An endpoint such as `https://host/prefix` is rejected: this client does not support endpoint path prefixes.

## What is signed

For each operation, the implementation creates an `x-amz-date` timestamp and an `x-amz-content-sha256` value. Uploads hash the supplied bytes with SHA-256; GET, DELETE, bucket creation, deletion, and root listing use the SHA-256 digest of an empty payload. It then builds a canonical request from:

1. HTTP method.
2. Canonical path and canonical, sorted query string.
3. Canonicalized signed headers.
4. Payload SHA-256.

`SignBuilder` derives the AWS4 key for `date/region/s3/aws4_request` and writes an `Authorization: AWS4-HMAC-SHA256 …` header. Header names are case-insensitive, sorted, and values are trimmed with whitespace runs collapsed. Header values containing CR or LF are rejected.

The exact headers used by the base client are `x-amz-date` and `x-amz-content-sha256`. `Backblaze` additionally signs `Host`; `Scaleway` adds the signed `x-amz-acl` header for uploads. Every signed header must also be sent unchanged—adding a header to the signature alone is insufficient.

## Encoding and object keys

Keys are UTF-8 percent-encoded using S3 canonical rules. A space becomes `%20`, never `+`; `/` remains a path separator; query names and values are encoded separately and sorted. Do not pre-encode a key: passing `folder/a%20b.txt` treats `%` as data and encodes it again. Pass `folder/a b.txt` instead.

```java
client.putObject("reports", "exports/September report.csv", csvBytes);
```

The client permits slash-delimited keys but rejects blank keys and control characters. It does not normalize `.` or `..` segments for S3 signing, because they can be significant object-key bytes.

## Diagnosing signature failures

- Confirm that the endpoint has no path and is the service's S3 API endpoint, not a public object URL.
- Use the provider's required region; a wrong region changes the credential scope and signature.
- Do not modify signed headers in a proxy or custom transport.
- Check system time. SigV4 uses the current UTC timestamp, and providers reject excessive clock skew.
- Verify the bucket policy and access-key permission separately from signing. A valid signature can still receive a 403 response.

The public client API returns `false` for non-2xx responses and does not expose the error body. Reproduce a failed request with provider-side request logs or use the underlying source as a starting point if response diagnostics are required.
