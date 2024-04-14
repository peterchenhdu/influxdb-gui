package com.gitee.dbquery.influxdbgui.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * TableModel
 *
 * @author pc
 * @since 2024/01/31
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StableModel {
    private Map<String, Object> stb;
    private DatabaseModel db;

    @Override
    public String toString() {
        return stb.get("name") + "@" + db.getName() + "@" + db.getConnectionModel().getName();
    }
}
