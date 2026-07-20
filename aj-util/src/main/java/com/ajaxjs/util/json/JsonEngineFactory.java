package com.ajaxjs.util.json;

import java.util.ServiceLoader;

public final class JsonEngineFactory {
    private JsonEngineFactory() {
    }

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