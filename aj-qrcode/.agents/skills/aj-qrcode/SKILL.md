---
name: aj-qrcode
description: Use and maintain the aj-qrcode dependency-free Java 8 QR Code generator. Trigger for implementation, debugging, review, tests, documentation, or usage involving com.ajaxjs.qr_code QrCode, QrSegment, QrSegmentAdvanced, Utils, or the com.ajaxjs.qr_code.fast QrCodeFast APIs, including text and binary encoding, optimal segmentation, Kanji mode, ECC levels, masks, PNG/SVG rendering, Reed-Solomon correction, and normal-versus-fast consistency.
---

# AJ QR Code

## Work from the repository

Read the checked-out implementation and tests before changing behavior. Preserve its Java 8 target and
zero-runtime-dependency design. This project combines the normal and fast variants of Nayuki's QR Code Model 2
generator.

Run verification with an explicit JDK 8 when the machine default is newer:

```bash
JAVA_HOME=/path/to/jdk8 mvn clean test
```

Preserve unrelated worktree changes.

## Choose the API

- Use `QrCode` and `QrSegment` for straightforward, readable application code.
- Use `QrCodeFast` and `QrSegmentFast` when generating many symbols and template/ECC memoization matters.
- Use `QrSegmentAdvanced.makeSegmentsOptimally` or its fast counterpart for mixed numeric, alphanumeric, byte, and Kanji
  text.
- Use `Utils.toImage`, `Utils.writePng`, and `Utils.toSvgString` to render the normal representation.

Read [references/api.md](references/api.md) before implementing usage, changing QR mathematics, or comparing the normal
and fast paths.

## Preserve QR invariants

- Keep versions within 1 through 40 and masks within -1 through 7.
- Preserve the ascending `Ecc.values()` order because ECC boosting relies on enum order.
- Preserve mode-specific character-count bit widths and segment length calculations.
- Treat encoded QR grids and segment payloads as immutable to callers.
- Keep finder, timing, alignment, format, and version modules excluded from data masking.
- Apply each mask formula exactly as defined by QR Code Model 2 and choose the lowest-penalty mask for automatic
  selection.
- Keep Reed-Solomon operations in GF(2^8/0x11D) identical between normal and fast implementations.
- Preserve Unicode code points when optimizing segments; do not split surrogate pairs.
- Encode byte-mode text as UTF-8 and use the Shift JIS mapping table only for QR Kanji mode.

## Test changes

Prefer deterministic in-memory assertions. Use JUnit temporary directories for PNG output and never write demo files
into the repository or platform-specific paths. Cover public entry points, meaningful validation failures, and
package-visible algorithm helpers without expanding tests for static lookup tables or trivial data holders.

When changing encoding mathematics, compare normal and fast output for representative numeric, alphanumeric, Unicode,
binary, forced-mask, and version-boundary inputs.

After changes, run `mvn clean package` with Java 8 and confirm all test classes and test methods remain package-visible
and use the `Test` prefix.
