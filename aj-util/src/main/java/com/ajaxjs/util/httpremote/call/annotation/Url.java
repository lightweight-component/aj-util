package com.ajaxjs.util.httpremote.call.annotation;

import com.ajaxjs.util.httpremote.call.HttpApiConfig;
import com.ajaxjs.util.httpremote.call.NoConfig;
import com.ajaxjs.util.httpremote.call.NoOp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.util.function.Consumer;

/**
 * Configures the base URL and global settings for an HTTP API interface.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Url {
    /**
     * The base URL for the HTTP API.
     *
     * @return the base URL
     */
    String value();

    /**
     * The callback class used to initialize every connection for this API.
     *
     * @return the connection initializer class
     */
    Class<? extends Consumer<HttpURLConnection>> initConnection() default NoOp.class;

    /**
     * The configuration class for unified return handling.
     *
     * @return the API config class
     */
    Class<? extends HttpApiConfig> config() default NoConfig.class;
}
