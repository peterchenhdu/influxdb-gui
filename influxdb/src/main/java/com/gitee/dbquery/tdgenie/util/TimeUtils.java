package com.gitee.dbquery.tdgenie.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * @author 风一样的码农
 * @since 2024/2/21
 **/
public class TimeUtils {
    public static Long LocalDateToLong(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
//        long stamp = instant.toEpochMilli() / 1000;
        return instant.toEpochMilli();
    }

    public static String LocalDateToString(LocalDate localDate) {
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").format(localDateTime);
    }

    public static void main(String[] args) {
        System.out.println(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").format(LocalDateTime.now()));
    }
}
