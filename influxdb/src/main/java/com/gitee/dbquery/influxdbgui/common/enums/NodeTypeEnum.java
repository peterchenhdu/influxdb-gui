package com.gitee.dbquery.influxdbgui.common.enums;

import lombok.Getter;

/**
 * @author 风一样的码农
 * @since 2024/2/6
 **/
@Getter
public enum NodeTypeEnum {
    /**
     * 根节点
     */
    ROOT(-1, "根节点"),
    /**
     * 连接
     */
    CONNECTION(1, "连接"),
    /**
     * 数据库
     */
    DB(2, "数据库"),
    /**
     * 超级表
     */
    STB(3, "超级表"),
    /**
     * 表
     */
    TB(4, "表");

    /**
     * 类型
     */
    private Integer value;
    /**
     * 描述
     */
    private String desc;

    NodeTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /**
     * 根据类型获取枚举
     *
     * @param type 类型
     * @return NodeTypeEnum
     */
    public static NodeTypeEnum getByType(Integer type) {
        for (NodeTypeEnum o : NodeTypeEnum.values()) {
            if (o.getValue().equals(type)) {
                return o;
            }
        }
        throw new RuntimeException("invalid Node type ");
    }
}
