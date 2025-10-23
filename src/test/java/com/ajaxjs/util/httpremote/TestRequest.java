package com.ajaxjs.util.httpremote;

import org.junit.jupiter.api.Test;

public class TestRequest {
    @Test
    void testGet() {
        String result = Get.text("https://www.qq.com");
        System.out.println(result);
    }
}
