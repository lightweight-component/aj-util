<h1 align="center">aj-ftp</h1>
<h3 align="center">一个轻量级 Java FTP 客户端</h3>

<div align="center" style="text-align: center;">

[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/aj-ftp?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/aj-ftp)
![Java Version](https://img.shields.io/badge/Java-8-blue)
[![Javadoc](https://img.shields.io/badge/javadoc-1.4-brightgreen.svg?)](https://javadoc.io/doc/com.ajaxjs/ajaxjs-ftp )
![coverage](https://img.shields.io/badge/coverage-80%25-yellowgreen.svg?maxAge=2592000)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/lightweight-component/aj-ftp)
![GitHub repo size](https://img.shields.io/github/repo-size/lightweight-component/aj-ftp)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/aj-ftp)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![中文](https://img.shields.io/badge/lang-中文-red)](./README.zh-CN.md)

</div>

<hr />


**aj-ftp** 是一个简单、轻量级的 Java FTP 客户端组件，主要用于实现文件的上传和下载功能。

## 核心特性

* **简单轻量**：易于使用，无依赖。
* **兼容性好**：基于 JDK 的 `sun.*` 扩展包实现。当运行环境（如 Android）不支持 `sun.*` 包时，此组件可以作为替代方案。
* **支持进度追踪**：内置了监控文件上传和下载进度的功能。

## 🚀 快速开始

### 安装

在 Maven 项目中，通过添加以下依赖来引入该库：

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-ftp</artifactId>
    <version>1.4</version>
</dependency>
```

### 使用示例

以下代码展示了如何使用`aj-ftp`连接到一个公共的 FTP 服务器进行文件上传和下载。

```java
import com.ajaxjs.net.ftp.SimpleFtpClient;
import java.io.IOException;

public class TestFTP {
    
    // 测试文件上传
    public void testUpload() throws IOException {
        // 连接到服务器
        try (SimpleFtpClient client = new SimpleFtpClient("speedtest.tele2.net", 21)) {
        // 使用匿名登录
        client.login("anonymous", "anonymous");
        // 上传文件 (本地路径, 服务器目标路径)
        client.upload("c:\\temp\\re.zip", "/upload/re.zip");
        // 关闭连接
        }
    }

    // 测试文件下载
    public void testDownload() throws IOException {
        // 连接到服务器
        try (SimpleFtpClient ftp = new SimpleFtpClient("speedtest.tele2.net", 21)) {
        // 使用匿名登录
        ftp.login("anonymous", "anonymous");
        // 下载文件 (服务器源路径, 本地目标路径)
        ftp.getFile("/1KB.zip", "c:\\temp\\re.zip");
        }
    }
}
```

## 🔗 相关资源

* **源代码仓库
  **: [GitHub](https://github.com/lightweight-component/aj-ftp) | [Gitcode](https://gitcode.com/lightweight-component/aj-ftp)
* **中文教程**: [CSDN 博客](https://blog.csdn.net/zhangxin09/article/details/134222511)
* **API 文档**: [Javadoc](https://javadoc.io/doc/com.ajaxjs/ajaxjs-ftp)
