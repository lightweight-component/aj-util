package com.ajaxjs.util.reflect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 针对 Clazz.getAllSuperClass 方法的单元测试
 */
class TestClazzGetAllSuperClass {
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
        assertArrayEquals(
                new Class<?>[]{Parent.class, GrandParent.class},
                Clazz.getAllSuperClass(Child.class)
        );
    }

    @Test
    void testGetAllSuperClazz_withSingleLevelInheritance() {
        // 测试单层继承：Parent -> GrandParent -> Object
        assertArrayEquals(
                new Class<?>[]{GrandParent.class},
                Clazz.getAllSuperClass(Parent.class)
        );
    }

    @Test
    void testGetAllSuperClazz_withNoParent() {
        // 测试没有父类的情况（除了 Object）
        assertArrayEquals(new Class<?>[0], Clazz.getAllSuperClass(NoParent.class));
    }

    @Test
    void testGetAllSuperClazz_withObjectClass() {
        // 测试 Object 类本身
        assertArrayEquals(new Class<?>[0], Clazz.getAllSuperClass(Object.class));
    }

    @Test
    void testGetAllSuperClazz_withInterface() {
        // 测试接口：接口没有父类（除了 Object，但这里被排除）
        assertArrayEquals(new Class<?>[0], Clazz.getAllSuperClass(Runnable.class));
    }

    @Test
    void testGetAllSuperClazz_withStandardLibraryClass() {
        // 使用标准库类测试：ArrayList -> AbstractList -> AbstractCollection -> Object
        assertArrayEquals(
                new Class<?>[]{java.util.AbstractList.class, java.util.AbstractCollection.class},
                Clazz.getAllSuperClass(java.util.ArrayList.class)
        );
    }
}
