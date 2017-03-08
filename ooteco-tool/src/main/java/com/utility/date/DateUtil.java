package com.utility.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

public class DateUtil {

    public static String DATE_FORMAT = "yyyy-MM-dd";
    public static String TIME_FORMAT = "HH:mm";
    public static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static String DATETIME_WEEK_FORMAT = "yyyy-MM-dd E HH:mm:ss";
    public static String DATETIME_AREA_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>> DATE_FORMAT_MAP = new ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>>();

    private static void init(final String pattern, final Locale locale) {
        String key = pattern;
        if (null != locale)
            key = pattern + locale.toString();
        ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                if (null == locale)
                    return new SimpleDateFormat(pattern);
                else
                    return new SimpleDateFormat(pattern, locale);
            }
        };
        DATE_FORMAT_MAP.putIfAbsent(key, threadLocal);
    }

    public static Date parse(String date) {
        if (date.contains("-")) {
            return parse(date, DATE_FORMAT);
        } else {
            return parse("yyyyMMdd");
        }
    }

    public static Date parse(String date, String pattern) {
        return parse(date, pattern, null);
    }

    public static Date parse(String date, String pattern, Locale locale) {
        String key = pattern;
        if (null != locale)
            key = pattern + locale.toString();
        if (!DATE_FORMAT_MAP.containsKey(key)) {
            init(pattern, locale);
        }
        SimpleDateFormat dateFormat = DATE_FORMAT_MAP.get(key).get();
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String format(Date date) {
        return format(date, DATE_FORMAT);
    }

    public static String format(Date date, String pattern) {
        return format(date, pattern, null);
    }

    public static String format(Date date, String pattern, Locale locale) {
        String key = pattern;
        if (null != locale)
            key = pattern + locale.toString();
        if (!DATE_FORMAT_MAP.containsKey(key)) {
            init(pattern, locale);
        }
        SimpleDateFormat dateFormat = DATE_FORMAT_MAP.get(key).get();
        return dateFormat.format(date);
    }

    public static Date addTime(Date date, int amount, int field) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, amount);
        return calendar.getTime();
    }

    /**
     * 设置时间
     */
    public static Date setTime(Date date, int amount, int field) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(field, amount);
        return calendar.getTime();
    }

    /**
     * 获取当地时间
     *
     * @param sourceDate     需要转换的时间
     * @param sourceTimeZone 原来的时区
     * @param targetTimeZone 目标时区
     * @return
     */
    public static Date dateTransformBetweenTimeZone(Date sourceDate, TimeZone sourceTimeZone, TimeZone targetTimeZone) {
        Long targetTime = sourceDate.getTime() + sourceTimeZone.getRawOffset() - targetTimeZone.getRawOffset();
        return new Date(targetTime);
    }

    /**
     * 获取时区差, unit: hour
     *
     * @param sourceTimeZone
     * @param targetTimeZone
     * @return targetTime/3600000
     */
    public static int timeZoneDifferent(TimeZone sourceTimeZone, TimeZone targetTimeZone) {
        int targetTime = sourceTimeZone.getRawOffset() - targetTimeZone.getRawOffset();
        return targetTime / 3600000;
    }

    /**
     * 转化航班时间
     *
     * @param totalMinutes
     * @return
     */
    public static String getShowFlyingTime(int totalMinutes) {
        int hour = totalMinutes / 60;
        int minute = totalMinutes % 60;
        StringBuilder builder = new StringBuilder("Duration:");
        if (hour == 1) {
            builder.append("One hour");
        } else if (hour > 1) {
            builder.append(hour).append(" hours");
        }
        if (minute == 1) {
            builder.append("a minute");
        } else if (hour > 1) {
            builder.append(minute).append(" minutes");
        }
        return builder.toString();
    }

    public static String translateTravelItineraryTime(String date, String time) throws ParseException {
        Date dateTemp = parse(date, "yyyy-MM-dd");
        DateFormat df = new SimpleDateFormat("EEEEEEEEEE,dd MMMMMMM yyyy,", Locale.ENGLISH);
        String dateShow = df.format(dateTemp);
        return new StringBuilder(dateShow).append(time).toString();
    }

    public static int timeDiff(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            throw new NullPointerException("date can not be null.");
        }
        Long diff = (d1.getTime() - d2.getTime()) / 86400000;
        return diff.intValue();
    }

    /**
     * @param d1   第一个时间
     * @param d2   第二个时间
     * @param unit 时间差单位(Calendar.MINUTE,Calendar.SECOND,Calendar.DATE,Calendar.
     *             MILLISECOND)
     * @return
     */
    public static int timeDiff(Date d1, Date d2, int unit) {
        if (d1 == null || d2 == null) {
            throw new NullPointerException("date can not be null.");
        }
        Long diff = null;
        switch (unit) {
            case Calendar.MINUTE:
                diff = (d1.getTime() - d2.getTime()) / 60000;
                return diff.intValue();
            case Calendar.SECOND:
                diff = (d1.getTime() - d2.getTime()) / 1000;
                return diff.intValue();
            case Calendar.HOUR:
            case Calendar.HOUR_OF_DAY:
                diff = (d1.getTime() - d2.getTime()) / 3600000;
                return diff.intValue();
            case Calendar.DATE:
                diff = (d1.getTime() - d2.getTime()) / 86400000;
                return diff.intValue();
            case Calendar.MILLISECOND:
                diff = d1.getTime() - d2.getTime();
                return diff.intValue();
            default:
                throw new RuntimeException("invalid time unit.");
        }
    }

    public static Long daysBetween(Date sourceDate, Date targetDate) throws ParseException {
        Long time2 = targetDate.getTime();
        Long time1 = sourceDate.getTime();
        Long temp = time2 - time1;
        Long between_days = temp % (1000 * 3600 * 24) > 0 ? (temp / (1000 * 3600 * 24)) + 1L : temp / (1000 * 3600 * 24);
        return between_days;
    }

    public static void main(String[] args) throws ParseException {
        String a = "2016-01-01T13:50:00.000+1000";
        Date date = new Date();
        String ss = DateUtil.format(date, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        System.out.println(DateUtil.daysBetween(new Date(), DateUtil.parse(a, "yyyy-MM-dd'T'HH:mm:ss.SSSZ")));
    }

}
