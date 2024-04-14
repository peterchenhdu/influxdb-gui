package com.gitee.dbquery.influxdbgui.sdk.dto.res;

import com.gitee.dbquery.influxdbgui.sdk.annotation.TdField;
import lombok.Data;

/**
 * @author 风一样的码农
 * @since 2023/8/18
 **/
@Data
public class TableResDTO {
    @TdField("table_name")
    private String name;
    @TdField
    private String createdTime;
    @TdField
    private Integer columns;
    @TdField
    private String stableName;
    @TdField
    private String uid;
    @TdField
    private String tid;
    @TdField("vgId")
    private String vgId;
}
