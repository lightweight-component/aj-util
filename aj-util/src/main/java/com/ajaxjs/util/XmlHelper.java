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
 * Utility class for parsing, querying, serializing and converting XML data.
 *
 * <p>This class is based on the standard Java DOM, XPath and Transformer APIs
 * and provides convenience methods for common XML operations, including:</p>
 *
 * <ul>
 *     <li>secure DOM parser creation</li>
 *     <li>XPath queries</li>
 *     <li>root-element parsing</li>
 *     <li>node attribute access</li>
 *     <li>inner-XML serialization</li>
 *     <li>Java bean and map to XML conversion</li>
 *     <li>XML to simple or multi-value map conversion</li>
 * </ul>
 *
 * <p>The DOM parser created by this class disables DTD declarations,
 * external entities, external DTD/schema access, XInclude and entity
 * expansion in order to reduce exposure to XXE and related XML attacks.</p>
 *
 * <p>Unless explicitly stated otherwise, XML strings are interpreted as
 * UTF-8 encoded content.</p>
 * <p>
 * Ref: <a href="https://blog.csdn.net/axman/article/details/420910">...</a>
 */
public class XmlHelper {
    /**
     * Creates a secure, non-namespace-aware {@link DocumentBuilder}.
     *
     * <p>This is equivalent to:</p>
     *
     * <pre>{@code
     * initBuilder(false)
     * }</pre>
     *
     * @return configured secure document builder
     * @throws IllegalStateException if a secure XML parser cannot be created
     */
    public static DocumentBuilder initBuilder() {
        return initBuilder(false);
    }

    /**
     * Creates a secure {@link DocumentBuilder}.
     *
     * <p>The returned parser applies the following restrictions:</p>
     *
     * <ul>
     *     <li>DOCTYPE declarations are disabled</li>
     *     <li>external general entities are disabled</li>
     *     <li>external parameter entities are disabled</li>
     *     <li>external DTD loading is disabled</li>
     *     <li>external DTD access is disabled</li>
     *     <li>external schema access is disabled</li>
     *     <li>XInclude processing is disabled</li>
     *     <li>entity-reference expansion is disabled</li>
     *     <li>secure-processing mode is enabled</li>
     * </ul>
     *
     * <p>Parser warnings are ignored, while parsing errors and fatal errors are propagated.</p>
     *
     * @param namespaceAware whether namespace processing should be enabled
     * @return configured secure document builder
     * @throws IllegalStateException if the parser cannot be configured or created securely
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
     * Evaluates an XPath expression against the root element of an XML
     * document and passes each matched node to the supplied consumer.
     *
     * <p>The XPath expression is evaluated against the element returned by
     * {@link #getRoot(String)}, not against the {@link Document} object
     * itself.</p>
     *
     * <p>The current implementation uses a non-namespace-aware parser.
     * XPath expressions using XML namespace prefixes therefore require
     * additional namespace handling not provided by this method.</p>
     *
     * @param xml   XML content
     * @param xpath XPath expression
     * @param fn    consumer invoked for every matching node
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if the XML is invalid or the XPath expression cannot be compiled
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
     * Collects the attributes of all nodes matched by the supplied XPath
     * expression into a map.
     *
     * <p>Attribute names are used as map keys and attribute values as map
     * values.</p>
     * <p>If multiple matched nodes contain attributes with the same name,
     * values from later matched nodes overwrite earlier values.</p>
     * <p>Nodes without attributes are ignored.</p>
     *
     * @param xml   XML content
     * @param xpath XPath's expression used to locate nodes
     * @return map containing attribute names and values; never {@code null}
     * @throws NullPointerException     if {@code xml} or {@code xpath} is  {@code null}
     * @throws IllegalArgumentException if the XML or XPath expression is invalid
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
     * Parses XML and passes each direct child node of the root element to
     * the supplied consumer.
     *
     * <p>This method iterates over the complete DOM child-node list.
     * Therefore the consumer may receive not only element nodes, but also
     * text nodes, comments, CDATA sections and other DOM node types.</p>
     *
     * <p>Whitespace between elements is commonly represented as text nodes.
     * Callers interested only in elements should check:</p>
     *
     * <pre>{@code
     * if (node.getNodeType() == Node.ELEMENT_NODE) {
     *     ...
     * }
     * }</pre>
     *
     * @param xml XML content
     * @param fn  consumer invoked for every direct child node
     * @throws NullPointerException     if {@code xml} or {@code fn} is {@code null}
     * @throws IllegalArgumentException if the XML is invalid
     */
    public static void parseXML(String xml, Consumer<Node> fn) {
        Objects.requireNonNull(xml, "parseXML.xml");
        Objects.requireNonNull(fn, "parseXML.fn");

        NodeList nodeList = getRoot(xml).getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++)
            fn.accept(nodeList.item(i));
    }

    /**
     * Parses XML content and returns its document root element.
     *
     * <p>The XML string is converted to UTF-8 bytes and parsed using the
     * secure, non-namespace-aware parser returned by {@link #initBuilder()}.</p>
     *
     * @param xml XML document content
     * @return document root element
     * @throws NullPointerException     if {@code xml} is {@code null}
     * @throws IllegalArgumentException if the XML cannot be parsed
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
     * Serializes the child nodes of the supplied DOM node and returns its
     * inner XML.
     *
     * <p>The supplied node itself is not serialized. Only its children are
     * serialized and concatenated.</p>
     *
     * <p>For example, given:</p>
     *
     * <pre>{@code
     * <item>Hello <b>World</b></item>
     * }</pre>
     *
     * <p>the result is conceptually:</p>
     *
     * <pre>{@code
     * Hello <b>World</b>
     * }</pre>
     *
     * <p>This method therefore differs from
     * {@link Node#getTextContent()}, which returns textual content only and
     * does not preserve nested XML markup.</p>
     *
     * @param node DOM node whose child nodes should be serialized
     * @return serialized inner XML, or an empty string if the node has no
     * children
     * @throws NullPointerException  if {@code node} is {@code null}
     * @throws IllegalStateException if DOM Load and Save 3.0 serialization is not supported
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
     * Returns the value of a named attribute from a DOM node.
     *
     * <p>Attribute lookup is case-sensitive.</p>
     *
     * @param node     DOM node
     * @param attrName attribute name
     * @return attribute value, or {@code null} if the node has no attributes or the requested attribute does not exist
     * @throws NullPointerException if {@code node} or {@code attrName} is {@code null}
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

    /**
     * Pattern used to validate simple XML element names generated from map keys.
     *
     * <p>The accepted subset supports Unicode letters, digits and combining
     * marks, together with underscore, hyphen and period. Namespace prefixes
     * using {@code :} are intentionally not supported.</p>
     */
    private static final Pattern XML_ELEMENT_NAME = Pattern.compile("[_\\p{L}][_\\p{L}\\p{N}\\p{M}.-]*");

    /**
     * Converts a Java bean into XML.
     *
     * <p>The bean is first converted to a map using
     * {@link JsonUtil#pojo2map(Object)} and the resulting map is then
     * serialized using {@link #mapToXml(Map)}.</p>
     *
     * @param bean bean to serialize
     * @return XML representation of the bean
     * @throws NullPointerException     if {@code bean} is {@code null}
     * @throws IllegalArgumentException if a generated map key is not a valid XML element name
     * @throws IllegalStateException    if XML serialization fails
     */
    public static String beanToXml(Object bean) {
        Objects.requireNonNull(bean, "beanToXml.bean");

        return mapToXml(JsonUtil.pojo2map(bean));
    }

    /**
     * Converts a map into an XML document.
     *
     * <p>The generated document uses a fixed root element named
     * {@code <xml>}.</p>
     *
     * <p>Each map entry becomes one or more child elements whose name is the
     * map key:</p>
     *
     * <pre>{@code
     * Map:
     * name -> Alice
     *
     * XML:
     * <xml>
     *     <name>Alice</name>
     * </xml>
     * }</pre>
     *
     * <p>If a value implements {@link Iterable}, one element is generated
     * for every item:</p>
     *
     * <pre>{@code
     * tags -> ["a", "b"]
     *
     * <xml>
     *     <tags>a</tags>
     *     <tags>b</tags>
     * </xml>
     * }</pre>
     *
     * <p>A {@code null} value generates an empty element. Non-null scalar
     * values are converted using {@link Object#toString()}.</p>
     *
     * <p>Map keys are validated using a restricted XML element-name syntax.
     * Namespace-prefixed names containing {@code :} are not accepted.</p>
     *
     * <p>The transformer is configured to disable external DTD and external
     * stylesheet access.</p>
     *
     * @param data map to serialize
     * @return XML document as a string
     * @throws NullPointerException     if {@code data} is {@code null}
     * @throws IllegalArgumentException if a map key is {@code null} or is not a valid XML element name
     * @throws IllegalStateException    if XML serialization fails
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

    /**
     * Appends one element to the supplied XML root element.
     *
     * <p>If {@code value} is non-null, its
     * {@link Object#toString()} representation is stored as a text node.
     * A {@code null} value produces an empty element.</p>
     *
     * @param doc   owning a DOM document
     * @param root  parent element
     * @param key   XML element name
     * @param value value to serialize; may be {@code null}
     */
    private static void appendElement(Document doc, Element root, String key, Object value) {
        Element field = doc.createElement(key);

        if (value != null)
            field.appendChild(doc.createTextNode(value.toString()));

        root.appendChild(field);
    }

    /**
     * Converts the direct child elements of the XML root element into a simple string map.
     *
     * <p>Only direct child nodes of type
     * {@link Node#ELEMENT_NODE} are processed. Text nodes, comments and other
     * node types are ignored.</p>
     *
     * <p>The element name becomes the map key and
     * {@link Element#getTextContent()} becomes the value.</p>
     *
     * <p>If duplicate child-element names are present, later values overwrite
     * earlier values.</p>
     *
     * <p>For XML containing repeated values that must be preserved, use
     * {@link #xmlToMultiValueMap(String)} instead.</p>
     *
     * @param xml XML content
     * @return map containing direct child element names and text values
     * @throws NullPointerException     if {@code xml} is {@code null}
     * @throws IllegalArgumentException if the XML cannot be parsed
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
     * Converts the direct child elements of the XML root element into a map
     * while preserving repeated element names.
     *
     * <p>The first occurrence of an element name is stored as a
     * {@link String}. When the same element name occurs again, the existing
     * value is promoted to a {@link List} of strings and all subsequent
     * values are appended to that list.</p>
     *
     * <p>For example:</p>
     *
     * <pre>{@code
     * <xml>
     *     <tag>a</tag>
     *     <tag>b</tag>
     *     <name>Alice</name>
     * </xml>
     * }</pre>
     *
     * <p>produces conceptually:</p>
     *
     * <pre>{@code
     * tag  -> ["a", "b"]
     * name -> "Alice"
     * }</pre>
     *
     * <p>A {@link LinkedHashMap} is used so that the order of first appearance of element names is preserved.</p>
     *
     * <p>Only direct child element nodes are processed. Text nodes, comments
     * and other DOM node types are ignored.</p>
     *
     * @param xml XML content
     * @return multi-value map containing direct child element values
     * @throws NullPointerException     if {@code xml} is {@code null}
     * @throws IllegalArgumentException if the XML cannot be parsed
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
