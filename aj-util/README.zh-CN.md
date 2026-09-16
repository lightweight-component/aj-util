<h1 align="center">AJ-Utils</h1>
<h3 align="center">小型的 Java 面向对象编程工具包</h3>

<div style="text-align: center;">

[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/ajaxjs-util?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/ajaxjs-util)
![Java Version](https://img.shields.io/badge/Java-8-blue)
[![Javadoc](https://img.shields.io/badge/javadoc-1.3.8-brightgreen.svg?)](https://javadoc.io/doc/com.ajaxjs/ajaxjs-util )
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/lightweight-component/aj-util)
![GitHub repo size](https://img.shields.io/github/repo-size/lightweight-component/aj-util)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/aj-util)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![QQ群](https://framework.ajaxjs.com/static/qq.svg)](https://shang.qq.com/wpa/qunwpa?idkey=3877893a4ed3a5f0be01e809e7ac120e346102bd550deb6692239bb42de38e22)

</div>

<hr />

AJ Utilities 是一个以 Java 8 为兼容基线的轻量级 Java 工具库。它结合面向对象 API 与静态辅助方法：适合的场景通过重载构造器适配输入，简单操作则可直接调用。

## 模块

| 类 / 包                                    | 用途                                                                      |
|------------------------------------------|-------------------------------------------------------------------------|
| StrUtil、StringBytes、Base64Utils、UrlCodec | 字符串、字节/十六进制、Base64、表单与查询参数值编码                                           |
| ConvertBasicValue、ObjectHelper、MapTool   | 类型转换、对象与 Map 辅助操作                                                       |
| RegExpUtils、RegExpHelper                 | 正则表达式                                                                   |
| RandomTools、HashHelper                   | 随机数/字符串、UUIDv7、摘要与 HMAC                                                 |
| JsonUtil、XmlHelper                       | JSON 转换、XML 解析与 Map 转换                                                  |
| date                                     | DateTools、DateTypeConvert 和 Formatter                                   |
| io                                       | DataReader、DataWriter、FileHelper、ResourceHelper、ZipHelper 和 UnzipHelper |
| httpremote                               | HTTP 请求、上传、下载与调用代理；模型位于 httpremote.model                                |
| reflect                                  | Clazz、Fields、Methods、Types 和 NewInstance                                |
| cryptography                             | 对称加密、RSA、签名、密钥与证书辅助操作                                                   |
| log                                      | Trace、TextBox 与操作日志注解                                                   |

## 安装

当前源码声明的版本为 **1.3.8**，兼容基线为 Java 8；运行时限制见[已知问题](to_fix.md)。

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>ajaxjs-util</artifactId>
    <version>1.3.8</version>
</dependency>
```

默认 JSON 引擎使用 Jackson 2。以下依赖在工具库 POM 中为 `provided`，使用该引擎的应用需要在运行时自行提供。版本与当前 POM
保持一致：

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

## 快速开始

```java
import com.ajaxjs.util.RandomTools;
import com.ajaxjs.util.StringBytes;
import com.ajaxjs.util.UrlCodec;
import com.ajaxjs.util.io.ZipHelper;
import com.ajaxjs.util.io.UnzipHelper;

String queryValue = new UrlCodec("a b+c").encodeQueryValue(); // a%20b%2Bc
String hex = StringBytes.bytesToHex(new byte[]{0x0A, 0x2F}); // 0A2F
String id = RandomTools.uuidV7(); // 32 hex characters
java.util.Date createdAt = RandomTools.showTime(id);

new ZipHelper("input-directory", "output.zip").zip();
new UnzipHelper("output.zip", "extracted").extract();
```

ZipHelper 的 String 构造器接受目录。压缩单个文件请使用 `new ZipHelper(new java.io.File("input.txt"), "output.zip")`。

## 重构迁移说明

- UrlEncode 和 UrlHelper 合并为 UrlCodec。显式选择表单或查询参数值方法，不要把整个 URL 当作查询参数值编码。
- 十六进制转换移至 StringBytes.bytesToHex / hexToBytes。
- Resources 更名为 ResourceHelper。读取 JAR 内资源应使用 getStream。
- 压缩与解压拆为实例 API：ZipHelper.zip() 和 UnzipHelper.extract()。
- Request、Response、HttpConstant、HttpMethod、PayloadType 位于 com.ajaxjs.util.httpremote.model。
- XML 与 Map 互转统一放在 XmlHelper。
- RandomTools.uuidV7() 返回紧凑字符串；uuidV7(true) 带连字符，showTime 支持两种格式。

## 使用边界

随机数/字符串不适合作为安全令牌。HMAC 必须显式设置密钥。摘要与 Base64 都不是加密。旧式加密便捷方法和 SkipSSL 不建议用于新的生产代码。

为保证字节转换跨环境一致，请显式选择字符集。不同流重载的关闭约定不同，文本读取仍存在换行保真问题；Java 17
下调用接口默认方法的反射实现也存在已知问题。已确认的问题与测试状态见 [to_fix.md](to_fix.md)，重构完成不代表所有遗留问题均已解决。

## 开发与 agent skill

在仓库根目录执行：

```sh
mvn -f aj-util/pom.xml test
```

仓库包含 [aj-util agent skill](../skills/aj-util/SKILL.md)，提供当前 API、行为约定与维护流程。

## 源码与文档

[GitHub](https://github.com/lightweight-component/aj-util) | [GitCode](https://gitcode.com/lightweight-component/aj-util)

[英文教程](https://aj-util.ajaxjs.com) | [中文教程](https://framework.ajaxjs.com/aj-util/cn/) | [DeepWiki](https://deepwiki.com/lightweight-component/aj-util) | [Javadoc](https://javadoc.io/doc/com.ajaxjs/ajaxjs-util) | [English README](README.md)
