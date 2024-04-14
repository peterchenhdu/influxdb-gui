package com.gitee.dbquery.tdgenie.sdk.dto.stb;

import com.gitee.dbquery.tdgenie.sdk.dto.field.TableFieldDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author 风一样的码农
 * @since 2023/8/23
 **/
@Data
public class StableUpdateDTO {
    private String db;
    private String tb;
    private List<TableFieldDTO> addList;
    private List<TableFieldDTO> deleteList;
    private List<TableFieldDTO> updateList;
    /**
     * Tag Name替换Map
     */
    private Map<String, String> tagNameChangeMap;
}
