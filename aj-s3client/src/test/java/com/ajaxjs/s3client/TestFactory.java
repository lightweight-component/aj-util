package com.ajaxjs.s3client;


import com.ajaxjs.s3client.factory.AliyunOSS;
import com.ajaxjs.s3client.factory.NeteaseOSS;
import com.ajaxjs.util.HashHelper;
import com.ajaxjs.util.httpremote.HttpConstant;
import com.ajaxjs.util.httpremote.Put;
import com.ajaxjs.util.httpremote.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@Disabled("Requires private provider credentials and live remote resources")
class TestFactory {
    Config nsoCfg = new Config();

    NeteaseOSS nso = new NeteaseOSS();

    Config aliCfg = new Config();
    AliyunOSS ali = new AliyunOSS();

    {
        Map<String, Object> cfg = TestBase.getConfigFromYml("application.yml");
        nsoCfg.setEndPoint((String) cfg.get("S3Storage_Nso_api"));
        nsoCfg.setAccessKey((String) cfg.get("S3Storage_Nso_accessKeyId"));
        nsoCfg.setSecretKey((String) cfg.get("S3Storage_Nso_accessSecret"));
        nsoCfg.setBucketName((String) cfg.get("S3Storage_Nso_bucket"));
        nsoCfg.setRemark("NOS");
        nso.setConfig(nsoCfg);

        aliCfg.setEndPoint((String) cfg.get("S3Storage_Oss_endpoint"));
        aliCfg.setAccessKey((String) cfg.get("S3Storage_Oss_accessKeyId"));
        aliCfg.setSecretKey((String) cfg.get("S3Storage_Oss_secretAccessKey"));
        aliCfg.setBucketName((String) cfg.get("S3Storage_Oss_bucket"));
        aliCfg.setRemark("OSS");

        ali.setConfig(aliCfg);
    }


    File file = new File(("D:\\code\\aj\\aj-business\\aj-base\\src\\test\\resources\\img.png"));
    byte[] content = readBytes(file);

    @Test
    void testNso() {
//        assertTrue(nso.createBucket("test6767ffg"));
//        assertTrue(nso.deleteBucket("test6767ffg"));
        nso.listBucket();
//        nso.listBucketXml();
//        assertTrue(nso.putObject("test2.png", content));
//        nso.getObject("test2.png");
//        nso.deleteObject("test2.png");
//        nso.createEmptyFile("test.txt");
    }

    @Test
    void testAli() {
//        assertTrue(ali.createBucket("test6765ffg"));
//        assertTrue(ali.deleteBucket("test6767ffg"));
        ali.listBucket();
//        ali.listBucketXml();
//        assertTrue(ali.putObject("test.png", content));
//        ali.getObject("test.png");
//        ali.deleteObject( "test.png");

    }

    static void upload_SigV2() {
        String accessKey = "6d3ae3ce9ce81caf42f093a31592e3da";
        String secretKey = "d388a9fa87fe69990601ffb498c486442657747fc0f00f5ec1f38ffb1df468f3";
        String method = "PUT";

        File file = new File(("D:\\code\\aj\\aj-business\\aj-base\\src\\test\\resources\\img.png"));
        byte[] content = readBytes(file);
        String contentMD5 = HashHelper.calcFileMD5(content);

        String contentType = "application/octet-stream";
        String date = "Wed, 28 Oct 2021 15:00:00 GMT";
        String resource = "/your-bucket-name/your-object-key";

        String stringToSign = method + "\n" + contentMD5 + "\n" + contentType + "\n" + date + "\n" + resource;

        String url = "https://a4d2252636e737ac1ced6ec8f0c9c68e.r2.cloudflarestorage.com";

        String signature = new HashHelper(HashHelper.HMAC_SHA1, stringToSign)
                .setKey(secretKey).hashAsBase64();
        String authorizationHeader = "AWS " + accessKey + ":" + signature;

        Response result = new Put(url, content, HttpConstant.FILE_TYPE, conn -> {  // 执行 PUT 请求上传文件
            conn.setRequestProperty("Date", date); // 设置请求头 Date
            conn.setRequestProperty("Authorization", authorizationHeader); // 设置请求头 Authorization
        }).getResp();

        System.out.println(result);
    }

    private static byte[] readBytes(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read test file: " + file, e);
        }
    }
}
