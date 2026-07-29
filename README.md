<h1 align="center">AJ Utilities</h1>
<h3 align="center">A Lightweight Java OOP Utils Library.</h3>

<div align="center" style="text-align: center;">

[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/ajaxjs-util?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/ajaxjs-util)
![Java Version](https://img.shields.io/badge/Java-8-blue)
[![Javadoc](https://img.shields.io/badge/javadoc-1.3.5-brightgreen.svg?)](https://javadoc.io/doc/com.ajaxjs/ajaxjs-util )
![coverage](https://img.shields.io/badge/coverage-80%25-yellowgreen.svg?maxAge=2592000)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/lightweight-component/aj-util)
![GitHub repo size](https://img.shields.io/github/repo-size/lightweight-component/aj-util)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/aj-util)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![中文](https://img.shields.io/badge/lang-中文-red)](./README.zh-CN.md)

</div>

<hr />

This is a Java toolkit that truly embraces Object-Oriented Programming (OOP). Unlike other libraries that rely heavily on static methods, this toolkit requires you to instantiate objects before invoking utility methods.

By utilizing overloaded constructors, it automatically adapts to and converts various input parameters. This approach not only handles complex scenarios with ease but also results in a cleaner API design and more concise, DRY (Don't Repeat Yourself) code.

Furthermore, this library is designed to be lightweight with minimal dependencies. The JAR file is only about 170KB in size and includes the following modules:

| Class/Package Module | Detail Description                                                                     | Memo                                          |
|----------------------|----------------------------------------------------------------------------------------|-----------------------------------------------|
| BytesHelper          | Byte array utility class                                                               |                                               |
| CollUtils            | Collection utility class                                                               |                                               |
| ConvertBasicValue    | Attempts to convert target type, note that not all types can be converted              |                                               |
| DateHelper           | Date utility class                                                                     |                                               |
| EncodeTools          | String URL/Base64 encoder                                                              |                                               |
| MessageDigestHelper  | MD5/SHA1/SHA256/384/512 encryption utility class                                       |                                               |
| ObjectHelper         | A helper for Java Object                                                               |                                               |
| HTTP Request         | A Small HTTP Request Component                                                         |                                               |
| RandomTools          | Random Numbers and Strings                                                             |                                               |
| RegExpUtils          | Regular expression utility class                                                       |                                               |
| StrUtil              | String utility class                                                                   |                                               |
| JsonUtil             | Encapsulation of Jackson Library: Conversion Methods Between JSON, Map, Bean, and List | Jackson is the only library that dependencies |
| XmlHelper            | XML processing utility class                                                           |                                               |
| Cryptography         | AES/RSA encryption and decryption package                                              |                                               |
| IO                   | File, resource, stream utility package                                                 |                                               |
| Reflection           | Reflection utility package                                                             |                                               |

## Source code

[Github](https://github.com/lightweight-component/aj-util) | [GitCode](https://gitcode.com/lightweight-component/aj-util)

## Link

[Tutorials](https://aj-util.ajaxjs.com) | [Tutorials(Chinese)](https://framework.ajaxjs.com/aj-util/cn/) | [DeepWiki Tutorials](https://deepwiki.com/lightweight-component/aj-util) | [Java Documents](https://javadoc.io/doc/com.ajaxjs/ajaxjs-util)

## Install

Runs on Java 8+. Maven:

```xml

<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>ajaxjs-util</artifactId>
    <version>1.3.5</version>
</dependency>
```