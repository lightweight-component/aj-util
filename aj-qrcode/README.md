[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/aj-qrcode?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/aj-qrcode)
[![Javadoc](https://img.shields.io/badge/javadoc-1.3-brightgreen.svg?)](https://javadoc.io/doc/com.ajaxjs/aj-qrcode)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/aj-qrcode)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)

# QR Code Generator

Forks from https://github.com/nayuki/QR-Code-generator. Puts normal version and fast version in one project.

When mentioning Java's QR code generator, most people would think of ZXing (Zebra Crossing).
However, when I searched for alternatives to ZXing, I found it difficult to replace. Initially, I hoped there would be a
few simple classes to generate QR codes, but there were almost none. Instead, other languages like JavaScript have
native QR code generation capabilities. In contrast, Java mostly offers solutions based on ZXing wrappers. Of course,
based on my own capabilities, it's impossible for me to write one from scratch — the best I can do is learn from and
observe others' open-source projects, then further wrap them. By coincidence, I finally found a lightweight QR code
generation project: the QR Code generator library by Japan's
Nayuki [https://github.com/nayuki/QR-Code-generator](https://github.com/nayuki/QR-Code-generator). With just a few
critical classes, it can generate QR codes with rich functionality and is well-documented, making it an excellent
project for learning the principles behind QR code generation.

Features of QR-Code-generator:

- No dependencies beyond JDK.
- Supports encoding all 40 versions (sizes) and all 4 error correction levels, adhering to QR Code Model 2 standards.
- Output format: Raw modules/pixels of QR Code symbols.
- More accurate detection of penalty patterns resembling position markers compared to other implementations.
- Consumes less space when encoding numeric and special alphanumeric text compared to general text.
- Allows users to specify the minimum and maximum version numbers, and the library will automatically choose the
  smallest version within this range suitable for the data.
- Users can manually specify the masking pattern or let the library automatically evaluate all 8 masks and select the
  optimal one.
- Users can specify an absolute error correction level or let the library enhance it without increasing the version
  number.
- Allows users to manually create a list of data segments and add ECI segments.
- Encodes Japanese Unicode text in Kanji mode, saving significant space compared to UTF-8 bytes.
- Optimizes segment mode switching for texts containing mixed numeric/alphanumeric/general/Kanji parts.
- Supports exporting in PNG/SVG formats.

Tutorial(For Chinese): https://zhangxin.blog.csdn.net/article/details/139821806.

## Install

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-qrcode</artifactId>
    <version>1.3</version>
</dependency>
```

## Usage

```java
String text = "Hello, world!";                  // User-supplied Unicode text
Ecc errCorLvl = Ecc.LOW;                        // Error correction level
QrCode qr = QrCode.encodeText(text, errCorLvl);    // Make the QR Code symbol

BufferedImage img = Utils.toImage(qr, 10, 4);       // Convert to bitmap image
File imgFile = new File("hello-world-QR.png");    // File path for output
ImageIO.

write(img,"png",imgFile);               // Write image to file

String svg = Utils.toSvgString(qr, 4, "#FFFFFF", "#000000");  // Convert to SVG XML code
File svgFile = new File("c:\\temp\\hello-world-QR.svg");   // File path for output
Files.

write(svgFile.toPath(),svg.

getBytes(StandardCharsets.UTF_8)); // Write image to file
```

