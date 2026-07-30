package com.ajaxjs.util.reflect;


import com.ajaxjs.util.ObjectHelper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestTypes {
    static class GenericOwner<T> {
        class NonGenericInner {
        }
    }

    static class GenericTypes<T extends Number, M extends Number & Comparable<M>> {
        T value;
        T[] values;
        List<? extends Number> upper;
        List<? super Integer> lower;
        List<?> any;
        M multiple;
    }

    static class StringList extends ArrayList<String> {
    }

    final Type type = new ParameterizedType() {
        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{String.class};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    };

    public List<String> getList2() {
        return ObjectHelper.listOf("a", "b");
    }

    public String getPlainString() {
        return "value";
    }

    public List<List<String>> getNestedList() {
        return null;
    }

    public GenericOwner<String>.NonGenericInner getParameterizedOwnerOnly() {
        return null;
    }

    @Test
    void testGetActualType() {
        Type[] actualType = Types.getActualType(type);

        assertNotNull(actualType);
        assertArrayEquals(new Type[]{String.class}, actualType);
        assertNull(Types.getActualType(String.class));
    }

    @Test
    void testGetGenericReturnType() throws NoSuchMethodException {
        Method method = TestTypes.class.getMethod("getList2");
        Type[] actualType = Types.getGenericReturnType(method);

        assertArrayEquals(new Type[]{String.class}, actualType);
    }

    @Test
    void testGetGenericFirstReturnType() throws NoSuchMethodException {
        Method method = TestTypes.class.getMethod("getList2");

        assertEquals(String.class, Types.getGenericFirstReturnType(method));
        assertNull(Types.getGenericFirstReturnType(TestTypes.class.getMethod("getPlainString")));
        assertEquals(List.class, Types.getGenericFirstReturnType(TestTypes.class.getMethod("getNestedList")));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> Types.getGenericFirstReturnType(TestTypes.class.getMethod("getParameterizedOwnerOnly"))
        );
        assertTrue(error.getMessage().contains("has no actual type arguments"));
    }

    @Test
    void testGetActualType2() {
        Class<?> clz = StringList.class;
        Type[] actualType = Types.getActualType(clz);

        assertArrayEquals(new Type[]{String.class}, actualType);
    }

    @Test
    void testGetActualClass() {
        assertEquals(String.class, Types.getActualClass(StringList.class));
        assertThrows(IllegalArgumentException.class, () -> Types.getActualClass(String.class));
        assertThrows(IllegalArgumentException.class, () -> Types.getActualClass(null));
    }

    @Test
    void testType2class() {
        Type type = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{String.class};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };

        Class<?> actualClass = Types.type2class(type);

        assertEquals(List.class, actualClass);
        assertEquals(String.class, Types.type2class(String.class));
        assertNull(Types.type2class(null));
    }

    @Test
    void resolvesTypeVariablesWildcardsAndGenericArrays() throws NoSuchFieldException {
        Type variable = GenericTypes.class.getDeclaredField("value").getGenericType();
        Type array = GenericTypes.class.getDeclaredField("values").getGenericType();
        Type upper = firstTypeArgument("upper");
        Type any = firstTypeArgument("any");

        assertEquals(Number.class, Types.type2class(variable));
        assertEquals(Number[].class, Types.type2class(array));
        assertEquals(Number.class, Types.type2class(upper));
        assertEquals(Object.class, Types.type2class(any));
    }

    @Test
    void rejectsTypesWithoutOneUniqueResolution() throws NoSuchFieldException {
        Type lower = firstTypeArgument("lower");
        Type multiple = GenericTypes.class.getDeclaredField("multiple").getGenericType();

        assertInstanceOf(WildcardType.class, lower);
        assertThrows(IllegalArgumentException.class, () -> Types.type2class(lower));
        assertThrows(IllegalArgumentException.class, () -> Types.type2class(multiple));
    }

    private static Type firstTypeArgument(String fieldName) throws NoSuchFieldException {
        ParameterizedType fieldType =
                (ParameterizedType) GenericTypes.class.getDeclaredField(fieldName).getGenericType();

        return fieldType.getActualTypeArguments()[0];
    }
}
