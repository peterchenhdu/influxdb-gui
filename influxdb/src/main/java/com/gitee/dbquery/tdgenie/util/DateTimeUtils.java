
package com.gitee.dbquery.tdgenie.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;

/**
 * Java8时间工具类
 */
public class DateTimeUtils {

    // 时间元素
    public static final String YEAR = "year";
    public static final String MONTH = "month";
    public static final String WEEK = "week";
    public static final String DAY = "day";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String SECOND = "second";
    public static final String REALTIME = "realtime";

    // 星期元素
    public static final String MONDAY = "MONDAY";// 星期一
    public static final String TUESDAY = "TUESDAY";// 星期二
    public static final String WEDNESDAY = "WEDNESDAY";// 星期三
    public static final String THURSDAY = "THURSDAY";// 星期四
    public static final String FRIDAY = "FRIDAY";// 星期五
    public static final String SATURDAY = "SATURDAY";// 星期六
    public static final String SUNDAY = "SUNDAY";// 星期日

    public static final String YYYY_MM_DD_STR = "yyyy-MM-dd";
    public static final String YYYY_MM_DD_HH_MM_SS_STR = "yyyy-MM-dd HH:mm:ss";
    public static final String HH_MM_SS_STR = "HH:mm:ss";

    // 根据指定格式显示日期和时间
    /**
     * yyyy-MM-dd
     */
    public static final DateTimeFormatter YYYY_MM_DD_EN = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * yyyy-MM-dd HH
     */
    public static final DateTimeFormatter YYYY_MM_DD_HH_EN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
    /**
     * yyyy-MM-dd HH:mm
     */
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_EN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_EN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * yyyyMMddHHmmss
     */
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static final DateTimeFormatter YYYY_MM_DD_T_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateTimeFormatter YYYY_MM_DD_T_HH_MM_SS_S = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
    public static final String STANDARD_TIME_PATTEN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    /**
     * HH:mm:ss
     */
    public static final DateTimeFormatter HH_MM_SS_EN = DateTimeFormatter.ofPattern("HH:mm:ss");
    /**
     * yyyy年MM月dd日
     */
    public static final DateTimeFormatter YYYY_MM_DD_CN = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    /**
     * yyyy年MM月dd日HH时
     */
    public static final DateTimeFormatter YYYY_MM_DD_HH_CN = DateTimeFormatter.ofPattern("yyyy年MM月dd日HH时");
    /**
     * yyyy年MM月dd日HH时mm分
     */
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_CN = DateTimeFormatter.ofPattern("yyyy年MM月dd日HH时mm分");
    /**
     * yyyy年MM月dd日HH时mm分ss秒
     */
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_CN = DateTimeFormatter.ofPattern("yyyy年MM月dd日HH时mm分ss秒");
    /**
     * HH时mm分ss秒
     */
    public static final DateTimeFormatter HH_MM_SS_CN = DateTimeFormatter.ofPattern("HH时mm分ss秒");

    // 本地时间显示格式：区分中文和外文显示
    public static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
    public static final DateTimeFormatter FULL_DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);
    public static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
    public static final DateTimeFormatter MEDIUM_DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    public static String formatNow(String formatStr) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(formatStr));
    }


    public static String formatNow(DateTimeFormatter formatter) {
        return LocalDateTime.now().format(formatter);
    }

    /**
     * 获取中文当前时间
     *
     * @return yyyy年MM月dd日HH时mm分ss秒
     */
    public static String getNowTimeCN() {
        return LocalDateTime.now().format(YYYY_MM_DD_HH_MM_SS_CN);
    }

    /**
     * 简写本地当前日期：yy-M-dd<br>
     * 例如：19-3-30为2019年3月30日
     *
     * @return 字符串yy-M-dd
     */
    public static String getNowLocalTimeShot() {
        return LocalDateTime.now().format(SHORT_DATE);
    }

    /**
     * 根据当地日期显示格式：yyyy年M月dd日 星期？（中国）
     *
     * @return 形如：2019年3月30日 星期六
     */
    public static String getNowLocalTimeFull() {
        return LocalDateTime.now().format(FULL_DATE);
    }

    /**
     * 根据当地显示日期格式：yyyy年M月dd日（中国）
     *
     * @return 形如 2019年3月30日
     */
    public static String getNowLocalTimeLong() {
        return LocalDateTime.now().format(LONG_DATE);
    }

    /**
     * 根据当地显示日期格式：yyyy-M-dd（中国）
     *
     * @return 形如：2019-3-30
     */
    public static String getNowLocalTimeMedium() {
        return LocalDateTime.now().format(MEDIUM_DATE);
    }

    /**
     * 时间转换 LocalDateTime to 2022-11-04T09:11:00.222+08:00
     *
     * @param dateTime LocalDateTime
     * @return String
     */
    public static String dateToCST(LocalDateTime dateTime) {
        OffsetDateTime od = OffsetDateTime.of(dateTime, ZoneOffset.of("+8"));
        return od.format(DateTimeFormatter.ofPattern(STANDARD_TIME_PATTEN));
    }


    public static LocalDateTime CSTToDate(String dateStr) {
        OffsetDateTime od = OffsetDateTime.parse(dateStr, DateTimeFormatter.ofPattern(STANDARD_TIME_PATTEN));
        return od.toLocalDateTime();
    }


    /**
     * 获取当前日期的节点时间（年，月，周，日，时，分，秒）
     *
     * @param node 日期中的节点元素（年，月，周，日，时，分，秒）
     * @return 节点数字，如创建此方法的时间：年 2019，月 3，日 30，周 6
     */
    public static Integer getNodeTime(String node) {
        LocalDateTime today = LocalDateTime.now();
        Integer resultNode = null;
        switch (node) {
            case YEAR:
                resultNode = today.getYear();
                break;
            case MONTH:
                resultNode = today.getMonthValue();
                break;
            case WEEK:
                resultNode = transformWeekEN2Num(String.valueOf(today.getDayOfWeek()));
                break;
            case DAY:
                resultNode = today.getDayOfMonth();
                break;
            case HOUR:
                resultNode = today.getHour();
                break;
            case MINUTE:
                resultNode = today.getMinute();
                break;
            case SECOND:
                resultNode = today.getSecond();
                break;
            default:
                // 当前日期是当前年的第几天。例如：2019/1/3是2019年的第三天
                resultNode = today.getDayOfYear();
                break;
        }
        return resultNode;
    }

    /**
     * 将英文星期转换成数字
     *
     * @param enWeek 英文星期
     * @return int，如果数字小于0，则检查，看是否输入错误 or 入参为null
     */
    public static int transformWeekEN2Num(String enWeek) {
        if (MONDAY.equals(enWeek)) {
            return 1;
        } else if (TUESDAY.equals(enWeek)) {
            return 2;
        } else if (WEDNESDAY.equals(enWeek)) {
            return 3;
        } else if (THURSDAY.equals(enWeek)) {
            return 4;
        } else if (FRIDAY.equals(enWeek)) {
            return 5;
        } else if (SATURDAY.equals(enWeek)) {
            return 6;
        } else if (SUNDAY.equals(enWeek)) {
            return 7;
        } else {
            return -1;
        }
    }

    /**
     * 获取当前日期之后（之后）的节点事件<br>
     * <ul>
     * 比如当前时间为：2019-03-30 10:20:30
     * </ul>
     * <li>node="hour",num=5L:2019-03-30 15:20:30</li>
     * <li>node="day",num=1L:2019-03-31 10:20:30</li>
     * <li>node="year",num=1L:2020-03-30 10:20:30</li>
     *
     * @param node 节点元素（“year”,"month","week","day","huor","minute","second"）
     * @param num  第几天（+：之后，-：之前）
     * @return 之后或之后的日期
     */
    public static String getAfterOrPreNowTime(String node, Long num) {
        LocalDateTime now = LocalDateTime.now();
        if (HOUR.equals(node)) {
            return now.plusHours(num).format(YYYY_MM_DD_HH_MM_SS_EN);
        } else if (DAY.equals(node)) {
            return now.plusDays(num).format(YYYY_MM_DD_HH_MM_SS_EN);
        } else if (WEEK.equals(node)) {
            return now.plusWeeks(num).format(YYYY_MM_DD_HH_MM_SS_EN);
        } else if (MONTH.equals(node)) {
            return now.plusMonths(num).format(YYYY_MM_DD_HH_MM_SS_EN);
        } else if (YEAR.equals(node)) {
            return now.plusYears(num).format(YYYY_MM_DD_HH_MM_SS_EN);
        } else if (MINUTE.equals(node)) {
            return now.plusMinutes(num).format(YYYY_MM_DD_HH_MM_SS_EN);
        } else if (SECOND.equals(node)) {
            return now.plusSeconds(num).format(YYYY_MM_DD_HH_MM_SS_EN);
        } else {
            return "Node is Error!";
        }
    }

    /**
     * 获取与当前日期相距num个之后（之前）的日期<br>
     * <ul>
     * 比如当前时间为：2019-03-30 10:20:30的格式日期
     * <li>node="hour",num=5L:2019-03-30 15:20:30</li>
     * <li>node="day",num=1L:2019-03-31 10:20:30</li>
     * <li>node="year",num=1L:2020-03-30 10:20:30</li>
     * </ul>
     *
     * @param dtf  格式化当前时间格式（dtf = yyyyMMddHHmmss_EN）
     * @param node 节点元素（“year”,"month","week","day","huor","minute","second"）
     * @param num  （+：之后，-：之前）
     * @return 之后之前的日期
     */
    public static String getAfterOrPreNowTimePlus(DateTimeFormatter dtf, String node, Long num) {
        LocalDateTime now = LocalDateTime.now();
        if (HOUR.equals(node)) {
            return now.plusHours(num).format(dtf);
        } else if (DAY.equals(node)) {
            return now.plusDays(num).format(dtf);
        } else if (WEEK.equals(node)) {
            return now.plusWeeks(num).format(dtf);
        } else if (MONTH.equals(node)) {
            return now.plusMonths(num).format(dtf);
        } else if (YEAR.equals(node)) {
            return now.plusYears(num).format(dtf);
        } else if (MINUTE.equals(node)) {
            return now.plusMinutes(num).format(dtf);
        } else if (SECOND.equals(node)) {
            return now.plusSeconds(num).format(dtf);
        } else {
            return "Node is Error!";
        }
    }

    /**
     * 当前时间的hour，minute，second之后（之前）的时刻
     *
     * @param node 时间节点元素（hour，minute，second）
     * @param num  之后（之后）多久时，分，秒（+：之后，-：之前）
     * @return HH:mm:ss 字符串
     */
    public static String getAfterOrPreNowTimeSimp(String node, Long num) {
        LocalTime now = LocalTime.now();
        if (HOUR.equals(node)) {
            return now.plusHours(num).format(HH_MM_SS_EN);
        } else if (MINUTE.equals(node)) {
            return now.plusMinutes(num).format(HH_MM_SS_EN);
        } else if (SECOND.equals(node)) {
            return now.plusSeconds(num).format(HH_MM_SS_EN);
        } else {
            return "Node is Error!";
        }
    }

    /**
     * 检查重复事件，比如生日。TODO This is a example.
     *
     * @return
     */
    public static boolean isBirthday(int month, int dayOfMonth) {
        MonthDay birthDay = MonthDay.of(month, dayOfMonth);
        MonthDay curMonthDay = MonthDay.from(LocalDate.now());// MonthDay只存储了月、日。
        return birthDay.equals(curMonthDay);
    }

    /**
     * 获取当前日期第index日之后(之前)的日期（yyyy-MM-dd）
     *
     * @param index 第index天
     * @return 日期字符串：yyyy-MM-dd
     */
    public static String getAfterOrPreDayDate(int index) {
        return LocalDate.now().plus(index, ChronoUnit.DAYS).format(YYYY_MM_DD_EN);
    }

    /**
     * 获取当前日期第index周之前（之后）的日期（yyyy-MM-dd）
     *
     * @param index 第index周（+：之后，-：之前）
     * @return 日期字符串：yyyy-MM-dd
     */
    public static String getAfterOrPreWeekDate(int index) {
        return LocalDate.now().plus(index, ChronoUnit.WEEKS).format(YYYY_MM_DD_EN);
    }

    /**
     * 获取当前日期第index月之前（之后）的日期（yyyy-MM-dd）
     *
     * @param index 第index月（+：之后，-：之前）
     * @return 日期字符串：yyyy-MM-dd
     */
    public static String getAfterOrPreMonthDate(int index) {
        return LocalDate.now().plus(index, ChronoUnit.MONTHS).format(YYYY_MM_DD_EN);
    }

    /**
     * 获取当前日期第index年之前（之后）的日期（yyyy-MM-dd）
     *
     * @param index 第index年（+：之后，-：之前）
     * @return 日期字符串：yyyy-MM-dd
     */
    public static String getAfterOrPreYearDate(int index) {
        return LocalDate.now().plus(index, ChronoUnit.YEARS).format(YYYY_MM_DD_EN);
    }

    /**
     * 获取指定日期之前之后的第index的日，周，月，年的日期
     *
     * @param date  指定日期格式：yyyy-MM-dd
     * @param node  时间节点元素（日周月年）
     * @param index 之前之后第index个日期
     * @return yyyy-MM-dd 日期字符串
     */
    public static String getAfterOrPreDate(String date, String node, int index) {
        date = date.trim();
        if (DAY.equals(node)) {
            return LocalDate.parse(date).plus(index, ChronoUnit.DAYS).format(YYYY_MM_DD_EN);
        } else if (WEEK.equals(node)) {
            return LocalDate.parse(date).plus(index, ChronoUnit.WEEKS).format(YYYY_MM_DD_EN);
        } else if (MONTH.equals(node)) {
            return LocalDate.parse(date).plus(index, ChronoUnit.MONTHS).format(YYYY_MM_DD_EN);
        } else if (YEAR.equals(node)) {
            return LocalDate.parse(date).plus(index, ChronoUnit.YEARS).format(YYYY_MM_DD_EN);
        } else {
            return "Wrong date format!";
        }
    }

    /**
     * 检测：输入年份是否是闰年？
     *
     * @param date 日期格式：yyyy-MM-dd
     * @return true：闰年，false：平年
     */
    public static boolean isLeapYear(String date) {
        return LocalDate.parse(date.trim()).isLeapYear();
    }

    /**
     * 计算两个日期字符串之间相差多少个周期（天，月，年）
     *
     * @param date1 yyyy-MM-dd
     * @param date2 yyyy-MM-dd
     * @param node  三者之一:(day，month,year)
     * @return 相差多少周期
     */
    public static int peridCount(String date1, String date2, String node) {
        date1 = date1.trim();
        date2 = date2.trim();
        if (DAY.equals(node)) {
            return Period.between(LocalDate.parse(date1), LocalDate.parse(date2)).getDays();
        } else if (MONTH.equals(node)) {
            return Period.between(LocalDate.parse(date1), LocalDate.parse(date2)).getMonths();
        } else if (YEAR.equals(node)) {
            return Period.between(LocalDate.parse(date1), LocalDate.parse(date2)).getYears();
        } else {
            return 0;
        }
    }


    /**
     * 日期转字符串
     *
     * @param date
     * @return
     */
    public static String format(LocalDateTime date) {
        return format(date, YYYY_MM_DD_HH_MM_SS_STR);
    }

    /**
     * 日期转字符串
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String format(LocalDateTime date, String pattern) {
        if (date == null) {
            return null;
        }
        if (ObjectUtils.isEmpty(pattern)) {
            return format(date);
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern);
        return df.format(date);
    }

    /**
     * 转换成时间戳
     *
     * @param time       待转换时间
     * @param zoneOffset 时区，非必填，默认东八区
     * @return 时间戳
     */
    public static Long toTimeMillis(LocalDateTime time, ZoneOffset zoneOffset) {
        if (zoneOffset == null) {
            return time.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        }

        return time.toInstant(zoneOffset).toEpochMilli();
    }

    /**
     * 转换成LocalDateTime
     *
     * @param time       待转换时间戳
     * @param zoneOffset 时区，非必填，默认东八区
     * @return 时间
     */
    public static LocalDateTime toLocalDateTime(Long time, ZoneOffset zoneOffset) {
        if (zoneOffset == null) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.of("+8"));
        }

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), zoneOffset);
    }
}
