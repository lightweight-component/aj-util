package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.ajaxjs.util.StrUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestStrUtil {
    final static String str = "中国";

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    void testConcatUrl() {
        assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa/", "/bbb/sds"));
        assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa", "bbb/sds"));
        assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa/", "bbb/sds"));
        assertEquals("sdsd/aaa/bbb/sds", concatUrl("sdsd/aaa", "/bbb/sds"));
    }

    @Test
    void testLeftPad() {
        assertEquals("@@@@@12345", leftPad("12345", 10, "@"));
    }

    @Test
    void messageFormat() {
        String result = MessageFormat.format("您好{0}，晚上好！您目前余额：{1,number,#.##}元，积分：{2}", "张三", 10.155, 10);
        System.out.println(result);
    }

    @Test
    void testJoin() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        System.out.println(join(list, "&"));
    }

    @Test
    void testPrint() {
        System.out.println(print("{} {} {}", "a", "b", "c"));
    }
}
