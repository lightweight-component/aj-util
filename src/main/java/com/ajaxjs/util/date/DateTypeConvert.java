package com.ajaxjs.util.date;

import java.time.*;
import java.util.Date;

/**
 * Transform among different date types
 */
public class DateTypeConvert {
    private long timestamp;

    public DateTypeConvert(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Although it's legacy, it's still widely used in many applications.
     * If it's used as Data Value, just okay with it.
     */
    private Date input;

    public DateTypeConvert(Date input) {
        this.input = input;
    }

    /**
     * SQL-specific date class
     */
    private java.sql.Date sqlDate;

    public DateTypeConvert(java.sql.Date sqlDate) {
        this.sqlDate = sqlDate;
    }

    /**
     * SQL timestamp class
     */
    private java.sql.Timestamp sqlTimestamp;

    public DateTypeConvert(java.sql.Timestamp sqlTimestamp) {
        this.sqlTimestamp = sqlTimestamp;

    }

    /**
     * Date without time or timezone (e.g., 2025-10-23)
     */
    private LocalDate localDate;

    public DateTypeConvert(LocalDate localDate) {
        this.localDate = localDate;
    }

    /**
     * Time without date or timezone (e.g., 14:30:00)
     */
    private LocalTime localTime;

    public DateTypeConvert(LocalTime localTime) {
        this.localTime = localTime;
    }

    /**
     * Date and time without timezone
     */
    private LocalDateTime localDateTime;

    public DateTypeConvert(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    /**
     * Date and time with timezone
     */
    private ZonedDateTime zonedDateTime;

    public DateTypeConvert(ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
    }

    /**
     * Date and time with offset from UTC
     */
    private OffsetDateTime offsetDateTime;

    public DateTypeConvert(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }

    /**
     * Time with offset from UTC
     */
    private OffsetTime offsetTime;

    public DateTypeConvert(OffsetTime offsetTime) {
        this.offsetTime = offsetTime;
    }

    /**
     * Timestamp in UTC (nanosecond precision)
     */
    private Instant instant;

    public DateTypeConvert(Instant instant) {
        this.instant = instant;
    }

    /**
     * Convert the input to the specified date type
     *
     * @param clz    The target date type
     * @param zoneId The time zone. Optional, defaults to system default if passed null
     * @param <T>    The target date type
     * @return The converted date
     */
//    @SuppressWarnings("unchecked")
//    public <T> T to(Class<T> clz, ZoneId zoneId) {
//        if (input != null) {
//            if (clz == LocalDate.class) {
//                LocalDate localDate = input.toInstant()
//                        .atZone(zoneId == null ? ZoneId.systemDefault() : zoneId)
//                        .toLocalDate();
//
//                return (T) localDate;
//            }
//        }
//
//        if (localDate != null) {
//            if (clz == Date.class) {
//                Date date = Date.from(localDate.atStartOfDay(zoneId == null ? ZoneId.systemDefault() : zoneId).toInstant());
//
//                return (T) date;
//            }
//        }
//
//        throw new UnsupportedOperationException("Can not transform this date type to another date type");
//    }
    @SuppressWarnings("unchecked")
    public <T> T to(Class<T> clz, ZoneId zoneId) {
        ZoneId zone = zoneId != null ? zoneId : ZoneId.systemDefault();
        Instant baseInstant;

        if (input != null) {
            baseInstant = input.toInstant();
        } else if (sqlDate != null) {
            baseInstant = sqlDate.toLocalDate().atStartOfDay(zone).toInstant();
        } else if (sqlTimestamp != null) {
            baseInstant = sqlTimestamp.toInstant();
        } else if (localDate != null) {
            baseInstant = localDate.atStartOfDay(zone).toInstant();
        } else if (localDateTime != null) {
            baseInstant = localDateTime.atZone(zone).toInstant();
        } else if (zonedDateTime != null) {
            baseInstant = zonedDateTime.toInstant();
        } else if (offsetDateTime != null) {
            baseInstant = offsetDateTime.toInstant();
        } else if (offsetTime != null) {
            baseInstant = offsetTime.atDate(LocalDate.now()).toInstant();
        } else if (instant != null) {
            baseInstant = instant;
        } else if (timestamp != 0L) {
            baseInstant = Instant.ofEpochMilli(timestamp);
        } else
            throw new UnsupportedOperationException("No input date/time set.");

        // Convert baseInstant to target type
        if (clz == Instant.class) {
            return (T) baseInstant;
        } else if (clz == Date.class) {
            return (T) Date.from(baseInstant);
        } else if (clz == java.sql.Date.class) {
            return (T) java.sql.Date.valueOf(baseInstant.atZone(zone).toLocalDate());
        } else if (clz == java.sql.Timestamp.class) {
            return (T) java.sql.Timestamp.from(baseInstant);
        } else if (clz == LocalDate.class) {
            return (T) baseInstant.atZone(zone).toLocalDate();
        } else if (clz == LocalTime.class) {
            return (T) baseInstant.atZone(zone).toLocalTime();
        } else if (clz == LocalDateTime.class) {
            return (T) baseInstant.atZone(zone).toLocalDateTime();
        } else if (clz == ZonedDateTime.class) {
            return (T) baseInstant.atZone(zone);
        } else if (clz == OffsetDateTime.class) {
            return (T) baseInstant.atOffset(zone.getRules().getOffset(baseInstant));
        } else if (clz == OffsetTime.class) {
            return (T) baseInstant.atZone(zone).toOffsetDateTime().toOffsetTime();
        }

        throw new UnsupportedOperationException("Unsupported target type: " + clz.getName());
    }
}
