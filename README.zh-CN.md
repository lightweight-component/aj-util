[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/ajaxjs-util?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/ajaxjs-util)
[![Javadoc](https://img.shields.io/badge/javadoc-1.2.7-brightgreen.svg?)](https://javadoc.io/doc/com.ajaxjs/ajaxjs-util )
![coverage](https://img.shields.io/badge/coverage-80%25-yellowgreen.svg?maxAge=2592000)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/aj-util)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![QQ群](https://framework.ajaxjs.com/static/qq.svg)](https://shang.qq.com/wpa/qunwpa?idkey=3877893a4ed3a5f0be01e809e7ac120e346102bd550deb6692239bb42de38e22)

# AJ Utilities

**经典的版本，不再维护**

轻量级的 Java 工具库，包含多个强大的组件。该库的 JAR 文件大小约为 90KB，并包含了以下模块：

| 类/包模块               | 详细描述                                      | 备注              |
|---------------------|-------------------------------------------|-----------------|
| BytesHelper         | 字节数组工具类                                   |                 |
| CollUtils           | 集合工具类                                     |                 |
| ConvertBasicValue   | 尝试将目标类型进行转换，注意并非所有类型都可以转换                 |                 |
| DateHelper          | 日期工具类                                     |                 |
| EncodeTools         | URL/Base64 编码工具                           |                 |
| MessageDigestHelper | MD5/SHA1/SHA256/384/512 加密工具类             |                 |
| ObjectHelper        | Java 对象辅助工具                               |                 |
| HTTP Request        | 一个小型 HTTP 请求组件                            |                 |
| RandomTools         | 随机数和随机字符串生成工具                             |                 |
| RegExpUtils         | 正则表达式工具类                                  |                 |
| StrUtil             | 字符串工具类                                    |                 |
| JsonUtil            | Jackson 库的封装：JSON、Map、Bean 和 List 之间的转换方法 | Jackson 是唯一的依赖库 |
| XmlHelper           | XML 处理工具类                                 |                 |
| Cryptography        | AES/RSA 加密解密包                             |                 |
| IO                  | 文件、资源、流操作工具包                              |                 |
| Reflection          | 反射工具包                                     |                 |

## 源码

[Github](https://github.com/lightweight-component/aj-util) | [GitCode](https://gitcode.com/lightweight-component/aj-util)

## 链接

- [教程](https://aj-util.ajaxjs.com/)
- [DeepWiki 教程](https://deepwiki.com/lightweight-component/aj-util) 
- [Java 文档](https://javadoc.io/doc/com.ajaxjs/ajaxjs-util)

## 安装

运行环境：Java 8 及以上版本。

```xml

<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>ajaxjs-util</artifactId>
    <version>1.2.7</version>
</dependency>
```