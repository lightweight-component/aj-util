package com.ajaxjs.util.json.jackson2;

import com.ajaxjs.util.json.JsonEngine;
import com.ajaxjs.util.json.JsonEngineProvider;

/**
 * Service provider that creates a Jackson 2 based {@link JsonEngine}.
 */
public class Jackson2Provider implements JsonEngineProvider {
    /**
     * Returns the provider priority. Higher values take precedence.
     *
     * @return the priority value
     */
    @Override
    public int priority() {
        return 10;
    }

    /**
     * Creates a new Jackson 2 JSON engine instance.
     *
     * @return the JSON engine
     */
    @Override
    public JsonEngine create() {
        return new Jackson2Engine();
    }
}