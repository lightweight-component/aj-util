package com.ajaxjs.util.io;

import com.ajaxjs.util.CommonConstant;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Resource Loading Utility Class
 * <p>
 * This class provides methods for accessing resources from classpath, reading resource files,
 * loading properties, and working with resources inside JAR files. It simplifies common resource
 * access patterns in Java applications.
 */
@Slf4j
public class Resources {
    /**
     * 获取 Classpath 根目录下的资源文件的路径
     *
     * @param resource 文件名称，输入空字符串这返回 Classpath 根目录
     * @param isDecode 是否解码
     * @return 所在工程路径和资源路径
     * @throws IllegalArgumentException      如果资源不存在
     * @throws UnsupportedOperationException 如果资源不是普通文件 URL
     */
    public static String getResourcesFromClasspath(String resource, boolean isDecode) {
        URL url = Resources.class.getClassLoader().getResource(resource);

        if (url == null) {
            log.warn("The resource " + resource + " not found");
            throw new IllegalArgumentException("Resource not found: " + resource);
        }

        return url2path(url, isDecode);
    }

    /**
     * 获取当前类目录下的资源文件
     * 测试时候，源码目录没有，要手动复制
     *
     * @param clz      类引用
     * @param resource 资源文件名
     * @return 当前类的绝对路径
     * @throws IllegalArgumentException      如果资源不存在
     * @throws UnsupportedOperationException 如果资源不是普通文件 URL
     */
    public static String getResourcesFromClass(Class<?> clz, String resource) {
        return getResourcesFromClass(clz, resource, true);
    }

    /**
     * 获取当前类目录下的资源文件
     *
     * @param clz      类引用
     * @param resource 资源文件名
     * @param isDecode 是否解码
     * @return 当前类的绝对路径
     * @throws IllegalArgumentException      如果资源不存在
     * @throws UnsupportedOperationException 如果资源不是普通文件 URL
     */
    public static String getResourcesFromClass(Class<?> clz, String resource, boolean isDecode) {
        return url2path(clz.getResource(resource), isDecode);
    }

    /**
     * 获取 Classpath 根目录下的资源文件
     *
     * @param resource 文件名称，输入空字符串这返回 Classpath 根目录。可以支持包目录，例如  com\\foo\\new-file.txt
     * @return 所在工程路径和资源路径
     * @throws IllegalArgumentException      如果资源不存在
     * @throws UnsupportedOperationException 如果资源不是普通文件 URL
     */
    public static String getResourcesFromClasspath(String resource) {
        return getResourcesFromClasspath(resource, true);
    }

    /**
     * Converts a URL to a file system path
     * <p>
     * Handles the conversion of URL paths (which may start with "/") to standard file system paths.
     * It Also provides optional URL decoding to handle encoded characters in the path.
     * For example, url.getPath() returns `/D:/project/a`
     *
     * @param url      The URL object to convert
     * @param isDecode Whether to decode URL-encoded characters in the path
     * @return The converted file system path
     * @throws IllegalArgumentException      if the URL is null
     * @throws UnsupportedOperationException if the URL does not use the file protocol
     */
    private static String url2path(URL url, boolean isDecode) {
        if (url == null)
            throw new IllegalArgumentException("Resource URL must not be null.");

        if (!"file".equalsIgnoreCase(url.getProtocol()))
            throw new UnsupportedOperationException("Resource is not a file-system resource: " + url);

        try {
            return isDecode ? Paths.get(url.toURI()).toString() : Paths.get(url.getPath()).toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid resource URL: " + url, e);
        }
    }

    /**
     * Java 类文件 去掉后面的 .class 只留下类名
     *
     * @param file        Java 类文件
     * @param packageName 包名称
     * @return 类名
     */
    public static String getClassName(File file, String packageName) {
        String clzName = file.getName().substring(0, file.getName().length() - 6);

        return packageName + '.' + clzName;
    }

    /**
     * 从 classpath 获取资源文件的内容
     *
     * @param path 资源文件路径，例如 application.yml
     * @return 资源文件的内容
     */
    public static String getResourceText(String path) {
        try (InputStream in = getResource(path)) {
            return new DataReader(in).readAsString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 可以在 JAR 中获取资源文件
     * <a href="https://www.cnblogs.com/coderxx/p/13566423.html">...</a>
     * <p>
     * 根据路径获取资源的 InputStream
     * 此方法用于从类路径中加载资源，返回一个输入流，以便读取该资源
     * 主要用于简化资源文件的读取过程，避免直接操作文件系统或处理类路径的问题
     *
     * @param path 资源的路径。可以是类路径上的相对路径或文件系统中的绝对路径
     * @return 找到的资源输入流
     * @throws IllegalArgumentException 如果资源不存在
     */
    public static InputStream getResource(String path) {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

        if (in == null)
            throw new IllegalArgumentException("Resource not found: " + path);

        return in;
    }

    /**
     * 获取正在运行的 JAR 文件的目录
     * 如果您在 IDE 中运行代码，则该代码可能会返回项目的根目录
     *
     * @return JAR 文件的目录
     */
    public static String getJarDir() {
        try {
            return new File(Resources.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error when accessing the dir of the JAR.", e);
        }
    }

    /**
     * 列出资源文件列表
     */
    static void listResourceFile() {
        ClassLoader classLoader = Resources.class.getClassLoader();
        URL resourceUrl = classLoader.getResource(CommonConstant.EMPTY_STRING);

        if (resourceUrl != null) {
            File[] files = new File(resourceUrl.getFile()).listFiles(); // 将URL转换为文件路径

            assert files != null;
            for (File file : files) {
                if (file.isFile()) // 是否为普通文件（非目录）
                    log.info(file.getName());
            }
        }
    }

    /**
     * 从类路径加载 properties 文件
     *
     * @param filename properties 文件路径
     * @return properties 文件
     */
    public static Properties getProperties(String filename) {
        Properties prop = new Properties();

        try (InputStream input = getResource(filename)) {
            prop.load(input);// 加载输入流中的键值对到 Properties 对象

            return prop;
        } catch (IOException e) {
            throw new RuntimeException("Properties File error " + filename, e);
        }
    }
}
