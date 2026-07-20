# Maintenance workflow

## Diagnose

1. Reproduce with the smallest input in an existing or new JUnit test.
2. Inspect the public method, private helpers, callers, and paired documentation.
3. Separate a confirmed defect from an API preference or compatibility risk.
4. For security reports, establish the trust boundary and whether attacker-controlled input reaches the sink.

## Implement

- Keep the patch local to the responsible abstraction.
- Preserve public method signatures and established return types by default.
- Use standard Java 8 APIs before adding dependencies.
- Avoid broad catch blocks. If wrapping is part of the existing API, keep the cause and use a non-sensitive message.
- Do not change unrelated formatting or user changes in a dirty worktree.

## Test matrix

Choose cases relevant to the change:

- happy path and regression input;
- null, empty, blank, zero, and negative values;
- first/last position and single-element input;
- delimiters repeated inside values;
- Unicode and explicit charset handling;
- values containing `$`, `\`, `=`, XML metacharacters, or path separators;
- inheritance through classes and interfaces, including diamonds;
- epoch, pure date, explicit offset, zone preservation, DST gap and overlap;
- truncated/corrupt streams, symlinks, ZIP traversal, and oversized archives;
- invalid keys, missing signing state, weak parameters, and redacted error messages.

Use the existing test naming and JUnit style in the neighboring package. Run the focused test, then `mvn -pl aj-util test`.

## Documentation synchronization

When behavior or validation changes:

1. Find both files with `rg --files docs-src/src | rg '(^|/)Name(-cn)?\.md$'`.
2. Update English and Chinese semantics together.
3. Ensure snippets use current class and method names.
4. State security and compatibility caveats directly; do not present legacy cryptography as recommended.
5. Search for stale statements across README files and `docs-src`.

## Completion report

Report changed files, behavior now guaranteed, tests executed and results, and any compatibility decisions or remaining risks. If tests could not run, include the exact reason.
