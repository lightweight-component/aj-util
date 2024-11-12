package com.ajaxjs.util.http_request;

import org.junit.Test;

public class TestPost {
    @Test
    public void testPost() {
        String result = Post.postForm("https://qq.com", "phone=13113777337&pwd=1234567890");
        System.out.println(result);
    }
}
