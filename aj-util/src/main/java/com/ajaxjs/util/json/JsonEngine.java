package com.ajaxjs.util.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON engine abstraction.
 * <p>
 * This interface defines a unified JSON processing API without exposing
 * any implementation-specific classes (such as Jackson's ObjectMapper,
 * JsonNode, JavaType, or TypeReference).
 * </p>
 *
 * <p>
 * Different JSON libraries (Jackson 2, Jackson 3, Gson, Fastjson, etc.)
 * can provide their own implementation of this interface.
 * </p>
 */
public interface JsonEngine {
    /**
     * Serializes a Java object into a compact JSON string.
     *
     * @param obj the Java object
     * @return JSON string
     */
    String toJson(Object obj);

    /**
     * Serializes a Java object into a pretty-printed JSON string.
     *
     * @param obj the Java object
     * @return formatted JSON string
     */
    String toJsonPretty(Object obj);

    /**
     * Deserializes a JSON string into a Java object.
     *
     * @param json  JSON string
     * @param clazz target class
     * @param <T>   target type
     * @return deserialized object
     */
    <T> T fromJson(String json, Class<T> clazz);

    /**
     * Deserializes a JSON object into a Map.
     *
     * @param json  JSON string
     * @param clazz value type
     * @param <T>   value type
     * @return Map
     */
    <T> Map<String, T> json2map(String json, Class<T> clazz);

    /**
     * Deserializes a JSON object into a LinkedHashMap,
     * preserving key order.
     *
     * @param json  JSON string
     * @param clazz value type
     * @param <T>   value type
     * @return LinkedHashMap
     */
    <T> LinkedHashMap<String, T> json2sortMap(String json, Class<T> clazz);

    /**
     * Deserializes a JSON array into a List.
     *
     * @param json  JSON array string
     * @param clazz element type
     * @param <T>   element type
     * @return List
     */
    <T> List<T> json2list(String json, Class<T> clazz);

    /**
     * Deserializes a JSON array into a list of maps.
     *
     * @param json JSON array string
     * @return List of Map
     */
    List<Map<String, Object>> json2mapList(String json);

    /**
     * Converts one Java object into another type.
     *
     * <p>
     * This is typically implemented using the underlying JSON library's
     * object conversion capability rather than serialization.
     * </p>
     *
     * @param obj   source object
     * @param clazz target class
     * @param <T>   target type
     * @return converted object
     */
    <T> T convertValue(Object obj, Class<T> clazz);

    /**
     * Converts a Java Bean into a Map.
     *
     * @param obj source bean
     * @return map
     */
    Map<String, Object> pojo2map(Object obj);

    /**
     * Converts a Java Bean into a typed Map.
     *
     * @param obj   source bean
     * @param clazz map value type
     * @param <T>   value type
     * @return map
     */
    <T> Map<String, T> pojo2map(Object obj, Class<T> clazz);

    /**
     * Converts a Map into a Java Bean.
     *
     * @param map   source map
     * @param clazz target class
     * @param <T>   target type
     * @return Java Bean
     */
    <T> T map2pojo(Map<String, Object> map, Class<T> clazz);

    /**
     * Parses a JSON string into an implementation-specific tree object.
     *
     * <p>
     * The returned object is intentionally declared as {@code Object}
     * to avoid exposing implementation-specific classes such as
     * Jackson's JsonNode.
     * </p>
     *
     * <p>
     * Applications that need to manipulate the tree should cast the
     * returned value to the corresponding implementation type.
     * </p>
     *
     * @param json JSON string
     * @return implementation-specific JSON tree object
     */
    Object json2Node(String json);
}