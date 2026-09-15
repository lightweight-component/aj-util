package com.ajaxjs.util.httpremote.call;

/**
 * Base interface for HTTP API proxy instances.
 */
public interface BaseCall {
    /**
     * Initializes the proxy instance.
     */
    void init();

    /**
     * Configures unified return handling for the proxy instance.
     */
    void setUnifiedReturn();
}
