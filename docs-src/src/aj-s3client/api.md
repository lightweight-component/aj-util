---
title: API and limitations
layout: layouts/aj-util.njk
alternate: /api-cn/
---

# API and limitations

`S3Client` is the public operation interface. `BaseS3Client` supplies configuration validation, URL encoding, default-bucket overloads, and a shared HTTP 2xx result check. The two signing base classes then implement the concrete HTTP operations.

## Operation contract

| Method | Request target | Result |
| --- | --- | --- |
| `listBucket()` | Configured endpoint root | Raw provider XML response. |
| `listBucketXml()` | Configured endpoint root | Provider XML converted to `Map<String, String>`. |
| `createBucket(bucket)` | Named bucket | `true` for any 2xx response. |
| `deleteBucket(bucket)` | Named bucket | `true` for any 2xx response. |
| `putObject(bucket, key, bytes)` | Named object | Uploads bytes, then returns `true` for success. |
| `getObject(bucket, key)` | Named object | Sends GET, discards response content, then returns success. |
| `deleteObject(bucket, key)` | Named object | `true` for any 2xx response. |

The overloads without a bucket use `Config.bucketName`; they reject a missing default bucket. The name `listBucket` does not accept a bucket or prefix argument, so this version's public API only requests the service root—it is not an object-listing API for a selected bucket.

## Validation and URL rules

`Config` must provide endpoint, access key, secret key, and region. The endpoint is HTTPS only, may omit the `https://` prefix, and must not contain user info, query, fragment, or a non-root path. A bucket name cannot be blank, contain `/` or `\\`, or include control characters. An object key cannot be blank or contain control characters.

`BaseS3ClientSigV4` creates path-style URLs. `BaseS3ClientSigV2` creates virtual-hosted-style URLs. In both cases, keys are encoded once by the client. Keep the original object key as input rather than a URL-escaped value.

## Error model

Configuration mistakes throw `IllegalStateException` or `IllegalArgumentException` before a request is sent. The underlying HTTP layer can also throw a runtime exception when it cannot complete a request. Once a response is available, `check(response)` treats any status from 200 through 299 as success; it does not parse provider error XML.

This means callers that need a response body, status code, error code, retry policy, timeout tuning, streaming upload/download, or observability must use/extend the lower-level implementation rather than relying solely on the boolean methods.

## Out of scope

- AWS SDK features such as multipart uploads, copy operations, presigned URLs, versions, tagging, lifecycle configuration, and STS credentials.
- Listing objects by bucket, prefix, delimiter, or continuation token.
- Downloading or returning object content.
- Per-request content type, metadata, encryption options, custom ACLs, and generic request headers.
- Automatic retries, idempotency handling, concurrency control, or credential refresh.

Do not assume support for these features from a provider's S3 compatibility statement; they are not part of this module's API.
