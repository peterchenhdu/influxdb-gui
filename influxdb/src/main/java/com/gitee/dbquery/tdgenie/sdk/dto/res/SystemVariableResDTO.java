package com.gitee.dbquery.tdgenie.sdk.dto.res;

import com.gitee.dbquery.tdgenie.sdk.annotation.TdField;
import lombok.Data;

/**
 * @author 风一样的码农
 * @since 2023/8/11
 **/
@Data
public class SystemVariableResDTO {
    @TdField
    private String name;
    @TdField
    private String value;
}
