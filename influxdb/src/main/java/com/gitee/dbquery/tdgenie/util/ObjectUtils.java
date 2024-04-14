package com.gitee.dbquery.tdgenie.util;


import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 对象工具类
 *
 * @author 风一样的码农
 * @since 1.0.3
 **/
public class ObjectUtils {

    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof Optional) {
            return !((Optional) obj).isPresent();
        } else if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        } else if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        } else if (obj instanceof Collection) {
            return ((Collection) obj).isEmpty();
        } else {
            return obj instanceof Map ? ((Map) obj).isEmpty() : false;
        }
    }


    public static Object  stringTypeConvert(String dataType, String value) {
        if(ObjectUtils.isEmpty(value)) {
            return null;
        }

        if("NCHAR".equals(dataType) || "TIMESTAMP".equals(dataType)) {
            return value;
        } else if("BOOL".equals(dataType)) {
            return Boolean.valueOf(value);
        } else if("FLOAT".equals(dataType)||"DOUBLE".equals(dataType)) {
            return Double.valueOf(value);
        } else {
            return Long.valueOf(value);
        }
    }

}
