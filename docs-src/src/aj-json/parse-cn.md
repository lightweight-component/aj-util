---
title: 动手解析 JSON
layout: layouts/aj-util-cn.njk
alternate: /parse/
---

# 动手解析 JSON

用于学习的公开入口是 `FMS`。将一段 JSON（或接近 JSON 的文本）交给它，再调用 `parse()`。

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

之所以需要类型转换，是因为 `parse()` 可能返回多种 Java 值。不完全确定输入内容时，应先判断根节点类型，再进行转换。

## 会得到哪些 Java 值？

| 写入的文本 | 得到的 Java 值 |
| --- | --- |
| `{...}` | `HashMap` |
| `[...]` | `ArrayList` |
| `"hello"` 或 `'hello'` | `String` |
| `true` / `false` | `Boolean` |
| `null` | `null` |
| `12` | `Integer`；超过 `Integer` 范围时为 `Long` |
| `3.14` | `Double` |

对象和数组可以嵌套：嵌套对象仍然是 `Map`，嵌套数组仍然是 `List`。

## 标准 JSON 与这个学习解析器

标准 JSON 要求键和字符串使用双引号：

```json
{"name":"Ada","active":true}
```

aj-json 还接受下面这种更好输入的写法，方便观察词法分析过程，但它不是可互通的标准 JSON：

```text
{name: 'Ada', active: true}
```

学习时可以分别尝试两种写法，再观察 token 的差异。真实接口里始终应收发标准 JSON，并交给成熟的 JSON 库处理。

## 出错也是学习的一部分

格式不正确时通常会抛出 `JsonParseException`。异常信息会带上字符位置、行和列，帮助定位解析器在哪个地方无法继续。

```java
try {
    new FMS("{name: }").parse();
} catch (RuntimeException e) {
    System.out.println(e.getMessage());
}
```

这个示例用于理解语法错误如何出现，不应把异常文案当作稳定的生产接口。

下一步阅读[解析器如何工作](/aj-json/how-it-works-cn/)。
