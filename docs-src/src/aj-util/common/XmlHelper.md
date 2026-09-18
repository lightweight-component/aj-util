---
title: XmlHelper
description: Secure XML parsing, XPath queries, and XML/Map conversion.
tags:
  - XML
  - parsing
  - Java
layout: layouts/aj-util.njk
---

# XmlHelper

`XmlHelper` parses XML strings, queries DOM nodes, and converts simple XML structures to and from maps. It does not read file paths: every `xml` argument is XML content.

## Secure parsing and queries

`initBuilder()` creates a `DocumentBuilder` with DTD declarations, external entities, external DTD/schema access, XInclude, and entity expansion disabled. `initBuilder(boolean namespaceAware)` controls namespace awareness while retaining these security settings. Parsing failures use sanitized messages and do not append the full XML input.

```java
String xml = "<root><item id=\"7\">value</item></root>";

Element root = XmlHelper.getRoot(xml);
XmlHelper.xPath(xml, "/root/item", node -> System.out.println(node.getTextContent()));
XmlHelper.parseXML(xml, rootNode -> System.out.println(rootNode.getNodeName()));
```

`xPath` invokes the consumer for each selected node. `parseXML` invokes its `Consumer<Node>` once with the root element. `nodeAsMap(xml, xpath)` returns the selected node's attributes; `getNodeAttribute(node, name)` returns `null` if the attribute is absent or the node has no attributes. `getInnerXml(node)` returns the node's child markup.

## Map and bean conversion

```java
String generated = XmlHelper.mapToXml(Collections.singletonMap("name", "Ada"));
Map<String, String> values = XmlHelper.xmlToMap("<xml><name>Ada</name></xml>");
Map<String, Object> repeated = XmlHelper.xmlToMultiValueMap(
        "<xml><tag>a</tag><tag>b</tag></xml>");
```

`mapToXml` produces an `<xml>` root and serializes map entries as elements. `xmlToMap` keeps one value per element name; `xmlToMultiValueMap` preserves repeated names as collections. `beanToXml` serializes JavaBean properties. XML element names must be valid; these helpers are for simple data exchange rather than arbitrary XML schema processing.

## Node-level operations

```java
String xml = "<root><user id=\"7\"><name>Ada</name></user></root>";
Element root = XmlHelper.getRoot(xml);
Element user = (Element) root.getElementsByTagName("user").item(0);

String id = XmlHelper.getNodeAttribute(user, "id");
String inner = XmlHelper.getInnerXml(user); // <name>Ada</name>
Map<String, String> attributes = XmlHelper.nodeAsMap(xml, "/root/user");
```

`getNodeAttribute` returns `null` for a missing attribute; it does not distinguish a missing attribute from an
attribute whose value is absent. `nodeAsMap` is useful for attributes only—element text belongs to the node content.
XPath expressions use the JDK XPath implementation; pass a namespace-aware builder only when directly working with
namespaced DOM documents.

## Conversion limits

`mapToXml`, `xmlToMap`, and `xmlToMultiValueMap` are deliberately simple conventions rather than a general XML
binding framework. They do not preserve arbitrary mixed content, namespace semantics, comments, processing
instructions, schema constraints, or every possible Java object graph. Use a dedicated binding library when those
features are required.

## Implementation notes

The parser is built from the JDK DOM APIs. Before parsing, `XmlHelper` disables known XXE entry points on the factory
and builder configuration: DTD processing, external general/parameter entities, external DTD/schema access, XInclude,
and entity expansion. This is defense in depth for ordinary XML input, not a substitute for resource limits or a
trusted-input policy when processing very large documents.

XPath selection evaluates against the parsed DOM and passes the resulting nodes to the consumer. Map conversion walks
the DOM's direct elements; `xmlToMultiValueMap` exists because a plain map cannot otherwise represent repeated element
names without losing data.
