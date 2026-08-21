package com.ajaxjs.s3client.factory;


import com.ajaxjs.s3client.BaseS3ClientSigV2;
import com.ajaxjs.util.HashHelper;
import com.ajaxjs.util.date.DateTools;
import com.ajaxjs.util.httpremote.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Alibaba Cloud OSS client using its S3-compatible Signature Version 2 protocol.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AliyunOSS extends BaseS3ClientSigV2 {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthSignature(String data) {
        return getAuthSignature((key, value) -> new HashHelper(HashHelper.HMAC_SHA1, value)
                .setKey(key).hashAsBase64(), data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean putObject(String bucketName, String objectName, byte[] fileBytes) {
        validateConfiguration(false);

        if (fileBytes == null)
            throw new IllegalArgumentException("Object content is required.");

        String now = DateTools.nowGMTDate();
        String data = "PUT\n" + getCanonicalResource(now, bucketName, objectName);
        String url = getObjectUrl(bucketName, objectName);

        Response result = putBinary(url, fileBytes, setRequestHead(now, data));

        return eTagCheck(result, null);
    }
}
