<h1 align="center">aj-images</h1>
<h3 align="center">A small image tools for Java.</h3>

<div align="center" style="text-align: center;">


[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/aj-image?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/aj-image)
![Java Version](https://img.shields.io/badge/Java-8-blue)
[![Javadoc](https://img.shields.io/badge/javadoc-1.0-brightgreen.svg?)](https://javadoc.io/doc/com.ajaxjs/aj-image )
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/lightweight-component/aj-image)
![GitHub repo size](https://img.shields.io/github/repo-size/lightweight-component/aj-image)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/aj-image)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![中文](https://img.shields.io/badge/lang-中文-red)](./README.zh-CN.md)
</div>

<hr />
aj-image is a lightweight tool for server-side image processing and composition in Java. It combines images, text, and rectangles for tasks such as posters and watermarks. `ImageContainer` is the composition entry point; other utilities cover format conversion, thumbnails, rounded corners, and pixel matrices.

## Maven dependency

Requires Java 8 or later. Version `1.0` is published to Maven Central. Add this dependency to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-image</artifactId>
    <version>1.0</version>
</dependency>
```

No additional repository configuration is normally needed.

## What can it do?

- Images: positioning, scaling, rotation, rounded corners, opacity, and blur.
- Text: fonts, font sizes, colours, opacity, rotation, strikethrough, baseline positioning, and shrinking or ellipsis when a single line is too wide.
- Rectangles: dimensions, solid colours, gradients, opacity, rounded corners, and borders. Setting a border draws only the outline, without filling the interior.
- All three support horizontal centring, horizontal positioning directions, and repeated tiling. Direction controls coordinate anchoring, not image mirroring or reversed text.

## Image utilities: usable without composition

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

## Current limitations

- Multiline text must be split in advance; automatic width-based wrapping is not implemented. `setAutoBreakLine(true)` still requires `breakLineElements` with each line's position and style. `setSpace()` currently has no effect.
- Image elements require a `BufferedImage` supplied through `setImage()`; `setImgUrl()` does not load images. The background overload `ImageContainer.setBackgroundImage(String, ...)` does accept local paths and HTTP/HTTPS URLs, but offers no configurable network timeout or authentication. Do not pass untrusted URLs directly.
- Image blur uses weighted smoothing rather than a strict Gaussian blur. It smooths colour channels while preserving the existing alpha values.
