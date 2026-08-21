package com.ajaxjs.s3client;

import com.ajaxjs.s3client.signer_v4.AwsCredentials;
import com.ajaxjs.s3client.signer_v4.CanonicalRequest;
import com.ajaxjs.s3client.signer_v4.SignBuilder;
import com.ajaxjs.util.HashHelper;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Shared request-signing support for S3-compatible Signature Version 4 providers.
 */
public abstract class BaseS3ClientSigV4 extends BaseS3Client {
    /**
     * 空字符串的 SHA-256 哈希值，用于某些的操作
     */
    public final static String EMPTY_SHA256 = HashHelper.getSHA256("");

    /**
     * Initializes a signer with the required SigV4 headers.
     *
     * @param date SigV4 timestamp
     * @param hash payload digest
     * @return initialized signer
     * @throws IllegalStateException    if client configuration is missing
     * @throws IllegalArgumentException if signing configuration is invalid
     */
    public SignBuilder initSignatureBuilder(String date, String hash) {
        return initSignatureBuilder(date, hash, null);
    }

    /**
     * Initializes a signer with required and optional signed headers.
     *
     * @param date                SigV4 timestamp
     * @param hash                payload digest
     * @param extraRequestHeaders 其他自定义的字段，参与签名和实际 HTTP 头请求
     * @return initialized signer
     * @throws IllegalStateException    if client configuration is missing
     * @throws IllegalArgumentException if signing configuration is invalid
     */
    public SignBuilder initSignatureBuilder(String date, String hash, Map<String, String> extraRequestHeaders) {
        String accessKey = getConfig().getAccessKey(), secretKey = getConfig().getSecretKey();

        SignBuilder builder = new SignBuilder(new AwsCredentials(accessKey, secretKey), getConfig().getRegion())
                .header("x-amz-date", date)
//                .header("host", "s3.us-west-002.backblazeb2.com")
                .header("x-amz-content-sha256", hash);

        if (isSetHost()) {
            String host = getConfig().getEndPoint().replaceAll("http(s?)://", "");
            builder = builder.header("host", host);
        }

        if (extraRequestHeaders != null && !extraRequestHeaders.isEmpty()) {
            for (String key : extraRequestHeaders.keySet())
                builder = builder.header(key, extraRequestHeaders.get(key));
        }

        return builder;
    }

    /**
     * 创建一个指向指定端点的 HTTP 请求对象。
     *
     * @param method   HTTP 请求方法，例如 GET、POST 等。
     * @param endPoint HTTP 请求的端点（URL）。
     * @return HttpRequest 对象，用于发送 HTTP 请求。
     * @throws RuntimeException 如果端点的 URI 语法有误。
     */
    public static CanonicalRequest getCanonicalRequest(String method, String endPoint) {
        try {
            return new CanonicalRequest(method, new URI(endPoint));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param date      timestamp
     * @param signature authorization value
     * @param hash      payload digest
     * @return header initializer
     */
    public Consumer<HttpURLConnection> setRequestHead(String date, String signature, String hash) {
        return setRequestHead(date, signature, hash, null);
    }

    /**
     * @param date                timestamp
     * @param signature           authorization value
     * @param hash                payload digest
     * @param extraRequestHeaders additional signed headers; may be {@code null}
     * @return header initializer
     */
    public Consumer<HttpURLConnection> setRequestHead(String date, String signature, String hash, Map<String, String> extraRequestHeaders) {
        return conn -> {
            conn.setRequestProperty("x-amz-date", date); // 设置请求头 Date
            conn.setRequestProperty("x-amz-content-sha256", hash); // 设置请求头
            conn.setRequestProperty(AUTHORIZATION, signature); // 设置请求头 Authorization

            if (isSetHost()) {
                String host = getConfig().getEndPoint().replaceAll("http(s?)://", "");
                conn.setRequestProperty("host", host);
            }

            if (extraRequestHeaders != null && !extraRequestHeaders.isEmpty()) {
                for (String key : extraRequestHeaders.keySet())
                    conn.setRequestProperty(key, extraRequestHeaders.get(key));
            }
        };
    }
}
