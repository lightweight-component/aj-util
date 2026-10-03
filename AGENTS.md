# Repository guidance

## Structure

- Each Java module is a Maven subproject. Keep production code under `*/src/main/java` and tests under `*/src/test/java`.
- Source documentation for the website lives in `docs-src/src`; generated site files live in `docs-src/dist` or the published `docs` directory. Edit the source files, not generated output.
- Module documentation is organised as `docs-src/src/<module>/index.md` and `cn.md`. Keep both pages aligned whenever a public API, dependency version, or behaviour changes.

## Documentation work

- Verify examples against the module's current Java source and POM before publishing them.
- State supported protocol and security boundaries explicitly; do not imply support for protocols that a module does not implement.
- Prefer try-with-resources in Java examples for network clients, streams, and other closeable resources.
- Use absolute site paths such as `/aj-ftp/` for internal documentation links.
- For S3-compatible modules, identify the concrete provider client and signature version; document endpoint, region, bucket-policy, and credential-handling requirements rather than assuming AWS defaults.
- Describe observable operation results precisely. In particular, distinguish a successful HTTP status from returning downloaded bytes, persisted files, or a provider-level business outcome.
- Keep English and Chinese module pages structurally aligned, including provider-support tables and security warnings.

## Cryptography API

- `com.ajaxjs.util.cryptography.rsa.Rsa` is the current key-bound RSA cipher. Do not document or reintroduce the removed `RsaDefault` or the former stateful `Rsa` API.
- Generate key pairs with `RestoreKey.generateKeyPair(int)`. For ordinary text transport, encrypt with a public-key `Rsa`, store or send `CipherResult.toBase64()`, and decrypt that Base64 value with a private-key `Rsa`.
- The default `Rsa` transformation is OAEP SHA-256 with MGF1 SHA-256. Use `Rsa.RSA_CIPHER_SHA1` only when an external protocol explicitly requires the compatibility mode; RSA remains for short values, not bulk payloads.

## Validation

- From `docs-src`, run `npm test` after documentation or layout changes.
- For changes to a Java module, run the smallest relevant Maven test command before broader builds.
- When changing `aj-s3client`, run `mvn -pl aj-s3client test`; provider-integration tests require private credentials and may remain disabled.
- Do not delete or overwrite unrelated working-tree changes.
