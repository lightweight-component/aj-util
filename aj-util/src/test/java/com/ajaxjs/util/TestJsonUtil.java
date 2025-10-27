package com.ajaxjs.util;


import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestJsonUtil {
    private String validJson;
    private String invalidJson;
    private String mismatchedJson;

    @Data
    class User {
        private String name;
        private int age;

    }

    @BeforeEach
    void setUp() {
        validJson = "{\"name\":\"John\", \"age\":30}";
        invalidJson = "{\"name\":\"John\", \"age\":30"; // 缺少右大括号
        mismatchedJson = "{\"name\":123, \"age\":\"thirty\"}"; // 键值类型不匹配
    }

    @Test
    void fromJson_ValidJson_ReturnsObject() {
        User user = JsonUtil.fromJson(validJson, User.class);
        assertNotNull(user, "The returned object should not be null.");
        assertEquals("John", user.getName(), "The name should match.");
        assertEquals(30, user.getAge(), "The age should match.");
    }

    @Test
    void fromJson_InvalidJson_ThrowsException() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            JsonUtil.fromJson(invalidJson, User.class);
        });

        assertTrue(exception.getMessage().contains("Failed to convert JSON to object"),
                "Exception message should indicate JSON conversion failure.");
    }

    @Test
    void fromJson_MismatchedJson_ThrowsException() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            JsonUtil.fromJson(mismatchedJson, User.class);
        });

        assertTrue(exception.getMessage().contains("Failed to convert JSON to object"),
                "Exception message should indicate JSON conversion failure.");
    }
}
