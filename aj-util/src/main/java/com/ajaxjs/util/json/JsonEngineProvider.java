package com.ajaxjs.util.json;

/**
 * JSON engine provider.
 * <p>
 * Implementations provide a concrete JsonEngine,
 * such as Jackson2Engine or Jackson3Engine.
 */
public interface JsonEngineProvider {
    /**
     * 优先级，越大越优先
     */
    int priority();

    /**
     * Create JSON engine instance.
     *
     * @return JsonEngine implementation
     */
    JsonEngine create();
}