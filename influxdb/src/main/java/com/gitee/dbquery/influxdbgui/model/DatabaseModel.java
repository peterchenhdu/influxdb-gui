package com.gitee.dbquery.influxdbgui.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DatabaseModel
 *
 * @author pc
 * @since 2024/01/31
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DatabaseModel {
    private String name;
    private Map<String, Object> databaseResDTO;
    private ConnectionModel connectionModel;

    @Override
    public String toString() {
        return name + "@" + connectionModel.getName();
    }
}
