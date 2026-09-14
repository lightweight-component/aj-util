# Workflow

## Establish scope

1. Locate the repository root and module separately. Read local instructions and code-style.md.
2. Read the relevant source, callers and tests; use references/modules.md for refactoring migrations.
3. Read aj-util/to_fix.md. Treat it as a dated baseline, not proof that current code still has every issue.
4. A review or documentation request does not authorize implementation changes. Preserve unrelated working-tree edits.

## Implementation and verification

- Preserve Java 8 source/runtime compatibility, public contracts and exception causes unless an API change is authorized.
- Add focused regression tests for authorized fixes; cover boundaries, malformed input, charset, timezone, ownership and security cases as relevant.
- Run from the repository root:

```sh
mvn -f aj-util/pom.xml -Dtest=TestClassName test
mvn -f aj-util/pom.xml test
```

From the module directory, omit the -f argument. Offline mode (-o) requires cached dependencies.
Report JDK, exact command and counts. Distinguish production defects from stale test expectations, missing fixtures and environment limitations. Do not equate a successful compile with passing tests, or a round trip with cryptographic safety.
Run Java 8 separately before claiming Java 8 runtime verification.

## Documentation

- Keep aj-util/README.md and aj-util/README.zh-CN.md semantically aligned, including POM version, current class names and examples.
- Detailed docs under docs-src/src generally pair name.md and name-cn.md; locate the actual pages first.
- Update aj-util/to_fix.md by removing or marking resolved findings only when source/test evidence supports it. Preserve unresolved findings and record new regression or test-maintenance work separately.
- Avoid hard-coded coverage/JAR-size claims without measurement. Do not call the local POM version the latest published version without release evidence.
- Keep identifiers untranslated and examples compilable. Distinguish current behavior from proposed contracts.
- For skill updates keep SKILL.md concise, route details to references, and run the skill-creator quick_validate.py against skills/aj-util.
