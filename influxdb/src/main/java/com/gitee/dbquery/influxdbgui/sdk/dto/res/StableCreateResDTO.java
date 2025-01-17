package com.gitee.dbquery.influxdbgui.sdk.dto.res;

import com.gitee.dbquery.influxdbgui.sdk.annotation.TdField;
import lombok.Data;

/**
 * @author 风一样的码农
 * @since 2023/8/11
 **/
@Data
public class StableCreateResDTO {
    @TdField("Table")
    private String db;
    @TdField("Create Table")
    private String createDbSql;
}
