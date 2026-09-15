package com.ajaxjs.util.httpremote.call.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter as an HTTP header value.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Header {
    /**
     * The name of the HTTP header.
     *
     * @return the header name
     */
    String value() default "";
}