package com.ajaxjs.util.reflect;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TestFields {
    public static class Parent {
        public String publicField;
        private int privateField;
    }

    public static class Child extends Parent {
        public String childField;
        private Object childPrivateField;
    }

    public static class NoFields {
    }

    @Test
    void testGetSuperClassDeclaredFields() {
        Field[] fields = Fields.getSuperClassDeclaredFields(Child.class);

        Set<String> names = Arrays.stream(fields).map(Field::getName).collect(Collectors.toSet());
        assertEquals(4, fields.length);
        assertEquals(
                new java.util.HashSet<>(Arrays.asList(
                        "publicField", "privateField", "childField", "childPrivateField"
                )),
                names
        );
    }

    @Test
    void testGetSuperClassDeclaredFieldsExcludesObject() {
        Field[] fields = Fields.getSuperClassDeclaredFields(Object.class);

        assertArrayEquals(new Field[0], fields);
    }

    @Test
    void testGetSuperClassDeclaredFieldsWithNoOwnFields() {
        Field[] fields = Fields.getSuperClassDeclaredFields(NoFields.class);

        assertArrayEquals(new Field[0], fields);
    }

    @Test
    void testFindFieldInCurrentClass() {
        Field field = Fields.findField(Child.class, "childField");

        assertNotNull(field);
        assertEquals("childField", field.getName());
        assertSame(Child.class, field.getDeclaringClass());
    }

    @Test
    void testFindFieldInSuperClass() {
        Field field = Fields.findField(Child.class, "privateField");

        assertNotNull(field);
        assertEquals("privateField", field.getName());
        assertSame(Parent.class, field.getDeclaringClass());
    }

    @Test
    void testFindFieldNotFound() {
        assertNull(Fields.findField(Child.class, "missingField"));
    }

    @Test
    void testFindFieldInParentOnlyClass() {
        Field field = Fields.findField(Parent.class, "publicField");

        assertNotNull(field);
        assertEquals("publicField", field.getName());
        assertSame(Parent.class, field.getDeclaringClass());
    }

    @Test
    void unwrapsNestedReflectionWrappers() {
        IllegalStateException cause = new IllegalStateException("boom");
        Throwable wrapped = new InvocationTargetException(new UndeclaredThrowableException(cause));

        assertSame(cause, Fields.getUnderLayerErr(wrapped));
        assertEquals("boom", Fields.getUnderLayerErrMsg(wrapped));
    }

    @Test
    void keepsCauseLessWrapperAndRejectsNull() {
        InvocationTargetException wrapper = new InvocationTargetException(null);

        assertSame(wrapper, Fields.getUnderLayerErr(wrapper));
        assertThrows(IllegalArgumentException.class, () -> Fields.getUnderLayerErr(null));
    }
}
