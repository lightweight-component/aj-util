package com.ajaxjs.util.json.jackson2;

import com.ajaxjs.util.json.JsonEngine;
import com.ajaxjs.util.json.JsonEngineProvider;

public class Jackson2Provider implements JsonEngineProvider {
    @Override
    public int priority() {
        return 10;
    }

    @Override
    public JsonEngine create() {
        return new Jackson2Engine();
    }
}