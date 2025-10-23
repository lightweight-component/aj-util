package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestObjectHelper {

    // 用于测试的非序列化类
    private static class SerializableObject implements Serializable {
        private String data = "data";
    }

    // 用于测试的非序列化类
    private static class NonSerializableObject {
        private String data = "data";
    }

    /**
     * 对象深度克隆
     *
     * @param <T> 对象泛型参数
     * @param obj 待克隆的对象
     * @return 克隆后的对象
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T clone(T obj) {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bout)) {
            oos.writeObject(obj);

            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
                // 说明：调用 ByteArrayInputStream 或 ByteArrayOutputStream 对象的 close 方法没有任何意义
                // 这两个基于内存的流只要垃圾回收器清理对象就能够释放资源，这一点不同于对外部资源（如文件流）的释放
                return (T) ois.readObject();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("对象深度克隆 Error.", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("对象深度克隆 Error. 找不到类", e);
        }
    }

    @Test
    public void clone_SuccessfulClone_ReturnsClonedObject() {
        SerializableObject serializableObject = new SerializableObject();
        SerializableObject clone = clone(serializableObject);
        assertNotSame(clone, serializableObject);
    }

    @Test
    public void mapOf_ValidInputs_ReturnsMapWithOneEntry() {
        String key = "key";
        String value = "value";
        Map<String, String> map = ObjectHelper.mapOf(key, value);

        assertNotNull(map, "The map should not be null");
        assertEquals(1, map.size(), "The map should contain one entry");
        assertTrue(map.containsKey(key), "The map should contain the key");
        assertEquals(value, map.get(key), "The map should contain the correct value for the key");

        Map<String, Object> map2 = ObjectHelper.mapOf("key", "value", "key2", 2);
        assertNotNull(map2);

        Map<String, Object> map3 = ObjectHelper.mapOf("key", "value", "key2", 2, "key3", true);
        assertNotNull(map3);
    }

    @Test
    public void mapOf_NullKey_ReturnsMapWithNullKey() {
        String value = "value";
        Map<String, String> map = ObjectHelper.mapOf(null, value);

        assertNotNull(map, "The map should not be null");
        assertEquals(1, map.size(), "The map should contain one entry");
        assertTrue(map.containsKey(null), "The map should contain the null key");
        assertEquals(value, map.get(null), "The map should contain the correct value for the null key");
    }

}
