---
title: RegExpHelper
description: Cached, object-oriented regular-expression matching.
tags:
  - regular expression
  - Java
layout: layouts/aj-util.njk
---

# RegExpHelper

`RegExpHelper` is the current regular-expression API. It owns one compiled `Pattern` and exposes matching operations through an object. `RegExpUtils` has been removed.

## Create and reuse a helper

Creating from a regular-expression string uses a shared thread-safe compiled-pattern cache. Reuse a helper when performing several operations with the same expression.

```java
RegExpHelper email = new RegExpHelper("[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}");

boolean found = email.contains("Contact a@ajaxjs.com");
boolean exact = email.fullMatch("a@ajaxjs.com");
```

`contains()` uses `Matcher.find()`; `fullMatch()` uses `Matcher.matches()` and therefore requires the entire input to match.

## Extract matches

```java
RegExpHelper number = new RegExpHelper("item-(\\d+)");

String first = number.match("item-42", 1);  // "42"
String lastGroup = number.match("item-42", -1); // last capture group
String[] all = number.matchAll("item-1,item-2");
```

`match(text)` returns group 0 of the first match. `match(text, -1)` returns the last capture group. Both return `null` when there is no match; an invalid group index follows the JDK `Matcher` exception behavior.

You can also pass an already compiled `Pattern`. It is used as-is and is not added to the shared string-pattern cache.

```java
RegExpHelper helper = new RegExpHelper(Pattern.compile("\\d+"));
```

For compatibility-style one-off calls, `RegExpHelper.match(regexp, text)` and `RegExpHelper.regMatch(regexp, text, groupIndex)` delegate to the object API and use the same cache.

## Matcher access and edge cases

`getMatcher(text)` exposes a fresh `Matcher` when cursor-level operations are needed. `Pattern` is immutable and may
be shared; `Matcher` is stateful, so do not share a `Matcher` between threads or reuse it after changing its state.

```java
RegExpHelper helper = new RegExpHelper("a(b)");
Matcher matcher = helper.getMatcher("zabz");

if (matcher.find()) {
    String whole = matcher.group();  // "ab"
    String captured = matcher.group(1); // "b"
}
```

The string-pattern cache is shared by all helpers and by `getPattern(String)`; two helpers constructed from the same
text receive the same `Pattern` instance. It is intended for repeated expressions, not unbounded user-supplied
expressions. Invalid regular expressions throw the JDK `PatternSyntaxException`; null input follows JDK null checks.

`matchAll` returns group 0 for every non-overlapping `find()` result. It does not return capture groups—call
`getMatcher` when all groups from all matches are required.

## Implementation notes

Regular-expression compilation is relatively expensive compared with creating a `Matcher`. `RegExpHelper` therefore
stores patterns in a `ConcurrentHashMap` and obtains them with `computeIfAbsent`: concurrent callers requesting the
same expression reuse a compiled `Pattern` without external locking. The cache deliberately stores only patterns
created from strings; a caller-supplied `Pattern` remains wholly under that caller's control.

Each matching method creates a new matcher because `Matcher` holds input and cursor state. This keeps normal helper
operations independent even when the underlying compiled pattern is shared.
