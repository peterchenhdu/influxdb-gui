package com.gitee.dbquery.tdgenie.sdk.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author 风一样的码农
 * @since 2021/12/13
 **/
@Retention(RetentionPolicy.RUNTIME)
public @interface TdField {
    String value() default "";
}
