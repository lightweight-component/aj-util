package com.ajaxjs.s3client.factory;


import com.ajaxjs.s3client.util.S3SigV4Utils;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.httpremote.HttpConstant;

import java.util.Map;

/**
 * Scaleway S3-compatible client that uploads objects with a public-read ACL.
 */
public class Scaleway extends CloudflareR2 {
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
        Map<String, String> acl = ObjectHelper.mapOf("x-amz-acl", ACL_PUBLIC_READ);

        String signature = initSignatureBuilder(now, contentSha256, acl).getS3Signature(getCanonicalRequest(HttpConstant.PUT, url), contentSha256);

        return check(putBinary(url, fileBytes, setRequestHead(now, signature, contentSha256, acl)));
    }
}
