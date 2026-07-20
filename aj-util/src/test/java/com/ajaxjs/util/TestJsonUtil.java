package com.ajaxjs.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonUtil 单元测试类
 * 覆盖所有 JSON 工具方法
 */
class TestJsonUtil {
    private String validJson;
    private String invalidJson;
    private String mismatchedJson;
    private String jsonArrayStr;
    private User testUser;

    @Data
    static class User {
        private String name;
        private int age;
    }

    @Data
    static class Person {
        private String firstName;
        private String lastName;
        private Integer age;
    }

    @Data
    static class MyPojoWithDate {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private Date createTime;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private Date updateTime;
    }

    @BeforeEach
    void setUp() {
        validJson = "{\"name\":\"John\", \"age\":30}";
        invalidJson = "{\"name\":\"John\", \"age\":30"; // 缺少右大括号
        mismatchedJson = "{\"name\":123, \"age\":\"thirty\"}"; // 键值类型不匹配
        jsonArrayStr = "[{\"name\":\"Alice\", \"age\":25}, {\"name\":\"Bob\", \"age\":35}]";
        testUser = new User();
        testUser.setName("TestUser");
        testUser.setAge(20);
    }

    // ==================== toJson 测试 ====================

    @Test
    void toJson_ValidObject_ReturnsJsonString() {
        User user = new User();
        user.setName("Alice");
        user.setAge(25);

        String json = JsonUtil.toJson(user);

        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"Alice\""));
        assertTrue(json.contains("\"age\":25"));
    }

    @Test
    void toJson_NullObject_ReturnsNullString() {
        String json = JsonUtil.toJson(null);
        assertEquals("null", json);
    }

    // ==================== toJsonPretty 测试 ====================

    @Test
    void toJsonPretty_ValidObject_ReturnsPrettyJsonString() {
        User user = new User();
        user.setName("Alice");
        user.setAge(25);

        String prettyJson = JsonUtil.toJsonPretty(user);

        assertNotNull(prettyJson);
        assertTrue(prettyJson.contains("\"name\" : \"Alice\"") || prettyJson.contains("\"name\":\"Alice\""));
        assertTrue(prettyJson.contains("\n")); // 美化后的 JSON 应该包含换行符
    }

    // ==================== fromJson 测试 ====================

    @Test
    void fromJson_ValidJson_ReturnsObject() {
        User user = JsonUtil.fromJson(validJson, User.class);
        assertNotNull(user, "The returned object should not be null.");
        assertEquals("John", user.getName(), "The name should match.");
        assertEquals(30, user.getAge(), "The age should match.");
    }

    @Test
    void fromJson_InvalidJson_ThrowsException() {
        Exception exception = assertThrows(RuntimeException.class, () -> JsonUtil.fromJson(invalidJson, User.class));

        assertTrue(exception.getMessage().contains("Failed to convert JSON to object"),
                "Exception message should indicate JSON conversion failure.");
    }

    @Test
    void fromJson_MismatchedJson_ThrowsException() {
        Exception exception = assertThrows(RuntimeException.class, () -> JsonUtil.fromJson(mismatchedJson, User.class));

        assertTrue(exception.getMessage().contains("Failed to convert JSON to object"),
                "Exception message should indicate JSON conversion failure.");
    }

    @Test
    void fromJson_ComplexObject_ReturnsCorrectObject() {
        String complexJson = "{\"firstName\":\"John\", \"lastName\":\"Doe\", \"age\":30}";
        Person person = JsonUtil.fromJson(complexJson, Person.class);

        assertNotNull(person);
        assertEquals("John", person.getFirstName());
        assertEquals("Doe", person.getLastName());
        assertEquals(30, person.getAge());
    }

    // ==================== json2map (带 Class 参数) 测试 ====================

    @Test
    void json2map_WithClass_ReturnsTypedMap() {
        String json = "{\"user1\":{\"name\":\"Alice\", \"age\":25}, \"user2\":{\"name\":\"Bob\", \"age\":35}}";
        Map<String, User> map = JsonUtil.json2map(json, User.class);

        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("Alice", map.get("user1").getName());
        assertEquals(25, map.get("user1").getAge());
        assertEquals("Bob", map.get("user2").getName());
    }

    // ==================== json2sortMap 测试 ====================

    @Test
    void json2sortMap_ValidJson_ReturnsLinkedHashMap() {
        String json = "{\"zKey\":{\"name\":\"Z\", \"age\":1}, \"aKey\":{\"name\":\"A\", \"age\":2}, \"mKey\":{\"name\":\"M\", \"age\":3}}";
        LinkedHashMap<String, User> map = JsonUtil.json2sortMap(json, User.class);

        assertNotNull(map);
        assertEquals(3, map.size());
        // LinkedHashMap 保持插入顺序（Jackson 默认按字母排序键）
        Iterator<String> keys = map.keySet().iterator();
        assertEquals("aKey", keys.next());
        assertEquals("mKey", keys.next());
        assertEquals("zKey", keys.next());
    }

    // ==================== json2map (无 Class 参数) 测试 ====================

    @Test
    void json2map_WithoutClass_ReturnsObjectMap() {
        String json = "{\"name\":\"Test\", \"age\":30, \"active\":true}";
        Map<String, Object> map = JsonUtil.json2map(json);

        assertNotNull(map);
        assertEquals("Test", map.get("name"));
        assertEquals(30, map.get("age"));
        assertEquals(true, map.get("active"));
    }

    // ==================== json2StrMap 测试 ====================

    @Test
    void json2StrMap_ValidJson_ReturnsStringMap() {
        String json = "{\"key1\":\"value1\", \"key2\":\"value2\"}";
        Map<String, String> map = JsonUtil.json2StrMap(json);

        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    // ==================== json2list 测试 ====================

    @Test
    void json2list_ValidJsonArray_ReturnsList() {
        List<User> users = JsonUtil.json2list(jsonArrayStr, User.class);

        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("Alice", users.get(0).getName());
        assertEquals(25, users.get(0).getAge());
        assertEquals("Bob", users.get(1).getName());
        assertEquals(35, users.get(1).getAge());
    }

    @Test
    void json2list_EmptyArray_ReturnsEmptyList() {
        String emptyArray = "[]";
        List<User> users = JsonUtil.json2list(emptyArray, User.class);

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    // ==================== json2mapList 测试 ====================

    @Test
    void json2mapList_ValidJsonArray_ReturnsMapList() {
        List<Map<String, Object>> list = JsonUtil.json2mapList(jsonArrayStr);

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("Alice", list.get(0).get("name"));
        assertEquals(25, list.get(0).get("age"));
    }

    // ==================== convertValue 测试 ====================

    @Test
    void convertValue_SameType_ReturnsConvertedObject() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("age", 25);

        User user = JsonUtil.convertValue(map, User.class);

        assertNotNull(user);
        assertEquals("Alice", user.getName());
        assertEquals(25, user.getAge());
    }

    @Test
    void convertValue_DifferentType_ReturnsConvertedObject() {
        User user = new User();
        user.setName("John");
        user.setAge(30);

        Person person = JsonUtil.convertValue(user, Person.class);

        assertNotNull(person);
        assertEquals("John", person.getFirstName());
        assertEquals(30, person.getAge());
    }

    // ==================== pojo2map (无 Class 参数) 测试 ====================

    @Test
    void pojo2map_WithoutClass_ReturnsObjectMap() {
        Map<String, Object> map = JsonUtil.pojo2map(testUser);

        assertNotNull(map);
        assertEquals("TestUser", map.get("name"));
        assertEquals(20, map.get("age"));
    }

    // ==================== pojo2map (带 Class 参数) 测试 ====================

    @Test
    void pojo2map_WithClass_ReturnsTypedMap() {
        Map<String, String> map = JsonUtil.pojo2map(testUser, String.class);

        assertNotNull(map);
        assertEquals("TestUser", map.get("name"));
        assertEquals("20", map.get("age")); // int 转为 String
    }

    // ==================== map2pojo 测试 ====================

    @Test
    void map2pojo_ValidMap_ReturnsPojo() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("age", 25);

        User user = JsonUtil.map2pojo(map, User.class);

        assertNotNull(user);
        assertEquals("Alice", user.getName());
        assertEquals(25, user.getAge());
    }

    @Test
    void map2pojo_EmptyMap_ReturnsEmptyPojo() {
        Map<String, Object> map = new HashMap<>();

        User user = JsonUtil.map2pojo(map, User.class);

        assertNotNull(user);
        assertNull(user.getName());
        assertEquals(0, user.getAge());
    }

    // ==================== json2Node 测试 ====================

    @Test
    void json2Node_ValidJson_ReturnsJsonNode() {
        Object node = JsonUtil.json2Node(validJson);

        assertNotNull(node);
        // Jackson 返回的是 JsonNode 类型
        assertTrue(node instanceof com.fasterxml.jackson.databind.JsonNode);
    }

    @Test
    void json2Node_JsonArray_ReturnsArrayNode() {
        Object node = JsonUtil.json2Node(jsonArrayStr);

        assertNotNull(node);
        assertTrue(node instanceof com.fasterxml.jackson.databind.JsonNode);
    }

    // ==================== setEngine/getEngine 测试 ====================

    @Test
    void setEngine_NullEngine_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            JsonUtil.setEngine(null);
        });

        assertEquals("JsonEngine cannot be null.", exception.getMessage());
    }

    @Test
    void getEngine_CalledMultipleTimes_ReturnsSameEngine() {
        // 第一次调用会初始化引擎
        Object engine1 = JsonUtil.getEngine();
        Object engine2 = JsonUtil.getEngine();

        assertNotNull(engine1);
        assertSame(engine1, engine2);
    }

    // ==================== 日期测试 ====================

    @Test
    void testDate() {
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("name", "Test Device");
        sourceMap.put("createTime", LocalDateTime.of(2023, 10, 27, 10, 30, 0));

        MyPojoWithDate pojo = JsonUtil.map2pojo(sourceMap, MyPojoWithDate.class);
        System.out.println("Converted POJO: " + pojo);
        assertNotNull(pojo);
    }

    // ==================== 边界情况测试 ====================

    @Test
    void toJson_EmptyObject_ReturnsEmptyJson() {
        Object emptyObj = new Object();
        String json = JsonUtil.toJson(emptyObj);
        assertNotNull(json);
    }

    @Test
    void fromJson_EmptyJsonObject_ReturnsEmptyObject() {
        String emptyJson = "{}";
        User user = JsonUtil.fromJson(emptyJson, User.class);
        assertNotNull(user);
        assertNull(user.getName());
        assertEquals(0, user.getAge());
    }

    @Test
    void json2list_NestedObjects_ReturnsCorrectList() {
        String nestedJson = "[{\"name\":\"Parent1\", \"age\":40, \"child\":{\"name\":\"Child1\", \"age\":10}}]";
        // 这里使用 Map 类型来接收嵌套对象
        List<Map<String, Object>> list = JsonUtil.json2mapList(nestedJson);
        assertNotNull(list);
        assertEquals(1, list.size());
    }
}