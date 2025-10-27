package com.ajaxjs.util.date;

import lombok.RequiredArgsConstructor;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class Formatter {
    private final TemporalAccessor temporal;

    public Formatter(Date date) {
        this.temporal = DateTools.toLocalDateTime(date);
    }

    public String format() {
        return getDateTimeFormatter().format(temporal);
    }

    public String format(String format) {
        return getDateFormatter(format).format(temporal);
    }

    public static final String DATE = "yyyy-MM-dd";

    public static final String DATETIME = "yyyy-MM-dd HH:mm:ss";

    public static final String DATETIME_SHORT = "yyyy-MM-dd HH:mm";

    private static final Map<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>();

    public static DateTimeFormatter getDateFormatter(String format) {
        return FORMATTER_CACHE.computeIfAbsent(format, DateTimeFormatter::ofPattern);
    }

    public static DateTimeFormatter getDateFormatter() {
        return getDateFormatter(DATE);
    }

    public static DateTimeFormatter getDateTimeFormatter() {
        return getDateFormatter(DATETIME);
    }

    public static DateTimeFormatter getDateTimeShortFormatter() {
        return getDateFormatter(DATETIME_SHORT);
    }

    public final static DateTimeFormatter GMT_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT"));

    public final static DateTimeFormatter ISO8601_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);
}
