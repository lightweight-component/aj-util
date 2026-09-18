# Workflow and evidence

## Establish scope

Read SKILL.md, module POM, target source, matching tests and both language pages. Preserve unrelated dirty/untracked changes. A documentation request does not authorize Java changes. HTTP belongs to aj-http despite the retained util package name; cryptography belongs to aj-util.

## Java work (only when authorized)

Preserve Java 8 and existing contracts unless a change is approved. Prefer local HttpServer/in-memory tests with deterministic status/body/stream behavior over public-service dependencies.

Relevant narrow checks from repository root:

```sh
mvn -f aj-http/pom.xml -Dtest=TestMultipartPost,TestMultipartWriter,TestHttpIoHelpers test
```

Tests for Get/Post/Put/Delete/Head/HttpFileDownload and call/TestCall contain external-service or environment assumptions; inspect before running the whole module. Do not claim offline execution for them. Module dependency ajaxjs-util:1.3.8 and parent ajaxjs-parent:1.36 must be resolvable; sibling source is not automatically used by this standalone POM. Report exact failure rather than changing dependency/source implicitly. Record JDK and tests run; compile is not runtime verification.

Cover: 2xx/non-2xx/redirects, missing status/body, init vs auto-send, timeout and read/write failure, callback exceptions, request/response stream ownership, multipart boundaries/injection, file collisions/cleanup and unsupported proxy annotations as applicable. Never copy credentials into logs or fixtures.

## Documentation/skills

- Homes: docs-src/src/aj-http/index.md and cn.md; guides pair name.md/name-cn.md under http_request/.
- Use current POM artifact aj-net, not guessed aj-http; version is source state, not release availability.
- Keep menu scoped to aj-http, update links directly to final location, and preserve both legacy HTTP prefixes in legacyRoutes.js.
- Update test-docs.js for routes, bilingual menus, missing old APIs and source contracts. Documentation contract tests are not Java execution.
- Validate from docs-src with npm test and npm run build; repository root git diff --check. Do not publish docs/ or stage/commit/reset unrelated work.
- Keep SKILL.md concise, detailed contracts in references, metadata in agents/openai.yaml matching aj-util conventions. Run skill-creator quick_validate.py if available; otherwise report unavailable and perform structural/link checks without claiming equivalent validation.

## Known evidence limits

Local Request/DataReader text includes trailing newline, while TestMultipartPost expects trimmed text; resolved artifact may differ. CallHandler annotations/config interfaces exceed implemented features. A successful round trip or a localhost test is not a TLS/SSRF/filesystem security audit. Report mismatches and unrun network tests explicitly; do not repair Java in a documentation-only task.
