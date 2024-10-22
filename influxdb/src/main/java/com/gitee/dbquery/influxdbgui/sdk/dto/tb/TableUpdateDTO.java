package com.gitee.dbquery.influxdbgui.sdk.dto.tb;

import com.gitee.dbquery.influxdbgui.sdk.dto.field.TableFieldDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author 风一样的码农
 * @since 2023/8/23
 **/
@Data
public class TableUpdateDTO {
    private String db;
    private String tb;
    private List<TableFieldDTO> addList;
    private List<TableFieldDTO> deleteList;
    private List<TableFieldDTO> updateList;
    /**
     * Tag Name修改Map
     */
    private Map<String, String> tagNameChangeMap;
}
