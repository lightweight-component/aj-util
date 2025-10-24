package com.ajaxjs.util.httpremote.call;

import com.ajaxjs.util.httpremote.HttpConstant;
import com.ajaxjs.util.httpremote.Request;
import com.ajaxjs.util.httpremote.call.annotation.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;

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

        if (method.isAnnotationPresent(GET.class)) {
            GET get = method.getAnnotation(GET.class);
            String url;

            if ("".equals(get.value()))
                url = rootUrl;
            else
                url = rootUrl + get.value();

            Class<? extends Consumer<HttpURLConnection>> initClz = get.initConnection();
            Consumer<HttpURLConnection> init = null;

            if (initClz != NoOp.class) {
                try {
                    init = initClz.newInstance();
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            request = new Request(HttpConstant.HttpMethod.GET, url);
            request.init(init);
            request.connect();
        } else if (method.isAnnotationPresent(POST.class)) {

        } else if (method.isAnnotationPresent(PUT.class)) {

        } else if (method.isAnnotationPresent(DELETE.class)) {
        }

        if (request != null) {
            if (returnType == String.class)
                return request.getResp().getResponseText();
            else if (returnType == Map.class)
                return request.getResp().responseAsJson();
            else  // bean
                return request.getResp().responseAsBean(returnType);
        } else
            throw new UnsupportedOperationException("Config API error");
    }

    private static final CallHandler HANDLER = new CallHandler();

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, HANDLER);
    }
}
