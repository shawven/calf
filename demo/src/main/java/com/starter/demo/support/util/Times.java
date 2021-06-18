package com.starter.demo.support.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * java8时间工具类
 *
 * @see LocalDate
 * @see LocalDateTime
 *
 * @author Shoven
 * @date 2020-09-22
 */
public class Times {

    public static final String DATE_NO_HYPHEN = "yyyyMMdd";

    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    public static final DateTimeFormatter DATE_NO_HYPHEN_FORMATTER = DateTimeFormatter.ofPattern(DATE_NO_HYPHEN);

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME);

    public static Date toDate(YearMonth time) {
        if (time == null) {
            return null;
        }
        return toDate(time.atDay(1));
    }

    public static Date toDate(LocalDate time) {
        if (time == null) {
            return null;
        }
        return toDate(time.atStartOfDay());
    }

    public static Date toDate(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        Instant instant = time.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static long toMillis(YearMonth time) {
        if (time == null) {
            return 0;
        }
        return toMillis(time.atDay(1));
    }

    public static long toMillis(LocalDate time) {
        if (time == null) {
            return 0;
        }
        return toMillis(time.atStartOfDay());
    }

    public static long toMillis(LocalDateTime time) {
        if (time == null) {
            return 0;
        }
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static YearMonth toYearMonth(Date time) {
        if (time == null) {
            return null;
        }
        return YearMonth.from(toLocalDate(time));
    }

    public static LocalDate toLocalDate(Date time) {
        if (time == null) {
            return null;
        }
        return toLocalDateTime(time).toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(Date time) {
        if (time == null) {
            return null;
        }
        return LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault());
    }

    public static YearMonth toYearMonth(long time) {
        return YearMonth.from(toLocalDate(time));
    }

    public static LocalDate toLocalDate(long time) {
        return toLocalDate(new Date(time));
    }

    public static LocalDateTime toLocalDateTime(long time) {
        return toLocalDateTime(new Date(time));
    }

    public static Date parseDate(String time) {
        return toDate(parseLocalDateTime(time));
    }

    public static Date parseDate(String time, String pattern) {
        return toDate(parseLocalDateTime(time, pattern));
    }

    public static LocalDateTime parseLocalDateTime(String time) {
        return LocalDateTime.parse(time, DATE_TIME_FORMATTER);
    }

    public static LocalDateTime parseLocalDateTime(String time, String pattern) {
        return LocalDateTime.parse(time, DateTimeFormatter.ofPattern(pattern));
    }
}
