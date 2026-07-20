package com.ajaxjs.util;

import com.ajaxjs.util.json.JsonEngine;
import com.ajaxjs.util.json.JsonEngineFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulation of Jackson Library: Conversion Methods Between JSON, Map, Bean, and List.
 */
@Slf4j
public final class JsonUtil {
    private static volatile JsonEngine engine;

    private JsonUtil() {
    }

    public static void setEngine(JsonEngine engine) {
        if (engine == null)
            throw new IllegalArgumentException("JsonEngine cannot be null.");

        JsonUtil.engine = engine;
    }

    public static JsonEngine getEngine() {
        if (engine != null)
            return engine;

        synchronized (JsonUtil.class) {
            if (engine == null)
                engine = JsonEngineFactory.create();
        }

        return engine;
    }

    /**
     * Converts a Java object to a JSON string.
     *
     * @param obj The Java object to be converted to a JSON string.
     * @return The JSON string representation of the Java object.
     */
    public static String toJson(Object obj) {
        return getEngine().toJson(obj);
    }

    /**
     * Java Bean, list, array converts to pretty json string
     *
     * @param obj Java Bean, list, array
     * @return The JSON string representation of the Java object.
     */
    public static String toJsonPretty(Object obj) {
        return getEngine().toJsonPretty(obj);
    }

    /**
     * Converts a JSON string to an object of the specified type.
     *
     * @param jsonStr   The JSON string representing the data to be converted.
     * @param valueType The class type of the target object.
     * @param <T>       The generic type parameter indicating the type of the returned object.
     * @return The converted object of type T.
     * @throws RuntimeException If the JSON string cannot be converted to the target type.
     */
    public static <T> T fromJson(String jsonStr, Class<T> valueType) {
        try {
            return getEngine().fromJson(jsonStr, valueType);
        } catch (Exception e) {
            log.warn("Failed to converts a JSON string: {} to an object of the specified type.", jsonStr);
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

    /**
     * JSON string convert to map with javaBean
     *
     * @param <T>     The type of the value in the Map
     * @param jsonStr The JSON string to be converted
     * @param clazz   The class of the JavaBean type in the Map value
     * @return Returns a Map containing keys of type String and values of the specified JavaBean type
     */
    public static <T> Map<String, T> json2map(String jsonStr, Class<T> clazz) {
        return getEngine().json2map(jsonStr, clazz);
    }

    /**
     * JSON string converts to map with javaBean
     * This method converts a JSON string into a LinkedHashMap of String keys and JavaBean values.
     * It uses the Jackson library's ObjectMapper to parse the JSON string according to the specified JavaBean type,
     * and stores it in a LinkedHashMap, preserving the order of insertion.
     *
     * @param jsonStr The JSON string to be converted
     * @param clazz   The class type of the JavaBean
     * @param <T>     Generic parameter, represents the type of JavaBean
     * @return Returns a LinkedHashMap containing String keys and JavaBean values of the specified type
     */
    public static <T> LinkedHashMap<String, T> json2sortMap(String jsonStr, Class<T> clazz) {
        return getEngine().json2sortMap(jsonStr, clazz);
    }

    /**
     * JSON string converts to map
     * This method are used
     * to convert a JSON string into a Map object with String as the key type and Object as the value type.
     * It simplifies the process of accessing and manipulating JSON data by converting it into a Map format.
     *
     * @param jsonStr The JSON string to be converted.
     * @return Returns a Map object containing the key-value pairs converted from the JSON string.
     */
    public static Map<String, Object> json2map(String jsonStr) {
        return json2map(jsonStr, Object.class);
    }

    /**
     * Converts a JSON string to a Map object.
     * This method specifically converts a string in JSON format into a Map object where the key and value are both of type String.
     * It is used to facilitate the parsing and use of JSON data in programs.
     *
     * @param jsonStr The JSON string to be converted, which should be in a format that can be correctly parsed into a Map.
     * @return Returns a Map object with String keys and values, representing the converted JSON data.
     */
    public static Map<String, String> json2StrMap(String jsonStr) {
        return json2map(jsonStr, String.class);
    }

    /**
     * JSON array string converts to list with Java Bean
     *
     * @param jsonArrayStr The JSON array string, representing a list of objects
     * @param clazz        The class type of the list elements, used to specify the type of Java Bean
     * @param <T>          Generic type parameter, indicating the type of elements in the list
     * @return Returns a list of Java Bean objects converted from the JSON array string
     */
    public static <T> List<T> json2list(String jsonArrayStr, Class<T> clazz) {
        return getEngine().json2list(jsonArrayStr, clazz);
    }

    /**
     * JSON array string converts to list with Java Bean
     *
     * @param jsonArrayStr The JSON array string, representing a list of objects
     * @return Returns a list of Map objects converted from the JSON array string
     */
    public static List<Map<String, Object>> json2mapList(String jsonArrayStr) {
        return getEngine().json2mapList(jsonArrayStr);
    }

    /**
     * Converts an object to another object.
     * This method uses ObjectMapper from the Jackson library
     * to convert an object of any type to another type specified by class
     * It is primarily used for data type conversion when the specific type is known at runtime
     *
     * @param obj   The object to be converted, can be of any type
     * @param clazz The target type class to convert to
     * @param <T>   Generic parameter, represents the target type to convert to
     * @return Returns an object of the converted target type
     */
    public static <T> T convertValue(Object obj, Class<T> clazz) {
        return getEngine().convertValue(obj, clazz);
    }

    /**
     * Java Bean converts to map.
     * This method is used to convert a Java Bean object into a Map object,
     * facilitating operations such as lookup, addition, and deletion.
     * It does not require a detailed explanation for simple delegation to another overload method.
     *
     * @param obj The Java Bean object to be converted
     * @return Returns a Map object containing all the properties of the Java Bean
     */
    public static Map<String, Object> pojo2map(Object obj) {
        return pojo2map(obj, Object.class);
    }

    /**
     * Java Bean converts to map
     *
     * @param obj   The Java Bean object to be converted
     * @param clazz The class type of the map value
     * @param <T>   The generic type of the map value
     * @return Returns a map converted from the Java Bean, with String as the key and the specified generic type as the value
     */
    public static <T> Map<String, T> pojo2map(Object obj, Class<T> clazz) {
        return getEngine().pojo2map(obj, clazz);
    }

    /**
     * Map converts to Java Bean
     *
     * @param map   The map to be converted
     * @param clazz The class type of the Java Bean
     * @param <T>   The generic type of the Java Bean
     * @return Returns a Java Bean object converted from the map
     */
    public static <T> T map2pojo(Map<String, Object> map, Class<T> clazz) {
        return getEngine().map2pojo(map, clazz);
    }

    /**
     * JSON string converts to node
     *
     * @param jsonStr The JSON string to be converted
     * @return JsonNode object converted from the JSON string
     */
    public static Object json2Node(String jsonStr) {
        return getEngine().json2Node(jsonStr);
    }
}
