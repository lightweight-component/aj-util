package com.ajaxjs.util.reflect;


import org.junit.jupiter.api.Test;

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
        assertThrows(RuntimeException.class, () -> Clazz.getClassByName("com.example.NotFoundClass"));
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
        Class<?> actual = Clazz.getClassByInterface(List.class);
        assertEquals(List.class, actual);
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
    }
}
