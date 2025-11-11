package com.ajaxjs.util.httpremote.call;

import com.ajaxjs.util.CommonConstant;
import com.ajaxjs.util.UrlHelper;
import com.ajaxjs.util.httpremote.HttpConstant;
import com.ajaxjs.util.httpremote.Request;
import com.ajaxjs.util.httpremote.call.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
public class CallHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Class<?> declaringClass = method.getDeclaringClass();
        Url urlAnn = declaringClass.getAnnotation(Url.class);

        if (urlAnn == null)
            throw new UnsupportedOperationException("Please give the root url");

        String rootUrl = urlAnn.value();
        Class<?> returnType = method.getReturnType();
        Request request = null;
        Class<? extends Consumer<HttpURLConnection>> initClzByClz = urlAnn.initConnection();
        Class<? extends Consumer<HttpURLConnection>> initByMethod = null;

        if (method.isAnnotationPresent(GET.class)) {
            GET get = method.getAnnotation(GET.class);
            String url = getUrl(rootUrl, get);
            Consumer<HttpURLConnection> init = getInitConnection(initClzByClz, get.initConnection());

            request = new Request(HttpConstant.HttpMethod.GET, url);
        } else if (method.isAnnotationPresent(POST.class)) {
            POST post = method.getAnnotation(POST.class);
            String url = getUrl(rootUrl, post);

            initByMethod = post.initConnection();
            request = new Request(HttpConstant.HttpMethod.POST, url);
        } else if (method.isAnnotationPresent(PUT.class)) {
            PUT put = method.getAnnotation(PUT.class);
            String url = getUrl(rootUrl, put);

            initByMethod = put.initConnection();
            request = new Request(HttpConstant.HttpMethod.PUT, url);
        } else if (method.isAnnotationPresent(DELETE.class)) {
            DELETE delete = method.getAnnotation(DELETE.class);
            String url = getUrl(rootUrl, delete);

            initByMethod = delete.initConnection();
            request = new Request(HttpConstant.HttpMethod.DELETE, url);
        }

        if (request != null) {
            Consumer<HttpURLConnection> init = getInitConnection(initClzByClz, initByMethod);
            request.init(init);
            request.connect();

            if (returnType == String.class)
                return request.getResp().getResponseText();
            else if (returnType == Map.class)
                return request.getResp().responseAsJson();
            else  // bean
                return request.getResp().responseAsBean(returnType);
        } else
            throw new UnsupportedOperationException("Config API error");
    }

    static String getUrl(String rootUrl, Annotation annotation) {
        String valueOnMethod;

        try {
            Method method = annotation.annotationType().getMethod("value");
            Object value = method.invoke(annotation);

            valueOnMethod = value == null ? CommonConstant.EMPTY_STRING : value.toString();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.warn("There is NO such method when calling " + annotation.annotationType().getSimpleName() + ".value", e);
            throw new RuntimeException(e);
        }

        if (CommonConstant.EMPTY_STRING.equals(valueOnMethod))
            return rootUrl;
        else
            return UrlHelper.concatUrl(rootUrl, valueOnMethod);
    }

    private Consumer<HttpURLConnection> getInitConnection(
            Class<? extends Consumer<HttpURLConnection>> initClzByClz,
            Class<? extends Consumer<HttpURLConnection>> initClzByMethod) {
        Consumer<HttpURLConnection> initClz = newInstance(initClzByClz);
        Consumer<HttpURLConnection> initMethod = newInstance(initClzByMethod);

        if (initClz != null && initMethod != null)
            return initClz.andThen(initMethod);
        else if (initClz != null)
            return initClz;
        else
            return initMethod;
    }

    static final Map<Class<? extends Consumer<HttpURLConnection>>, Consumer<HttpURLConnection>> INIT_CONNECTION_CACHE = new ConcurrentHashMap<>();

    private static Consumer<HttpURLConnection> newInstance(Class<? extends Consumer<HttpURLConnection>> clz) {
        if (clz != null && clz != NoOp.class) {
            Consumer<HttpURLConnection> init;

            if (INIT_CONNECTION_CACHE.containsKey(clz))
                init = INIT_CONNECTION_CACHE.get(clz);
            else {
                try {
                    init = clz.newInstance();
                    INIT_CONNECTION_CACHE.put(clz, init);
                } catch (InstantiationException e) {
                    log.warn("There is Instantiation Error when calling " + clz.getSimpleName() + ".value", e);
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    log.warn("There is Illegal Access when calling " + clz.getSimpleName() + ".value", e);
                    throw new RuntimeException(e);
                }
            }

            return init;
        }

        return null;
    }

    private static final CallHandler HANDLER = new CallHandler();

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, HANDLER);
    }
}
