package com.ajaxjs.util.io;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class TestResourceHelper {
    @Test
    void locatesClasspathResource() {
        String resourcePath = new ResourceHelper("com/test.txt").getPath(true);

        assertNotNull(resourcePath);
        assertTrue(Files.exists(Paths.get(resourcePath)));
    }

    @Test
    void missingClasspathResourceThrows() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new ResourceHelper("non-existent-resource.txt").getPath(true)
        );

        assertTrue(error.getMessage().contains("non-existent-resource.txt"));
    }

    @Test
    void locatesAbsoluteResourceRelativeToClass() {
        String resourcePath = new ResourceHelper("/test.txt", TestResourceHelper.class).getPath(true);

        assertNotNull(resourcePath);
        assertTrue(Files.exists(Paths.get(resourcePath)));

        assertThrows(
                IllegalArgumentException.class,
                () -> new ResourceHelper("non-existent-resource.txt", TestResourceHelper.class).getPath(true)
        );
    }

    @Test
    void opensAndReadsResourceContent() throws Exception {
        ResourceHelper helper = new ResourceHelper("test.txt");

        try (InputStream input = helper.getStream()) {
            assertNotNull(input);

            byte[] bytes = new DataReader(input).readAsBytes();

            assertEquals("你好 Hi", new String(bytes, StandardCharsets.UTF_8));
        }

        assertThrows(IllegalArgumentException.class, () -> new ResourceHelper("non-existent-resource.txt").getStream());
    }

    @Test
    void readsResourceText() {
        assertEquals("你好 Hi", new ResourceHelper("test.txt").toString().trim());
        assertThrows(IllegalArgumentException.class, () -> new ResourceHelper("non-existent-resource.txt").toString());
    }

    @Test
    void loadsPropertiesWithExactValues() {
        Properties properties = new ResourceHelper("test-demo.properties"
        ).getProperties();

        assertEquals("hi", properties.getProperty("database.ipPort"));
        assertEquals("root", properties.getProperty("database.username"));
        assertEquals("root", properties.getProperty("database.password"));
        assertEquals(3, properties.size());
    }

    @Test
    void missingPropertiesThrows() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new ResourceHelper("non-existent.properties").getProperties()
        );

        assertTrue(error.getMessage().contains("non-existent.properties"));
    }

    @Test
    void derivesClassNameAndRuntimeDirectory() {
        assertEquals("com.example.Sample",
                ResourceHelper.getClassName(new File("Sample.class"), "com.example")
        );

        String jarDir =
                ResourceHelper.getJarDir();

        assertNotNull(jarDir);
        assertFalse(jarDir.isEmpty());
    }

    @Test
    void rejectsNonClassFileWhenDerivingClassName() {
        assertThrows(IllegalArgumentException.class,
                () -> ResourceHelper.getClassName(new File("Sample.txt"), "com.example")
        );
    }

    @Test
    void listsClasspathRootFilesWhenFileProtocolIsAvailable() {
        ResourceHelper helper = new ResourceHelper("test.txt");

        List<File> files = new ArrayList<>();

        try {
            helper.list(files::add);
        } catch (UnsupportedOperationException e) {
            /*
             * The test may run from a JAR where the classpath root does not
             * use the file protocol. In that case list() intentionally does
             * not support enumeration.
             */
            return;
        }

        assertFalse(files.isEmpty());
    }

    @Test
    void listRejectsNullConsumer() {
        ResourceHelper helper = new ResourceHelper("test.txt");

        assertThrows(NullPointerException.class, () -> helper.list(null));
    }

    @Test
    void constructorRejectsNullResource() {
        assertThrows(NullPointerException.class, () -> new ResourceHelper(null));
    }

    @Test
    void classRelativeResourceUsesTargetClassPackage() {
        ResourceHelper helper = new ResourceHelper("test.txt", TestResourceHelper.class);
        /*
         * This test assumes test.txt is located in the same package as
         * TestResourceHelper:
         *
         * src/test/resources/com/ajaxjs/util/io/test.txt
         */
        String path = helper.getPath(true);

        assertNotNull(path);
        assertTrue(Files.exists(Paths.get(path)));
    }

    @Test
    void absoluteClassResourceUsesClasspathRoot() {
        ResourceHelper helper = new ResourceHelper("/test.txt", TestResourceHelper.class);
        String path = helper.getPath(true);

        assertNotNull(path);
        assertTrue(Files.exists(Paths.get(path)));
    }
}