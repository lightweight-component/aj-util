package com.ajaxjs.util.httpremote.call;

import java.net.HttpURLConnection;
import java.util.function.Consumer;

/**
 * No-op connection initializer used as a default value for annotations.
 */
public class NoOp implements Consumer<HttpURLConnection> {
    @Override
    public void accept(HttpURLConnection conn) {
        // no-op
    }
}
