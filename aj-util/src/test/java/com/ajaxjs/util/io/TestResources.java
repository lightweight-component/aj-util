package com.ajaxjs.util.io;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class TestResources {
    @Test
    void locatesClasspathResource() {
        String resourcePath = Resources.getResourcesFromClasspath("com/test.txt");

        assertNotNull(resourcePath);
        assertTrue(Files.exists(Paths.get(resourcePath)));
    }

    @Test
    void missingClasspathResourceThrows() {
        assertThrows(
                RuntimeException.class,
                () -> Resources.getResourcesFromClasspath("non-existent-resource.txt")
        );
    }

    @Test
    void locatesAbsoluteResourceRelativeToClass() {
        String resourcePath = Resources.getResourcesFromClass(TestResources.class, "/test.txt");

        assertNotNull(resourcePath);
        assertTrue(Files.exists(Paths.get(resourcePath)));
        assertThrows(IllegalArgumentException.class,
                () -> Resources.getResourcesFromClass(TestResources.class, "non-existent-resource.txt"));
    }

    @Test
    void opensAndReadsResourceContent() throws Exception {
        try (InputStream input = Resources.getResource("test.txt")) {
            assertNotNull(input);
            byte[] bytes = new DataReader(input).readAsBytes();
            assertEquals("你好 Hi", new String(bytes, StandardCharsets.UTF_8));
        }

        assertThrows(IllegalArgumentException.class,
                () -> Resources.getResource("non-existent-resource.txt"));
    }

    @Test
    void readsResourceText() {
        assertEquals("你好 Hi", Resources.getResourceText("test.txt"));
        assertThrows(IllegalArgumentException.class,
                () -> Resources.getResourceText("non-existent-resource.txt"));
    }

    @Test
    void loadsPropertiesWithExactValues() {
        Properties properties = Resources.getProperties("test-demo.properties");

        assertEquals("hi", properties.getProperty("database.ipPort"));
        assertEquals("root", properties.getProperty("database.username"));
        assertEquals("root", properties.getProperty("database.password"));
        assertEquals(3, properties.size());
    }

    @Test
    void missingPropertiesThrows() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> Resources.getProperties("non-existent.properties")
        );

        assertTrue(error.getMessage().contains("non-existent.properties"));
    }

    @Test
    void derivesClassNameAndRuntimeDirectory() {
        assertEquals(
                "com.example.Sample",
                Resources.getClassName(new java.io.File("Sample.class"), "com.example")
        );
        assertNotNull(Resources.getJarDir());
        assertFalse(Resources.getJarDir().isEmpty());
    }
}
