package com.ajaxjs.util.httpremote.call.annotation;

import com.ajaxjs.util.httpremote.call.NoOp;
import com.ajaxjs.util.httpremote.model.PayloadType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.util.function.Consumer;

/**
 * Marks an interface method as an HTTP POST request.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface POST {
    /**
     * The URL path to call.
     *
     * @return the URL path
     */
    String value() default "";

    /**
     * The payload type used to serialize the request body.
     *
     * @return the payload type
     */
    PayloadType type() default PayloadType.FORM;

    /**
     * How to initialize the connection?
     *
     * @return The callback class to initialize the connection.
     */
    Class<? extends Consumer<HttpURLConnection>> initConnection() default NoOp.class;
}
