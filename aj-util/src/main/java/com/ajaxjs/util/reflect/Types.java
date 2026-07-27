package com.ajaxjs.util.reflect;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Type Reflection Utility - Provides methods for working with Java reflection types,
 * particularly for extracting generic type information from classes, methods, and types.
 *
 * <p>This class simplifies common operations for accessing and manipulating generic types
 * in Java's reflection system, making it easier to work with parameterized types at runtime.
 */
@Slf4j
public class Types {
    /**
     * Gets the array of generic type arguments.
     *
     * @param type The Type object to extract generic type arguments from
     * @return The array of generic type arguments, or null if the specified Type is not a ParameterizedType
     */
    public static Type[] getActualType(Type type) {
        if (type instanceof ParameterizedType)
            return ((ParameterizedType) type).getActualTypeArguments();
        else {
            log.warn("{} may not be a generic", type);
            return null;
        }
    }

    /**
     * Gets the generic type arguments from a method's return type, such as String in List&lt;String&gt;
     * rather than the generic parameter T.
     *
     * @param method The method to analyze
     * @return The actual type arguments, which may be multiple
     */
    public static Type[] getGenericReturnType(Method method) {
        return getActualType(method.getGenericReturnType());
    }

    /**
     * Gets the generic type arguments from a method's return type, such as String in List&lt;String&gt;
     * rather than the generic parameter T. This method retrieves only the first type and converts it to a Class.
     *
     * @param method The method to analyze
     * @return The first actual type as a Class
     */
    public static Class<?> getGenericFirstReturnType(Method method) {
        Type[] type = getGenericReturnType(method);

        return type == null ? null : type2class(type[0]);
    }

    /**
     * Gets the generic type arguments from a class definition, such as String in List&lt;String&gt;
     *
     * @param clz The class to analyze. The class must point to an instance, see
     *            <a href="https://stackoverflow.com/questions/8436055/how-to-get-class-of-generic-type-when-there-is-no-parameter-of-it">...</a>
     * @return The actual type arguments
     */
    public static Type[] getActualType(Class<?> clz) {
        return getActualType(clz.getGenericSuperclass());
    }

    /**
     * Gets the actual class from a generic type definition
     *
     * @param clz The class to analyze
     * @return The actual class
     * @throws IllegalArgumentException if the class does not declare a parameterized superclass,
     *                                  or its first type argument cannot be resolved to a Class
     */
    public static Class<?> getActualClass(Class<?> clz) {
        if (clz == null)
            throw new IllegalArgumentException("Class must not be null.");

        Type[] actualType = getActualType(clz);

        if (actualType == null || actualType.length == 0)
            throw new IllegalArgumentException("Class " + clz.getName() + " does not declare a parameterized superclass.");

        Class<?> actualClass = type2class(actualType[0]);

        if (actualClass == null)
            throw new IllegalArgumentException("The first generic type argument of " + clz.getName()
                    + " cannot be resolved to a Class.");

        return actualClass;
    }

    /**
     * Converts a Type to a Class. For a ParameterizedType, returns the Class
     * represented by its raw type.
     *
     * @param type The Type interface to convert
     * @return The corresponding Class, or null if the type cannot be resolved to a Class
     */
    public static Class<?> type2class(Type type) {
        if (type instanceof Class)
            return (Class<?>) type;
        else if (type instanceof ParameterizedType)
            return type2class(((ParameterizedType) type).getRawType());

        return null;
    }

    /*
        从 Spring 4.0 开始 Spring 中添加了 ResolvableType 工具，这个类可以更加方便的用来回去泛型信息。
        Ref: https://my.oschina.net/qq596392912/blog/3028409
     */
}
