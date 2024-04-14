package com.gitee.dbquery.influxdbgui.sdk.dto.db;

import lombok.Data;

@Data
public class DbConfigAddDTO {
    private String dbName;
    private String days;
    private String keep;
    private String cache;
    private String blocks;
    private String quorum;
    private String comp;
    private String walLevel;
    private String fsync;
    private String replica;
    private String precision;
    private String update;
    private String cacheLast;
    private String minRows;
    private String maxRows;
}
