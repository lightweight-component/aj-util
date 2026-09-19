# AJ QR Code module guidance

This file supplements the repository-level `AGENTS.md` for work in `aj-qrcode`.

## Module identity

- Maven artifact: `com.ajaxjs:aj-qrcode`; Java 8; no non-JDK runtime dependencies.
- Production packages: `com.ajaxjs.qr_code` and `com.ajaxjs.qr_code.fast`.
- The implementation is derived from Nayuki's QR Code generator. Preserve applicable upstream copyright and license notices in derived files.

## API and behavior

- Prefer the regular public API: `QrCode`, `QrSegment`, `QrSegmentAdvanced`, `Ecc`, and `Utils`.
- Preserve `QrCode` immutability and the compatibility behavior that out-of-bounds `getModule(x, y)` returns `false`.
- QR Code Model 2 covers versions 1–40. Preserve automatic smallest-version selection, optional ECL boosting, and masks `-1` or `0`–`7`.
- `DataTooLongException`, `IllegalArgumentException`, and `NullPointerException` are observable contracts; never silently truncate input.
- Keep normal and `fast` APIs distinct. `Utils` renders `QrCode`, not `QrCodeFast`.

## Encoding and rendering

- Preserve numeric, alphanumeric, byte/UTF-8, and Kanji modes with their validation rules.
- ECI declares a character-set assignment; it does not transcode byte data or guarantee scanner support.
- `Utils.toImage()` needs a positive pixel scale and non-negative border, measured in modules.
- `Utils.toSvgString()` inserts CSS color strings into SVG; documentation must not recommend untrusted color input.
- This module encodes but does not decode/scan QR codes. Do not claim logo-overlay support or scanning guarantees.

## Tests and documentation

- Tests are under `aj-qrcode/src/test/java`; cover normal and `fast` packages when shared encoding behavior changes.
- Run `mvn -pl aj-qrcode test` for module changes when practical.
- Keep `docs-src/src/aj-qrcode/index.md` and `cn.md` aligned, with source-verified examples and explicit capacity/security boundaries.
- Run `npm test` from `docs-src` after documentation or site changes.
