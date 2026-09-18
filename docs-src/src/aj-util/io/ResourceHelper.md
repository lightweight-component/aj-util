---
title: ResourceHelper
description: Locate and read classpath resources
layout: layouts/aj-util.njk
lang: en
alternate: /io/ResourceHelper-cn/
---

# ResourceHelper

`ResourceHelper` locates resources on the Java classpath. Constructing it without a target class resolves names from
the classpath root; constructing it with a class follows `Class.getResource` rules.

```java
String text = new ResourceHelper("config/application.properties").toString();
Properties properties = new ResourceHelper("config/application.properties").getProperties();

InputStream stream = new ResourceHelper("config.properties", MyService.class).getStream();
```

`getStream()` works for resources packaged in JAR files. `getPath(boolean)` and `list(Consumer<File>)` require a
filesystem-backed resource and may not work for `jar:` URLs. `getJarDir()` returns the directory containing the running
JAR when that location is available. Close streams returned by `getStream()` yourself.

## Choosing a resource name

The two constructors intentionally use different lookup roots. This is often the cause of a resource that exists in
`src/test/resources` but cannot be found at runtime.

| Construction | Resource name | Lookup location |
| --- | --- | --- |
| `new ResourceHelper("a/b.txt")` | no leading slash | classpath root: `a/b.txt` |
| `new ResourceHelper("file.txt", MyService.class)` | no leading slash | package of `MyService` |
| `new ResourceHelper("/file.txt", MyService.class)` | leading slash | classpath root: `file.txt` |

For example, a test resource located at
`src/test/resources/com/ajaxjs/util/io/test.txt` can be read relative to its test class:

```java
try (InputStream in = new ResourceHelper("test.txt", MyIoTest.class).getStream()) {
    byte[] bytes = new DataReader(in).readAsBytes();
    String text = new String(bytes, StandardCharsets.UTF_8);
}

// A leading slash changes the same constructor to a classpath-root lookup.
String rootText = new ResourceHelper("/test.txt", MyIoTest.class).toString();
```

`getStream()`, `toString()` and `getProperties()` throw `IllegalArgumentException` when the resource is absent. This
is deliberate: a required packaged configuration should fail at the lookup site instead of producing a later
`NullPointerException`.

## Reading text and properties

`toString()` delegates to `DataReader`, so it decodes with UTF-8, normalizes lines to the current platform separator,
and closes its stream. `getProperties()` loads a standard Java `Properties` file and also closes its stream.

```java
Properties db = new ResourceHelper("test-demo.properties").getProperties();
String username = db.getProperty("database.username");

String json = new ResourceHelper("fixtures/request.json").toString();
```

Use `getStream()` when another API needs the stream, for example an image decoder or certificate parser. In that case
the caller owns closure; do not pass a stream to an API and then close it before that API has consumed it.

## Filesystem and packaged-resource boundaries

Classpath lookup returns a URL, not necessarily a `File`. A development build usually exposes `file:` URLs, while a
deployed application commonly exposes `jar:` URLs. `getPath(true)` converts only the former into a usable filesystem
path; it is therefore appropriate for tests or an unpacked application, not for a resource that must work from inside
an executable JAR. `list(Consumer<File>)` has the same `file:`-only limitation and enumerates the classpath root.

`getJarDir()` is a deployment helper for locating files next to the running artifact, not a way to locate a resource
inside that artifact. `getClassName(File, String)` turns a `.class` file name plus a package prefix into a binary class
name, and rejects non-`.class` inputs.

## Implementation notes

The class does not copy resources to a temporary directory. It keeps the classpath abstraction intact and exposes a
stream first; this is why JAR resources work reliably with `getStream()` but not with file-oriented APIs. Prefer a
stream unless an external API truly requires a `Path` or `File`.

