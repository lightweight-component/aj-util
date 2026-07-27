package com.ajaxjs.util.reflect;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

public class TestMethods {
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
        assertNotNull(new Methods(new Foo2()).findDeclaredMethod("m1"));// 按实际对象

        Methods rm = new Methods(Foo2.class);
        assertNotNull(rm.findDeclaredMethod("m1"));// 按类引用
        assertNotNull(rm.findDeclaredMethod("m1", String.class)); // 按参数类型
        assertNotNull(rm.findDeclaredMethod("m1", "foo"));// 按实际参数
        assertNotNull(rm.findDeclaredMethod("m1"));
        assertNotNull(rm.findDeclaredMethod("m1", String.class));
        assertNull(rm.findDeclaredMethod("m2"));
    }

    static class Foo1 {
        public void foo(Foo1 a) {

        }
    }

    static class Bar2 extends Foo1 {

    }

    @Test
    public void testGetMethodByUpCastingSearch() {
        assertNull(new Methods(Foo1.class).findDeclaredMethod("foo", new Bar2())); // 找不到
        assertNotNull(new Methods(Foo1.class).getMethodByArgumentUpCastingSearch("foo", new Bar2())); // 找到了
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

    public interface Parent {
    }

    public interface Child extends Parent {
    }

    public static class D implements C {
    }

    public static class ChildImpl implements Child {
    }

    public static class InterfaceTarget {
        public String inheritedInterface(Parent value) {
            return "parent";
        }
    }

    @Test
    public void testDeclaredMethod() {
        assertNotNull(new Methods(A.class).getMethodByArgumentUpCastingSearch("bar", new D()));
        assertNotNull(new Methods(A.class).getMethodByArgumentInterface("bar", new D()));// 找到了
        assertNull(new Methods(C.class).findDeclaredMethod("missing", Object.class));
        assertNull(new Methods(C.class).findDeclaredMethod("missing"));
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
        Methods rm = new Methods();
        assertNull(Methods.execute(new Foo3(), "m1"));
        assertNotNull(Methods.execute(new Foo3(), "m1", new Object[]{"foo"}));
        assertNull(Methods.execute(new Foo3(), "nullable"));
        assertThrows(IllegalArgumentException.class, () -> Methods.execute(new Bar2(), "m1"));
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> Methods.execute(new Foo3(), "fail"));
        assertEquals("boom", error.getMessage());
        assertEquals("bar", Methods.execute(new Bar3(), "m1", new Object[]{"bar"}));
        assertEquals("foo", Methods.execute(new Bar3(), "m1", new Class[]{String.class}, new Object[]{"foo"}));
    }

    @Test
    public void testGetUnderLayerErrWithoutCause() {
        InvocationTargetException wrapper = new InvocationTargetException(null);

        assertSame(wrapper, Fields.getUnderLayerErr(wrapper));
        assertThrows(IllegalArgumentException.class, () -> Fields.getUnderLayerErr(null));
    }
}
