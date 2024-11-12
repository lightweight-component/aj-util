package com.ajaxjs.util.http_request;

import org.junit.Test;

import java.time.Duration;

public class TestGet {
    @Test
    public void testGet() {
        String result = Get.get("https://beta.bingolink.biz/iamapi/user/af38ddf7-dd53-4bad-bee9-0d81abefb817?access_token=bG9jYWw6RUxFQ2Y2aEZnY206R2VESE1SQmZtVQ", req -> {
            req.timeout(Duration.ofMillis(9000))// 设置读取数据超时 read timeout
                    .header("key1", "v1")
                    .header("key2", "v2");
        });
        System.out.println(result);


    }
}
