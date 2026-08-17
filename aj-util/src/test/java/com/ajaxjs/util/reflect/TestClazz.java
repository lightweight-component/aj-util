package com.ajaxjs.util.reflect;


import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestClazz {
    interface RootInterface {
    }

    interface LeftInterface extends RootInterface {
    }

    interface RightInterface extends RootInterface {
    }

    static class InterfaceImplementation implements LeftInterface, RightInterface {
    }

    @Test
    void testGetClassByName() {
        Class<?> actual = Clazz.getClassByName("java.lang.String");
        assertEquals(String.class, actual);
    }

    @Test
    void testGetClassByName_whenClassNotFound() {
        RuntimeException error =
                assertThrows(RuntimeException.class, () -> Clazz.getClassByName("com.example.NotFoundClass"));

        assertInstanceOf(ClassNotFoundException.class, error.getCause());
    }

    @Test
    void testGetClassByName_whenClassFoundWithGenerics() {
        Class<?> actual = Clazz.getClassByName("java.util.ArrayList");
        assertEquals(ArrayList.class, actual);
    }

    @Test
    void testGetClassByNameWithTargetType() {
        Class<CharSequence> actual = Clazz.getClassByName("java.lang.String", CharSequence.class);

        assertEquals(String.class, actual);
        assertThrows(ClassCastException.class,
                () -> Clazz.getClassByName("java.lang.String", Number.class));
        assertThrows(IllegalArgumentException.class,
                () -> Clazz.getClassByName("java.lang.String", null));
    }

    @Test
    void testGetClassByInterface() {
        Type parameterized = new ParameterizedType() {
            public Type[] getActualTypeArguments() {
                return new Type[]{String.class};
            }

            public Type getRawType() {
                return List.class;
            }

            public Type getOwnerType() {
                return null;
            }
        };

        assertEquals(List.class, Clazz.getClassByInterface(List.class));
        assertEquals(List.class, Clazz.getClassByInterface(parameterized));
        assertNull(Clazz.getClassByInterface(null));
    }

    @Test
    void testGetDeclaredInterface() {
        assertArrayEquals(
                new Class<?>[]{LeftInterface.class, RootInterface.class, RightInterface.class},
                Clazz.getDeclaredInterface(InterfaceImplementation.class)
        );

        assertArrayEquals(
                new Class<?>[]{RootInterface.class},
                Clazz.getDeclaredInterface(LeftInterface.class)
        );
    }

    @Test
    void convertsRuntimeArgumentsToClasses() {
        assertArrayEquals(
                new Class<?>[]{String.class, Integer.class},
                Clazz.args2class(new Object[]{"value", 1})
        );
        assertNull(Clazz.args2class(null));
        assertNull(Clazz.args2class(new Object[0]));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> Clazz.args2class(new Object[]{"value", null})
        );
        assertEquals("Argument at index 1 must not be null.", error.getMessage());
    }

    @Test
    void rejectsNullHierarchyInputs() {
        assertEquals("Class must not be null.",
                assertThrows(IllegalArgumentException.class, () -> Clazz.getDeclaredInterface(null)).getMessage());
        assertEquals("Class must not be null.",
                assertThrows(IllegalArgumentException.class, () -> Clazz.getAllSuperClass(null)).getMessage());
    }
}
