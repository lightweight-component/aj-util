package com.ajaxjs.util.reflect;

import com.ajaxjs.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * ReflectMethod 类用于反射执行方法
 * 输入参数 Can be either a class object or an instance object
 */
@Slf4j
public class ReflectMethod {
    private Object inputObject;

    private Class<?> inputClass;

    /**
     * This for method execution
     */
    public ReflectMethod() {
    }

    public ReflectMethod(Object inputObject) {
        this.inputObject = inputObject;
    }

    public ReflectMethod(Class<?> inputClass) {
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
     * Get a declared method by method name.
     * This method searches only for methods declared directly in the specified class.
     * This method can access private methods.
     *
     * @param methodName The name of the method to find
     * @return The declared method, or null if method doesn't exist
     * @throws RuntimeException if the method is not found
     */
    public Method getDeclaredMethod(String methodName, Class<?>... args) {
        getInputClass();

        try {
            Method method = ObjectHelper.isEmpty(args) ? inputClass.getDeclaredMethod(methodName) : inputClass.getDeclaredMethod(methodName, args);

            if (!method.isAccessible())
                method.setAccessible(true);

            return method;
        } catch (NoSuchMethodException e) {
            log.warn("No Such Method Exception {}", methodName, e);
            throw new RuntimeException("No Such Method Exception " + methodName, e);
        }
    }

    /**
     * 查找对象父类身上指定的方法，可以包括私有方法
     *
     * @param methodName 方法名称
     * @param args       参数类引用
     * @return 匹配的方法对象，null 表示找不到
     * @throws SecurityException 如果访问声明方法或修改其可访问性被拒绝
     */
    public Method getSuperClassDeclaredMethod(String methodName, Class<?>... args) {
        Class<?> clz = getInputClass();

        for (; clz != null && clz != Object.class; clz = clz.getSuperclass()) {
            try {
                Method method = clz.getDeclaredMethod(methodName, args);

                if (!method.isAccessible())
                    method.setAccessible(true);

                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }

        return null;
    }

    /**
     * Get a method object with parameter types, supporting overloaded methods.
     * This method searches for public methods including inherited ones.
     * This method can not access private methods.
     *
     * @param method The method name to search for
     * @param args   Explicit parameter type list for method overloading resolution
     * @return The matching method object, or null if not found
     */
    public Method getMethod(String method, Class<?>... args) {
        getInputClass();

        try {
            return ObjectHelper.isEmpty(args) ? inputClass.getMethod(method) : inputClass.getMethod(method, args);
        } catch (NoSuchMethodException | SecurityException e) {
//            StringBuilder str = new StringBuilder();
//
//            for (Class<?> clz : args)
//                str.append(clz.getName());

//            log.warn("类找不到这个方法 {}.{}({})。", cls.getName(), method, str.toString().isEmpty() ? "void" : str.toString());
            return null;
        }
    }

    public Method getMethod(String method, Object... args) {
        return getMethod(method, Clazz.args2class(args));
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

    /**
     * Find a method by name with automatic parameter type upcasting support.
     * This method searches through the class hierarchy for compatible parameter types.
     * Supports only single parameter methods currently.
     *
     * @param method Method name to find
     * @param arg    Argument object (must be an object, not a Class) for parameter type matching
     * @return Matching method object, or null if not found
     * @deprecated Use {@link #findCompatibleMethod(String, Object...)}.
     */
    @Deprecated
    public Method getMethodByArgumentUpCastingSearch(String method, Object arg) {
        return findCompatibleMethod(method, arg);
    }

    /**
     * 循环 object 向上转型（接口）
     *
     * @param method 方法名称
     * @param arg    参数对象，可能是子类或接口，所以要在这里找到对应的方法，当前只支持单个参数
     * @return 方法对象
     * @deprecated Use {@link #findCompatibleMethod(String, Object...)}.
     */
    @Deprecated
    public Method getMethodByArgumentInterface(String method, Object arg) {
        return findCompatibleMethod(method, arg);
    }

}
