package com.gitee.dbquery.influxdbgui.sdk.dto.db;

import lombok.Data;

@Data
public class DbConfigUpdateDTO {
    private String dbName;
    private String cacheLast;
    private String comp;
    private String blocks;
    private String keep;
    private String quorum;
    private String replica;
}
