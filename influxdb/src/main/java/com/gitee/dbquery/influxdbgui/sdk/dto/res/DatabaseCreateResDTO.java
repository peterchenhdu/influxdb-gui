package com.gitee.dbquery.influxdbgui.sdk.dto.res;

import com.gitee.dbquery.influxdbgui.sdk.annotation.TdField;
import lombok.Data;

/**
 * @author 风一样的码农
 * @since 2023/8/11
 **/
@Data
public class DatabaseCreateResDTO {
    @TdField("Database")
    private String db;
    @TdField("Create Database")
    private String createDbSql;
}
