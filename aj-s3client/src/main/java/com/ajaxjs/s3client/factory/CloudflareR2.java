package com.ajaxjs.s3client.factory;

import com.ajaxjs.s3client.BaseS3ClientSigV4;
import com.ajaxjs.s3client.util.S3SigV4Utils;
import com.ajaxjs.util.httpremote.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * Cloudflare 的 R2 客户端
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CloudflareR2 extends BaseS3ClientSigV4 {
    /**
     * {@inheritDoc}
     */
    @Override
    public String listBucket() {
        validateConfiguration(false);
        String now = S3SigV4Utils.now();// 获取当前 GMT 时间，用于请求头 Date 字段
        String url = normalizedEndpoint();
        String signature = initSignatureBuilder(now, EMPTY_SHA256).getS3Signature(getCanonicalRequest(HttpConstant.GET, url), EMPTY_SHA256);
        Response result = new Get(url, setRequestHead(now, signature, EMPTY_SHA256)).getResp();

        return result.getResponseText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> listBucketXml() {
        validateConfiguration(false);
        String now = S3SigV4Utils.now();// 获取当前 GMT 时间，用于请求头 Date 字段
        String url = normalizedEndpoint();
        String signature = initSignatureBuilder(now, EMPTY_SHA256).getS3Signature(getCanonicalRequest(HttpConstant.GET, url), EMPTY_SHA256);

        return Get.apiXml(url, setRequestHead(now, signature, EMPTY_SHA256));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createBucket(String bucketName) {
        validateConfiguration(false);
        String now = S3SigV4Utils.now();// 获取当前 GMT 时间，用于请求头 Date 字段
        String url = bucketUrl(bucketName);
        String signature = initSignatureBuilder(now, EMPTY_SHA256).getS3Signature(getCanonicalRequest(HttpConstant.PUT, url), EMPTY_SHA256);

        return check(new Put(url, null, HttpConstant.FILE_TYPE, setRequestHead(now, signature, EMPTY_SHA256)).getResp());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteBucket(String bucketName) {
        validateConfiguration(false);
        String now = S3SigV4Utils.now();// 获取当前 GMT 时间，用于请求头 Date 字段
        String url = bucketUrl(bucketName);
        String signature = initSignatureBuilder(now, EMPTY_SHA256).getS3Signature(getCanonicalRequest(HttpConstant.DELETE, url), EMPTY_SHA256);

        return check(new Delete(url, setRequestHead(now, signature, EMPTY_SHA256)).getResp());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean putObject(String bucketName, String objectName, byte[] fileBytes) {
        validateConfiguration(false);
        if (fileBytes == null)
            throw new IllegalArgumentException("Object content is required.");
        String now = S3SigV4Utils.now();
        String url = objectUrl(bucketName, objectName);
        String contentSha256 = S3SigV4Utils.calcFileSHA256(fileBytes);
        String signature = initSignatureBuilder(now, contentSha256).getS3Signature(getCanonicalRequest(HttpConstant.PUT, url), contentSha256);

        return check(putBinary(url, fileBytes, setRequestHead(now, signature, contentSha256)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getObject(String bucketName, String objectName) {
        validateConfiguration(false);
        String now = S3SigV4Utils.now();
        String url = objectUrl(bucketName, objectName);
        String signature = initSignatureBuilder(now, EMPTY_SHA256)
                .getS3Signature(getCanonicalRequest(HttpConstant.GET, url), EMPTY_SHA256);

        return check(new Get(url, setRequestHead(now, signature, EMPTY_SHA256)).getResp());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteObject(String bucketName, String objectName) {
        validateConfiguration(false);
        String now = S3SigV4Utils.now();
        String url = objectUrl(bucketName, objectName);
        String signature = initSignatureBuilder(now, EMPTY_SHA256)
                .getS3Signature(getCanonicalRequest(HttpConstant.DELETE, url), EMPTY_SHA256);

        return check(new Delete(url, setRequestHead(now, signature, EMPTY_SHA256)).getResp());
    }
}
