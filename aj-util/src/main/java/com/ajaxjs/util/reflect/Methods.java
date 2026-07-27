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
 * Can be either a class object or an instance object
 */
@Slf4j
public class Methods {
    private Object inputObject;

    private Class<?> inputClass;

    public Methods(Object inputObject) {
        this.inputObject = inputObject;
    }

    public Methods(Class<?> inputClass) {
        this.inputClass = inputClass;
    }

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
    public Method findDeclaredMethod(String methodName, Class<?>... parameterTypes) {
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
     * @param parameters 参数列表
     * @return 匹配的方法对象，null 表示找不到 The declared method, or null if method doesn't exist
     */
    public Method findDeclaredMethod(String methodName, Object... parameters) {
        return findDeclaredMethod(methodName, Clazz.args2class(parameters));
    }

    /**
     * Finds a public method whose parameter types are compatible with the supplied arguments.
     * Besides exact matches, this method supports superclass and interface assignment, including
     * interfaces inherited through other interfaces.
     *
     * @param methodName method name
     * @param args       invocation arguments; {@code null} is compatible with non-primitive parameters
     * @return the best compatible method, or {@code null} if no compatible method exists
     */
    public Method findCompatibleMethod(String methodName, Object... args) {
        Class<?> targetClass = getInputClass();
        Object[] actualArgs = args == null ? new Object[0] : args;
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
            log.warn("IllegalArgumentException when executing method of {}", method.getName());
            throw e;
        } catch (InvocationTargetException e) {
            Throwable e1 = e.getTargetException();
            log.error("反射执行方法异常！所在类[{}] 方法：[{}]", instance.getClass().getName(), method.getName());

            throw e1;
        }
    }

    public static Object execute(Object instance, Method method) throws Throwable {
        return execute(instance, method, null);
    }

    public static Object execute(Object instance, String methodName) throws Throwable {
        return execute(instance, methodName, null);
    }

    public static Object execute(Object instance, String methodName, Object[] parameters) throws Throwable {
        Method method = new Methods(instance).findCompatibleMethod(methodName, parameters);

        return execute(instance, method, parameters);
    }

    /**
     * 调用方法。 注意获取方法对象，原始类型和包装类型不能混用，否则得不到正确的方法， 例如 Integer 不能与 int 混用。 这里提供一个
     * argType 的参数，指明参数类型为何。
     *
     * @param instance        对象实例
     * @param methodName      方法名称
     * @param parametersTypes 参数类型
     * @param parameters      参数值
     * @return 执行结果
     * @throws IllegalArgumentException 实例为 null 或找不到匹配的方法
     * @throws Throwable                方法执行失败
     */
    public static Object execute(Object instance, String methodName, Class<?>[] parametersTypes, Object[] parameters) throws Throwable {
        Method method = new Methods(instance).findDeclaredMethod(methodName, parametersTypes);

        return execute(instance, method, parameters);
    }

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
