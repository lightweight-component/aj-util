# aj-qrcode API Reference

## Normal API

Create symbols with:

```java
QrCode qr = QrCode.encodeText("Hello, world!", Ecc.MEDIUM);
BufferedImage image = Utils.toImage(qr, 8, 4);
String svg = Utils.toSvgString(qr, 4, "#FFFFFF", "#000000");
```

Use `encodeBinary` for arbitrary bytes. Use `encodeSegments` when selecting modes manually or forcing version, mask, and
ECC-boost behavior.

`QrSegment` factories create byte, numeric, alphanumeric, and ECI segments. `QrSegmentAdvanced` supplies optimal
mixed-mode segmentation and QR Kanji encoding.

## Fast API

`QrCodeFast` and `QrSegmentFast` mirror the normal encoding entry points but store modules in packed integer arrays.
`QrTemplate` memoizes version-dependent function patterns, masks, and zigzag data positions. `ReedSolomonGenerator`
memoizes ECC generator tables by degree.

Choose the fast variant for repeated or high-volume generation. Do not mix normal segments with fast encoders.

## Error correction and sizing

`Ecc` supports `LOW`, `MEDIUM`, `QUARTILE`, and `HIGH`. When `boostEcl` is enabled, encoding may select a stronger level
without increasing the chosen version.

Version 1 has 21 modules per side; each higher version adds four. `DataTooLongException` means no allowed version can
contain the encoded segments at the requested ECC constraints.

## Segmentation

Numeric mode is most compact for digits. Alphanumeric mode accepts uppercase letters, digits, space, and `$%*+-./:`.
Other text falls back to UTF-8 byte mode unless advanced segmentation selects QR Kanji mode for encodable characters.

Use ECI segments only when the decoder needs an explicit character-set interpretation. The library does not
automatically prepend an ECI segment for ordinary UTF-8 text.

## Rendering

`Utils.toImage(qr, scale, border)` creates an RGB image. Scale must be positive and border non-negative.
`Utils.writePng` writes a supplied image. `Utils.toSvgString` returns complete SVG XML with configurable CSS colors.

The fast package has no production rendering helper; render `QrCodeFast.getModule(x, y)` directly or adapt it to the
same raster/SVG loop used by `Utils`.

## Test map

- `TestQrCode` covers normal buffers, segments, advanced segmentation, encoding, QR mathematics, masking, and rendering.
- `TestQrCodeFast` covers packed buffers, fast segments, optimal segmentation, memoizers, templates, Reed-Solomon
  generation, encoding, and penalty/mask helpers.
