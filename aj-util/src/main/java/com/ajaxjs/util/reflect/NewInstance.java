package com.ajaxjs.util.reflect;

import com.ajaxjs.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * The reflection utility class for creating new instances of classes.
 */
@Slf4j
public class NewInstance<T> {
    /**
     * 要创建实例的类对象
     */
    private final Class<T> clz;

    /**
     * 构造函数参数列表
     */
    private Object[] args;

    /**
     * 创建实例
     *
     * @param clz 要创建实例的类对象
     */
    public NewInstance(Class<T> clz) {
        if (clz == null)
            throw new IllegalArgumentException("Class must not be null.");

        if (clz.isInterface())
            throw new IllegalArgumentException("所传递的 class 类型参数为接口 " + clz + "，无法实例化");

        this.clz = clz;
    }

    /**
     * 创建实例
     *
     * @param clz  要创建实例的类对象
     * @param args 构造函数参数列表
     */
    public NewInstance(Class<T> clz, Object... args) {
        this(clz);
        this.args = args;
    }

    /**
     * 创建实例
     *
     * @param className 要创建实例的类名称
     * @param args      构造函数参数列表
     */
    public NewInstance(String className, Object... args) {
        this((Class<T>) Clazz.getClassByName(className), args);
    }

    /**
     * 创建实例
     * 该函数根据给定的参数列表创建指定类的实例。如果参数列表为空，则使用默认构造函数创建实例；否则，使用指定的构造函数创建实例。
     *
     * @return 创建的实例对象
     */
    public T newInstance() {
        Constructor<T> constructor;

        if (ObjectHelper.isEmpty(args))
            try {
                constructor = clz.getConstructor();
            } catch (NoSuchMethodException e) {
                log.error("The constructor of this class {} is not found.", clz.getName(), e);
                throw new RuntimeException("The constructor of this class " + clz.getName() + " is not found.", e);
            }
        else
            constructor = getConstructor(clz, Clazz.args2class(args));// 获取构造器

        return newInstance(constructor, args);
    }

    /**
     * Creates an instance using a specific constructor and arguments.
     * This function creates an instance of the class using the provided constructor
     * and argument list through reflection.
     * 根据构造器创建实例
     * 该函数根据给定的构造器和参数列表创建指定类的实例。它使用反射调用构造函数来实例化对象，并在实例化失败时抛出异常。
     *
     * @param constructor The class constructor to use 类构造器
     * @param args        Optional arguments for the constructor 获取指定参数类型的构造函数，这里传入我们想调用的构造函数所需的参数。可以不传。
     * @param <T>         The class reference type 类引用
     * @return The created object instance 对象实例
     * @throws IllegalArgumentException if constructor is null
     */
    public static <T> T newInstance(Constructor<T> constructor, Object... args) {
        if (constructor == null)
            throw new IllegalArgumentException("Constructor must not be null.");

        try {
            return ObjectHelper.isEmpty(args) ? constructor.newInstance() : constructor.newInstance(args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error occurred when creating instance of class: " + constructor.getDeclaringClass(), e);
        } catch (InstantiationException | IllegalArgumentException | InvocationTargetException e) {
            log.error("Error occurred when creating instance of class: {}", constructor.getDeclaringClass(), e);
            throw new RuntimeException("Error occurred when creating instance of class: " + constructor.getDeclaringClass(), e);
        }
    }

    /**
     * Gets a constructor for a class, supporting overloaded constructors with different parameters.
     * This function retrieves a constructor based on the provided class object and optional parameter types.
     * If parameter types are provided, the matching constructor is returned;
     * otherwise, the no-args constructor is returned.
     * 获取类的构造器，可以支持重载的构造器（不同参数的构造器）
     * 这个函数用于获取类的构造函数。它接受两个参数，一个是类对象，一个是可选的参数类型数组。
     * 如果传入了参数类型数组，则获取与该数组匹配的构造函数；如果没有传入参数类型数组，则获取空参数列表的构造函数。
     * 如果找不到合适的构造函数，会记录日志并抛出 RuntimeException。
     *
     * @param clz    类对象
     * @param argClz 指定构造函数的参数类型，这里传入我们想调用的构造函数所需的参数类型
     * @param <T>    类引用
     * @return 类的构造器
     * @throws IllegalArgumentException 如果 clz 为 null
     */
    public static <T> Constructor<T> getConstructor(Class<T> clz, Class<?>... argClz) {
        if (clz == null)
            throw new IllegalArgumentException("Class must not be null.");

        try {
            return argClz != null ? clz.getConstructor(argClz) : clz.getConstructor();
        } catch (NoSuchMethodException e) {
            log.error("Error occurred when creating instance of class: {}", clz, e);
            throw new RuntimeException("Error occurred when creating instance of class: " + clz, e);
        } catch (SecurityException e) {
            log.error("Security Error occurred when getting the constructor  of class: {}", clz, e);
            throw new RuntimeException("Security Error occurred when getting the constructor  of class: " + clz, e);
        }
    }

    /**
     * Checks if a class has any constructors with parameters.
     * This function determines whether the provided class has any constructors that
     * accept parameters by examining all available constructors.
     *
     * @param clz The class object to check
     * @return true if the class has at least one constructor with parameters
     * @throws IllegalArgumentException if clz is null
     */
    public static boolean hasArgsCon(Class<?> clz) {
        if (clz == null)
            throw new IllegalArgumentException("Class must not be null.");

        Constructor<?>[] constructors = clz.getConstructors();

        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length != 0)
                return true;
        }

        return false;
    }
}
