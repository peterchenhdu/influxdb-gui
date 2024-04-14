package com.gitee.dbquery.influxdbgui.sdk.dto.res;

import com.gitee.dbquery.influxdbgui.sdk.annotation.TdField;
import lombok.Data;

/**
 * @author 风一样的码农
 * @since 2023/8/18
 **/
@Data
public class StableResDTO {
    @TdField
    private String name;
    @TdField
    private String createdTime;
    @TdField
    private Integer columns;
    @TdField
    private Integer tags;
    @TdField
    private Integer tables;
}
