/**
 * Copyright Sp42 frank@ajaxjs.com Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.ajaxjs.util;

import org.w3c.dom.*;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * XML Processing Utility Class - Provides methods for parsing, manipulating, and querying XML documents.
 * <a href="https://blog.csdn.net/axman/article/details/420910">...</a>
 *
 * <p>This class simplifies common XML operations including XPath queries, DOM parsing,
 * node text extraction, attribute retrieval, and conversion between XML formats.
 * It provides a clean API for working with XML in Java applications.
 *
 * @author sp42 frank@ajaxjs.com
 */
public class XmlHelper {
    /**
     * Initializes and returns a DocumentBuilder for XML parsing operations.
     *
     * @return A DocumentBuilder instance for XML transformation and parsing
     */
    public static DocumentBuilder initBuilder() {
        return initBuilder(false);
    }

    /**
     * Initializes and returns a DocumentBuilder with the requested namespace awareness
     * and secure-processing restrictions applied. DTD declarations, external entities,
     * external DTD/schema access, XInclude, and entity-reference expansion are disabled.
     *
     * @param namespaceAware whether the builder should be namespace-aware
     * @return a configured DocumentBuilder
     */
    public static DocumentBuilder initBuilder(boolean namespaceAware) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(namespaceAware);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, CommonConstant.EMPTY_STRING);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, CommonConstant.EMPTY_STRING);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) {
                    // Parser warnings are intentionally not written to stderr.
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }
            });

            return builder;
        } catch (ParserConfigurationException | IllegalArgumentException e) {
            throw new IllegalStateException("Unable to initialize secure XML parser.", e);
        }
    }

    /**
     * Retrieves a specific node using XPath query.
     *
     * @param xml   The XML content
     * @param xpath The XPath expression to locate nodes
     * @param fn    The consumer function to process each matched Node
     */
    public static void xPath(String xml, String xpath, Consumer<Node> fn) {
        Objects.requireNonNull(xml, "xPath.xml");
        Objects.requireNonNull(xpath, "xPath.xpath");
        Objects.requireNonNull(fn, "xPath.fn");

        try {
            XPathExpression expr = XPathFactory.newInstance().newXPath().compile(xpath);
            NodeList nodes = (NodeList) expr.evaluate(getRoot(xml), XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++)
                fn.accept(nodes.item(i));
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("Invalid XPath expression: " + xpath, e);
        }
    }


    /**
     * Converts all attributes of a node to a map.
     *
     * @param xml   The XML content
     * @param xpath The XPath expression to locate the node
     * @return A map containing attribute names as keys and attribute values as values
     */
    public static Map<String, String> nodeAsMap(String xml, String xpath) {
        Map<String, String> map = new HashMap<>();

        xPath(xml, xpath, node -> {
            NamedNodeMap _map = node.getAttributes();

            if (_map != null) {
                for (int i = 0, n = _map.getLength(); i < n; i++) {
                    Node _node = _map.item(i);
                    map.put(_node.getNodeName(), _node.getNodeValue());
                }
            }
        });

        return map;
    }

    /**
     * Parses XML content and processes nodes with a consumer function.
     *
     * @param xml The XML content to parse
     * @param fn  The bi-consumer function to process each Node
     */
    public static void parseXML(String xml, Consumer<Node> fn) {
        Objects.requireNonNull(xml, "parseXML.xml");
        Objects.requireNonNull(fn, "parseXML.fn");

        NodeList nodeList = getRoot(xml).getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++)
            fn.accept(nodeList.item(i));
    }

    /**
     * Gets the root element from the given XML string.
     *
     * @param xml The XML string content
     * @return The root Element of the XML document
     */
    public static Element getRoot(String xml) {
        Objects.requireNonNull(xml, "getRoot.xml");

        try (InputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            return initBuilder().parse(in).getDocumentElement();
        } catch (SAXException | IOException e) {
            throw new IllegalArgumentException("Invalid XML content.", e);
        }
    }

    /**
     * Gets the text content within a node, including nested tags.
     *
     * @param node The node object to extract text from
     * @return The inner text content
     */
    public static String getInnerXml(Node node) {
        Objects.requireNonNull(node, "getInnerXml.node");
        Object feature = node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");

        if (!(feature instanceof DOMImplementationLS))
            throw new IllegalStateException("DOM Load and Save 3.0 is not supported.");

        DOMImplementationLS lsImpl = (DOMImplementationLS) feature;
        LSSerializer lsSerializer = lsImpl.createLSSerializer();
        DOMConfiguration domConfig = lsSerializer.getDomConfig();
        domConfig.setParameter("xml-declaration", false);
        NodeList childNodes = node.getChildNodes();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < childNodes.getLength(); i++)
            sb.append(lsSerializer.writeToString(childNodes.item(i)));

        return sb.toString();
    }

    /**
     * Gets a specific attribute value from a node.
     *
     * @param node     The node object
     * @param attrName The name of the attribute to retrieve, it's case-sensitive
     * @return The attribute value, or {@code null} if the node has no attributes or the named attribute is absent
     */
    public static String getNodeAttribute(Node node, String attrName) {
        Objects.requireNonNull(node, "getNodeAttribute.node");
        Objects.requireNonNull(attrName, "getNodeAttribute.attrName");

        NamedNodeMap attributes = node.getAttributes();

        if (attributes == null)
            return null;

        Node attr = attributes.getNamedItem(attrName);

        return attr == null ? null : attr.getNodeValue();
    }

    private static final Pattern XML_ELEMENT_NAME = Pattern.compile("[_\\p{L}][_\\p{L}\\p{N}\\p{M}.-]*");

    /**
     * 将给定的对象转换为 XML 格式的字符串
     *
     * @param bean 要转换的对象
     * @return 转换后的XML格式的字符串
     */
    public static String beanToXml(Object bean) {
        Objects.requireNonNull(bean, "beanToXml.bean");

        return mapToXml(JsonUtil.pojo2map(bean));
    }

    /**
     * 将 Map 转换为 XML 格式的字符串
     *
     * @param data Map 类型数据
     * @return XML 格式的字符串
     * @throws IllegalArgumentException 如果 Map key 不是合法的 XML 元素名
     */
    public static String mapToXml(Map<String, ?> data) {
        Objects.requireNonNull(data, "mapToXml.data");

        Document doc = initBuilder().newDocument();
        Element root = doc.createElement("xml");
        doc.appendChild(root);

        data.forEach((key, value) -> {
            if (key == null || !XML_ELEMENT_NAME.matcher(key).matches())
                throw new IllegalArgumentException("Invalid XML element name for map key: " + key);

            if (value instanceof Iterable) {
                for (Object item : (Iterable<?>) value)
                    appendElement(doc, root, key, item);
            } else
                appendElement(doc, root, key, value);
        });

        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.toString();
        } catch (TransformerException e) {
            throw new IllegalStateException("Unable to convert Map to XML.", e);
        }
    }

    private static void appendElement(Document doc, Element root, String key, Object value) {
        Element field = doc.createElement(key);

        if (value != null)
            field.appendChild(doc.createTextNode(value.toString()));

        root.appendChild(field);
    }

    /**
     * Converts the direct child elements of the XML root into a simple map.
     * <p>
     * If duplicate element names exist, later values overwrite earlier ones.
     */
    public static Map<String, String> xmlToMap(String xml) {
        Objects.requireNonNull(xml, "xmlToMap.xml");
        Map<String, String> data = new HashMap<>();
        NodeList nodes = getRoot(xml).getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                data.put(element.getNodeName(), element.getTextContent());
            }
        }

        return data;
    }

    /**
     * Converts the direct child elements of the XML root into a multi-value map.
     * <p>
     * A single element is stored as a String.
     * Repeated elements are stored as a List<String>.
     */
    public static Map<String, Object> xmlToMultiValueMap(String xml) {
        Objects.requireNonNull(xml, "xmlToMultiValueMap.xml");

        Map<String, Object> data = new LinkedHashMap<>();
        NodeList nodes = getRoot(xml).getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element element = (Element) node;
            String key = element.getNodeName();
            String value = element.getTextContent();
            Object oldValue = data.get(key);

            if (oldValue == null)
                data.put(key, value);
            else if (oldValue instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> values = (List<String>) oldValue;
                values.add(value);
            } else {
                List<String> values = new ArrayList<>();
                values.add(oldValue.toString());
                values.add(value);

                data.put(key, values);
            }
        }

        return data;
    }
}
