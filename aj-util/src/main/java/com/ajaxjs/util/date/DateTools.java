package com.ajaxjs.util.date;

import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.RegExpUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Utility class providing common date and time operations.
 *
 * <p>Includes conversions between various date representations (legacy {@link Date},
 * {@link LocalDateTime}, {@link LocalDate}, strings, timestamps), as well as methods
 * for obtaining the current date/time in several standard formats.
 */
public class DateTools {
    /**
     * The year-month-day in RegExp String, to match like 2016-08-18
     */
    private final static String DATE_YEAR = "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])";

    /**
     * The year-month-day-hour-minute-second in RegExp String, to match like 2016-08-18 11:20:05
     */
    private final static String DATE_TIME = DATE_YEAR + " ([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]";

    /**
     * Converts a basic data value (long/int/string) or a Java 8 date/time object to a {@link Date}.
     *
     * <p>Supported input types:
     * <ul>
     *   <li>{@code null} - returns {@code null}</li>
     *   <li>{@link Date} - returned as-is</li>
     *   <li>{@link Long} - treated as milliseconds since epoch</li>
     *   <li>{@link Integer} - treated as seconds since epoch and converted to milliseconds</li>
     *   <li>{@link String} - parsed as {@code yyyy-MM-dd HH:mm:ss}, {@code yyyy-MM-dd},
     *       or {@code yyyy-MM-dd HH:mm}</li>
     *   <li>{@link LocalDateTime} / {@link LocalDate} - converted using the system default timezone</li>
     * </ul>
     *
     * @param obj any object to be converted to a date
     * @return the converted {@link Date}, or {@code null} if the input is null, blank, or unsupported
     * @throws java.time.format.DateTimeParseException if a non-blank string is not in a supported format
     */
    public static Date object2Date(Object obj) {
        if (obj == null)
            return null;
        else if (obj instanceof Date)
            return (Date) obj;
        else if (obj instanceof Long)
            return new Date((Long) obj);
        else if (obj instanceof Integer)
            return object2Date(Long.parseLong(obj + "000")); /* 10-digit int timestamp padded to 13-digit milliseconds */
        else if (obj instanceof String) {
            String str = obj.toString();

            if (ObjectHelper.isEmptyText(str))
                return null;

            LocalDateTime dateTime;
            if (RegExpUtils.match(DATE_TIME, str))
                dateTime = LocalDateTime.parse(str, Formatter.getDateTimeParser());
            else if (RegExpUtils.match(DATE_YEAR, str))
                return new DateTypeConvert(LocalDate.parse(str, Formatter.getDateParser())).to(Date.class, null);
            else
                dateTime = LocalDateTime.parse(str, Formatter.getDateTimeShortParser());

            return new DateTypeConvert(dateTime).to(Date.class, null);

            // The input date is invalid and cannot be converted to a date type. Please re-enter a valid date string format or consider another approach.
        } else if (obj instanceof LocalDateTime)
            return new DateTypeConvert((LocalDateTime) obj).to(Date.class, null);
        else if (obj instanceof LocalDate)
            return new DateTypeConvert((LocalDate) obj).to(Date.class, null);

        return null;
    }

    /**
     * Transform Date to LocalDateTime
     * It's a shorthand for new DateTypeConvert(date).to(LocalDateTime.class, null);
     *
     * @param date The input date
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Obtain current time by specified format.
     *
     * @param formatter The formatter object
     * @return The current time
     */
    public static String now(DateTimeFormatter formatter) {
        return LocalDateTime.now().format(formatter);
    }

    /**
     * Obtain current time by specified format.
     *
     * @param format The format string
     * @return The current time
     */
    public static String now(String format) {
        return now(Formatter.getDateFormatter(format));
    }

    /**
     * Obtain current time, which is formatted by default like "yyyy-MM-dd HH:mm:ss".
     *
     * @return The current time
     */
    public static String now() {
        return now(Formatter.getDateTimeFormatter());
    }

    /**
     * Obtain current time, which is formatted like "yyyy-MM-dd HH:mm".
     *
     * @return The current time
     */
    public static String nowShort() {
        return now(Formatter.getDateTimeShortFormatter());
    }

    /**
     * Returns the current timestamp formatted according to RFC1123 date format.
     * This format is commonly used for HTTP headers and request signatures.
     *
     * @return The current time in RFC1123 format
     */
    public static String nowGMTDate() {
        return Formatter.GMT_FORMATTER.format(Instant.now());
    }

    /**
     * Returns the current timestamp formatted according to ISO8601 standard using UTC time.
     * The format is {@code yyyy-MM-dd'T'HH:mm:ss'Z'} and is commonly used for S3 storage signatures.
     *
     * @return The current time in ISO8601 format
     */
    public static String newISO8601Date() {
        return Formatter.ISO8601_FORMATTER.format(Instant.now());
    }

    /**
     * Returns the current instant in ISO-8601 format, preserving the fractional-second
     * precision supplied by the system clock (up to nanoseconds).
     *
     * @return The current time in high-precision ISO8601 format
     */
    public static String newISO8601DateWithHigherPrecision() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }
}
