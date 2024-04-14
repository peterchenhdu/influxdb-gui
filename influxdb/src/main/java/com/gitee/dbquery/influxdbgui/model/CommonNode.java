package com.gitee.dbquery.influxdbgui.model;

import com.gitee.dbquery.influxdbgui.common.enums.NodeTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CommonNode
 *
 * @author pc
 * @since 2024/01/31
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonNode {
    private String name;
    private NodeTypeEnum type;
    private Object Data;


    @Override
    public String toString() {
        return name;
    }
}
