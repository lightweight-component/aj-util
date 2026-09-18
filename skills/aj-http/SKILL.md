---
name: aj-http
description: Use, document and review the aj-http Java HTTP client extracted from aj-util. Trigger for com.ajaxjs.util.httpremote requests, responses, multipart uploads, file downloads, annotation proxies, TLS or HTTP resource/error handling. The current Maven artifact is com.ajaxjs:aj-net, despite the project name aj-http.
---

# AJ HTTP

Use current source and tests as authority, not old AJ Util HTTP examples or class comments alone. The Java namespace still starts with `com.ajaxjs.util`, but this code belongs to the separate `aj-http/` module.

## Locate and read

1. Locate the repository containing `aj-http/`, `aj-util/`, `docs-src/` and `skills/`.
2. Read `aj-http/pom.xml` for exact coordinates/dependencies, then the target under `aj-http/src/main/java/com/ajaxjs/util/httpremote/`.
3. Read matching tests under `aj-http/src/test/java/com/ajaxjs/util/httpremote/`. Distinguish public-network examples from deterministic local tests.
4. Read the bilingual pages under `docs-src/src/aj-http/` and the relevant reference:
   - [Module map and migration](references/modules.md)
   - [Lifecycle, errors, resources and security](references/contracts.md)
   - [Change and validation workflow](references/workflow.md)

For AJ Util encoding/JSON/I/O/cryptography dependencies, consult [AJ Util](../aj-util/SKILL.md). If no checkout is available, ask for the dependency version before promising exact signatures.

## Implementation and review boundaries

- Preserve Java 8 compatibility and public signatures unless an API change is explicitly authorized.
- Use `Request` for explicit status/resource control. Verify whether the constructor sends immediately before adding `init`/`connect`.
- Trace the implementation: an annotation's existence does not mean CallHandler handles it.
- Never log credentials or payload secrets; never recommend SkipSSL trust-all settings for production.
- Check stream ownership, disconnection, redirects/replay, timeout/error propagation, filename collisions and untrusted URL/SSRF boundaries.
- Keep fixes focused and add local-server or in-memory regression tests for authorized implementation changes.

## Documentation-only work

Do not modify Java source/tests or publish repository-level `docs/` output. Preserve unrelated dirty/untracked changes. Update English and `-cn` pages together; homes use `index.md` and `cn.md`. Keep imports, return types, Maven coordinates and resource/error guidance aligned with bodies and tests.

HTTP guides live at `docs-src/src/aj-http/http_request/{Base,Get,advanced-usage}[-cn].md`. Both historical `/http_request/*` and `/aj-util/http_request/*` URLs redirect directly to these locations; maintain `_data/legacyRoutes.js`, project-specific menus and `test-docs.js` when changing routes. Do not move HTTP navigation back into the AJ Util menu.

Run `npm test`, `npm run build` from `docs-src`, and `git diff --check`. Report exact commands, unresolved source/test mismatches and tests not run. Build success does not prove Java runtime correctness or cryptographic/TLS security.
