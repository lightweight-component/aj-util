package com.ajaxjs.util.reflect;


import com.ajaxjs.util.ObjectHelper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestTypes {
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

    @Test
    public void testGetActualType() {
        Type[] actualType = Types.getActualType(type);

        assert actualType != null;
        assertEquals(actualType.length, 1);
        assertEquals(actualType[0], String.class);
    }

    @Test
    public void testGetGenericReturnType() throws NoSuchMethodException {
        Method method = TestTypes.class.getMethod("getList2");
        Type[] actualType = Types.getGenericReturnType(method);

        assertEquals(1, actualType.length);
        assertEquals(String.class, actualType[0]);
    }

    @Test
    public void testGetGenericFirstReturnType() throws NoSuchMethodException {
        Method method = TestTypes.class.getMethod("getList2");
        Class<?> actualType = Types.getGenericFirstReturnType(method);

        assertEquals(String.class, actualType);
    }

    @Test
    public void testGetActualType2() {
        Class<?> clz = StringList.class;
        Type[] actualType = Types.getActualType(clz);

        assertEquals(1, actualType.length);
        assertEquals(String.class, actualType[0]);
    }

    @Test
    public void testGetActualClass() throws NoSuchMethodException {
        assertEquals(String.class, Types.getActualClass(StringList.class));
        assertThrows(IllegalArgumentException.class, () -> Types.getActualClass(String.class));
    }

    @Test
    public void testType2class() {
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
    }
}
