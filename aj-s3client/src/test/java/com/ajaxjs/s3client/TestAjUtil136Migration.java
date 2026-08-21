package com.ajaxjs.s3client;

import com.ajaxjs.s3client.factory.AliyunOSS;
import com.ajaxjs.s3client.factory.NeteaseOSS;
import com.ajaxjs.util.HashHelper;
import com.ajaxjs.util.httpremote.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for the ajaxjs-util 1.3.6 API migration.
 */
class TestAjUtil136Migration {
    @Test
    void sha256OfEmptyTextUsesCurrentHashHelper() {
        assertEquals(
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                BaseS3ClientSigV4.EMPTY_SHA256
        );
    }

    @Test
    void aliyunHmacKeepsSecretAndPayloadOrder() {
        Config config = config("OSS", "secret");
        AliyunOSS client = new AliyunOSS();
        client.setConfig(config);

        String expected = "OSS access:" + new HashHelper(HashHelper.HMAC_SHA1, "payload")
                .setKey("secret").hashAsBase64();

        assertEquals(expected, client.getAuthSignature("payload"));
    }

    @Test
    void neteaseHmacKeepsSecretAndPayloadOrder() {
        Config config = config("NOS", "secret");
        NeteaseOSS client = new NeteaseOSS();
        client.setConfig(config);

        assertEquals(
                "NOS access:" + HashHelper.getHmacSHA256("payload", "secret", false),
                client.getAuthSignature("payload")
        );
    }

    @Test
    void everySuccessfulHttpStatusIsAccepted() {
        Response response = new Response();
        response.setHttpCode(201);
        assertTrue(BaseS3Client.check(response));

        response.setHttpCode(299);
        assertTrue(BaseS3Client.check(response));

        response.setHttpCode(300);
        assertFalse(BaseS3Client.check(response));
        assertFalse(BaseS3Client.check(null));
    }

    private static Config config(String remark, String secret) {
        Config config = new Config();
        config.setRemark(remark);
        config.setAccessKey("access");
        config.setSecretKey(secret);

        return config;
    }
}
