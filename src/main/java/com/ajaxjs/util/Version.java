package com.ajaxjs.util;


import java.util.TimeZone;

/**
 * 初始化，检测是否可以运行
 */
public class Version {
    /**
     * 获取操作系统名称
     */
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    /**
     * 是否调试模式（开发模式）
     */
    public static boolean isDebug;

    static {
        if (!"Asia/Shanghai".equals(System.getProperty("user.timezone"))) {
            System.err.println("当前 JVM 非中国大陆时区");
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        }

        /*
         * 有两种模式：本地模式和远程模式（自动判断） 返回 true 表示是非 linux 环境，为开发调试的环境，即 isDebug = true； 返回
         * false 表示在部署的 linux 环境下。 Linux 的为远程模式
         */
        isDebug = !(OS_NAME.contains("nix") || OS_NAME.contains("nux") || OS_NAME.indexOf("aix") > 0);
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
