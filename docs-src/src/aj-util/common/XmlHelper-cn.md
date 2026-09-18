---
title: XmlHelper
description: 安全 XML 解析、XPath 查询与 XML/Map 转换。
tags:
  - XML
  - 解析
  - Java
layout: layouts/aj-util-cn.njk
---

# XmlHelper

`XmlHelper` 用于解析 XML 字符串、查询 DOM 节点，以及在简单 XML 结构和 Map 之间转换。所有 `xml` 参数都是 XML 内容，不是文件路径。

## 安全解析与查询

`initBuilder()` 创建的 `DocumentBuilder` 会禁用 DTD 声明、外部实体、外部 DTD/Schema 访问、XInclude 和实体展开。`initBuilder(boolean namespaceAware)` 仅控制命名空间感知，仍保留这些安全设置。解析失败使用脱敏消息，不会附带完整 XML 内容。

```java
String xml = "<root><item id=\"7\">value</item></root>";

Element root = XmlHelper.getRoot(xml);
XmlHelper.xPath(xml, "/root/item", node -> System.out.println(node.getTextContent()));
XmlHelper.parseXML(xml, rootNode -> System.out.println(rootNode.getNodeName()));
```

`xPath` 会对每个选中的节点调用消费者。`parseXML` 使用 `Consumer<Node>`，仅对根元素调用一次。`nodeAsMap(xml, xpath)` 返回所选节点属性；节点没有属性或属性不存在时，`getNodeAttribute(node, name)` 返回 `null`。`getInnerXml(node)` 返回节点的子标记内容。

## Map 与 Bean 转换

```java
String generated = XmlHelper.mapToXml(Collections.singletonMap("name", "Ada"));
Map<String, String> values = XmlHelper.xmlToMap("<xml><name>Ada</name></xml>");
Map<String, Object> repeated = XmlHelper.xmlToMultiValueMap(
        "<xml><tag>a</tag><tag>b</tag></xml>");
```

`mapToXml` 生成 `<xml>` 根节点，并将 Map 条目序列化为元素。`xmlToMap` 对每个元素名只保留一个值；`xmlToMultiValueMap` 会将重复元素名保留为集合。`beanToXml` 序列化 JavaBean 属性。这些方法要求合法的 XML 元素名，适用于简单数据交换，不适合处理任意 XML Schema。

## 节点级操作

```java
String xml = "<root><user id=\"7\"><name>Ada</name></user></root>";
Element root = XmlHelper.getRoot(xml);
Element user = (Element) root.getElementsByTagName("user").item(0);

String id = XmlHelper.getNodeAttribute(user, "id");
String inner = XmlHelper.getInnerXml(user); // <name>Ada</name>
Map<String, String> attributes = XmlHelper.nodeAsMap(xml, "/root/user");
```

属性不存在时 `getNodeAttribute` 返回 `null`；它不区分“属性不存在”和“属性值为空”。`nodeAsMap` 仅处理属性，元素文本属于节点内容。XPath 使用 JDK XPath 实现；只有直接处理带命名空间的 DOM 文档时，才需要使用支持命名空间的 builder。

## 转换边界

`mapToXml`、`xmlToMap` 和 `xmlToMultiValueMap` 是约定明确的简单转换工具，并非通用 XML 绑定框架。它们不保留任意混合内容、命名空间语义、注释、处理指令、Schema 约束或所有 Java 对象图。需要这些能力时，应使用专门的数据绑定库。

## 实现原理与取舍

解析器基于 JDK DOM API 构建。解析前，`XmlHelper` 会在工厂和 builder 配置上关闭常见 XXE 入口：DTD 处理、外部通用/参数实体、外部 DTD/Schema 访问、XInclude 和实体展开。这是面向普通 XML 输入的纵深防御；处理超大文档时，仍不能替代资源上限或可信输入策略。

XPath 选择在解析后的 DOM 上执行，并将结果节点交给消费者。Map 转换会遍历 DOM 的直接元素；由于普通 Map 无法表示重复元素名而不丢失数据，才提供了 `xmlToMultiValueMap`。
