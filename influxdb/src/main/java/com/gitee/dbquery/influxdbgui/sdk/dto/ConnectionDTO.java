
package com.gitee.dbquery.influxdbgui.sdk.dto;


import lombok.Data;

/**
 * <p>
 * 数据连接
 * </p>
 *
 * @author PiChen
 * @since 2021-11-30
 */
@Data
public class ConnectionDTO {

    private String ip;

    private String restfulPort;

    private String username;

    private String password;

    private String db;

    private String version;
}
