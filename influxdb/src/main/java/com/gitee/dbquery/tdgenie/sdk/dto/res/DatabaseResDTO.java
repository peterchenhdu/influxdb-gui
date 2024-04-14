
package com.gitee.dbquery.tdgenie.sdk.dto.res;

import com.gitee.dbquery.tdgenie.sdk.annotation.TdField;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 数据库
 * </p>
 *
 * @author PiChen
 * @since 2021-12-01
 */
@Data
public class DatabaseResDTO implements Serializable {
    @TdField
    private String name;
    @TdField
    private String days;
    @TdField
    private String keep;
    @TdField("cache(MB)")
    private String cache;
    @TdField
    private String blocks;
    @TdField
    private String quorum;
    @TdField
    private String comp;
    @TdField("wallevel")
    private String walLevel;
    @TdField
    private String fsync;
    @TdField
    private String replica;
    @TdField
    private String update;
    @TdField("cachelast")
    private String cacheLast;
    @TdField("minrows")
    private String minRows;
    @TdField("maxrows")
    private String maxRows;
    @TdField
    private String precision;
}
