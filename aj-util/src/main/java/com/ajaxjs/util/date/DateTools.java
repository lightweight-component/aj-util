package com.ajaxjs.util.date;

import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.RegExpUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

            if (RegExpUtils.match(DATE_TIME, str))
                return localDateTime2Date(parseDateTime(str));
            else if (RegExpUtils.match(DATE_YEAR, str))
                return localDate2Date(parseDate(str));
            else
                return localDateTime2Date(parseDateTimeShort(str));
            // 输入日期不合法，不能转为日期类型。请重新输入日期字符串格式类型，或考虑其他方法。
        } else if (obj instanceof LocalDateTime)
            return localDateTime2Date((LocalDateTime) obj);
        else if (obj instanceof LocalDate)
            return localDate2Date((LocalDate) obj);

        return null;
    }
}
