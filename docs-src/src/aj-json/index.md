---
title: aj-json
layout: layouts/aj-util.njk
---

# aj-json: a JSON parser made for learning

aj-json is a small project created to learn how JSON parsing works and to explore a small piece of compiler theory. It is **not a production-ready JSON library**. Treat it as readable study material: run it, change it, and follow the code from characters to Java objects.

For production systems, choose a maintained JSON library such as Jackson, Gson, or JSON-B. Do not use aj-json for untrusted input, compatibility-sensitive APIs, or data that must be handled without surprises.

## What you can learn here

When a program receives JSON text, it needs to answer two simple questions:

1. What does each piece of text mean? For example, `{` starts an object and `true` is a boolean.
2. In what order may these pieces appear? For example, an object key must be followed by `:` and then a value.

aj-json separates those jobs. `Lexer` reads the characters and produces small labels called tokens. `FMS` (the state machine) reads those tokens in order and builds ordinary Java values.

## Important limits

- There is no supported object-to-JSON serializer in the current source tree.
- The parser is intentionally small, not a complete implementation or a compatibility promise for the JSON standard.
- It accepts some relaxed input, including unquoted object keys and single-quoted strings. Those forms are not standard JSON.
- Object order is not preserved because parsed objects use `HashMap`.
- Numbers become `Integer`, `Long`, or `Double`; this is unsuitable when exact decimal precision matters.
- It is not designed, tested, or hardened for production traffic or hostile input.

## Start here

Add the dependency only if you are following the example or reading the source:

```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-json</artifactId>
    <version>1.6</version>
</dependency>
```

Then continue to [parse your first JSON](/aj-json/parse/). When you are ready to see the moving parts, read [how the parser works](/aj-json/how-it-works/).

## Source and references

- [Source code](https://github.com/lightweight-component/aj-util/tree/main/aj-json)
- [Maven Central](https://central.sonatype.com/artifact/com.ajaxjs/aj-json)
- [Javadoc](https://javadoc.io/doc/com.ajaxjs/aj-json)
