---
name: aj-util
description: Use and maintain the aj-util Java 8 utility library. Trigger for questions, implementation, debugging, review, tests, or documentation involving com.ajaxjs.util APIs, including strings, conversion, JSON/XML, URL encoding, date/time, reflection, I/O/ZIP, HTTP helpers, hashing, random values, and cryptography.
---

# AJ Util

Use the repository's current source as the authority for API signatures and behavior. Treat documentation as guidance that may lag behind the implementation.

## Locate the project

1. Find `aj-util/pom.xml` and set its parent as the repository root.
2. Read the target class under `aj-util/src/main/java/com/ajaxjs/util` before proposing code.
3. Read the matching English and Chinese pages under `docs-src/src` when changing public behavior or examples.
4. Read nearby tests under `aj-util/src/test/java`; add a regression test for every bug fix.

If the skill is installed globally and no aj-util checkout is open, use [references/modules.md](references/modules.md) for package navigation and ask for the project or dependency version when exact behavior matters.

## Choose the relevant guidance

- For package and documentation paths, read [references/modules.md](references/modules.md).
- For behavioral contracts and security-sensitive APIs, read [references/contracts.md](references/contracts.md).
- For implementation, testing, and documentation conventions, read [references/workflow.md](references/workflow.md).

Load only the reference relevant to the task. For cross-package reviews or broad changes, read all three.

## Work on code

1. Confirm the requested behavior from source, tests, and documentation.
2. Preserve Java 8 language and runtime compatibility. Do not introduce APIs added after Java 8.
3. Prefer focused fixes that preserve public signatures unless the user explicitly authorizes an API change.
4. Validate arguments at public boundaries and use explicit, actionable exceptions.
5. Preserve causes when translating exceptions, but never include keys, credentials, full XML payloads, tokens, or plaintext secrets in messages or logs.
6. Add boundary tests: null/empty input, zero, first and last positions, malformed data, repeated delimiters, and security payloads as applicable.
7. Run the narrow test first, then the module suite:

```bash
mvn -pl aj-util -Dtest=TestClassName test
mvn -pl aj-util test
```

If the parent POM or local JDK prevents a full build, report the exact command and failure; do not claim verification.

## Update documentation

When public behavior changes, update both language variants in the same change:

- English: `name.md`
- Chinese: `name-cn.md`

Keep examples compilable against the current source. Document validation rules, exception behavior, charset/time-zone semantics, overlap behavior, and security limitations where relevant. Do not translate class names, method names, Maven coordinates, or code identifiers.

## Review code

Prioritize correctness and security over style. Trace callers before reporting a defect. For each confirmed issue, provide the triggering input, observed behavior, intended behavior, impact, and a minimal repair plan. Pay special attention to parser boundaries, resource ownership, integer overflow, class/interface traversal, time-zone transitions, archive traversal/bombs, cryptographic defaults, and sensitive-data leakage.
