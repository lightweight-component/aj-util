package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import static com.ajaxjs.util.UrlCodec.concatUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestUrlCodec {
    @Test
    void testEncode_Form_NormalCase() {
        assertEquals("Hello+World%21+%E4%BD%A0%E5%A5%BD", new UrlCodec("Hello World! 你好").encodeForm());
    }

    // 测试 encodeQuery()
    @Test
    void testEncodeFormQuery_ReplacesSpacesWithPercent20() {
        assertEquals("hello%20world", new UrlCodec("hello world").encodeQueryValue());
    }

    // 测试 decode()
    @Test
    void testDecode_Form_NormalCase() {
        assertEquals("Hello World! 你好", new UrlCodec("Hello+World%21+%E4%BD%A0%E5%A5%BD").decodeForm());
    }

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    void testConcatUrl() {
        assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa/", "/bbb/sds"));
        assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa", "bbb/sds"));
        assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa/", "bbb/sds"));
        assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa", "/bbb/sds"));
    }
}
