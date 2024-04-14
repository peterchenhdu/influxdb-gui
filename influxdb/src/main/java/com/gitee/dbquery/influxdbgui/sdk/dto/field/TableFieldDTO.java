
package com.gitee.dbquery.influxdbgui.sdk.dto.field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 * 表字段
 * </p>
 *
 * @author PiChen
 * @since 2021-11-30
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TableFieldDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String dataType;
    private Integer length;
    /**
     * 有定义TAG的为STABLE
     */
    private Boolean isTag;

}
