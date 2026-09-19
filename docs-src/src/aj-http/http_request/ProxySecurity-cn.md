---
title: 注解代理、流与 TLS
layout: layouts/aj-util-cn.njk
alternate: /http_request/ProxySecurity/
---

# 注解代理、流与 TLS

注解代理是直接 HTTP API 之上的轻量便利层，适合简单 JSON/表单接口；但它不暴露完整请求生命周期、原始状态处理，也没有实现包中所有已声明的注解。关键集成应优先使用 `Request`。

## 一个已支持的代理接口

```java
import com.ajaxjs.util.httpremote.call.CallHandler;
import com.ajaxjs.util.httpremote.call.annotation.GET;
import com.ajaxjs.util.httpremote.call.annotation.POST;
import com.ajaxjs.util.httpremote.call.annotation.Url;
import java.util.Map;

@Url("https://example.com/api")
public interface ExampleApi {
    @GET("/health")
    String health();

    @POST("/users")
    Map<String, Object> createUser(Map<String, Object> body);
}

ExampleApi api = CallHandler.create(ExampleApi.class);
String health = api.health();
Map<String, Object> created = api.createUser(user);
```

`CallHandler` 当前分派 `@GET`、`@POST`、`@PUT` 和 `@DELETE`。返回类型仅限 `String`、原始 `Map` 或 Bean 类。POST/PUT 会选择**第一个 `Map` 参数**作为请求体；POST 默认表单，PUT 默认 JSON。方法级连接初始化器在类级 `@Url.initConnection` 初始化器之后执行。初始化器实例会缓存，因而须有 public 无参构造函数，并且可安全复用。

## 路径变量与配置限制

```java
@GET("/users/{id}")
Map<String, Object> find(String id);
```

路径替换使用编译得到的 Java 参数名和 `toString()` 原值。接口必须以 `-parameters` 编译；否则运行时参数名可能是 `arg0`，无法匹配 `{id}`。值**不会** URL 编码，URL 拼接也只是简单字符串连接。调用代理前请自行编码、校验路径数据。

包中出现了更多注解/接口，并不代表分派器已实现它们：

| 已存在 API | 当前行为 |
| --- | --- |
| `@HEAD`、`@Header`、`@RawBody`、`@FormData` | `CallHandler` 不分派。 |
| `@Url.config` / `HttpApiConfig` | 当前分派器不消费。 |
| `PayloadType.FILE_UPLOAD` | 会走到不支持的普通 multipart Map 路径；请使用 `MultipartPost`。 |
| `create2()` / `BaseCall.init()` | 不是可靠的初始化生命周期；没有默认方法分派。 |
| 集合/泛型返回与 Object 方法 | 没有专门的代理行为。 |

代理调用不会返回带状态感知的原始 `Response`，也不暴露显式连接句柄。若状态、重试策略、请求头或资源释放会影响正确性，请使用[直接生命周期](/aj-http/http_request/Base-cn/)。

## 流与 gzip

二进制响应应在连接前安装流消费者，并在回调内同步读取；`Request` 会在回调返回后关闭流。

```java
Request request = new Request(HttpMethod.GET, downloadUrl);
request.setInputStreamConsumer(in -> {
    try (OutputStream out = Files.newOutputStream(Paths.get("download.bin"))) {
        byte[] buffer = new byte[8192];
        for (int size; (size = in.read(buffer)) != -1; )
            out.write(buffer, 0, size);
    } catch (IOException e) {
        throw new UncheckedIOException(e);
    }
});

HttpURLConnection connection = request.init();
try {
    Response response = request.connect();
    if (!response.isOk())
        throw new IllegalStateException("Download failed: " + response.getHttpCode(), response.getEx());
} finally {
    connection.disconnect();
}
```

`Head.gzip(connection, in)` 只会包装单个、不区分大小写的 `gzip` Content-Encoding token；无编码或编码链会返回原流。gzip 不会自动解压。创建 `GZIPInputStream` 出错会包装为 `UncheckedIOException`；之后读取仍可能因数据损坏/截断抛出 `IOException`。

## TLS 与日志边界

生产环境应保留 JDK 正常的证书和主机名校验。`SkipSSL.init()` 会修改 JVM 全局默认值，信任所有服务端证书和主机名；`SkipSSL.setSSL_Ignore(...)` 对单个 HTTPS 连接关闭两项校验。这些方法仅适合受控的本地诊断，绝不能作为生产证书问题的解决手段。即便传入客户端 key manager，`getSocketFactory(...)` 仍信任全部服务端证书；`loadCert` 是包级可见，并非公开的客户端证书 API。

`Request.printLog` 会在 info 级别记录 URL、UTF-8 请求体、HTTP 状态和最多 460 个响应字符，并把格式化文本存入 MDC。截断不等于脱敏。请避免记录凭据、Bearer token、Cookie、个人信息、签名 URL 或敏感响应字段。

## 实现原理与取舍

该模块保持 Java 8 的 `HttpURLConnection` 实现。便利代理选择紧凑 API，而不是完整的声明式客户端特性集合；直接请求对象仍是流处理、基于状态决策、连接配置和更安全集成策略的出口。

文件传输请阅读[上传与下载](/aj-http/http_request/Transfer-cn/)，普通接口调用请阅读[HTTP 方法辅助类](/aj-http/http_request/Get-cn/)。

