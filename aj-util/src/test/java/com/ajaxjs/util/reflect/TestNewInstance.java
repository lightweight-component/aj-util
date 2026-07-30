package com.ajaxjs.util.reflect;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class TestNewInstance {
    public static class Sample {
        private final String value;

        public Sample() {
            this("default");
        }

        public Sample(String value) {
            this.value = value;
        }
    }

    public static class NoDefaultConstructor {
        public NoDefaultConstructor(String value) {
        }
    }

    public static class ThrowingConstructor {
        public ThrowingConstructor() {
            throw new IllegalStateException("constructor failed");
        }
    }

    @Test
    void createsInstancesUsingClassAndClassName() {
        Sample defaultSample = new NewInstance<>(Sample.class).newInstance();
        Sample argumentSample = new NewInstance<>(Sample.class, "configured").newInstance();
        Object namedSample = new NewInstance<>(Sample.class.getName(), "named").newInstance();

        assertEquals("default", defaultSample.value);
        assertEquals("configured", argumentSample.value);
        assertInstanceOf(Sample.class, namedSample);
        assertEquals("named", ((Sample) namedSample).value);
    }

    @Test
    void getsAndInvokesExactPublicConstructor() throws Exception {
        Constructor<Sample> constructor = NewInstance.getConstructor(Sample.class, String.class);

        assertSame(Sample.class, constructor.getDeclaringClass());
        assertArrayEquals(new Class<?>[]{String.class}, constructor.getParameterTypes());
        assertEquals("direct", NewInstance.newInstance(constructor, "direct").value);
    }

    @Test
    void reportsConstructorCapabilitiesAndInvalidTargets() {
        assertTrue(NewInstance.hasArgsCon(Sample.class));
        assertFalse(NewInstance.hasArgsCon(Object.class));
        assertThrows(IllegalArgumentException.class, () -> new NewInstance<>(Runnable.class));
        assertThrows(RuntimeException.class, () -> new NewInstance<>(NoDefaultConstructor.class).newInstance());
        assertThrows(
                RuntimeException.class,
                () -> NewInstance.getConstructor(Sample.class, Integer.class)
        );

        assertEquals("Class must not be null.",
                assertThrows(IllegalArgumentException.class, () -> new NewInstance<>((Class<Object>) null)).getMessage());
        assertEquals("Class must not be null.",
                assertThrows(IllegalArgumentException.class, () -> NewInstance.getConstructor(null)).getMessage());
        assertEquals("Class must not be null.",
                assertThrows(IllegalArgumentException.class, () -> NewInstance.hasArgsCon(null)).getMessage());
        assertEquals("Constructor must not be null.",
                assertThrows(IllegalArgumentException.class, () -> NewInstance.newInstance(null)).getMessage());
    }

    @Test
    void preservesConstructorFailureAsCause() {
        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> new NewInstance<>(ThrowingConstructor.class).newInstance()
        );

        assertNotNull(error.getCause());
        assertEquals(java.lang.reflect.InvocationTargetException.class, error.getCause().getClass());
        assertEquals("constructor failed", error.getCause().getCause().getMessage());
    }
}
