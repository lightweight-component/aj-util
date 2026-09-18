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

