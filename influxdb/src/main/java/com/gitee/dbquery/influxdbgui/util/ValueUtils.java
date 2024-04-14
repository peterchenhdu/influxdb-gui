package com.gitee.dbquery.influxdbgui.util;

/**
 * @author 风一样的码农
 * @since 1.0.0 2024/2/4 21:11
 **/
public class ValueUtils {
    public static String getString(String obj, Object defaultValue) {
        return obj == null ? defaultValue.toString() : obj;
    }
}
