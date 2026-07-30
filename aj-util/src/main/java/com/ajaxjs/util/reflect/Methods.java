package com.ajaxjs.util.reflect;

import com.ajaxjs.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Reflection helper for locating and invoking methods on a configured class or object.
 * <p>
 * Declared-method lookup walks the class hierarchy and can make non-public methods accessible.
 * Compatible lookup searches public methods and matches runtime arguments against superclass,
 * interface, and primitive-wrapper parameter types.
 */
@Slf4j
public class Methods {
    /**
     * Object used to derive the input class when the helper is constructed from an instance.
     */
    private Object inputObject;

    /**
     * Class on which methods are searched.
     */
    private Class<?> inputClass;

    /**
     * Creates a method helper for an object instance.
     *
     * @param inputObject object whose runtime class will be searched
     */
    public Methods(Object inputObject) {
        this.inputObject = inputObject;
    }

    /**
     * Creates a method helper for a class.
     *
     * @param inputClass class on which methods will be searched
     */
    public Methods(Class<?> inputClass) {
        this.inputClass = inputClass;
    }

    /**
     * Returns the configured input class. If this helper was constructed from an object,
     * its runtime class is resolved lazily.
     *
     * @return class on which methods are searched
     * @throws IllegalArgumentException if neither a class nor an object was supplied
     */
    public Class<?> getInputClass() {
        if (inputClass == null) {
            if (inputObject == null)
                throw new IllegalArgumentException("No input argument of class or object.");

            inputClass = inputObject.getClass();
        }

        return inputClass;
    }

    /**
     * 查找对象父类身上指定的方法，可以包括私有方法及父类方法。
     * Get a declared method by method name. This method can access private methods and super methods.
     *
     * @param methodName     方法名称 The name of the method to find
     * @param parameterTypes 参数类引用
     * @return 匹配的方法对象，null 表示找不到 The declared method, or null if method doesn't exist
     */
    public Method findDeclaredMethodByTypes(String methodName, Class<?>... parameterTypes) {
        Class<?> clz = getInputClass();

        for (; clz != null && clz != Object.class; clz = clz.getSuperclass()) {
            try {
                Method method = ObjectHelper.isEmpty(parameterTypes) ? clz.getDeclaredMethod(methodName) : clz.getDeclaredMethod(methodName, parameterTypes);

                if (!method.isAccessible())
                    method.setAccessible(true);

                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }

        return null;
    }

    /**
     * 查找对象父类身上指定的方法，可以包括私有方法及父类方法。
     * Get a declared method by method name. This method can access private methods and super methods.
     *
     * @param methodName 方法名称 The name of the method to find
     * @param parameters 参数列表；按照每个参数的精确运行时类型查找
     * @return 匹配的方法对象，null 表示找不到 The declared method, or null if method doesn't exist
     */
    public Method findDeclaredMethod(String methodName, Object... parameters) {
        if (ObjectHelper.isEmpty(parameters))
            return findDeclaredMethodByTypes(methodName);

        Class<?>[] parameterTypes = new Class<?>[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] == null)
                return null;// 无法精确匹配；需要调用方使用 findDeclaredMethodByTypes() 明确类型

            parameterTypes[i] = parameters[i].getClass();
        }

        return findDeclaredMethodByTypes(methodName, parameterTypes);
    }

    /**
     * Finds a public method whose parameter types are compatible with the supplied arguments.
     * Besides exact matches, this method supports superclass and interface assignment, including
     * interfaces inherited through other interfaces.
     *
     * @param methodName method name
     * @param parameters invocation arguments; {@code null} is compatible with non-primitive parameters
     * @return the compatible method with the lowest type-distance score, or {@code null} if none exists.
     * If unrelated overloads have the same score, the first method returned by reflection is selected
     */
    public Method findCompatibleMethod(String methodName, Object... parameters) {
        Class<?> targetClass = getInputClass();
        Object[] actualArgs = parameters == null ? new Object[0] : parameters;
        Method bestMethod = null;
        int bestScore = Integer.MAX_VALUE;

        for (Method candidate : targetClass.getMethods()) {
            if (!candidate.getName().equals(methodName))
                continue;

            Class<?>[] parameterTypes = candidate.getParameterTypes();
            if (parameterTypes.length != actualArgs.length)
                continue;

            int score = getCompatibilityScore(parameterTypes, actualArgs);
            if (score < bestScore) {
                bestMethod = candidate;
                bestScore = score;
            }
        }

        return bestMethod;
    }

    /**
     * Calculates the total assignment distance between arguments and method parameters.
     *
     * @return a non-negative score, where a lower value is a closer match;
     * {@link Integer#MAX_VALUE} means incompatible
     */
    private static int getCompatibilityScore(Class<?>[] parameterTypes, Object[] args) {
        int score = 0;

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = wrapPrimitive(parameterTypes[i]);
            Object arg = args[i];

            if (arg == null) {
                if (parameterTypes[i].isPrimitive())
                    return Integer.MAX_VALUE;

                score += 100;
                continue;
            }

            Class<?> argumentType = arg.getClass();
            if (!parameterType.isAssignableFrom(argumentType))
                return Integer.MAX_VALUE;

            score += getTypeDistance(argumentType, parameterType);
        }

        return score;
    }

    /**
     * Maps a primitive type to its wrapper class so reflective lookup can compare it
     * with the runtime class of a boxed argument.
     */
    private static Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive())
            return type;
        if (type == boolean.class)
            return Boolean.class;
        if (type == byte.class)
            return Byte.class;
        if (type == char.class)
            return Character.class;
        if (type == short.class)
            return Short.class;
        if (type == int.class)
            return Integer.class;
        if (type == long.class)
            return Long.class;
        if (type == float.class)
            return Float.class;
        if (type == double.class)
            return Double.class;

        return Void.class;
    }

    /**
     * Finds the shortest superclass/interface distance from {@code source} to {@code target}.
     * A visited set prevents duplicate traversal in interface diamonds.
     */
    private static int getTypeDistance(Class<?> source, Class<?> target) {
        if (source == target)
            return 0;

        Queue<Class<?>> queue = new ArrayDeque<>();
        Queue<Integer> distances = new ArrayDeque<>();
        Set<Class<?>> visited = new HashSet<>();
        queue.add(source);
        distances.add(0);
        visited.add(source);

        while (!queue.isEmpty()) {
            Class<?> current = queue.remove();
            int distance = distances.remove();
            Class<?> superclass = current.getSuperclass();

            if (superclass != null) {
                if (superclass == target)
                    return distance + 1;

                if (visited.add(superclass)) {
                    queue.add(superclass);
                    distances.add(distance + 1);
                }
            }

            for (Class<?> currentInterface : current.getInterfaces()) {
                if (currentInterface == target)
                    return distance + 1;

                if (visited.add(currentInterface)) {
                    queue.add(currentInterface);
                    distances.add(distance + 1);
                }
            }
        }

        return Integer.MAX_VALUE;
    }

    /*--------------------------METHOD EXECUTION--------------------------------------*/

    /**
     * Invokes a resolved method and unwraps {@link InvocationTargetException}, propagating
     * the exception thrown by the target method.
     *
     * @param instance   target object
     * @param method     method to invoke
     * @param parameters invocation arguments, or {@code null} for no arguments
     * @return target method's return value
     * @throws IllegalArgumentException if the instance or method is {@code null}, or arguments do not match
     * @throws Throwable                if access fails or the target method throws
     */
    public static Object execute(Object instance, Method method, Object[] parameters) throws Throwable {
        if (instance == null)
            throw new IllegalArgumentException("Instance must not be null.");

        if (method == null)
            throw new IllegalArgumentException("Method must not be null.");

        try {
            return ObjectHelper.isEmpty(parameters) ? method.invoke(instance) : method.invoke(instance, parameters);
        } catch (IllegalAccessException e) {
            log.warn("IllegalAccessException when executing method of {}", method.getName());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("IllegalArgumentException when executing method of {}", method.getName());
            throw e;
        } catch (InvocationTargetException e) {
            log.error("InvocationTargetException when executing method of {}", method.getName());

            throw e.getTargetException();
        }
    }

    /**
     * Invokes a no-argument method.
     *
     * @param instance target object
     * @param method   method to invoke
     * @return target method's return value
     * @throws Throwable if invocation fails
     */
    public static Object execute(Object instance, Method method) throws Throwable {
        return execute(instance, method, null);
    }

    /**
     * Finds and invokes a compatible public no-argument method by name.
     *
     * @param instance   target object
     * @param methodName method name
     * @return target method's return value
     * @throws Throwable if lookup or invocation fails
     */
    public static Object execute(Object instance, String methodName) throws Throwable {
        return execute(instance, methodName, null);
    }

    /**
     * Finds a public method using exact parameter types.
     *
     * @param methodName     method name
     * @param parameterTypes exact parameter types; null means no parameters
     * @return matched public method, or null if not found
     */
    public Method findPublicExactMethodByTypes(String methodName, Class<?>[] parameterTypes) {
        try {
            Class<?>[] types = parameterTypes == null ? new Class<?>[0] : parameterTypes;

            for (Class<?> type : types) {
                if (type == null)
                    throw new IllegalArgumentException("Parameter types must not contain null.");
            }

            return getInputClass().getMethod(methodName, types);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    /**
     * Finds a public method using the exact runtime types of argument values.
     *
     * @param methodName method name
     * @param parameters runtime argument values
     * @return matched public method, or null when no exact match is available
     */
    public Method findPublicExactMethod(String methodName, Object[] parameters) {
        if (ObjectHelper.isEmpty(parameters))
            return findPublicExactMethodByTypes(methodName, new Class<?>[0]);

        Class<?>[] parameterTypes = new Class<?>[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] == null)
                return null;// 无法精确匹配，交给 compatible lookup

            parameterTypes[i] = parameters[i].getClass();
        }

        return findPublicExactMethodByTypes(methodName, parameterTypes);
    }

    /**
     * Finds a compatible public method from runtime argument values and invokes it.
     *
     * @param instance   target object
     * @param methodName method name
     * @param parameters invocation arguments, or {@code null} for no arguments
     * @return target method's return value
     * @throws IllegalArgumentException if the instance is null or no compatible method exists
     * @throws Throwable                if invocation fails
     */
    public static Object execute(Object instance, String methodName, Object[] parameters) throws Throwable {
        Methods methods = new Methods(instance);
        Method method = methods.findPublicExactMethod(methodName, parameters);

        if (method == null)
            method = methods.findCompatibleMethod(methodName, parameters);

        return execute(instance, method, parameters);
    }

    /**
     * Finds and invokes a public method using explicitly supplied, exact parameter types.
     * This overload is useful when runtime argument types differ from declared parameter types,
     * such as {@link Integer} and {@code int}.
     *
     * @param instance       target object
     * @param methodName     method name
     * @param parameterTypes exact declared parameter types; {@code null} means no parameters
     * @param parameters     invocation arguments, or {@code null} for no arguments
     * @return target method's return value
     * @throws IllegalArgumentException if the instance is null, a parameter type is null,
     *                                  no public method exists, or arguments do not match
     * @throws Throwable                if invocation fails
     */
    public static Object execute(Object instance, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        Method method = new Methods(instance).findPublicExactMethodByTypes(methodName, parameterTypes);

        return execute(instance, method, parameters);
    }

    /**
     * Invokes a static method.
     *
     * @param method static method to invoke
     * @param args   invocation arguments, or {@code null} for no arguments
     * @return target method's return value
     * @throws UnsupportedOperationException if the supplied method is not static
     * @throws Throwable                     if invocation fails
     */
    public static Object executeStatic(Method method, Object[] args) throws Throwable {
        if (!Modifier.isStatic(method.getModifiers())) {
            log.warn("This is not a static method: {}", method);
            throw new UnsupportedOperationException("This is not a static method.");
        }

        return execute(new Object(), method, args);
    }

    /**
     * 通过反射调用接口的默认方法。
     * <p>
     * 此方法旨在提供一种通过反射机制调用Java 8及以上版本接口中默认方法的手段。
     * 它绕过了直接调用默认方法需要实例化一个类的限制，通过MethodHandles和反射机制实现。
     * <p>
     * 调用 Interface 的 default 方法
     * <a href="https://www.jianshu.com/p/63691220f81f">...</a>
     * <a href="https://link.jianshu.com/?t=http://stackoverflow.com/questions/22614746/how-do-i-invoke-java-8-default-methods-refletively">...</a>
     *
     * @param proxy  接口的代理实例，用于调用默认方法。
     * @param method 要调用的默认方法的Method对象。
     * @param args   调用方法时所需的参数数组。
     * @return 调用默认方法后的返回值。
     * @throws RuntimeException 如果在调用过程中发生任何异常，将会抛出运行时异常。
     */
    public static Object executeDefault(Object proxy, Method method, Object[] args) {
        try {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);

            Class<?> declaringClass = method.getDeclaringClass();
            int allModes = MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE;

            return constructor.newInstance(declaringClass, allModes)
                    .unreflectSpecial(method, declaringClass)
                    .bindTo(proxy)
                    .invokeWithArguments(args);
        } catch (Throwable e) {
            log.warn("Error when executing default method: {}", method, e);
            throw new RuntimeException(e);
        }
    }
}
