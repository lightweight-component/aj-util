<h1 align="center">AJ-Utils</h1>
<h3 align="center">小型的 Java 编程工具包</h3>

<div style="text-align: center;">

![Java Version](https://img.shields.io/badge/Java-8-blue)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/lightweight-component/aj-util)
![GitHub repo size](https://img.shields.io/github/repo-size/lightweight-component/aj-util)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/aj-util)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![QQ群](https://framework.ajaxjs.com/static/qq.svg)](https://shang.qq.com/wpa/qunwpa?idkey=3877893a4ed3a5f0be01e809e7ac120e346102bd550deb6692239bb42de38e22)

</div>

<hr />

一个面向 Java 8 的轻量级工具库集合，提供日常开发常用的通用能力，以及 FTP、S3 对象存储、二维码和密码学等独立组件。

## 特点

- **Java 8 兼容**：各组件均以 Java 8 及以上运行环境为目标。
- **清晰、易懂、简单**：保持轻量设计，API 与实现尽量直观，方便阅读、使用与维护。
- **重视质量**：提供良好的代码注释、文档和单元测试，为集成与后续演进提供保障。
- **模块化使用**：按需引入相应组件，避免不必要的依赖。

## 项目列表

| 项目                                   | Maven ArtifactId  | 简介                                                   | 文档                                    |
|--------------------------------------|-------------------|------------------------------------------------------|---------------------------------------|
| [aj-util](./aj-util)                 | `ajaxjs-util`     | 基于 OOP 风格的通用 Java 工具库，涵盖字符串、集合、日期、JSON、XML、IO、反射等能力。 | [README](./aj-util/README.md)         |
| [aj-ftp](./aj-ftp)                   | `aj-ftp`          | 轻量级 FTP 客户端，支持上传、下载与进度追踪。                            | [README](./aj-ftp/README.md)          |
| [aj-s3client](./aj-s3client)         | `aj-s3client`     | 轻量级 S3 兼容对象存储客户端。                                    | [README](./aj-s3client/README.md)     |
| [aj-qrcode](./aj-qrcode)             | `aj-qrcode`       | 轻量级二维码生成组件，支持 PNG、SVG 等输出。                           | [README](./aj-qrcode/README.md)       |
| [aj-cryptography](./aj-cryptography) | `aj-cryptography` | AES、DES、PBE、RSA 等加密与解密能力。                            | [README](./aj-cryptography/README.md) |
| [aj-http](./aj-http)                 | `aj-net`          | 简洁的 HTTP 请求组件。                                       | [README](./aj-http/README.md)         |

## 源代码

- [GitHub](https://github.com/lightweight-component/aj-util)
- [GitCode](https://gitcode.com/lightweight-component/aj-util)
- [Gitee](https://gitee.com/lightweight-components/aj-util)

## 许可证

本项目采用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt) 开源许可证。
