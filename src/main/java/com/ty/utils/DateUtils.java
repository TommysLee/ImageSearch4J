package com.ty.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日期时间工具类
 *
 * @Author Tommy
 * @Date 2022/1/26
 */
public final class DateUtils {

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public static final String DATESTYLE_EX_YEAR_MONTH = DEFAULT_DATE_FORMAT;

    public static final String DATESTYLE_DETAIL_HH_MM_SS = "yyyy/MM/dd HH:mm:ss";

    public static int THOUSAND = 1000;

    /** 日期正则 **/
    public static final String DATE_REGEX = "(\\d{4}[-/]?\\d{1,2}[-/]?\\d{1,2})(\\s(\\d{1,2}\\:\\d{1,2}(\\:\\d{1,2})?))?";

    private DateUtils() {

    }

    /**
     * 根据参数构建日期对象
     *
     * @param parts 日期各组成部分，依次为：年、月、日、时、分、秒
     * @return Date
     */
    public static Date buildDate(int ... parts) {
        Date date = null;
        int len = parts.length;
        if (len > 0) {
            int[] t = {0, 1, 1, 0, 0, 0};
            for (int i = 0; i < len; i++) {
                t[i] = parts[i];
            }
            date = asDate(LocalDateTime.of(t[0], t[1], t[2], t[3], t[4], t[5]));
        }
        return date;
    }

    /**
     * 获取日期格式化对象
     *
     * @param pattern
     * @return SimpleDateFormat
     */
    private static SimpleDateFormat getDateParser(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    /**
     * 毫秒数转化为时间字符串
     *
     * @param time
     *            毫秒数
     * @param pattern
     *            格式
     * @return 时间字符串
     */
    public static String longToDate(Long time, String pattern) {
        if (time != null)
            return getDateParser(pattern).format(new Date(time));
        else
            return "";
    }

    /**
     * 毫秒数转化为时间字符串
     *
     * @param time
     *            毫秒数
     * @return 时间字符串
     */
    public static String longToDate(Long time) {
        return getDateParser(DEFAULT_DATE_FORMAT).format(new Date(time));
    }

    /**
     * 毫秒数转化为时间字符串
     *
     * @param time
     *            毫秒数
     * @return 时间字符串
     */
    public static String longToDate2(Long time) {
        return getDateParser("MM-dd HH:mm").format(new Date(time));
    }

    /**
     * 毫秒数转化为时间字符串
     *
     * @param time
     *            毫秒数
     * @return 时间字符串
     */
    public static String longToDateAll(Long time) {
        return getDateParser(DEFAULT_DATE_TIME_FORMAT).format(new Date(time));
    }

    /**
     * 毫秒数转化为时间字符串
     *
     * @param time
     *            毫秒数
     * @return 时间字符串
     */
    public static String longToDateAllNew(Long time) {
        return getDateParser("yyyyMMddHHmmss").format(new Date(time));
    }

    /**
     * 将时间增加指定的偏移量，单位：秒
     *
     * @param n 偏移量
     * @return Date
     */
    public static Date offset(Date date, int n) {
        if (null != date) {
            return new Date(date.getTime() + n * 1000);
        }
        return date;
    }

    /**
     * 获取当前时间
     *
     * @return 当前时间
     */
    public static String nowText() {
        return getDateParser(DEFAULT_DATE_TIME_FORMAT).format(now());
    }

    /**
     * 获取当前时间
     *
     * @param offset 时间偏移量
     * @return 当前时间
     */
    public static Date now(int... offset) {
        Date now = new Date();
        if (offset.length > 0) {
            now = offset(now, offset[0]);
        }
        return now;
    }

    /**
     * 获取时间的小时数
     *
     * @param date 日期
     * @return int
     */
    public static int hour(Date ... date) {
        Date time = date.length > 0? date[0] : now();
        return asLocalDateTime(time).getHour();
    }

    /**
     * String型日期转为long型
     *
     * @param source
     *            时间字符串
     * @return 毫秒数
     */
    public static long dateToLong(String source) {
        if (StringUtils.isNotBlank(source)) {
            try {
                return getDateParser("yyyy/MM/dd").parse(source).getTime();
            } catch (ParseException e) {
                try {
                    return getDateParser(DEFAULT_DATE_FORMAT).parse(source).getTime();
                } catch (ParseException e1) {
                    return -1;
                }
            }
        }
        return -1;
    }

    /***
     * String型日期转为long型
     *
     * @param source
     *            时间字符串
     * @return 毫秒数
     */
    public static long dateToLong(String source, String pattern) {
        if (StringUtils.isNotBlank(source)) {
            try {
                return getDateParser(pattern).parse(source).getTime();
            } catch (ParseException e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * 时间字符串增加一天
     *
     * @param source
     *            时间字符串
     * @return 增加一天后的毫秒数
     */
    public static long dateAddOneDayAndToLong(String source) {
        try {
            Date date = getDateParser("yyyy/MM/dd").parse(source);
            Calendar cd = Calendar.getInstance();
            cd.setTime(date);
            cd.add(Calendar.DAY_OF_MONTH, 1);
            return cd.getTime().getTime();
        } catch (ParseException e) {
            try {
                Date date = getDateParser(DEFAULT_DATE_FORMAT).parse(source);
                Calendar cd = Calendar.getInstance();
                cd.setTime(date);
                cd.add(Calendar.DAY_OF_MONTH, 1);
                return cd.getTime().getTime();
            } catch (ParseException e1) {
                return -1;
            }
        }
    }

    /**
     * 时间字符串增加一天
     *
     * @param source
     *            时间字符串
     * @return 增加一天后的毫秒数
     */
    public static long nextDate(String source) {
        try {
            return getDateParser("yyyy/MM/dd").parse(source).getTime() + 24
                    * 60 * 60 * 1000;
        } catch (ParseException e) {
            try {
                return getDateParser(DEFAULT_DATE_FORMAT).parse(source).getTime() + 24
                        * 60 * 60 * 1000;
            } catch (ParseException e1) {
                return -1;
            }
        }

    }

    /**
     * 毫秒数减去一天后的时间字符串
     *
     * @param time
     *            毫秒数
     * @return 毫秒数减去一天后的时间字符串
     */
    public static String longToFrontDate(Long time) {
        return getDateParser(DEFAULT_DATE_FORMAT).format(
                new Date(time - 24 * 60 * 60 * 1000));
    }

    /***
     * String型日期转为long型
     *
     * @param source
     *            String型日期
     * @return long 日期
     * @throws ParseException
     */
    public static long dateAllToLong(String source) {
        try {
            return getDateParser("yyyy/MM/dd HH:mm:ss").parse(source).getTime();
        } catch (ParseException e) {
            try {
                return getDateParser(DEFAULT_DATE_TIME_FORMAT).parse(source)
                        .getTime();
            } catch (ParseException e1) {
                return -1;
            }
        }
    }

    /**
     * 获取年月日
     *
     * @return 年月日
     */
    public static long genYMD() {
        return Long.valueOf(getDateParser("yyyyMMdd").format(new Date()));
    }

    /**
     * 获取年月
     *
     * @return 年月
     */
    public static int genYM(Date date) {
        date = null != date? date : new Date();
        return Integer.parseInt(getDateParser("yyyyMM").format(date));
    }

    /**
     * 获取日期字符
     *
     * @param date
     *            日期
     * @param format
     *            格式 如:yyy-MM-dd
     * @return 日期字符
     */
    public static String getDate(Date date, String format) {
        return getDateParser(format).format(date);
    }

    /**
     * 获取日期对象
     *
     * @param dataString
     *            时间字符串
     * @param format
     *            格式
     * @return 日期对象
     * @throws ParseException
     *             ParseException
     */
    public static Date toDate(String dataString, String format)
            throws ParseException {
        return getDateParser(format).parse(dataString);
    }

    /**
     * 获取系统时间
     *
     * @return 当前日期
     */
    public static String time() {
        return nowText();
    }

    /**
     * 获取当前日期
     *
     * @return 当前日期
     */
    public static String getCurrentDate() {
        return DateUtils.getDate(new Date(), "yy/MM/dd");
    }

    /**
     * 获得当前日期
     *
     * @return 当前日期
     */
    public static long getNowDate() {
        return DateUtils.dateToLong(getDateParser(DEFAULT_DATE_FORMAT).format(
                new Date()));
    }

    /**
     * 获得指定日期的毫秒数
     *
     * @param curDate
     *            指定日期
     * @return 指定日期的毫秒数
     */
    public static long getNowDate(Date curDate) {
        return DateUtils
                .dateToLong(getDateParser(DEFAULT_DATE_FORMAT).format(curDate));
    }

    /**
     * 获取当前时间加上任意天数后的日期
     *
     * @param dayNum
     *            天数
     * @return 当前时间加上任意天数后的日期
     */
    public static String getNewDateByAdd(int dayNum) {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, dayNum);
        return (getDateParser(DEFAULT_DATE_FORMAT)).format(cal.getTime());
    }

    /**
     * 获取当前时间加上任意天数后的日期
     *
     * @param dayNum
     *            天数
     * @param format
     *            格式
     * @return 当前时间加上任意天数后的日期
     */
    public static String getNewDateByAdd(int dayNum, String format) {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, dayNum);
        return (getDateParser(format)).format(cal.getTime());
    }

    /**
     * 获取指定时间加上任意时间后的日期
     *
     * @param date
     *            指定时间
     * @param num
     *            指定增加格式的增加量
     * @param timeType
     *            指定增加格式
     * @return 指定时间加上任意时间后的日期
     */
    public static Date getDateByAdd(Date date, int num, int timeType) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(timeType, num);
        return cal.getTime();
    }

    /**
     * 获取指定时间加上任意小时后的日期
     *
     * @param hour
     *            小时
     * @return 指定时间加上任意小时后的日期
     */
    public static String getNewDateByAddHour(int hour) {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, hour);
        return (getDateParser(DEFAULT_DATE_TIME_FORMAT)).format(cal.getTime());
    }

    /**
     * 获取当前月份最大天数
     *
     * @author lidong
     * @return 当前月份最大天数
     * @2011-12-26
     */
    public static int lastDayOfMonth() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int value = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        return value;
    }

    /**
     * 将日期字符串转换为日期对象
     *
     * @param dateText
     *            ----> 日期字符串(yyyy-MM-dd 或 yyyy/MM/dd)
     * @return Date
     */
    public static Date parseDate(String dateText) {

        Date date = null;
        if (StringUtils.isNotBlank(dateText)) {
            dateText = dateText.replace("/", "-");
            final SimpleDateFormat dateFormat = getDateParser(DEFAULT_DATE_FORMAT);
            try {
                date = dateFormat.parse(dateText);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    /**
     * 将String型日期转换成日期对象
     *
     * @param 	dateText 日期
     * @param 	format	 日期格式
     * @return
     */
    public static Date parseDate(String dateText, String format) throws ParseException {

        Date date = null;
        if (StringUtils.isNotBlank(dateText) && StringUtils.isNotBlank(format)) {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
            simpleDateFormat.applyPattern(format);
            try {
                date = simpleDateFormat.parse(dateText);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    /**
     * 将日期对象转换为日期字符串
     *
     * @param date
     * @return String
     */
    public static String toText(Date date) {

        String result = null;
        final SimpleDateFormat dateFormat = new SimpleDateFormat();
        final String datePattern = DEFAULT_DATE_FORMAT;
        final String dateTimePattern = DEFAULT_DATE_TIME_FORMAT;
        final String timePattern = "HHmmss";
        if (null != date) {
            dateFormat.applyPattern(timePattern);
            if (Integer.parseInt(dateFormat.format(date)) > 0) {
                dateFormat.applyPattern(dateTimePattern);
            } else {
                dateFormat.applyPattern(datePattern);
            }
            result = dateFormat.format(date);
        }
        return result;
    }

    /**
     * 计算两个日期之间相差的天数
     *
     * @param smdate
     *            较小的时间
     * @param bdate
     *            较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    public static int daysBetween(Date smdate, Date bdate)
            throws ParseException {
        SimpleDateFormat sdf = getDateParser(DEFAULT_DATE_FORMAT);
        smdate = sdf.parse(sdf.format(smdate));
        bdate = sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 判断日期是否在指定日期范围内（全闭区间）
     *
     * @param target 目标日期
     * @param min    日期范围左边界（包含）
     * @param max    日期范围右边界（包含）
     * @return boolean
     */
    public static boolean between(LocalDate target, LocalDate min, LocalDate max) {
        boolean flag = false;
        if (null != target) {
            flag = !target.isBefore(min) && !target.isAfter(max);
        }
        return flag;
    }

    /**
     * 判断时间是否在指定时间范围内（全闭区间）
     *
     * @param target 目标时间
     * @param min    时间范围左边界（包含）
     * @param max    时间范围右边界（包含）
     * @return boolean
     */
    public static boolean between(LocalTime target, LocalTime min, LocalTime max) {
        boolean flag = false;
        if (null != target) {
            flag = !target.isBefore(min) && !target.isAfter(max);
        }
        return flag;
    }

    /**
     * 获取当月的天数
     *
     * @return int
     */
    public static int getDayOfMonth() {
        LocalDate date = LocalDate.now();
        return date.getDayOfMonth();
    }

    /**
     * 判断字符串是否为日期格式
     *
     * @param dateText
     * @return boolean
     */
    public static boolean isDate(String dateText) {
        boolean flag = false;
        if (StringUtils.isNotBlank(dateText)) {
            flag = Pattern.compile(DATE_REGEX).matcher(dateText).matches();
        }
        return flag;
    }

    /**
     * 将日期字符串转换为日期对象
     *
     * @param dateText
     * @return Date
     */
    public static Date toDate(String dateText) {
        Date date = null;
        if (isDate(dateText)) {
            dateText = StringUtils.replace(dateText, "/", "-");
            final Matcher matcher = Pattern.compile(DATE_REGEX).matcher(dateText);
            matcher.matches();

            final StringBuilder pattern = new StringBuilder(
                    StringUtils.contains(dateText, "-") ? DEFAULT_DATE_FORMAT : "yyyyMMdd");
            pattern.append(StringUtils.isNotBlank(matcher.group(3)) ? "HH:mm" : StringUtils.EMPTY);
            pattern.append(StringUtils.isNotBlank(matcher.group(4)) ? ":ss" : StringUtils.EMPTY);
            try {
                date = new SimpleDateFormat(pattern.toString()).parse(dateText);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    /**
     * Date类型转为String类型
     *
     * @param date 输入的日期
     * @param style 输入的日期格式
     * @return String
     */
    public static String dateToString(Date date, String style) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat dFmt = new SimpleDateFormat(style);
        return dFmt.format(date);
    }

    /**
     * 将时间乘以指定的基数
     *
     * @param time 时间的毫秒数
     * @param radix 基数
     * @return Date
     */
    public static Date multiply(long time, double radix) {
        return new Date(Double.valueOf(time * radix).longValue());
    }

    /**
     * 时间大于运算符
     *
     * @param date1
     * @param date2
     * @return boolean
     */
    public static boolean gt(Date date1, Date date2) {

        boolean flag = false;
        if (null != date1 && null != date2) {
            flag = date1.getTime() > date2.getTime();
        }
        return flag;
    }

    /**
     * 以给定的时间单位，判定两个时间的时间差是否超出指定的时间间隔
     *
     * @param date1 第一个时间
     * @param date2 第二个时间
     * @param interval 时间间隔
     * @param timeUnit 时间单位
     * @return boolean
     */
    public static boolean isBeyond(Date date1, Date date2, int interval, TimeUnit timeUnit) {

        boolean flag = false;
        if (null != date1 && null != date2) {
            long date1Ms = date1.getTime();
            long date2Ms = date2.getTime();

            // 时间差绝对值
            long span = date1Ms - date2Ms;
            switch (timeUnit) {
                case SECONDS:
                    interval = interval * THOUSAND;
                    break;
                case MINUTES:
                    interval = interval * 60 * THOUSAND;
                    break;
                case HOURS:
                    interval = interval * 60 * 60 * THOUSAND;
                    break;
                case DAYS:
                    interval = interval * 24 * 60 * 60 * THOUSAND;
                    break;
            }
            flag = span >= interval;
        }
        return flag;
    }

    /**
     * 判定两个时间的时间差是否超出指定的时间间隔（单位：分钟）
     *
     * @param date1 第一个时间
     * @param date2 第二个时间
     * @param interval 时间间隔
     * @return boolean
     */
    public static boolean isBeyond(Date date1, Date date2, int interval) {
        return isBeyond(date1, date2, interval, TimeUnit.MINUTES);
    }

    /**
     * 判断指定时间与当前时间的时间差是否超出时间间隔
     *
     * @param date 比较的日期对象
     * @param interval 时间间隔（单位：秒）
     * @return boolean
     */
    public static boolean isBeyondNow(Date date, int interval) {
        return isBeyond(now(), date, interval, TimeUnit.SECONDS);
    }

    /**
     * 以给定的时间单位，判定两个时间的时间差是否在指定的间隔内
     *
     * @param date1 第一个时间
     * @param date2 第二个时间
     * @param interval 时间间隔
     * @param timeUnit 时间单位
     * @return boolean
     */
    public static boolean isBelow(Date date1, Date date2, int interval, TimeUnit timeUnit) {
        boolean flag = false;
        if (null != date1 && null != date2) {
            long date1Ms = date1.getTime();
            long date2Ms = date2.getTime();

            // 时间差绝对值
            long span = date1Ms - date2Ms;
            switch (timeUnit) {
                case SECONDS:
                    interval = interval * THOUSAND;
                    break;
                case MINUTES:
                    interval = interval * 60 * THOUSAND;
                    break;
                case HOURS:
                    interval = interval * 60 * 60 * THOUSAND;
                    break;
                case DAYS:
                    interval = interval * 24 * 60 * 60 * THOUSAND;
                    break;
            }
            flag = span < interval;
        }
        return flag;
    }

    /**
     * 判定两个时间的时间差是否在指定的间隔内（单位：分钟）
     *
     * @param date1 第一个时间
     * @param date2 第二个时间
     * @param interval 时间间隔
     * @return boolean
     */
    public static boolean isBelow(Date date1, Date date2, int interval) {
        return isBelow(date1, date2, interval, TimeUnit.MINUTES);
    }

    /**
     * 如果日期对象为空，则返回默认值
     *
     * @param date
     * @param defaultDate
     * @return Date
     */
    public static Date defaultIfBlank(Date date, Date defaultDate) {
        return null != date? date : defaultDate;
    }

    /**
     * 如果日期对象为空，则返回当前时间
     *
     * @param date
     * @return Date
     */
    public static Date nowIfBlank(Date date) {
        return defaultIfBlank(date, now());
    }

    /**
     * 将LocalDate对象转换为Date对象
     *
     * @param localDate
     * @return Date
     */
    public static Date asDate(LocalDate localDate) {
       return Date.from(asInstant(localDate));
    }

    /**
     * 将LocalDate对象转换为Instant对象
     *
     * @param localDate
     * @return Instant
     */
    public static Instant asInstant(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    /**
     * 将LocalDateTime对象转换为Date对象
     *
     * @param localDateTime
     * @return Date
     */
    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(asInstant(localDateTime));
    }

    /**
     * 将LocalDateTime对象转换为Instant对象
     *
     * @param localDateTime
     * @return Instant
     */
    public static Instant asInstant(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * 将Date对象转换为LocalDate对象
     *
     * @param date
     * @return LocalDate
     */
    public static LocalDate asLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 将Date对象转换为LocalDateTime对象
     *
     * @param date
     * @return LocalDateTime
     */
    public static LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 将日期减去指定的年数
     *
     * @param date
     * @param n
     * @return Date
     */
    public static Date minusYears(Date date, int n) {
        if (null != date) {
            LocalDateTime dateTime = asLocalDateTime(date);
            return asDate(dateTime.minusYears(n));
        }
        return date;
    }

    /**
     * 将日期减去指定的月数
     *
     * @param date
     * @param n
     * @return Date
     */
    public static Date minusMonths(Date date, int n) {
        if (null != date) {
            LocalDateTime dateTime = asLocalDateTime(date);
            return asDate(dateTime.minusMonths(n));
        }
        return date;
    }

    /**
     * 将日期减去指定的天数
     *
     * @param date
     * @param n
     * @return Date
     */
    public static Date minusDays(Date date, int n) {
        if (null != date) {
            LocalDateTime dateTime = asLocalDateTime(date);
            return asDate(dateTime.minusDays(n));
        }
        return date;
    }

    /**
     * 将日期减去指定的小时数
     *
     * @param date
     * @param n
     * @return Date
     */
    public static Date minusHours(Date date, int n) {
        if (null != date) {
            LocalDateTime dateTime = asLocalDateTime(date);
            return asDate(dateTime.minusHours(n));
        }
        return date;
    }

    /**
     * 获取年的第一天的日期
     *
     * @param date
     * @return Date
     */
    public static Date firstDayOfYear(Date date) {
        if (null != date) {
            LocalDate localDate = asLocalDate(date);
            return firstDayOfYear(localDate.getYear());
        }
        return null;
    }

    /**
     * 获取指定年份的第一天的日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date firstDayOfYear(int year) {
        return asDate(LocalDate.of(year, 1, 1));
    }

    /**
     * 获取年的最后一天的日期
     *
     * @param date
     * @return Date
     */
    public static Date lastDayOfYear(Date date) {
        if (null != date) {
            LocalDate localDate = asLocalDate(date);
            return lastDayOfYear(localDate.getYear());
        }
        return null;
    }

    /**
     * 获取指定年份的最后一天的日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date lastDayOfYear(int year) {
        return asDate(LocalDate.of(year, 12, 31));
    }

    /**
     * 获取月的第一天的日期
     *
     * @param date
     * @return Date
     */
    public static Date firstDayOfMonth(Date date) {
        if (null != date) {
            LocalDate localDate = asLocalDate(date);
            return asDate(LocalDate.of(localDate.getYear(), localDate.getMonthValue(), 1));
        }
        return date;
    }

    /**
     * 获取月的最后一天的日期
     *
     * @param date
     * @return Date
     */
    public static Date lastDayOfMonth(Date date) {
        if (null != date) {
            LocalDate localDate = asLocalDate(date);
            return asDate(LocalDate.of(localDate.getYear(), localDate.getMonthValue(), localDate.lengthOfMonth()));
        }
        return date;
    }

    /**
     * 前一天
     *
     * @param date
     * @return Date
     */
    public static Date prevDay(Date date) {
        if (null != date) {
            LocalDate localDate = asLocalDate(date);
            return asDate(localDate.minusDays(1));
        }
        return date;
    }

    /**
     * 次日
     *
     * @param date
     * @return Date
     */
    public static Date nextDay(Date date) {
        if (null != date) {
            LocalDate localDate = asLocalDate(date);
            return asDate(localDate.plusDays(1));
        }
        return date;
    }

    /**
     * 次年第一天
     *
     * @param date
     * @return Date
     */
    public static Date firstDayOfNextYear(Date date) {
        return nextDay(lastDayOfYear(date));
    }

    /**
     * 次月第一天
     *
     * @param date
     * @return Date
     */
    public static Date firstDayOfNextMonth(Date date) {
        return nextDay(lastDayOfMonth(date));
    }

    /**
     * 时间截断
     *
     * @param date
     * @param unit the unit to truncate to, not null
     * @return Date
     */
    public static Date truncatedTo(Date date, TemporalUnit unit) {
        if (null != date) {
            LocalDateTime dateTime = asLocalDateTime(date).truncatedTo(unit);
            date = asDate(dateTime);
        }
        return date;
    }
}
