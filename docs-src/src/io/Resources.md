---
title: Resources
description: Consistent classpath resource loading for files and JARs
tags:
  - resource loading
  - classpath
  - Java
layout: layouts/aj-util.njk
---

# Resources

`Resources` loads classpath resources as streams, text, properties, or file-system paths.

All resource lookup methods throw `IllegalArgumentException` when the requested resource does not exist. They do
not return `null` and do not write diagnostics to standard error.

## Streams, text, and properties

Use stream-based APIs for code that must work both from an IDE and from a packaged JAR:

```java
try (InputStream in = Resources.getResource("config/app.yml")) {
    // read resource
}

String text = Resources.getResourceText("templates/page.html");
Properties properties = Resources.getProperties("application.properties");
```

## File-system paths

`getResourcesFromClasspath(...)` and `getResourcesFromClass(...)` return paths only for resources backed by a
`file:` URL:

```java
String path = Resources.getResourcesFromClasspath("com/example/config.properties");
String relative = Resources.getResourcesFromClass(MyClass.class, "template.txt");
```

A resource inside a JAR is not a normal `Path`; these methods throw `UnsupportedOperationException` for non-file
URLs. Use `getResource(...)` for JAR content instead of attempting to convert the URL display text into a path.

`getClassName(File, String)` derives a fully qualified class name from a `.class` filename, and `getJarDir()` returns
the parent directory of the running code source.
