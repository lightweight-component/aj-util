---
title: Image utilities and output
layout: layouts/aj-util.njk
alternate: /utilities-cn/
---

# Image utilities and output

## Tool overview

The `com.ajaxjs.image.utils` package also provides standalone image-processing tools.

| Class | What it does | Important details |
| --- | --- | --- |
| `ImageConvert` | Converts encoded formats such as JPG, PNG, GIF, and BMP; returns file bytes, writes to a stream, or saves a file; supports encoding quality | Call `setImageFormat()` first; the filename extension does not select the format. JPG and BMP output do not preserve alpha |
| `ImageEffect` | Scaling by a factor, resizing, thumbnails, rounded corners, canvas padding, and square canvases | Read the result through `getOutput()`; effects do not implicitly chain. Padding is black; square canvases keep the source at the top-left |
| `ImageMatrix` | Extracts raw RGB, BGR, RGBA, or grayscale bytes; crops three-channel matrices; creates images from matrices | Raw pixels are not PNG/JPG file bytes. `cutMatrix()` only supports RGB/BGR. NV21 is not implemented; `createRGBAImage()` currently drops alpha |
| `ImageTypeHandler` | Reads dimensions and format information; decodes the whole image or a region, optionally using a reader-supported pixel type | Requires complete encoded bytes and a format name. Reads only the first frame and cannot be reused after decoding. Manage it with try-with-resources |
| `ImageEncoder` | Encodes a `BufferedImage` into file bytes or a stream, or saves already encoded bytes | Shared by conversion and composition. `writeFile()` takes encoded bytes; it does not convert an image itself |
| `ImageBase` | Common image loading, copying, and processed-result output | Abstract base class, not instantiated directly. `ImageConvert`, `ImageEffect`, and `ImageMatrix` accept in-memory images, encoded bytes, local files, or HTTP/HTTPS URLs |

### Example: convert a format

The result contains a complete PNG file, suitable for saving or uploading, not a raw pixel array.

```java
import com.ajaxjs.image.model.constant.ImageFormat;
import com.ajaxjs.image.utils.ImageConvert;

byte[] pngBytes = new ImageConvert("input.jpg")
        .setImageFormat(ImageFormat.PNG)
        .convert();
```

### Example: thumbnail followed by rounded corners

Each effect reads the image supplied to its constructor. To apply another effect to the previous result, create a new processor with `getOutput()` instead of directly chaining effect methods.

```java
import com.ajaxjs.image.model.constant.ImageFormat;
import com.ajaxjs.image.utils.ImageEffect;
import java.awt.image.BufferedImage;

BufferedImage thumbnail = new ImageEffect("input.jpg")
        .thumbnail(320, 240, 2.0)
        .getOutput();

new ImageEffect(thumbnail)
        .roundCorner(24)
        .outputAsFile(ImageFormat.PNG, 1.0f, "thumbnail.png");
```

Thumbnails never enlarge the source and may be smaller than the requested bounds. Parent directories must already exist when saving files. For network input, restrict image sources and sizes yourself; current network-loading entry points have no configurable timeout or authentication.

## Behaviour to know

- `resize()` preserves indexed palettes and transparency. Aspect-ratio fitting keeps each dimension at least one pixel. Unrecognizable image bytes or HTTP image content are rejected during loading.
- `getMatrix(MatType.RGBA)` returns straight RGBA values, including for premultiplied input, so RGB is not darkened a second time.
- `ImageConvert.setQuality(null)` uses the encoder's default quality; explicit quality must be between `0` and `1`. `0` is a valid explicit quality value.
- Thumbnails stay inside the requested size without enlarging the source. Extreme aspect ratios use a centred crop. `scale()` preserves transparency when the source has an alpha channel.
- Region decoding returns an image sized to the selected region, including when an output pixel type is specified.
- `getMatrix(MatType.BGR)` returns blue, green, red bytes in that order. NV21 conversion is not implemented and throws `UnsupportedOperationException`.

## Output and failure handling

Conversion, effect output, and composition share the same image encoder for JPG, PNG, GIF, and BMP. Transparent input is explicitly converted to opaque RGB for JPG and BMP so that encoder differences between Java versions do not change the result.

File output first completes encoding, then writes a temporary sibling file and replaces the destination. Encoding or temporary-write failures do not truncate an existing file. Replacement prefers an atomic move and falls back to a regular move when unsupported; failure guarantees for that fallback depend on the file system.

`combine()` publishes its result only after every drawing step succeeds. A later failed composition preserves the previous successful image; a failed first composition cannot be exported as a partial image. Encoding errors retain their original cause for diagnosis.

Blur is computed into a separate image without changing the input pixels. Repeated drawing and tiling do not progressively blur the source.

`ImageTypeHandler` releases its reader and input stream after decoding succeeds or fails. For metadata-only use, use try-with-resources to release resources even without decoding. `ImageConvert.convert(OutputStream)` leaves the caller's output stream open.

Use the module tests as small runnable examples of these behaviours.

[Source code](https://github.com/lightweight-component/aj-util/tree/master/aj-image)

Related: [Image composition](/aj-image/composition/) · [Home](/aj-image/)
