package com.ajaxjs.s3client.factory;

import com.ajaxjs.s3client.BaseS3ClientSigV2;
import com.ajaxjs.util.HashHelper;
import com.ajaxjs.util.date.DateTools;
import com.ajaxjs.util.httpremote.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Base64;

/**
 * Legacy NetEase Object Storage client retained for compatibility.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NeteaseOSS extends BaseS3ClientSigV2 {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthSignature(String data) {
        return getAuthSignature((key, value) -> HashHelper.getHmacSHA256(value, key, false), data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean putObject(String bucketName, String objectName, byte[] fileBytes) {
        validateConfiguration(false);

        if (fileBytes == null)
            throw new IllegalArgumentException("Object content is required.");

        byte[] md5Bytes = new HashHelper(HashHelper.MD5, fileBytes).getMessageDigest();
        String md5Hex = com.ajaxjs.util.BytesHelper.bytesToHexStr(md5Bytes).toLowerCase(java.util.Locale.ROOT);
        String contentMd5 = Base64.getEncoder().encodeToString(md5Bytes);
        String now = DateTools.nowGMTDate();
        String data = "PUT\n" + contentMd5 + getCanonicalResource(now, bucketName, objectName);
        String url = getObjectUrl(bucketName, objectName);

        Response result = putBinary(url, fileBytes, conn -> {
            conn.addRequestProperty(DATE, now);
            conn.addRequestProperty(AUTHORIZATION, getAuthSignature(data));
            conn.addRequestProperty("Content-MD5", contentMd5);
//            conn.addRequestProperty("Content-Length", String.valueOf(fileBytes.length));
//            conn.addRequestProperty("x-nos-entity-type", "json");
        });

        // NOS documents this legacy operation as a single-part, unencrypted PUT.
        return singlePartMd5ETagCheck(result, md5Hex);
    }
}
