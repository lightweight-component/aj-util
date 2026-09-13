/**
 * Copyright Sp42 frank@ajaxjs.com Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.ajaxjs.util.io;

import com.ajaxjs.util.CommonConstant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Utility class for locating and reading resources from the Java classpath.
 *
 * <p>The resource can be resolved in one of two ways:</p>
 *
 * <ul>
 *     <li>
 *         If no target class is specified, the resource is loaded through
 *         the application {@link ClassLoader}. Resource names are resolved
 *         relative to the classpath root and normally must not start with
 *         {@code /}.
 *     </li>
 *     <li>
 *         If a target class is specified, the resource is loaded using
 *         {@link Class#getResource(String)} or
 *         {@link Class#getResourceAsStream(String)}. In this mode, a resource
 *         name without a leading {@code /} is resolved relative to the
 *         package of the target class, while a resource name beginning with
 *         {@code /} is resolved from the classpath root.
 *     </li>
 * </ul>
 *
 * <p>For example:</p>
 *
 * <pre>{@code
 * // From the classpath root
 * ResourceHelper helper = new ResourceHelper("config/application.properties");
 *
 * // Relative to the package of MyClass
 * ResourceHelper packageResource =new ResourceHelper("config.properties", MyClass.class);
 *
 * // From the classpath root through Class.getResource()
 * ResourceHelper rootResource = new ResourceHelper("/config.properties", MyClass.class);
 * }</pre>
 *
 * <p>Resources packaged inside a JAR can always be read through
 * {@link #getStream()}, but they do not necessarily correspond to ordinary
 * filesystem paths. Therefore methods such as {@link #getPath(boolean)} and
 * {@link #list(Consumer)} have additional protocol restrictions.</p>
 */
public class ResourceHelper {
    /**
     * Resource name used for classpath lookup.
     */
    private final String resource;

    /**
     * Optional class used for class-relative resource lookup.
     *
     * <p>If {@code null}, the resource is resolved using the application
     * class loader.</p>
     */
    private final Class<?> targetClass;

    /**
     * Creates a resource helper using class-loader-based lookup.
     *
     * <p>The resource name is resolved relative to the classpath root.</p>
     *
     * @param resource resource name
     * @throws NullPointerException if {@code resource} is {@code null}
     */
    public ResourceHelper(String resource) {
        this(resource, null);
    }

    /**
     * Creates a resource helper using either class-loader lookup or
     * class-relative lookup.
     *
     * <p>If {@code targetClass} is {@code null}, lookup is performed through
     * the application class loader.</p>
     *
     * <p>If {@code targetClass} is not {@code null}, lookup follows
     * {@link Class#getResource(String)} semantics:</p>
     *
     * <ul>
     *     <li>{@code "file.txt"} - relative to the target class package</li>
     *     <li>{@code "/file.txt"} - relative to the classpath root</li>
     * </ul>
     *
     * @param resource    resource name
     * @param targetClass optional class used for relative resource lookup
     * @throws NullPointerException if {@code resource} is {@code null}
     */
    public ResourceHelper(String resource, Class<?> targetClass) {
        this.resource = Objects.requireNonNull(resource, "resource");
        this.targetClass = targetClass;
    }

    /**
     * Returns the filesystem path of this resource.
     *
     * <p>This method is only available for resources backed by the
     * {@code file:} protocol. Resources located inside JAR archives typically
     * use another protocol and therefore cannot be represented as ordinary
     * filesystem paths.</p>
     *
     * <p>If {@code targetClass} is configured, lookup follows
     * {@link Class#getResource(String)} rules. Otherwise the application
     * class loader is used.</p>
     *
     * @param isDecode whether URL-encoded path characters should be decoded  using {@link URL#toURI()}
     * @return filesystem path of the resource
     * @throws IllegalArgumentException      if the resource cannot be found
     * @throws UnsupportedOperationException if the resource is not backed by the {@code file:} protocol
     */
    public String getPath(boolean isDecode) {
        URL resourceUrl;

        if (targetClass == null)
            resourceUrl = getClassLoader().getResource(resource);
        else
            resourceUrl = targetClass.getResource(resource);

        if (resourceUrl == null)
            throw new IllegalArgumentException("Resource not found: " + resource);

        return url2path(resourceUrl, isDecode);
    }

    /**
     * Converts a {@link URL} representing a local filesystem resource into
     * a platform-specific filesystem path.
     *
     * <p>Only {@code file:} URLs are supported. For example, on Windows,
     * {@link URL#getPath()} may return a value similar to
     * {@code /D:/project/file.txt}.</p>
     *
     * <p>When {@code isDecode} is {@code true}, the URL is first converted
     * into a {@link java.net.URI}, allowing encoded characters such as
     * {@code %20} to be interpreted correctly by
     * {@link Paths#get(java.net.URI)}.</p>
     *
     * <p>When {@code isDecode} is {@code false}, {@link URL#getPath()} is used directly.</p>
     *
     * @param url      resource URL
     * @param isDecode whether URL-encoded path characters should be decoded
     * @return platform-specific filesystem path
     * @throws IllegalArgumentException      if {@code url} is {@code null}  or cannot be converted to a URI
     * @throws UnsupportedOperationException if the URL protocol is not {@code file}
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
     * Opens this resource as an input stream.
     *
     * <p>Unlike {@link #getPath(boolean)}, this method can also read resources
     * packaged inside JAR files because it does not require the resource to
     * exist as a regular filesystem file.</p>
     *
     * <p>The caller is responsible for closing the returned stream.</p>
     *
     * @return input stream for the resource
     * @throws IllegalArgumentException if the resource cannot be found
     */
    public InputStream getStream() {
        InputStream in;

        if (targetClass == null)
            in = getClassLoader().getResourceAsStream(resource);
        else
            in = targetClass.getResourceAsStream(resource);

        if (in == null)
            throw new IllegalArgumentException("Resource not found: " + resource);

        return in;
    }

    /**
     * Reads the complete resource contents as text.
     *
     * <p>The resource is opened using {@link #getStream()} and automatically
     * closed after reading.</p>
     *
     * @return textual contents of the resource
     * @throws IllegalArgumentException if the resource cannot be found
     * @throws UncheckedIOException     if an I/O error occurs while reading
     */
    @Override
    public String toString() {
        try (InputStream in = getStream()) {
            return new DataReader(in).readAsString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 获取正在运行的 JAR 文件的目录
     * 如果您在 IDE 中运行代码，则该代码可能会返回项目的根目录
     *
     * @return JAR 文件的目录
     */

    /**
     * Returns the directory containing the running application code.
     *
     * <p>When the class is loaded from a JAR file, this usually returns the
     * directory containing that JAR.</p>
     *
     * <p>When running from an IDE or from compiled class directories, the
     * returned directory may instead correspond to the build output directory
     * or one of its parent directories.</p>
     *
     * @return directory containing the code source of {@link ResourceHelper}
     * @throws RuntimeException if the code-source URL cannot be converted to a URI
     */
    public static String getJarDir() {
        try {
            return new File(ResourceHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error when accessing the dir of the JAR.", e);
        }
    }

    /**
     * Lists filesystem entries located at the root of the current classpath.
     *
     * <p>Each discovered file or directory is passed to the supplied
     * consumer.</p>
     *
     * <p>This method only supports classpath roots backed by the
     * {@code file:} protocol, such as classes and resources loaded directly
     * from directories during development. It does not enumerate resources
     * contained inside JAR files.</p>
     *
     * <p>If the classpath root cannot be located or the underlying directory
     * cannot be listed, this method returns without invoking the consumer.</p>
     *
     * @param fn consumer invoked for each discovered filesystem entry
     * @throws NullPointerException          if {@code fn} is {@code null}
     * @throws UnsupportedOperationException if the classpath root is not backed by the {@code file:} protocol
     */
    public void list(Consumer<File> fn) {
        Objects.requireNonNull(fn, "list.fn");
        URL resourceUrl = getClassLoader().getResource(CommonConstant.EMPTY_STRING);

        if (resourceUrl == null) {
            return;
        }

        if (!"file".equalsIgnoreCase(resourceUrl.getProtocol()))
            throw new UnsupportedOperationException("Listing resources is only supported for file-system resources: " + resourceUrl);

        File[] files = new File(resourceUrl.getFile()).listFiles(); // 将URL转换为文件路径

        if (files == null)
            return;

        for (File file : files) {
            fn.accept(file);
        }
    }

    /**
     * Loads this resource as a Java {@link Properties} file.
     *
     * <p>The resource is opened using {@link #getStream()} and automatically
     * closed after loading.</p>
     *
     * <p>This method uses {@link Properties#load(InputStream)}. Under the
     * Java properties-file format, the input stream is interpreted using
     * ISO-8859-1 semantics; characters outside that encoding traditionally
     * need to be represented using Unicode escapes such as
     * {@code \u4E2D\u6587}.</p>
     *
     * @return loaded properties
     * @throws IllegalArgumentException if the resource cannot be found
     * @throws RuntimeException         if the property resource cannot be read
     */
    public Properties getProperties() {
        Properties prop = new Properties();

        try (InputStream input = getStream()) {
            prop.load(input);// 加载输入流中的键值对到 Properties 对象

            return prop;
        } catch (IOException e) {
            throw new RuntimeException("Properties File error on: " + resource, e);
        }
    }

    /**
     * Derives a fully qualified Java class name from a {@code .class} file and package name.
     *
     * <p>For example:</p>
     *
     * <pre>{@code
     * File file = new File("Example.class");
     * String name =  ResourceHelper.getClassName(file, "com.example");
     * // com.example.Example
     * }</pre>
     *
     * @param file        Java class file
     * @param packageName Java package name
     * @return fully qualified class name
     * @throws IllegalArgumentException if the file name does not end with  {@code .class}
     */
    public static String getClassName(File file, String packageName) {
        String name = file.getName();

        if (!name.endsWith(".class"))
            throw new IllegalArgumentException("Not a class file: " + file);

        String clzName = name.substring(0, name.length() - 6);

        return packageName + '.' + clzName;
    }

    /**
     * Returns the preferred class loader for general application resource
     * lookup.
     *
     * <p>The lookup order is:</p>
     *
     * <ol>
     *     <li>Current thread context class loader</li>
     *     <li>{@link ResourceHelper}'s defining class loader</li>
     *     <li>System class loader</li>
     * </ol>
     *
     * <p>The thread context class loader is preferred because it generally
     * works better in application servers, plugin systems and other
     * environments using custom class-loader hierarchies.</p>
     *
     * @return class loader used for resource lookup
     */
    private static ClassLoader getClassLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        if (loader == null)
            loader = ResourceHelper.class.getClassLoader();

        if (loader == null)
            loader = ClassLoader.getSystemClassLoader();

        return loader;
    }
}
