package com.ajaxjs.util.reflect;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class TestMethods {
    public static class DeclaredParent {
        private String hidden(String value) {
            return "hidden:" + value;
        }
    }

    public static class DeclaredTarget extends DeclaredParent {
        public String overloaded() {
            return "none";
        }

        public String overloaded(String value) {
            return "string:" + value;
        }
    }

    @Test
    void resolvesInputClassFromClassOrObject() {
        assertSame(DeclaredTarget.class, new Methods(DeclaredTarget.class).getInputClass());
        assertSame(DeclaredTarget.class, new Methods(new DeclaredTarget()).getInputClass());

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> new Methods((Object) null).getInputClass());
        assertEquals("No input argument of class or object.", error.getMessage());
    }

    @Test
    void findsExactDeclaredOverloads() {
        Methods methods = new Methods(DeclaredTarget.class);

        Method noArgs = methods.findDeclaredMethod("overloaded");
        assertNotNull(noArgs);
        assertEquals("overloaded", noArgs.getName());
        assertSame(DeclaredTarget.class, noArgs.getDeclaringClass());
        assertArrayEquals(new Class<?>[0], noArgs.getParameterTypes());
        assertSame(String.class, noArgs.getReturnType());

        Method withStringType = methods.findDeclaredMethod("overloaded", String.class);
        assertNotNull(withStringType);
        assertArrayEquals(new Class<?>[]{String.class}, withStringType.getParameterTypes());

        Method withStringValue = methods.findDeclaredMethod("overloaded", "value");
        assertEquals(withStringType, withStringValue);
        assertNull(methods.findDeclaredMethod("overloaded", Integer.class));
        assertNull(methods.findDeclaredMethod("missing"));
    }

    @Test
    void findsAndMakesInheritedPrivateMethodAccessible() throws Throwable {
        Methods methods = new Methods(DeclaredTarget.class);
        Method method = methods.findDeclaredMethod("hidden", String.class);

        assertNotNull(method);
        assertSame(DeclaredParent.class, method.getDeclaringClass());
        assertTrue(Modifier.isPrivate(method.getModifiers()));
        assertTrue(method.isAccessible());
        assertEquals("hidden:value", Methods.execute(new DeclaredTarget(), method, new Object[]{"value"}));
    }

    public interface Parent {
    }

    public interface Child extends Parent {
    }

    public static class ChildImpl implements Child {
    }

    public static class CompatibleTarget {
        public String choose(Object value) {
            return "object";
        }

        public String choose(CharSequence value) {
            return "sequence";
        }

        public String choose(String value) {
            return "string";
        }

        public String accept(Parent value) {
            return "parent";
        }

        public String primitive(int value) {
            return "int:" + value;
        }

        public String nullable(CharSequence value) {
            return value == null ? "null" : value.toString();
        }
    }

    @Test
    void choosesClosestCompatiblePublicMethod() {
        Methods methods = new Methods(CompatibleTarget.class);

        Method exact = methods.findCompatibleMethod("choose", "text");
        assertNotNull(exact);
        assertArrayEquals(new Class<?>[]{String.class}, exact.getParameterTypes());

        Method interfaceMatch = methods.findCompatibleMethod("choose", new StringBuilder("text"));
        assertNotNull(interfaceMatch);
        assertArrayEquals(new Class<?>[]{CharSequence.class}, interfaceMatch.getParameterTypes());

        Method primitiveMatch = methods.findCompatibleMethod("primitive", Integer.valueOf(7));
        assertNotNull(primitiveMatch);
        assertArrayEquals(new Class<?>[]{int.class}, primitiveMatch.getParameterTypes());

        Method objectMatch = methods.findCompatibleMethod("choose", Integer.valueOf(7));
        assertNotNull(objectMatch);
        assertArrayEquals(new Class<?>[]{Object.class}, objectMatch.getParameterTypes());

        assertNull(methods.findCompatibleMethod("primitive", "7"));
        assertNull(methods.findCompatibleMethod("missing", "text"));

        Method nullable = methods.findCompatibleMethod("nullable", new Object[]{null});
        assertNotNull(nullable);
        assertArrayEquals(new Class<?>[]{CharSequence.class}, nullable.getParameterTypes());
    }

    @Test
    void traversesInheritedInterfacesWithoutDuplicates() throws Throwable {
        Methods methods = new Methods(CompatibleTarget.class);
        ChildImpl argument = new ChildImpl();
        Method method = methods.findCompatibleMethod("accept", argument);

        assertNotNull(method);
        assertSame(CompatibleTarget.class, method.getDeclaringClass());
        assertArrayEquals(new Class<?>[]{Parent.class}, method.getParameterTypes());
        assertEquals("parent", Methods.execute(new CompatibleTarget(), "accept", new Object[]{argument}));
    }

    public static class InvocationTarget {
        public String echo(String value) {
            return "echo:" + value;
        }

        public String nullable() {
            return null;
        }

        public void fail() {
            throw new IllegalStateException("boom");
        }

        private String privateEcho(String value) {
            return "private:" + value;
        }

        public static String staticEcho(String value) {
            return "static:" + value;
        }
    }

    @Test
    void executesByMethodNameAndPreservesReturnValue() throws Throwable {
        InvocationTarget target = new InvocationTarget();

        assertEquals("echo:value", Methods.execute(target, "echo", new Object[]{"value"}));
        assertNull(Methods.execute(target, "nullable"));
        assertEquals(
                "private:value",
                Methods.execute(target, "privateEcho", new Class<?>[]{String.class}, new Object[]{"value"})
        );
    }

    @Test
    void propagatesTargetExceptionAndValidatesRequiredArguments() throws Exception {
        InvocationTarget target = new InvocationTarget();
        Method echo = InvocationTarget.class.getMethod("echo", String.class);

        IllegalStateException targetError =
                assertThrows(IllegalStateException.class, () -> Methods.execute(target, "fail"));
        assertEquals("boom", targetError.getMessage());

        IllegalArgumentException instanceError =
                assertThrows(IllegalArgumentException.class, () -> Methods.execute(null, echo, new Object[]{"x"}));
        assertEquals("Instance must not be null.", instanceError.getMessage());

        IllegalArgumentException methodError =
                assertThrows(IllegalArgumentException.class, () -> Methods.execute(target, (Method) null));
        assertEquals("Method must not be null.", methodError.getMessage());

        IllegalArgumentException missingError =
                assertThrows(IllegalArgumentException.class, () -> Methods.execute(target, "missing"));
        assertEquals("Method must not be null.", missingError.getMessage());
    }

    @Test
    void executesStaticMethodAndRejectsInstanceMethod() throws Throwable {
        Method staticMethod = InvocationTarget.class.getMethod("staticEcho", String.class);
        Method instanceMethod = InvocationTarget.class.getMethod("echo", String.class);

        assertEquals("static:value", Methods.executeStatic(staticMethod, new Object[]{"value"}));

        UnsupportedOperationException error = assertThrows(
                UnsupportedOperationException.class,
                () -> Methods.executeStatic(instanceMethod, new Object[]{"value"})
        );
        assertEquals("This is not a static method.", error.getMessage());
    }

    interface Greeting {
        default String greet(String name) {
            return "hello:" + name;
        }
    }

    @Test
    void executesInterfaceDefaultMethod() throws Exception {
        Method method = Greeting.class.getMethod("greet", String.class);
        Greeting proxy = (Greeting) Proxy.newProxyInstance(
                Greeting.class.getClassLoader(),
                new Class<?>[]{Greeting.class},
                (instance, invoked, args) -> {
                    if (invoked.isDefault())
                        return Methods.executeDefault(instance, invoked, args);
                    throw new UnsupportedOperationException(invoked.getName());
                }
        );

        assertEquals("hello:ajaxjs", proxy.greet("ajaxjs"));
    }
}
