package com.ajaxjs.s3client;


import com.ajaxjs.util.date.DateTools;
import com.ajaxjs.util.httpremote.Delete;
import com.ajaxjs.util.httpremote.Get;
import com.ajaxjs.util.httpremote.HttpConstant;
import com.ajaxjs.util.httpremote.Put;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Base implementation for providers that use the legacy S3 Signature Version 2 scheme.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseS3ClientSigV2 extends BaseS3Client {
    /**
     * 生成验证的签名
     *
     * @param data 数据
     * @return 验证的签名字符串
     */
    abstract public String getAuthSignature(String data);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createBucket(String bucketName) {
        validateConfiguration(false);
        validateBucketName(bucketName);
        String now = DateTools.nowGMTDate();
        String data = "PUT\n" + getCanonicalResource(now, bucketName, "");
        String url = getFullEndPoint(bucketName);

        return check(new Put(url, null, HttpConstant.FILE_TYPE, setRequestHead(now, data)).getResp());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteBucket(String bucketName) {
        validateConfiguration(false);
        validateBucketName(bucketName);
        String now = DateTools.nowGMTDate();
        String data = "DELETE\n" + getCanonicalResource(now, bucketName, "");
        String url = getFullEndPoint(bucketName);

        return check(new Delete(url, setRequestHead(now, data)).getResp());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String listBucket() {
        validateConfiguration(false);
        String now = DateTools.nowGMTDate();
        String canonicalHeaders = "", canonicalResource = "/";
        String data = "GET\n\n\n" + now + "\n" + canonicalHeaders + canonicalResource;
        String url = getEndPoint();

        return new Get(url, setRequestHead(now, data)).getResp().getResponseText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> listBucketXml() {
        validateConfiguration(false);
        String now = DateTools.nowGMTDate();
        String canonicalHeaders = "", canonicalResource = "/";
        String data = "GET\n\n\n" + now + "\n" + canonicalHeaders + canonicalResource;
        String url = getEndPoint();

        return Get.apiXml(url, setRequestHead(now, data));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getObject(String bucketName, String objectName) {
        validateConfiguration(false);
        String now = DateTools.nowGMTDate();
        String data = "GET\n" + getCanonicalResource(now, bucketName, objectName);
        String url = getObjectUrl(bucketName, objectName);

        return check(new Get(url, setRequestHead(now, data)).getResp());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteObject(String bucketName, String objectName) {
        validateConfiguration(false);
        String now = DateTools.nowGMTDate();// 获取当前时间，用于请求头
        String data = "DELETE\n" + getCanonicalResource(now, bucketName, objectName);
        String url = getObjectUrl(bucketName, objectName);

        return check(new Delete(url, setRequestHead(now, data)).getResp());
    }

    /**
     * Applies an HMAC function and formats the Authorization value.
     *
     * @param callback function receiving the secret key and canonical data
     * @param data     canonical data to sign
     * @return complete Authorization header value
     * @throws IllegalStateException    if the client configuration is missing
     * @throws IllegalArgumentException if a required signing value is missing
     */
    protected String getAuthSignature(BiFunction<String, String, String> callback, String data) {
        String signature = callback.apply(getConfig().getSecretKey(), data);

        return getSignValue(signature);
    }

    /**
     * Formats an encoded signature as a provider-specific Authorization value.
     *
     * @param signature encoded signature
     * @return formatted Authorization value
     * @throws IllegalStateException    if the client configuration is missing
     * @throws IllegalArgumentException if a required signing value is missing
     */
    protected String getSignValue(String signature) {
        if (getConfig() == null)
            throw new IllegalStateException("S3 configuration is required.");
        requireText(getConfig().getAccessKey(), "S3 access key");
        requireText(getConfig().getSecretKey(), "S3 secret key");
        requireText(getConfig().getRemark(), "SigV2 provider prefix");

        return getConfig().getRemark() + " " + getConfig().getAccessKey() + ":" + signature;
    }

    /**
     * 构建资源签名路径
     * 该方法用于构建一个包含当前时间、规范化的头信息和资源路径的字符串，主要用于授权访问AWS S3对象。
     *
     * @param now        表示当前时间的字符串，格式为特定的日期时间格式，用于签名中记录请求的时间。
     * @param bucketName S3存储桶的名称，是签名中必须包含的路径部分。
     * @param objectName S3对象（文件）的名称，是签名中指定的具体资源。
     * @return 返回一个字符串，该字符串包括换行符、当前时间、空的规范化头信息以及规范化的资源路径，用于构建签名。
     * @throws IllegalArgumentException 如果存储桶名称或对象名称无效
     */
    protected static String getCanonicalResource(String now, String bucketName, String objectName) {
        validateBucketName(bucketName);

        if (objectName == null)
            throw new IllegalArgumentException("S3 object name must not be null.");

        if (!objectName.isEmpty())
            validateObjectName(objectName);

        String encodedObjectName = objectName.isEmpty() ? "" : com.ajaxjs.s3client.util.URLEncoding.encodePath(objectName);
        String canonicalHeaders = "", canonicalResource = "/" + bucketName + "/" + encodedObjectName;

        // 拼接字符串返回，包括换行符、当前时间、空的规范化头以及资源路径
        return "\n\n" + now + "\n" + canonicalHeaders + canonicalResource;
    }

    /**
     * Creates a callback that applies the SigV2 date and authorization headers.
     *
     * @param now  request date
     * @param data canonical data
     * @return signed-header initializer
     */
    public Consumer<HttpURLConnection> setRequestHead(String now, String data) {
        return conn -> {
            conn.addRequestProperty(DATE, now);
            conn.addRequestProperty(AUTHORIZATION, getAuthSignature(data));   // 设置请求授权头和日期头
        };
    }

    /**
     * Returns the configured service endpoint.
     *
     * @return configured service endpoint with an HTTPS scheme
     * @throws IllegalStateException    if configuration is missing
     * @throws IllegalArgumentException if configuration is invalid
     */
    public String getEndPoint() {
        validateConfiguration(false);

        return normalizedEndpoint();
    }

    /**
     * Builds a virtual-hosted-style endpoint for a bucket.
     *
     * @param bucketName bucket name
     * @return virtual-hosted-style HTTPS endpoint
     * @throws IllegalArgumentException if the configuration or bucket name is invalid
     */
    public String getFullEndPoint(String bucketName) {
        validateConfiguration(false);
        validateBucketName(bucketName);
        String endpoint = normalizedEndpoint();

        return HTTPS + bucketName + "." + endpoint.substring(HTTPS.length());
    }

    /**
     * Builds an encoded virtual-hosted-style object URL.
     *
     * @param bucketName bucket name
     * @param objectName object key
     * @return encoded object URL
     * @throws IllegalArgumentException if the bucket name or object key is invalid
     */
    protected String getObjectUrl(String bucketName, String objectName) {
        validateObjectName(objectName);

        return getFullEndPoint(bucketName) + "/" + com.ajaxjs.s3client.util.URLEncoding.encodePath(objectName);
    }
}
