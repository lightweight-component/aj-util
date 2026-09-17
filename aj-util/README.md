<h1 align="center">AJ Utilities</h1>
<h3 align="center">A Lightweight Java OOP Utils Library.</h3>

<div align="center" style="text-align: center;">


[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/ajaxjs-util?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/ajaxjs-util)
![Java Version](https://img.shields.io/badge/Java-8-blue)
[![Javadoc](https://img.shields.io/badge/javadoc-1.3.8-brightgreen.svg?)](https://javadoc.io/doc/com.ajaxjs/ajaxjs-util )
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/lightweight-component/aj-util)
![GitHub repo size](https://img.shields.io/github/repo-size/lightweight-component/aj-util)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/aj-util)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![中文](https://img.shields.io/badge/lang-中文-red)](./README.zh-CN.md)

</div>

<hr />

AJ Utilities is a lightweight Java utility library with a Java 8 baseline. It combines object-oriented APIs with static
helpers: overloaded constructors adapt inputs where useful, while simple operations remain directly callable.

## Modules

| Classes / package                           | Purpose                                                                       |
|---------------------------------------------|-------------------------------------------------------------------------------|
| StrUtil, StringBytes, Base64Utils, UrlCodec | Strings, bytes/hex, Base64, form and query-value encoding                     |
| ConvertBasicValue, ObjectHelper, MapTool    | Type conversion, object and map helpers                                       |
| RegExpUtils, RegExpHelper                   | Regular expressions                                                           |
| RandomTools, HashHelper                     | Random numbers/strings, UUIDv7, digests and HMAC                              |
| JsonUtil, XmlHelper                         | JSON conversion, XML parsing and map conversion                               |
| date                                        | DateTools, DateTypeConvert and Formatter                                      |
| io                                          | DataReader, DataWriter, FileHelper, ResourceHelper, ZipHelper and UnzipHelper |
| httpremote                                  | HTTP helpers, upload, download and call proxies; models in httpremote.model   |
| reflect                                     | Clazz, Fields, Methods, Types and NewInstance                                 |
| cryptography                                | Symmetric encryption, RSA, signatures, key and certificate helpers            |
| log                                         | Trace, TextBox and operation-log annotation                                   |

## Install

The current checkout declares version **1.3.8**. Java 8 is the compatibility baseline; see [known issues](to_fix.md) for
runtime limitations.

```xml

<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>ajaxjs-util</artifactId>
    <version>1.3.8</version>
</dependency>
```

The default JSON engine uses Jackson 2. Both dependencies below are `provided` in the library POM, so applications using
it must supply them at runtime. These versions match the current POM:

```xml

<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.22.1</version>
</dependency>
<dependency>
<groupId>com.fasterxml.jackson.datatype</groupId>
<artifactId>jackson-datatype-jsr310</artifactId>
<version>2.22.1</version>
</dependency>
```

## Quick start

```java
import com.ajaxjs.util.RandomTools;
import com.ajaxjs.util.StringBytes;
import com.ajaxjs.util.UrlCodec;
import com.ajaxjs.util.io.ZipHelper;
import com.ajaxjs.util.io.UnzipHelper;

String queryValue=new UrlCodec("a b+c").encodeQueryValue(); // a%20b%2Bc
        String hex=StringBytes.bytesToHex(new byte[]{0x0A,0x2F}); // 0A2F
        String id=RandomTools.uuidV7(); // 32 hex characters
        java.util.Date createdAt=RandomTools.showTime(id);

        new ZipHelper("input-directory","output.zip").zip();
        new UnzipHelper("output.zip","extracted").extract();
```

The String-based ZipHelper constructor accepts a directory. For a single file,
use `new ZipHelper(new java.io.File("input.txt"), "output.zip")`.

## Refactoring notes

- UrlEncode and UrlHelper are consolidated into UrlCodec. Choose form or query-value methods explicitly; do not encode
  an entire URL as a query value.
- Hex conversion is now StringBytes.bytesToHex / hexToBytes.
- Resources is now ResourceHelper. Use getStream for resources packaged in JARs.
- Compression and extraction are separate instance APIs: ZipHelper.zip() and UnzipHelper.extract().
- Request, Response, HttpConstant, HttpMethod and PayloadType are in com.ajaxjs.util.httpremote.model.
- XML/map conversion belongs to XmlHelper.
- RandomTools.uuidV7() returns compact text; uuidV7(true) includes hyphens. showTime accepts both formats.

## Usage boundaries

Random numbers/strings are not security tokens. HMAC requires an explicit key. Hashing and Base64 are not encryption.
Legacy crypto convenience methods and SkipSSL are not recommended for new production code.

Use explicit charsets for portable byte conversion. Stream overloads differ in ownership, and text readers still have
line-ending limitations. Java 17 default-interface reflection is also a known issue. See [to_fix.md](to_fix.md) for
verified findings and test status; refactoring does not mean every outstanding issue is resolved.

## Development and agent skill

From the repository root:

```sh
mvn -f aj-util/pom.xml test
```

The repository includes an [aj-util agent skill](../skills/aj-util/SKILL.md) describing current APIs, contracts and the
maintenance workflow.

## Source and documentation

[GitHub](https://github.com/lightweight-component/aj-util) | [GitCode](https://gitcode.com/lightweight-component/aj-util)

[English tutorials](https://aj-util.ajaxjs.com) | [Chinese tutorials](https://framework.ajaxjs.com/aj-util/cn/) | [DeepWiki](https://deepwiki.com/lightweight-component/aj-util) | [Javadoc](https://javadoc.io/doc/com.ajaxjs/ajaxjs-util)
