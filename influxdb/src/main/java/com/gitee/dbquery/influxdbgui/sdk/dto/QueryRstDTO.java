package com.gitee.dbquery.influxdbgui.sdk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author 风一样的码农
 * @since 2022/3/7
 **/
@AllArgsConstructor
@Data
public class QueryRstDTO {
    private List<String> columnList;
    private List<Map<String, Object>> dataList;
}
