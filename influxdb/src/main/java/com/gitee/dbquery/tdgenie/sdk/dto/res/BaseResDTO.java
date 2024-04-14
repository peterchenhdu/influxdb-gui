package com.gitee.dbquery.tdgenie.sdk.dto.res;

import lombok.Data;

import java.util.List;

/**
 * @author chenpi
 * @since 2024/3/20
 **/
@Data
public class BaseResDTO {
    private String status;
    private Integer code;
    private String desc;
    private Integer rows;
    private List<String> head;
    private List<List<Object>> column_meta;
    private List<List<Object>> data;
}
