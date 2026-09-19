---
title: Provider factories
layout: layouts/aj-util.njk
alternate: /factories-cn/
---

# Provider factories

The `com.ajaxjs.s3client.factory` package contains concrete clients, not a factory-method API. Instantiate the provider class directly, create `Config`, and call `setConfig`.

| Class | Base class | URL style | Provider-specific behavior |
| --- | --- | --- | --- |
| `CloudflareR2` | `BaseS3ClientSigV4` | Path style | General SigV4 implementation; R2 commonly uses region `auto`. |
| `Backblaze` | `CloudflareR2` | Path style | Enables `Host` in the signed and sent headers. |
| `Scaleway` | `CloudflareR2` | Path style | Sends and signs `x-amz-acl: public-read` during every object upload. |
| `AliyunOSS` | `BaseS3ClientSigV2` | Virtual-hosted style | HMAC-SHA1/Base64 authorization with `OSS` prefix. |
| `NeteaseOSS` | `BaseS3ClientSigV2` | Virtual-hosted style | Legacy HMAC-SHA256 authorization with `NOS` prefix and MD5/ETag upload check. |

## CloudflareR2

Use this class for the module's default path-style SigV4 flow. All bucket methods use `https://endpoint/bucket`; all object methods append an encoded key. It has no provider-specific ACL or Host override.

## Backblaze

`Backblaze` is `CloudflareR2` with `isSetHost` enabled. Its signing builder includes `host` and the request callback sends the same header. Set the provider region explicitly. Do not use this subclass merely to force a Host header for another service unless that service's exact signature requirements have been validated.

## Scaleway

`Scaleway.putObject` is materially different from the other SigV4 uploads: it always sets `x-amz-acl: public-read`, includes that header in the canonical signature, and makes the uploaded object publicly readable if the provider honors the canned ACL. This policy is per upload and cannot be disabled through `Config`. Select another client when public-read is not acceptable.

## AliyunOSS and NeteaseOSS

These classes use bucket-as-host addressing, so the configured endpoint is transformed to `https://{bucket}.{endpoint}`. Set `remark` to the exact factory prefix (`OSS` or `NOS`). `NeteaseOSS` is explicitly legacy compatibility code; its strict MD5 ETag check is only suitable for the single-part, unencrypted request path implemented here.

## Selecting a client

Select by the service and its required request model, not just by a claim of “S3 compatibility.” In particular, confirm signing version, endpoint URL shape, required region, Host-header behavior, canned-ACL policy, and whether virtual-hosted bucket DNS is available. This package does not contain a generic AWS S3 client class or an automatic provider detector.
