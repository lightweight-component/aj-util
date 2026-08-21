<h1 align="center">aj-s3client</h1>
<h3 align="center">A Lightweight S3 Client / S3 存储 Java 客户端</h3>

<div align="center" style="text-align: center;">

[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/aj-s3client?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/aj-s3client)
![Java Version](https://img.shields.io/badge/Java-8-blue)
[![Javadoc](https://img.shields.io/badge/javadoc-1.4-brightgreen.svg?)](https://javadoc.io/doc/com.ajaxjs/aj-s3client)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/lightweight-component/aj-s3client)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/lightweight-component/aj-s3client)
![GitHub repo size](https://img.shields.io/github/repo-size/lightweight-component/aj-s3client)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)

</div>

<hr />

Currently, it's uncommon to store binary files directly on application servers. Instead, people prefer using "object
storage" solutions such as Amazon AWS S3, Alibaba Cloud OSS, Cloudflare R2, etc.
AWS was the pioneer in this area, making its S3 protocol almost a de facto standard.
Most major vendors have implemented this protocol in their object storage offerings.
Almost all of them provide convenient SDKs for quickly integrating S3 services.
Speaking of complexity, building an S3 client isn't overly challenging.
Driven by a momentary itch to create something, I decided to build my own S3 client.
This component focuses on being lightweight, consisting of no more than ten classes, while still covering the basic
operations needed for daily OSS tasks.

Support these S3 Storages directly:

- Cloudflare R2
- Scaleway
- Backblaze
- Aliyun OSS
- Netease OSS

Tutorial(For Chinese): https://blog.csdn.net/zhangxin09/article/details/137671230.

## Source code

[Github](https://github.com/lightweight-component/aj-s3client) | [Gitcode](https://gitcode.com/lightweight-component/aj-s3client)

## Install

Requires Java 8 or higher.

```xml

<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-s3client</artifactId>
    <version>1.4</version>
</dependency>
```

The configuration file `application.yml` for unit tests was not committed with the VCS. Its content is as follows:

```yaml
S3Storage:
  Nso:
    accessKey: xx
    accessSecret: xx
    api: nos-eastchina1.126.net
    bucket: xx
  Oss:
    accessKeyId: xx
    secretAccessKey: xx
    endpoint: oss-cn-beijing.aliyuncs.com
    bucket: xx
  LocalStorage: # 本地保存
    absoluteSavePath: c:\temp\ # 若有此值，保存这个绝对路径上
```

## Usage

### Step 1: Set Up OSS Parameters

The first step is to configure all necessary OSS parameters. These parameters are detailed in the configuration object
`Config`. Below is an example of how to set up these parameters:

```java
/**
 * 配置
 */
@Data
public class Config {
    /**
     * 访问 API
     */
    private String endPoint;

    /**
     * 访问 Key
     */
    private String accessKey;

    /**
     * 访问密钥
     */
    private String secretKey;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 签名中的标识，每个厂商不同
     */
    private String remark;
}
```

This POJO can be injected into each vendor's implementation using Spring.
If you're not using Spring, the simplest way is to instantiate and configure it manually like this:

![https://i-blog.csdnimg.cn/blog_migrate/4fd200e1c60016fe1acc5afd70a54ada.png]()

For various OSS API operations, please refer to the S3Client interface.
Since Java developers are generally familiar with this, we won't go into detailed explanations here.

```java
package com.ajaxjs.s3client;

import java.util.Map;

/**
 * S3 客户端
 */
public interface S3Client {
    /**
     * 列出存储桶中的所有对象
     *
     * @return XML List
     */
    String listBucket();

    /**
     * 列出存储桶中的所有对象
     *
     * @return XML List Map
     */
    Map<String, String> listBucketXml();

    /**
     * 创建一个存储桶（Bucket）
     *
     * @param bucketName 存储桶的名称，必须全局唯一
     * @return true=操作成功
     */
    boolean createBucket(String bucketName);

    /**
     * 删除一个存储桶（Bucket）
     *
     * @param bucketName 存储桶的名称
     * @return true=操作成功
     */
    boolean deleteBucket(String bucketName);

    /**
     * 将字节数据上传到指定的存储桶中
     *
     * @param bucketName 存储桶的名称
     * @param objectName 对象（文件）在存储桶中的名称
     * @param fileBytes  要上传的文件的字节数据
     * @return true=操作成功
     */
    boolean putObject(String bucketName, String objectName, byte[] fileBytes);

    /**
     * 将字节数据上传到指定的存储桶中
     *
     * @param objectName 对象（文件）在存储桶中的名称
     * @param fileBytes  要上传的文件的字节数据
     * @return true=操作成功
     */
    boolean putObject(String objectName, byte[] fileBytes);

    /**
     * 将字节数据上传到指定的存储桶中
     *
     * @param bucketName 存储桶的名称
     * @param objectName 对象（文件）在存储桶中的名称
     * @return true=操作成功
     */
    boolean getObject(String bucketName, String objectName);

    /**
     * 将字节数据上传到指定的存储桶中
     *
     * @param objectName 对象（文件）在存储桶中的名称
     * @return true=操作成功
     */
    boolean getObject(String objectName);

    /**
     * 删除指定的文件
     *
     * @param bucketName 存储桶的名称
     * @param objectName 要删除的文件名称
     * @return true=操作成功
     */
    boolean deleteObject(String bucketName, String objectName);

    /**
     * 删除指定的文件
     *
     * @param objectName 要删除的文件名称
     * @return true=操作成功
     */
    boolean deleteObject(String objectName);
}
```

## Development Insights

During the implementation of this client, I’ve gained the following insights:

1. **Reliance on HTTP Communication**  Despite being an object storage service, OSS operations fundamentally revolve
   around file management — uploading, downloading, and deleting files — along with handling "Buckets". All these
   interactions are done via HTTP APIs. Therefore, the client must encapsulate an HttpClient component to handle API
   calls properly. Cloudflare R2 provides Postman examples for their APIs, which greatly helped me in reconstructing the
   Java HTTP requests.
1. **Authentication and Signing Requests** As with any protocol-based communication, authentication is a key challenge.
   The S3 protocol requires each HTTP request to include an `Authorization` header. Generating a valid signature is a
   relatively complex process involving multiple parameters and encryption steps, which takes up a significant portion
   of the codebase. There are two main signing versions: SigV2 and SigV4. This component supports both protocols.
1. **Variations Across Vendors** Although most vendors implement the S3 protocol, there are often subtle differences in
   their respective APIs. To accommodate these variations, the code design uses inheritance:
   `BaseS3Client–>BaseS3ClientSigV2/BaseS3ClientSigV4–>Variations Across Vendors`. This structure allows for shared
   logic at the base level while enabling customization per vendor.
1. **Signature Generation Is the Most Tricky Part** The most challenging part during development was generating the
   correct signature. Even a small mistake can lead to an invalid signature, causing the server to reject the request
   due to failed validation. Among the two versions, SigV2 is relatively simpler, while SigV4 involves more steps and
   stricter rules. So I found a great open-source implementation
   called [aws-v4-signer-java](https://github.com/lucasweb78/aws-v4-signer-java) — it’s zero-dependency, which fit my
   needs perfectly. I simplified and refactored a lot of its code to better suit this project. In addition, Alibaba
   Cloud's documentation on S3-compatible signing was also very clear and helpful. For more details, please refer to
   their [documentation](https://help.aliyun.com/zh/oss/developer-reference/recommend-to-use-signature-version-4?spm=a2c4g.11186623.0.0.398d28871Sob7M)
   and
   the [source code](https://github.com/aliyun/aliyun-oss-java-sdk/blob/master/src/main/java/com/aliyun/oss/internal/signer/OSSV4Signer.java).
1. **Unit testing is still extremely important** — especially when developing or refactoring components. In many cases,
   reproducing issues would be difficult without unit tests. Having solid test coverage can also significantly improve
   development efficiency.

This component is not a complete S3 client implementation. Rather, it covers only a small portion of the S3 features
that our software requires. The design goals were simplicity, minimal external dependencies, and low memory footprint.