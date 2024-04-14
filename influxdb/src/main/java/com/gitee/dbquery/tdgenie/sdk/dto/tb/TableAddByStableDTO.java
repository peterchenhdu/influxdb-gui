package com.gitee.dbquery.tdgenie.sdk.dto.tb;

import lombok.Data;

import java.util.Map;

/**
 * @author 风一样的码农
 * @since 2023/8/23
 **/
@Data
public class TableAddByStableDTO {
    private String dbName;
    private String tableName;
    private String stableName;
    private Map<String, String> tagValueMap;

}
