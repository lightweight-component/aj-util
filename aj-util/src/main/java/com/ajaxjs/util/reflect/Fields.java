package com.ajaxjs.util.reflect;

import com.ajaxjs.util.CommonConstant;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The reflection utility class for working with fields.
 */
public class Fields {
    /**
     * 获取本类及其父类的字段属性（包括 private 的）
     *
     * @param clz 当前类对象
     * @return 字段数组
     */
    public static Field[] getSuperClassDeclaredFields(Class<?> clz) {
        List<Field> fieldList = new ArrayList<>();

        while (clz != null && clz != Object.class) {  // 排除 Object 类
            Collections.addAll(fieldList, clz.getDeclaredFields());// 避免创建中间 ArrayList
            clz = clz.getSuperclass();
        }

        return fieldList.toArray(new Field[0]);
    }

    /**
     * 在指定类及其父类中查找指定名称的字段
     *
     * @param clazz     要查找的类
     * @param fieldName 要查找的字段名称
     * @return 找到的字段对象，如果未找到则返回 null
     */
    public static Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;

        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }

        return null;
    }


    /**
     * 获取包装异常中的底层异常。对于 InvocationTargetException 或
     * UndeclaredThrowableException，会沿 cause 链向下查找；如果包装异常没有 cause，
     * 则原样返回该包装异常。
     *
     * @param e 异常对象
     * @return 实际异常对象
     * @throws IllegalArgumentException 如果异常对象为 null
     */
    public static Throwable getUnderLayerErr(Throwable e) {
        if (e == null)
            throw new IllegalArgumentException("Throwable must not be null.");

        while (e instanceof InvocationTargetException || e instanceof UndeclaredThrowableException) {
            Throwable cause = e.getCause();

            if (cause == null || cause == e)
                break;

            e = cause;
        }

        return e;
    }

    /**
     * 获取实际抛出的那个异常对象，并去掉前面的包名。
     *
     * @param e 异常对象
     * @return 实际异常对象信息
     */
    public static String getUnderLayerErrMsg(Throwable e) {
        String msg = getUnderLayerErr(e).toString();

        return msg.replaceAll("^[^:]*:\\s?", CommonConstant.EMPTY_STRING);
    }
}