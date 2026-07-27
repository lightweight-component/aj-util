package com.ajaxjs.util.reflect;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class TestFields {
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
    public void testGetSuperClassDeclaredFields() {
        Field[] fields = Fields.getSuperClassDeclaredFields(Child.class);

        assertNotNull(fields);
        assertTrue(fields.length >= 4);
        assertTrue(java.util.Arrays.stream(fields).anyMatch(f -> f.getName().equals("publicField")));
        assertTrue(java.util.Arrays.stream(fields).anyMatch(f -> f.getName().equals("privateField")));
        assertTrue(java.util.Arrays.stream(fields).anyMatch(f -> f.getName().equals("childField")));
        assertTrue(java.util.Arrays.stream(fields).anyMatch(f -> f.getName().equals("childPrivateField")));
    }

    @Test
    public void testGetSuperClassDeclaredFieldsExcludesObject() {
        Field[] fields = Fields.getSuperClassDeclaredFields(Object.class);

        assertNotNull(fields);
        assertEquals(0, fields.length);
    }

    @Test
    public void testGetSuperClassDeclaredFieldsWithNoOwnFields() {
        Field[] fields = Fields.getSuperClassDeclaredFields(NoFields.class);

        assertNotNull(fields);
        assertEquals(0, fields.length);
    }

    @Test
    public void testFindFieldInCurrentClass() {
        Field field = Fields.findField(Child.class, "childField");

        assertNotNull(field);
        assertEquals("childField", field.getName());
    }

    @Test
    public void testFindFieldInSuperClass() {
        Field field = Fields.findField(Child.class, "privateField");

        assertNotNull(field);
        assertEquals("privateField", field.getName());
    }

    @Test
    public void testFindFieldNotFound() {
        assertNull(Fields.findField(Child.class, "missingField"));
    }

    @Test
    public void testFindFieldInParentOnlyClass() {
        Field field = Fields.findField(Parent.class, "publicField");

        assertNotNull(field);
        assertEquals("publicField", field.getName());
    }
}
