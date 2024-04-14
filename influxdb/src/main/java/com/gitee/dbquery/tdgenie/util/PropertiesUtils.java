package com.gitee.dbquery.tdgenie.util;

import java.util.Properties;

/**
 * @author chenpi
 * @since 2024/3/25
 **/
public class PropertiesUtils {
    public static Double getDouble(Properties properties, String key, Double defaultValue) {
        return Double.parseDouble(ValueUtils.getString(properties.getProperty(key), defaultValue));
    }
}
