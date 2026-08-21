---
name: aj-s3client
description: Use and maintain the aj-s3client lightweight Java 8 client for S3-compatible object storage. Trigger for implementation, debugging, review, tests, documentation, or usage involving com.ajaxjs.s3client APIs, Config, S3Client, SigV2 or SigV4 signing, canonical requests and headers, URL encoding, bucket/object operations, or the Aliyun OSS, NetEase OSS, Cloudflare R2, Backblaze B2, and Scaleway providers.
---

# AJ S3 Client

## Work from the repository

Read the checked-out source and relevant tests before changing behavior. Treat this library as a deliberately small
subset of S3 rather than as a replacement for a complete vendor SDK.

Target Java 8. Select a JDK 8 installation explicitly when the machine default is newer:

```bash
JAVA_HOME=/path/to/jdk8 mvn clean test
```

Preserve unrelated worktree changes and never place credentials, provider configuration, or real bucket names in
committed tests.

## Choose a provider

- Use `CloudflareR2` for the shared SigV4 path-style implementation.
- Use `Backblaze` for SigV4 with explicit Host signing enabled.
- Use `Scaleway` for SigV4 uploads requiring the `public-read` ACL.
- Use `AliyunOSS` for the legacy OSS SigV2-compatible flow.
- Use `NeteaseOSS` only for the retained legacy NOS SigV2 flow.

Configure a provider through `Config`, assign it with `setConfig`, and prefer the default-bucket convenience methods
only after setting `bucketName`.

Read [references/api.md](references/api.md) before implementing client usage, signing changes, or provider support.

## Preserve signing invariants

- Canonicalize paths and queries exactly once; never use form URL encoding for SigV4.
- Preserve duplicate query names, empty values, and S3-significant slash segments.
- Normalize signed header names to lowercase, sort them, collapse linear whitespace, and reject CR/LF values.
- Treat `SignBuilder` and canonical request/header models as immutable values.
- Include every header used during signing in the actual HTTP request with the same value.
- Use UTC SigV4 timestamps and UTF-8 for all signing bytes.
- Keep SigV2 secret/payload argument order provider-specific and covered by regression tests.
- Compare ETags with MD5 only for known single-part, unencrypted, untransformed uploads.
- Accept the complete HTTP 2xx range as success unless a provider operation requires a narrower contract.

## Validate inputs and URLs

Require HTTPS endpoint origins without credentials, path, query, or fragment. Validate bucket names and object keys
before inserting them into URLs or canonical resources. Encode object-key path components while preserving `/`
separators.

## Test changes

Prefer offline unit tests for signing, canonicalization, validation, encoding, and response handling. Keep live-provider
tests separate because they require uncommitted credentials and remote resources. Use temporary files instead of
platform-specific absolute paths when adding file-based tests.

After changes, run `mvn clean package` with Java 8. For documentation changes, also run the Maven JavaDoc goal with JDK
8 and resolve doclint errors.
