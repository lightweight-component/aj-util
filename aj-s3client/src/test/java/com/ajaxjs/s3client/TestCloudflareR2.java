package com.ajaxjs.s3client;

import com.ajaxjs.s3client.factory.CloudflareR2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Requires private Cloudflare R2 credentials and live remote resources")
class TestCloudflareR2 {
    CloudflareR2 client = new CloudflareR2();

    {
        Map<String, Object> _cfg = TestBase.getConfigFromYml("application.yml");

        Config cfg = new Config();
        cfg.setEndPoint((String) _cfg.get("S3Storage_R2_endpoint"));
        cfg.setAccessKey((String) _cfg.get("S3Storage_R2_accessKeyId"));
        cfg.setSecretKey((String) _cfg.get("S3Storage_R2_secretAccessKey"));
        cfg.setBucketName((String) _cfg.get("S3Storage_R2_bucket"));

        client.setConfig(cfg);
    }

    @Test
    void testListBucket() {
        String s = client.listBucket();
        assertNotNull(s);
        Map<String, String> stringStringMap = client.listBucketXml();
        System.out.println(stringStringMap);
    }

    @Test
    void testBucket() {
        assertTrue(client.createBucket("test"));
        assertTrue(client.deleteBucket("test"));
    }

    @Test
    void testPutObject() throws java.io.IOException {
        byte[] content = Files.readAllBytes(java.nio.file.Paths.get("D:\\code\\aj\\aj-business\\aj-base\\src\\test\\resources\\img.png"));

        assertTrue(client.putObject("ajaxjs", "s22.png", content));
    }
}
