package com.ajaxjs.util.reflect;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.*;

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
     * @throws IllegalArgumentException if a parameterized return type contains no actual type arguments
     */
    public static Class<?> getGenericFirstReturnType(Method method) {
        Type[] type = getGenericReturnType(method);

        if (type == null)
            return null;

        if (type.length == 0)
            throw new IllegalArgumentException("Parameterized return type has no actual type arguments: " + method);

        return type2class(type[0]);
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
     * Converts a {@link Type} to a {@link Class}.
     * <ul>
     *     <li>{@link ParameterizedType}: resolves its raw type.</li>
     *     <li>{@link TypeVariable}: resolves its single upper bound.</li>
     *     <li>{@link WildcardType}: resolves its single upper bound when no lower bound exists.</li>
     *     <li>{@link GenericArrayType}: resolves its component and creates the corresponding array class.</li>
     * </ul>
     *
     * @param type The Type interface to convert
     * @return The corresponding Class, or null when type is null
     * @throws IllegalArgumentException if the type is unsupported or cannot be resolved uniquely
     */
    public static Class<?> type2class(Type type) {
        if (type == null)
            return null;

        if (type instanceof Class)
            return (Class<?>) type;
        else if (type instanceof ParameterizedType)
            return type2class(((ParameterizedType) type).getRawType());
        else if (type instanceof GenericArrayType) {
            Class<?> componentType = type2class(((GenericArrayType) type).getGenericComponentType());

            return Array.newInstance(componentType, 0).getClass();
        } else if (type instanceof TypeVariable)
            return resolveSingleUpperBound(type, ((TypeVariable<?>) type).getBounds());
        else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;

            if (wildcardType.getLowerBounds().length > 0)
                throw new IllegalArgumentException("Wildcard type with a lower bound cannot be resolved uniquely: " + type);

            return resolveSingleUpperBound(type, wildcardType.getUpperBounds());
        }

        throw new IllegalArgumentException("Unsupported Type implementation: " + type.getClass().getName());
    }

    private static Class<?> resolveSingleUpperBound(Type source, Type[] bounds) {
        if (bounds.length != 1)
            throw new IllegalArgumentException("Type does not have exactly one upper bound: " + source);

        return type2class(bounds[0]);
    }

    /*
        从 Spring 4.0 开始 Spring 中添加了 ResolvableType 工具，这个类可以更加方便的用来回去泛型信息。
        Ref: https://my.oschina.net/qq596392912/blog/3028409
     */
}
