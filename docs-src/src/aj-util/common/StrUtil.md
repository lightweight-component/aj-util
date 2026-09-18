---
title: StrUtil
description: String templates and array/list joining.
tags:
  - string
  - template
layout: layouts/aj-util.njk
---

# StrUtil

`StrUtil` currently provides simple template replacement and array/list joining. It does not provide padding, character counting, or membership helpers.

## Templates

The Map overload replaces `${name}` placeholders. Missing or null map values become empty strings.

```java
Map<String, Object> values = new HashMap<>();
values.put("name", "Ada");
String message = StrUtil.simpleTpl("Hello, ${name}! ${missing}", values);
// Hello, Ada!
```

The JavaBean overload replaces `#{property}` placeholders with readable bean properties. A null bean property becomes the text `null`; write-only properties are skipped. Replacement values are literal, so `$` and backslashes are safe. If a getter fails, the thrown `RuntimeException` identifies the property and retains the getter exception as its cause.

```java
String message = StrUtil.simpleTpl("Hello, #{name}!", user);
```

## Join arrays and lists

```java
String array = StrUtil.join(new String[]{"a", null, "c"}, "&");
String list = StrUtil.join(Arrays.asList("a", "b", "c"), "[%s]", ", ");
// a&&c
// [a], [b], [c]
```

Available overloads accept a generic array, a string array with a format template, and string lists with an optional template. Null elements are rendered as empty strings.

## Placeholder and formatting rules

The two template forms deliberately use different placeholder syntax:

| Overload | Placeholder | Missing/null value |
| --- | --- | --- |
| `simpleTpl(String, Map)` | `${word}` | empty string |
| `simpleTpl(String, Object)` | `#{property}` | text `null` for a readable null property |

Map placeholders match word characters only (`${name_1}` is valid; `${user.name}` is not a placeholder for this API).
Unrecognized text remains unchanged. The Bean overload inspects JavaBean getter properties, including inherited ones;
it does not read fields directly.

```java
Map<String, Object> values = new HashMap<>();
values.put("path", "$1\\temp");
String result = StrUtil.simpleTpl("path=${path}", values);
// path=$1\temp
```

This works because replacements are quoted before applying `Matcher.appendReplacement`; `$1` is data, not a regex
back-reference. For list joins, the template is passed to `String.format`, so it should contain an appropriate
conversion such as `%s`.

## Choosing the right helper

`StrUtil` intentionally focuses on small formatting operations. For regular-expression replacement use
`RegExpHelper`; for URL/form encoding use `UrlCodec`; for bytes, character sets, and hexadecimal conversion use
`StringBytes`.

## Implementation notes

The Map template overload scans `${word}` with one precompiled `Pattern` and builds the result with
`Matcher.appendReplacement` / `appendTail`. Quoting replacement text prevents the regex engine from interpreting `$`
or backslashes in a value. The Bean overload instead uses JavaBeans introspection and ordinary `String.replace` for
each readable property, which explains both its `#{property}` syntax and its different null behavior.

Join methods use a `StringBuilder` and append the delimiter only between elements, avoiding a trailing delimiter.
They intentionally do not escape values or apply locale-sensitive formatting; perform those transformations before
joining when required.
