package com.ajaxjs.util.httpremote.call.annotation;

import com.ajaxjs.util.httpremote.call.NoOp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.util.function.Consumer;

/**
 * GET method annotation.
 * <p>
 * Indicates that the annotated method is a GET request.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GET {
    /**
     * The URL to call.
     *
     * @return The URL to call.
     */
    String value() default "";

    /**
     * How to initialize the connection?
     *
     * @return The callback class to initialize the connection.
     */
    Class<? extends Consumer<HttpURLConnection>> initConnection() default NoOp.class;
}
