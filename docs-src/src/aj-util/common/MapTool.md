---
title: MapTool
description: Join, parse, convert, and flatten Map values.
tags:
  - Map
  - conversion
  - Java
layout: layouts/aj-util.njk
---

# MapTool

`MapTool` contains static operations for serializing simple string-keyed maps, parsing key/value inputs, converting map values, and flattening nested maps. XML conversion is not part of `MapTool`; use `XmlHelper` for XML work.

## Join map entries

```java
Map<String, Object> values = new LinkedHashMap<>();
values.put("name", "Ada");
values.put("page", null);

String queryLike = MapTool.join(values); // name=Ada&page=
String custom = MapTool.join(values, ";", value -> String.valueOf(value));
```

`join` writes `key=value` pairs in the map's iteration order. Its default delimiter is `&`; null values become empty strings. It does not URL-encode keys or values—apply `UrlCodec` first when producing an HTTP query string.

## Create maps from key/value input

```java
Map<String, Object> fromPairs = MapTool.toMap(
        new String[]{"id=42", "page=3"}, Integer::valueOf);

Map<String, Object> fromColumns = MapTool.toMap(
        new String[]{"id", "name"}, new String[]{"42", "Ada"}, null);

Map<String, String> query = MapTool.toMap("name=Ada+Lovelace&token=x%3D%3D");
```

For array pairs, only the first `=` separates key and value; `null` entries are ignored, and an entry without `=` is rejected. Parallel key/value arrays must have the same length. The query-string overload decodes form encoding through `UrlCodec`; duplicate keys are overwritten by later values.

## Convert and flatten values

```java
Map<String, Integer> lengths = MapTool.as(values, value -> value.toString().length());

Map<String, Object> nested = new LinkedHashMap<>();
nested.put("server", Collections.singletonMap("host", "localhost"));
Map<String, Object> flat = MapTool.flatten(nested);
// {server.host=localhost}
```

`as` converts non-null values with the supplied function and preserves null values. `flatten` joins nesting levels with `.`. A literal `.` in a source key is escaped as `\\.`, and a literal backslash is escaped as `\\\\`; empty nested maps remain values. Circular map references are rejected with `IllegalArgumentException`.

## Detailed parsing behavior

The two `toMap` families intentionally differ when malformed input is encountered:

```java
Map<String, Object> strict = MapTool.toMap(
        new String[]{"token=header.payload=signature=="}, null);
// strict.get("token") is "header.payload=signature=="

Map<String, String> lenient = MapTool.toMap("flag&name=Ada");
// "flag" has no '=' and is ignored; lenient contains only name=Ada
```

The array form rejects a non-null pair without `=` because it normally represents structured configuration input. The
query-string form is more tolerant and ignores fields without a complete key/value pair. Neither form URL-encodes
output from `join`, and neither preserves multiple values for the same key.

For flattening, keys are paths rather than reversible original keys unless the caller understands the escaping rules:

```java
Map<String, Object> nested = new LinkedHashMap<>();
nested.put("a.b", Collections.singletonMap("c", 42));

Map<String, Object> flat = MapTool.flatten(nested);
// {a\\.b.c=42}
```

`flatten(null)` returns an empty `LinkedHashMap`. Lists, arrays, primitive values, and null are leaves; only `Map`
values are traversed.

## Implementation notes

`join` is a direct iteration over `map.keySet()`, so its output order is the concrete map's iteration order. Use a
`LinkedHashMap` when stable serialized order matters. The parser splits at most once (`split("=", 2)`), which is why
Base64 values, JWT-like strings, and signatures containing `=` remain intact.

`flatten` uses depth-first recursion and an identity-based set of maps currently on the recursion path. Identity rather
than `equals()` detects an actual object cycle without treating two equal-but-independent maps as a cycle. The current
path is removed after each branch, so reusing the same non-cyclic child map in two separate branches is supported.
