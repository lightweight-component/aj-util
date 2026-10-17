---
title: aj-image
layout: layouts/aj-util.njk
---

# aj-image

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

## Documentation

- [Image composition](/aj-image/composition/): drawing backgrounds, images, text, and rectangles, plus current limitations.
- [Composition tutorial and examples](/aj-image/tutorial/): start with a simple example and build a poster step by step.
- [Image utilities and output](/aj-image/utilities/): conversion, thumbnails, rounded corners, pixel matrices, region decoding, and saving files.

The `utils` tools can be used independently of composition.

[Source code](https://github.com/lightweight-component/aj-util/tree/master/aj-image)
