---
title: Image composition
layout: layouts/aj-util.njk
alternate: /composition-cn/
---

# Image composition

Use `ImageContainer` to set a background and canvas size, add elements, call `combine()`, and export the result. Later elements cover earlier ones. Element setters are not chainable.

## Minimal example

For complete examples with images, text, watermarks, and a QR image, read the [composition tutorial](/aj-image/tutorial/).

```java
import com.ajaxjs.image.ImageContainer;
import com.ajaxjs.image.model.TextEmbedded;
import com.ajaxjs.image.model.constant.ImageFormat;
import com.ajaxjs.image.model.constant.ZoomMode;
import java.awt.Font;

ImageContainer container = new ImageContainer();
container.setOutputFormat(ImageFormat.PNG);
container.setBackgroundImage("background.jpg", ZoomMode.Origin, 0, 0);

TextEmbedded title = new TextEmbedded();
title.setText("Hello aj-image");
title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
title.setX(30);
title.setY(40);
container.addElement(title);

container.combine();
container.save("poster.png");
```

This example reads an existing `background.jpg` and saves `poster.png`. Replace the assets and text as needed; the output directory must already exist. Use PNG for transparency or whole-canvas rounded corners.

## What can it do?

- Images: positioning, scaling, rotation, rounded corners, opacity, and blur.
- Text: fonts, font sizes, colours, opacity, rotation, strikethrough, baseline positioning, and shrinking or ellipsis when a single line is too wide.
- Rectangles: dimensions, solid colours, gradients, opacity, rounded corners, and borders. Setting a border draws only the outline, without filling the interior.
- All three support horizontal centring, horizontal positioning directions, and repeated tiling. Direction controls coordinate anchoring, not image mirroring or reversed text.

## Current limitations

- Multiline text must be split in advance; automatic width-based wrapping is not implemented. `setAutoBreakLine(true)` still requires `breakLineElements` with each line's position and style. `setSpace()` currently has no effect.
- Image elements require a `BufferedImage` supplied through `setImage()`; `setImgUrl()` does not load images. The background overload `ImageContainer.setBackgroundImage(String, ...)` does accept local paths and HTTP/HTTPS URLs, but offers no configurable network timeout or authentication. Do not pass untrusted URLs directly.
- Image blur uses weighted smoothing rather than a strict Gaussian blur. It smooths colour channels while preserving the existing alpha values.

## Behaviour to know

- `setBackgroundImage(image, zoomMode, width, height)` sizes the canvas according to the requested mode and draws the background at that size.
- Backgrounds also accept local paths or HTTP/HTTPS URLs. Read failures throw immediately without adding a background. Network reads have no configurable timeout or authentication; do not pass untrusted URLs directly.
- Painters preserve element coordinates and the caller's graphics state. Reusing an element does not move it farther on each draw. Repeated text moves its baseline along with each tile.
- Text fitting measures the text again after changing its local font size. If even `...` cannot fit, it draws empty text. The original text, font, and coordinates remain available for another draw.

`combine()` publishes a result only after every drawing step succeeds. If a later composition fails, the previous successful result remains available. Blur uses a separate image, without modifying the source or accumulating across tiles.

Next: [Image utilities and output](/aj-image/utilities/) · [Home](/aj-image/)
