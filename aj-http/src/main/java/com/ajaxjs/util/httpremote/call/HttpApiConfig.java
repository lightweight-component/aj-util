package com.ajaxjs.util.httpremote.call;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Configuration interface for customizing HTTP API proxy behavior.
 */
public interface HttpApiConfig {
    /**
     * Returns a consumer used to initialize every HTTP connection.
     *
     * @return the connection initializer
     */
    Consumer<HttpURLConnection> initConn();

    /**
     * Returns a function used to wrap or transform the unified return value.
     *
     * @param <T> the return type
     * @return the unified return transformer
     */
    <T> Function<Map<String, Object>, T> initUnifiedReturn();
}
