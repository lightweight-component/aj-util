package com.ajaxjs.util.reflect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 针对 Methods 类中 getAllSuperClazz 方法的单元测试
 */
class TestMethods {
    // 定义测试用的类层次结构
    static class GrandParent {
    }

    static class Parent extends GrandParent {
    }

    static class Child extends Parent {
    }

    static class NoParent {
    }

    @Test
    void testGetAllSuperClazz_withMultiLevelInheritance() {
        // 测试多层继承：Child -> Parent -> GrandParent -> Object
        Class<?>[] result = Methods.getAllSuperClazz(Child.class);

        // 应该返回 [Parent, GrandParent]，不包含 Object 和自己
        assertEquals(2, result.length);
        assertEquals(Parent.class, result[0]);
        assertEquals(GrandParent.class, result[1]);
    }

    @Test
    void testGetAllSuperClazz_withSingleLevelInheritance() {
        // 测试单层继承：Parent -> GrandParent -> Object
        Class<?>[] result = Methods.getAllSuperClazz(Parent.class);

        // 应该返回 [GrandParent]
        assertEquals(1, result.length);
        assertEquals(GrandParent.class, result[0]);
    }

    @Test
    void testGetAllSuperClazz_withNoParent() {
        // 测试没有父类的情况（除了 Object）
        Class<?>[] result = Methods.getAllSuperClazz(NoParent.class);

        // 应该返回空数组
        assertEquals(0, result.length);
    }

    @Test
    void testGetAllSuperClazz_withObjectClass() {
        // 测试 Object 类本身
        Class<?>[] result = Methods.getAllSuperClazz(Object.class);

        // Object 类没有父类（除了 null），应该返回空数组
        assertEquals(0, result.length);
    }

    @Test
    void testGetAllSuperClazz_withInterface() {
        // 测试接口：接口没有父类（除了 Object，但这里被排除）
        Class<?>[] result = Methods.getAllSuperClazz(Runnable.class);

        // 接口没有 super class（接口继承自 Object，但被排除）
        assertEquals(0, result.length);
    }

    @Test
    void testGetAllSuperClazz_withStandardLibraryClass() {
        // 使用标准库类测试：ArrayList -> AbstractList -> AbstractCollection -> Object
        Class<?>[] result = Methods.getAllSuperClazz(java.util.ArrayList.class);

        // 验证返回的数组不为空且包含预期的父类
        assertTrue(result.length >= 2);
        // 第一个应该是 AbstractList
        assertEquals(java.util.AbstractList.class, result[0]);
    }
}