---
title: How the parser works
layout: layouts/aj-util.njk
alternate: /how-it-works-cn/
---

# How the parser works

The easiest way to read aj-json is to think of it as a small assembly line. A string enters at one end; Java collections come out at the other.

```text
JSON text  →  Lexer  →  tokens  →  FMS state machine  →  Map / List / value
```

## Step 1: split text into meaningful pieces

`Lexer` moves through the input one character at a time. It turns punctuation such as `{`, `}`, `[`, `]`, `,`, and `:` into tokens. It also groups nearby characters into strings, numbers, and the words `true`, `false`, and `null`.

For `{name: 7}`, the important pieces are roughly: object start, `name`, colon, `7`, object end. The lexer also records line and column information, so later errors can point back to the place that caused trouble.

## Step 2: check the order

Seeing valid pieces is not enough. `{ name 7 }` has familiar pieces but is missing `:`. `FMS` is a finite-state machine: at each moment it knows what may come next. After an object key it expects `:`; after a value in an array it expects either `,` or `]`.

This is the small compiler idea at the heart of the project: first recognise words, then check that their order forms a sentence.

## Step 3: build values with stacks

`Operator` keeps a few stacks while the state machine runs:

- an object stack for maps and lists currently being built;
- a key stack for object keys waiting for their value;
- a state stack for returning to the surrounding array or object.

When it sees `{`, it starts a `HashMap`. When it sees `[`, it starts an `ArrayList`. When a nested value ends, it is placed into the collection that was being built around it. That is why arrays and objects can sit inside each other.

## A useful experiment

Start with a tiny input, put a breakpoint in `Lexer.next()`, then step through calls to `FMS.parse()`:

```java
Object value = new FMS("{a:[1, true]}").parse();
```

Watch how each token changes the state and how the `Map` and `List` grow. Next, remove the comma or closing bracket and see where `JsonParseException` is produced. This is more useful than memorising the state table.

## Why not use this design in an application?

The code is deliberately compact and educational. Production JSON processing needs strict standards behaviour, predictable numeric handling, security review, performance work, broad test coverage, streaming options, and long-term compatibility. aj-json does not aim to provide those things.

Return to [parse your first JSON](/aj-json/parse/) or the [overview](/aj-json/).
