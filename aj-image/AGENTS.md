# AJ Image module guidance

This supplements the repository-level `AGENTS.md` for `aj-image`.

## Identity and API

- Java 8 baseline. Maven coordinates currently are `com.ajaxjs:aj-image:1.0`; confirm the POM before changing version examples.
- Compose with `ImageContainer` and `model.ImageEmbedded`, `TextEmbedded`, `RectangleEmbedded`; constants are in `model.constant`. Do not revive the old ImageCombiner API or generate chainable element setters: current setters return void.
- Check the painters, not only Lombok-generated setters, before claiming a feature works. `imgUrl` and text `space` are currently unused. Multiline text requires caller-prepared `breakLineElements`; the library does not split text automatically.
- Image elements need a loaded `BufferedImage`. The string background overload separately supports local paths and HTTP/HTTPS. Network reads do not expose timeout/authentication settings; do not recommend untrusted URLs without source and size restrictions.
- `center` means horizontal centring; `direction` controls horizontal anchoring, not mirroring or reversed text. Blur is weighted smoothing, not strict Gaussian blur, and preserves existing alpha. A non-null rectangle border size draws only an outline.

## Utilities and documentation

- Document standalone `utils` tools as well as composition: `ImageConvert`, `ImageEffect`, `ImageMatrix`, `ImageTypeHandler`, `ImageEncoder`, and the abstract `ImageBase`.
- Effects read their original source; to combine effects, pass `getOutput()` into a new processor. Canvas padding is black. Keep raw pixel bytes distinct from encoded file bytes.
- NV21 is unsupported; matrix cropping supports RGB/BGR only; `createRGBAImage()` currently drops alpha. `ImageTypeHandler` decodes only the first frame and is single-use; use try-with-resources for metadata-only use too.
- Keep both READMEs and the bilingual site aligned: `index.md` / `cn.md` cover overview, Maven and reading links; `composition.md` / `composition-cn.md` cover composition; `utilities.md` / `utilities-cn.md` cover utils and output.
- `tutorial.md` / `tutorial-cn.md` teach current usage with minimal and full composition examples, not an old/new API comparison; distinguish tutorial helpers from library APIs and preserve each asset's role.
- For page additions/moves, update both shared layouts under `docs-src/src/_includes/layouts`, `alternate` language targets, and `docs-src/test-docs.js`. Preserve `/aj-image/` and `/aj-image/cn/`.
- Describe file-save guarantees precisely: encoding/temp-write failures preserve old files; non-atomic replacement fallback depends on the file system. Caller-provided output streams remain caller-owned.

## Tests and fixtures

- Keep fast deterministic tests independent of the network and developer drive paths. Use generated images for exact pixel/edge assertions, `src/test/resources/test.jpg` for general real-image flows, and temporary directories for automated-test output.
- The migrated legacy demo uses specific backgrounds, products, avatars, watermarks, QR images, local files and fonts. Preserve these inputs, positions and text; fail clearly on missing assets instead of substituting another picture.
- `TestMigratedApp` is opt-in with `-Daj.image.legacyDemo=true`. Do not run network/file-dependent demonstrations merely to validate unrelated code. Do not describe skipped demos as successful visual validation.
- For Java changes, run the relevant test first, then `mvn -f aj-image/pom.xml test`; use a Java 8 runtime before claiming Java 8 validation. Do not pin test counts in guidance.
- For documentation/layout changes, run `npm test` from `docs-src`. For Javadoc changes, verify with doclint and failure-on-warning/error enabled.
