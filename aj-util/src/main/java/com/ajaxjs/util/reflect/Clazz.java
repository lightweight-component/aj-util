package com.ajaxjs.util.reflect;

import com.ajaxjs.util.CommonConstant;
import com.ajaxjs.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Class Reflection Utility - Provides comprehensive methods for class reflection operations
 * including class loading, instantiation, constructor management, and type conversion.
 *
 * <p>This class simplifies common reflection tasks in Java, making it easier to work with
 * dynamic class loading, object creation, and interface implementations at runtime.
 */
@Slf4j
public class Clazz {
    /**
     * Gets a class object by its fully qualified name.
     *
     * @param clzName The fully qualified class name. Note special handling for inner classes.
     * @return The corresponding class object
     */
    public static Class<?> getClassByName(String clzName) {
        try {
            return Class.forName(clzName);
        } catch (ClassNotFoundException e) {
            log.error("Class:{} not Found.", clzName, e);
            throw new RuntimeException("Class:" + clzName + " not Found.");
        }
    }

    /**
     * Gets a class object by its fully qualified name with strong type casting.
     *
     * @param clzName The fully qualified class name
     * @param clz     The target type to cast to
     * @param <T>     The class reference type
     * @return The corresponding class object cast to the specified type
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClassByName(String clzName, Class<T> clz) {
        Class<?> c = getClassByName(clzName);

        return (Class<T>) c;
    }

    /**
     * Converts an array of objects to an array of their corresponding class objects.
     * This function transforms a variable argument list into a Class array where
     * each element represents the class type of the corresponding input argument.
     * 把参数转换为类对象列表
     * 这个 Java 函数将一个可变参数列表转换为一个类对象列表。它接受一个可变参数 args，返回一个 Class 类型的数组 clazz，
     * 数组长度与参数列表的长度相同，并且每个元素的类型与对应参数的类型相同。
     *
     * @param args The variable argument list
     * @return The array of corresponding class objects
     */
    public static Class<?>[] args2class(Object[] args) {
        if (ObjectHelper.isEmpty(args))
            return null;

        Class<?>[] clazz = new Class[args.length];

        for (int i = 0; i < args.length; i++)
            clazz[i] = args[i].getClass();

        return clazz;
    }

    /**
     * Gets the class object for a given interface type.
     *
     * @param type The interface type
     * @return The class object for the interface
     */
    public static Class<?> getClassByInterface(Type type) {
        String className = type.toString();
        className = className.replaceAll("<.*>$", CommonConstant.EMPTY_STRING).replaceAll("(class|interface)\\s", CommonConstant.EMPTY_STRING); // 不要泛型的字符

        return getClassByName(className);
    }

    /**
     * Gets all interfaces implemented by a given class.
     *
     * @param clz The target class
     * @return All interfaces implemented by the class
     */
    public static Class<?>[] getDeclaredInterface(Class<?> clz) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();

        for (Class<?> current = clz; current != null && current != Object.class; current = current.getSuperclass())
            for (Class<?> currentInterface : current.getInterfaces())
                addInterfaceHierarchy(currentInterface, interfaces);

        return interfaces.toArray(new Class[0]);
    }

    /**
     * Recursively adds an interface and all of its parent interfaces to the given set.
     *
     * @param current    the interface to add
     * @param interfaces the set collecting all interfaces
     */
    private static void addInterfaceHierarchy(Class<?> current, Set<Class<?>> interfaces) {
        if (!interfaces.add(current))
            return;

        for (Class<?> parent : current.getInterfaces())
            addInterfaceHierarchy(parent, interfaces);
    }

    /**
     * 获取所有父类，排除自己
     *
     * @param clz 类对象
     * @return 所有父类
     */
    public static Class<?>[] getAllSuperClass(Class<?> clz) {
        List<Class<?>> classList = new ArrayList<>();
        Class<?> current = clz.getSuperclass();  // 从父类开始

        while (current != null && current != Object.class) {
            classList.add(current);
            current = current.getSuperclass();
        }

//        for (; clz != Object.class; clz = clz.getSuperclass()) {
//            if (clz == null)
//                break;
//            else
//                clzList.add(clz);
//        }
//
//        if (!clzList.isEmpty())
//            clzList.remove(0); // 排除自己

        return classList.toArray(new Class[0]);
    }
}
