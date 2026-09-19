---
title: Parse your first JSON
layout: layouts/aj-util.njk
alternate: /parse-cn/
---

# Parse your first JSON

The public learning entry point is `FMS`. Give it a JSON-like string and call `parse()`.

```java
import com.ajaxjs.jsonparser.syntax.FMS;
import java.util.List;
import java.util.Map;

String text = "{name: 'Ada', active: true, scores: [95, 88]}";
Object result = new FMS(text).parse();

Map<String, Object> person = (Map<String, Object>) result;
System.out.println(person.get("name"));       // Ada
System.out.println(person.get("active"));     // true

List<Object> scores = (List<Object>) person.get("scores");
System.out.println(scores.get(0));             // 95
```

The cast is necessary because `parse()` can return several kinds of values. Check the root value before casting when you do not control the input.

## What Java values are returned?

| Text you write | Java value returned |
| --- | --- |
| `{...}` | `HashMap` |
| `[...]` | `ArrayList` |
| `"hello"` or `'hello'` | `String` |
| `true` / `false` | `Boolean` |
| `null` | `null` |
| `12` | `Integer`, or `Long` when it does not fit in `Integer` |
| `3.14` | `Double` |

Objects and arrays may be nested. Each nested object is another `Map`; each nested array is another `List`.

## Standard JSON versus this learning parser

Standard JSON requires double-quoted keys and strings:

```json
{"name":"Ada","active":true}
```

aj-json also accepts the easier-to-type form below because it is useful for observing the lexer, but it is not portable JSON:

```text
{name: 'Ada', active: true}
```

When learning, try both forms and inspect the tokens. In a real API, always emit and expect standard JSON, then use a mature JSON library to handle it.

## Errors are part of the lesson

Malformed input usually throws `JsonParseException`. Its message includes a character, line, and column position to help you find where the parser became confused.

```java
try {
    new FMS("{name: }").parse();
} catch (RuntimeException e) {
    System.out.println(e.getMessage());
}
```

This example is for learning how syntax errors surface. Do not use exception messages as a stable production interface.

Next, see [how the parser works](/aj-json/how-it-works/).
