package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.ajaxjs.util.MapTool.join;
import static org.junit.jupiter.api.Assertions.*;

class TestMapTool {
    /**
     * 判断 Map 非空，然后根据 key 获取 value，若 value 非空则作为参数传入函数接口
     *
     * @param map 输入的 Map
     * @param key map的键
     * @param s   如果过非空，那么接着要做什么？在这个回调函数中处理。传入的参数就是 map.get(key)的值
     * @param <T> 返回 value 的类型
     */
    public static <T> void getValue(Map<String, T> map, String key, Consumer<T> s) {
        if (map != null) {
            T value = map.get(key);

            if (value != null)
                s.accept(value);
        }
    }

    /**
     * 万能 Map 转换器，为了泛型的转换而设的一个方法，怎么转换在 fn 中处理
     *
     * @param map 原始 Map，key 必须为 String 类型
     * @param fn  转换函数
     * @param <K> Key 的类型
     * @param <T> 返回 value 的类型
     * @return 转换后的 map
     */
    public static <T, K> Map<String, T> as(Map<String, K> map, Function<K, T> fn) {
        Map<String, T> _map = new HashMap<>();
        map.forEach((k, v) -> _map.put(k, v == null ? null : fn.apply(v)));

        return _map;
    }

    /**
     * 将给定的 map 转换为 Map&lt;String, Object&gt; 类型的结果
     *
     * @param map 要转换的 map，包含 String 和 String[] 类型的键值对
     * @return 转换后的 Map&lt;String, Object&gt; 类型的结果
     */
    public static Map<String, Object> as(Map<String, String[]> map) {
        return as(map, arr -> ConvertBasicValue.toJavaValue(arr[0]));
    }
    final Map<String, Object> map = new HashMap<String, Object>() {
        private static final long serialVersionUID = 1L;

        {
            put("foo", null);
            put("bar", 500);
            put("zx", "hi");
        }
    };

    @Test
    void testJoin() {
        assertEquals("bar=500&foo=null&zx=hi", join(as(map, Object::toString)));
    }

    @Test
    void testToMap() {
        assertEquals(1, Objects.requireNonNull(MapTool.toMap(new String[]{"a", "b", "d"}, new String[]{"1", "c", "2"}, ConvertBasicValue::toJavaValue)).get("a"));
        assertEquals(1, Objects.requireNonNull(MapTool.toMap(new String[]{"a=1", "b=2", "d=c"}, ConvertBasicValue::toJavaValue)).get("a"));
        assertEquals("header.payload=signature", Objects.requireNonNull(
                MapTool.toMap(new String[]{"token=header.payload=signature"}, null)).get("token"));
//        assertEquals("你好", Objects.requireNonNull(MapTool.toMap(new String[]{"a=%e4%bd%a0%e5%a5%bd", "b=2", "d=c"}, EncodeTools::urlEncode)).get("a"));
    }

    @Test
    void testAsString() {
        assertEquals("500", as(map, Object::toString).get("bar"));
        assertEquals("[1, c, 2]", as(new HashMap<String, String[]>() {
            private static final long serialVersionUID = 1L;

            {
                put("foo", new String[]{"a", "b"});
                put("bar", new String[]{"1", "c", "2"});
            }
        }, Arrays::toString).get("bar"));
    }

    @Test
    void testAsObject() {
        assertEquals(500, as(new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("foo", "null");
                put("bar", "500");
                put("zx", "hi");
            }
        }, v -> ConvertBasicValue.toJavaValue(v.toString())).get("bar"));
    }

    public static class MapMock {
        static final boolean s = true;
        public final static Map<String, Object> user = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("id", 1L);
                put("name", "Jack");
                put("sex", s);
                put("age", 30);
                put("children", new String[]{"Tom", "Peter"});
                put("luckyNumbers", new int[]{2, 8, 6});
            }
        };
    }

    @Test
    void testMap2Bean() {
        TestCaseUserBean user = JsonUtil.map2pojo(TestXmlHelper.userWithoutChild, TestCaseUserBean.class);// 直接转
        assertNotNull(user);
        assertEquals(user.getName(), "Jack");
        assertEquals("directField22", user.directField);

        user = JsonUtil.map2pojo(MapMock.user, TestCaseUserBean.class);

        assertNotNull(user);
        assertEquals("Tom", user.getChildren()[0]);
        assertEquals(8, user.getLuckyNumbers()[1]);
        assertTrue(user.isSex());

    }

//	@Test
//	public void testBean2Json() {
//		TestCaseUserBean user = map2Bean(MapMock.user, TestCaseUserBean.class, true);
//		String json = beanToJson(user);
//		assertNotNull(json);
//
//		user = json2bean(json, TestCaseUserBean.class);
//		assertEquals("Jack", user.getName());
//		assertEquals(2, user.getLuckyNumbers()[0]);
//		assertNotNull(user);
//	}


    @Test
    void testFlatten() {
        // 构造一个示例嵌套 Map
        Map<String, Object> level3Map = new HashMap<>();
        level3Map.put("c1", "value_c1");
        level3Map.put("c2", 42);

        Map<String, Object> level2Map = new HashMap<>();
        level2Map.put("b1", "value_b1");
        level2Map.put("b2", level3Map); // b2 包含另一个 Map

        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("a1", "value_a1");
        rootMap.put("a2", level2Map); // a2 包含嵌套 Map
        rootMap.put("a3", null); // 测试 null 值
        rootMap.put("a4", ObjectHelper.listOf(1, 2, 3)); // 测试非 Map 值

        // 扁平化
        Map<String, Object> flattenedMap = MapTool.flatten(rootMap);

        Map<String, Object> expected = new HashMap<>();
        expected.put("a1", "value_a1");
        expected.put("a2.b1", "value_b1");
        expected.put("a2.b2.c1", "value_c1");
        expected.put("a2.b2.c2", 42);
        expected.put("a3", null);
        expected.put("a4", ObjectHelper.listOf(1, 2, 3));
        assertEquals(expected, flattenedMap);
    }
}
