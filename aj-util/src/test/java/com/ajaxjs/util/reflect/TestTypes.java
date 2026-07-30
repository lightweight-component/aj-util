package com.ajaxjs.util.reflect;


import com.ajaxjs.util.ObjectHelper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    public String getPlainString() {
        return "value";
    }

    public List<List<String>> getNestedList() {
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
}
