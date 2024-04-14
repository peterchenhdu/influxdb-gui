package com.gitee.dbquery.influxdbgui.sdk.dto.res;

import com.gitee.dbquery.influxdbgui.sdk.annotation.TdField;
import lombok.Data;

/**
 * @author 风一样的码农
 * @since 2023/8/23
 **/
@Data
public class TableFieldResDTO {
    @TdField("Field")
    private String field;
    @TdField("Type")
    private String type;
    @TdField("Length")
    private Integer length;
    @TdField("Note")
    private String note;
}

