package com.ajaxjs.util.reflect;


import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

public class TestReflectMethod {
    public static class Foo {
        public Foo() {
        }

        public Foo(String str, String str2) {
        }

        public void Bar() {

        }

        public void CC(String cc) {

        }

        public String Bar2() {
            return "bar2";
        }

        public String Bar3(String arg) {
            return arg;
        }
    }

    static class Foo2 {
        public void m1() {
        }

        public void m1(String arg) {
        }
    }

    static class Bar extends Foo {
        public void m2() {
        }
    }

    @Test
    public void testGetMethod() {
        assertNotNull(new ReflectMethod(new Foo2()).getMethod("m1"));// 按实际对象

        ReflectMethod rm = new ReflectMethod(Foo2.class);
        assertNotNull(rm.getMethod("m1"));// 按类引用
        assertNotNull(rm.getMethod("m1", String.class)); // 按参数类型
        assertNotNull(rm.getMethod("m1", "foo"));// 按实际参数
        assertNotNull(rm.getMethod("m1"));
        assertNotNull(rm.getMethod("m1", String.class));
        assertNull(rm.getMethod("m2"));
    }

    static class Foo1 {
        public void foo(Foo1 a) {

        }
    }

    static class Bar2 extends Foo1 {

    }

    @Test
    public void testGetMethodByUpCastingSearch() {
        assertNull(new ReflectMethod(Foo1.class).getMethod("foo", new Bar2())); // 找不到
        assertNotNull(new ReflectMethod(Foo1.class).getMethodByArgumentUpCastingSearch("foo", new Bar2())); // 找到了
    }

    public static class A {
        public String foo(A a) {
            return "A.foo";
        }

        public String bar(C c) {
            return "A.bar";
        }
    }

    public static class B extends A {
    }

    public interface C {
    }

    public static class D implements C {
    }

    @Test
    public void testDeclaredMethod() {
        assertNull(new ReflectMethod(A.class).getMethodByArgumentUpCastingSearch("bar", new D())); // 找不到
        assertNotNull(new ReflectMethod(A.class).getDMethodByArgumentInterface("bar", new D()));// 找到了
        assertNull(new ReflectMethod(C.class).getSuperClassDeclaredMethod("missing", Object.class));
        assertNull(new ReflectMethod(C.class).getSuperClassDeclaredMethod("missing"));
    }

    public static class Foo3 {
        public void m1() {
        }

        public String m1(String arg) {
            return arg;
        }

        public String nullable() {
            return null;
        }

        public void fail() {
            throw new IllegalStateException("boom");
        }
    }

    static class Bar3 extends Foo3 {
        public void m2() {
        }
    }

    @Test
    public void testExecuteMethod() throws Throwable {
        ReflectMethod rm = new ReflectMethod();
        assertNull(rm.execute(new Foo3(), "m1"));
        assertNotNull(rm.execute(new Foo3(), "m1", new Object[]{"foo"}));
        assertNull(rm.execute(new Foo3(), "nullable"));
        assertThrows(IllegalArgumentException.class, () -> rm.execute(new Bar2(), "m1"));
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> rm.execute(new Foo3(), "fail"));
        assertEquals("boom", error.getMessage());
        assertEquals("bar", rm.execute(new Bar3(), "m1", new Object[]{"bar"}));
        assertEquals("foo", rm.execute(new Bar3(), "m1", new Class[]{String.class}, new Object[]{"foo"}));
    }

    @Test
    public void testGetUnderLayerErrWithoutCause() {
        InvocationTargetException wrapper = new InvocationTargetException(null);

        assertSame(wrapper, Fields.getUnderLayerErr(wrapper));
        assertThrows(IllegalArgumentException.class, () -> Fields.getUnderLayerErr(null));
    }
}
