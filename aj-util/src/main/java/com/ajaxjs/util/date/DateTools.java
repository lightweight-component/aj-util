package com.ajaxjs.util.date;

import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.RegExpUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTools {
    /**
     * 年月日的正则表达式，例如 2016-08-18
     */
    private final static String DATE_YEAR = "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])";

    private final static String DATE_TIME = DATE_YEAR + " ([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]";

    /**
     * 支持任意对象转换为日期类型
     *
     * @param obj 任意对象
     * @return 日期类型对象，返回 null 表示为转换失败
     */
    public static Date object2Date(Object obj) {
        if (obj == null)
            return null;
        else if (obj instanceof Date)
            return (Date) obj;
        else if (obj instanceof Long)
            return new Date((Long) obj);
        else if (obj instanceof Integer)
            return object2Date(Long.parseLong(obj + "000")); /* 10 位长 int，后面补充三个零为13位 long 时间戳 */
        else if (obj instanceof String) {
            String str = obj.toString();

            if (ObjectHelper.isEmptyText(str))
                return null;

            LocalDateTime dateTime;
            if (RegExpUtils.match(DATE_TIME, str))
                dateTime = LocalDateTime.parse(str, Formatter.getDateTimeFormatter());
            else if (RegExpUtils.match(DATE_YEAR, str))
                dateTime = LocalDateTime.parse(str, Formatter.getDateFormatter());
            else
                dateTime = LocalDateTime.parse(str, Formatter.getDateTimeShortFormatter());

            return new DateTypeConvert(dateTime).to(Date.class, null);

            // 输入日期不合法，不能转为日期类型。请重新输入日期字符串格式类型，或考虑其他方法。
        } else if (obj instanceof LocalDateTime)
            return new DateTypeConvert((LocalDateTime) obj).to(Date.class, null);
        else if (obj instanceof LocalDate)
            return new DateTypeConvert((LocalDate) obj).to(Date.class, null);

        return null;
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static String now(DateTimeFormatter formatter) {
        return LocalDateTime.now().format(formatter);
    }

    public static String now(String format) {
        return now(Formatter.getDateFormatter(format));
    }

    public static String now() {
        return now(Formatter.getDateTimeFormatter());
    }

    public static String nowShort() {
        return now(Formatter.getDateTimeFormatter());
    }

    /**
     * 请求的时间戳，格式必须符合 RFC1123 的日期格式
     *
     * @return 当前日期
     */
    public static String nowGMTDate() {
        return Formatter.GMT_FORMATTER.format(Instant.now());
    }

    /**
     * 请求的时间戳。按照 ISO8601 标准表示，并需要使用 UTC 时间，格式为 yyyy-MM-ddTHH:mm:ssZ
     *
     * @return 当前日期
     */
    public static String newISO8601Date() {
        return Formatter.ISO8601_FORMATTER.format(Instant.now());
    }
}
