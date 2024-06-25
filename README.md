[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/ajaxjs-util?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/ajaxjs-util)
[![Javadoc](https://img.shields.io/badge/javadoc-1.1.5-brightgreen.svg?)](https://dev.ajaxjs.com/docs/javadoc/aj-util/)
![coverage](https://img.shields.io/badge/coverage-80%25-yellowgreen.svg?maxAge=2592000)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![QQ群](https://framework.ajaxjs.com/static/qq.svg)](https://shang.qq.com/wpa/qunwpa?idkey=3877893a4ed3a5f0be01e809e7ac120e346102bd550deb6692239bb42de38e22)

# AJAXJS Util 工具包

工具库/助手包是框架的基础构成部分之一，英文多见于 Utils、Helpers、Tools。本项目定位为轻量级工具库，jar 体积不过 150k 左右，尽量避免第三方依赖。鉴于 Spring
普及，于是就依赖他的工具方法（仅依赖 `Spring Core`）直接使用，如常见的 `StringUtils.isEmpty()` 等等。

本框架由下面若干模块所组成。

| 模块           | 说明                                                     | 备注 |
|--------------|--------------------------------------------------------|----|
| util         | 常规工具包，有字符串工具类、编码工具类、常用日期处理类、XML 工具类等等                  |    |
| sql          | 类似于 Apache DBUtils 的数据库工具程序，并提供类似于 MyBatis 基于注解的 ORM 层 |    |
| io           | 文件磁盘处理、流处理工具类、资源扫描器、Zip 解压缩                            |    |
| reflect      | 反射工具包                                                  |    |
| convert      | 值处理工具类、键对值转换                                           |    |
| cache        | 轻量级缓存服务，定时缓存、LRU/LFU 缓存                                |    |
| cryptography | 摘要算法：MD5/SHA；加密解密工具包： DES/AES/3D_DES/PBE/RSA/DH        |    |
| logger       | 基于`java.util.logger`封装的日志组件                            |    |                                                                                                                                          | [文档](https://gitee.com/sp42_admin/aj-utils/wikis/%E5%8F%AF%E7%83%AD%E6%9B%B4%E6%96%B0%E7%9A%84%E9%85%8D%E7%BD%AE%E4%B8%AD%E5%BF%83?sort_id=4390527) |
| map_traveler | 键值对遍历器，转换器                                             |    |                                                                                                                                          | [文档](https://gitee.com/sp42_admin/aj-utils/wikis/%E5%8F%AF%E7%83%AD%E6%9B%B4%E6%96%B0%E7%9A%84%E9%85%8D%E7%BD%AE%E4%B8%AD%E5%BF%83?sort_id=4390527) |
| regexp       | 类似 js 的正则表达式 API                                       |    |

<!-- | sdk_free     | 各种云厂商都为开发者提供各种 SDK 方便调用其 API，完成各种服务。但是又依赖洁癖的我痛恨“依赖地狱”。各种服务调用无非 HTTP 协议下去调用 API。API 接口是基础。于是我尝试收集各厂商的纯 HTTP API 调用例子，免除依赖。可能功能不是最全的，只是提供了基础的调用，以后希望通过不断完善来增强。 | [文档](https://gitee.com/sp42_admin/aj-utils/wikis/%E7%AE%80%E4%BB%8B?sort_id=4385414)                                                                | -->

Java Documents: https://dev.ajaxjs.com/docs/javadoc/aj-util/.

# 安装

要求 Java 1.8+。 Maven 坐标：

```xml

<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>ajaxjs-util</artifactId>
    <version>1.1.5</version>
</dependency>
```