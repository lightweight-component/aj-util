# aj-s3client guidance

## Module scope

- `aj-s3client` is a Java 8 Maven module for a limited set of S3-compatible object-storage operations. It is not a general AWS SDK.
- Keep production code in `src/main/java` and tests in `src/test/java`.
- Preserve the public `S3Client` contract: boolean operations mean an HTTP 2xx response, not that object bytes were returned, saved, replicated, or accepted by a higher-level workflow.

## Signing and URL compatibility

- Keep Signature Version 4 code under `BaseS3ClientSigV4` and `signer_v4`; keep legacy Signature Version 2 behavior under `BaseS3ClientSigV2` and the relevant provider client.
- Do not mix URL styles: SigV4 provider clients in this module use path-style URLs, while SigV2 provider clients use virtual-hosted-style bucket URLs.
- Canonical request construction, percent encoding, signed headers, endpoint validation, and the headers actually sent must remain consistent. Any signing change needs focused tests for the affected canonicalization or provider behavior.
- Endpoints are HTTPS origins only. Do not add support that silently weakens TLS validation or permits embedded credentials, query strings, fragments, or endpoint path prefixes without an explicit design change.

## Provider clients and security

- Make provider-specific behavior explicit in the corresponding class in `factory/`; do not present one provider's region, Host-header, ACL, or ETag rules as universal S3 behavior.
- `Scaleway.putObject` deliberately sends a signed `x-amz-acl: public-read`; preserve or change that policy only with a documented security review.
- Never add real access keys, secret keys, account IDs, bucket names, or live endpoints to source, tests, fixtures, or documentation. Use environment variables or locally ignored configuration for integration tests.
- Do not treat ETag as an MD5 value except where the implementation explicitly restricts the request to a compatible single-part, unencrypted path.

## Tests and documentation

- Run `mvn -pl aj-s3client test` for module changes. Unit tests for signing and URL encoding must remain offline; provider tests that require private credentials or remote resources should stay disabled by default.
- When changing the public API, signing behavior, providers, dependencies, or security boundaries, update both `docs-src/src/aj-s3client/index.md` and `docs-src/src/aj-s3client/cn.md`, plus the relevant bilingual guide pages.
- Keep the English and Chinese S3 documentation pages structurally aligned: `signature-v4`, `signature-v2`, `factories`, and `api` each have a `-cn` counterpart.
