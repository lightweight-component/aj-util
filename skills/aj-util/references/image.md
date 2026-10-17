# AJ Image workflow

Use this reference only for `aj-image` / `com.ajaxjs.image`. Read the repository and module `AGENTS.md` first when working in a checkout. Use current Java source and POM as the authority; do not assume a setter is implemented by a painter.

## Locate the relevant code

- Composition: `aj-image/src/main/java/com/ajaxjs/image/ImageContainer.java`.
- Element configurations: `model/ImageEmbedded.java`, `TextEmbedded.java`, `RectangleEmbedded.java`, sharing `EmbeddedElement`. Constants are in `model/constant`.
- Behavior: `painter/ImagePainter.java`, `TextPainter.java`, `RectanglePainter.java`, with common tiling in `Painter.java`.
- Standalone processing: `utils/ImageConvert`, `ImageEffect`, `ImageMatrix`, `ImageTypeHandler`, `ImageEncoder`, and abstract `ImageBase`.
- Tests: `aj-image/src/test/java`; general real-image fixture: `aj-image/src/test/resources/test.jpg`.
- Dependency: `com.ajaxjs:aj-image`, currently version `1.0`; verify the POM and release evidence before claiming a newer publication.

## Source-sensitive examples

- Element setters return void. Instantiate/configure/add elements rather than copying the old ImageCombiner builder chains.
- Image elements require `setImage(BufferedImage)`: `setImgUrl()` does not download. Background URL loading is a separate `ImageContainer.setBackgroundImage(String, ...)` overload.
- Text auto-fit uses `setAutoFitMaxWidth()` and `setAutoFitMinFontSize()`. Gradients use from/to colors and gradient direction. `autoBreakLine` requires caller-supplied lines; `space` is unused.
- Effects are source-based, not implicit pipelines. Pass one effect's `getOutput()` to a new processor for the next operation.
- Distinguish raw RGB/BGR/RGBA/gray matrices from encoded PNG/JPEG bytes. Document unsupported NV21, RGB/BGR-only matrix cropping, and alpha loss in `createRGBAImage()`.
- Use try-with-resources for `ImageTypeHandler` and caller-owned streams. It decodes only the first frame and closes after a single read.

## Documentation structure

| Topic | English | Chinese |
| --- | --- | --- |
| Overview, Maven dependency, reading links | `docs-src/src/aj-image/index.md` | `docs-src/src/aj-image/cn.md` |
| Composition examples and limits | `composition.md` | `composition-cn.md` |
| Usage tutorial and full poster example | `tutorial.md` | `tutorial-cn.md` |
| Utilities, effects, matrices, decoding and output | `utilities.md` | `utilities-cn.md` |

All topic pages are also under `docs-src/src/aj-image/`. Update the relevant pair and both module READMEs when public behavior or dependency instructions change. Keep the home concise; do not merge detailed guides back into it by default.

When adding/moving pages, preserve home URLs, set paired `alternate` values, use absolute `/aj-image/.../` links, and update both sidebar layouts and `docs-src/test-docs.js`. Check link targets, language switching and sitemap inclusion with `npm test` from `docs-src`.

## Fixture migration and verification

Keep legacy scene inputs intact: product photos, watermarks, avatars, QR images, backgrounds and fonts are not interchangeable with `test.jpg`. Preserve original URLs/local inputs, text and coordinates unless the user requests redesign. Missing original assets must fail clearly; do not silently substitute or claim a loaded placeholder is a valid QR image.

`TestMigratedApp` is opt-in. Run its network/local-asset cases only when needed and authorized; for an explicitly requested legacy-demo run, use `mvn -f aj-image/pom.xml test "-Dtest=TestMigratedApp" "-Daj.image.legacyDemo=true"`. Its output defaults to the module's `target/legacy-demo`; `aj.image.demoOutput` can override it. Keep skipped demonstrations distinct from passing deterministic tests.

For Java changes, run the narrow regression first and then `mvn -f aj-image/pom.xml test`. Validate Java 8 with an actual Java 8 runtime. For documentation-only work, validate the site without changing production code. Report observed results, not a fixed expected test count or a guarantee of no remaining bugs.
