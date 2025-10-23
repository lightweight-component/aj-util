package com.ajaxjs.util;

import java.util.Arrays;

public class DebugTools {
    /**
     * 打印数组以便测试
     *
     * @param arr 数组对象，可以为 null
     */
    public static void printArray(Object[] arr) {
        if (arr == null) {
            System.err.println("数组为空，null！");
            return;
        }

        if (arr.length == 0)
            System.err.println("数组不为空，但没有一个元素在内");

        System.out.println(Arrays.toString(arr));
    }

    /**
     * 是否在运行单元测试
     */
    private static Boolean isRunningTest;

    /**
     * 检测是否在运行单元测试
     *
     * @return true=运行单元测试
     */
    public static Boolean isRunningTest() {
        if (isRunningTest == null) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

            for (StackTraceElement e : stackTrace) {
                if (e.toString().lastIndexOf("junit.runners") > -1)
                    return true;
            }

            isRunningTest = false;
        }

        return isRunningTest;
    }
}
