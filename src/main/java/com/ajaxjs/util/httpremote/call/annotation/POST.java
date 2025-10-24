package com.ajaxjs.util.httpremote.call.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.util.function.Consumer;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface POST {
    String value() default "";



    Class<? extends Consumer<HttpURLConnection>> initConnection() default NoOp.class;
}
