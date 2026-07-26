package com.ajaxjs.util.json;

import java.util.ServiceLoader;

/**
 * Factory for loading the highest priority {@link JsonEngineProvider} via the service loader mechanism.
 */
public final class JsonEngineFactory {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private JsonEngineFactory() {
    }

    /**
     * Loads and creates a {@link JsonEngine} using the highest priority provider
     * registered through {@link ServiceLoader}.
     *
     * @return the created JSON engine
     * @throws IllegalStateException if no provider is found
     */
    public static JsonEngine create() {
        ServiceLoader<JsonEngineProvider> loader = ServiceLoader.load(JsonEngineProvider.class);
        JsonEngineProvider selected = null;

        for (JsonEngineProvider provider : loader) {
            if (selected == null || provider.priority() > selected.priority())
                selected = provider;
        }

        if (selected == null)
            throw new IllegalStateException("No JsonEngineProvider found.");

        return selected.create();
    }
}