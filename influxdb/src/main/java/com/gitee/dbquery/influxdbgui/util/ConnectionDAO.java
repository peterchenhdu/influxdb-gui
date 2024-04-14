package com.gitee.dbquery.influxdbgui.util;

import com.gitee.dbquery.influxdbgui.model.ConnectionModel;
import com.gitee.dbquery.influxdbgui.sdk.util.RestConnectionUtils;
import com.gitee.dbquery.influxdbgui.store.H2DbUtils;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenpi
 * @since 2024/3/25
 **/
@Slf4j
public class ConnectionDAO {
    public static List<Map<String, Object>> queryByName(String name) {
        try {
            return H2DbUtils.query("select * from t_connection where name='" + name + "'");
        } catch (SQLException e) {
            AlertUtils.showException(e);
        }
        return Collections.emptyList();
    }

    public static void deleteConnection(String name) {
        if(ObjectUtils.isEmpty(name)) {
            return;
        }
        try {
            H2DbUtils.executeUpdate("delete from t_connection where name='" + name + "';");
        } catch (SQLException e) {
            AlertUtils.showException(e);
        }
    }
    public static void connectionTbExistCheck() throws SQLException {
        List<String> tableNameList = new ArrayList<>();
        List<Map<String, Object>> tables = H2DbUtils.query("show tables;");
        for (Map<String, Object> tb : tables) {
            tableNameList.add(tb.get("TABLE_NAME").toString());
        }

        if (!tableNameList.contains("t_connection".toUpperCase())) {
            Map<String, Object> fieldMap = new HashMap<>();
            fieldMap.put("name", String.class);
            fieldMap.put("ip", String.class);
            fieldMap.put("port", String.class);
            fieldMap.put("username", String.class);
            fieldMap.put("password", String.class);
            fieldMap.put("version", String.class);
            H2DbUtils.createTable("t_connection", fieldMap);
        }
    }

    public static List<ConnectionModel> getConnectionList() throws SQLException {
        List<Map<String, Object>> connectionList = H2DbUtils.query("select * from  t_connection;");
        return connectionList.stream().map(con -> {
            ConnectionModel connectionDTO = new ConnectionModel();
            connectionDTO.setIp(con.get("IP").toString());
            connectionDTO.setPort(con.get("PORT").toString());
            connectionDTO.setUsername(con.get("USERNAME").toString());
            connectionDTO.setPassword(con.get("PASSWORD").toString());
            connectionDTO.setName(con.get("NAME").toString());
            if(con.get("VERSION") == null) {
                connectionDTO.setVersion(RestConnectionUtils.getServerVersion(connectionDTO));
            } else {
                connectionDTO.setVersion(con.get("VERSION").toString());
            }

            return connectionDTO;
        }).collect(Collectors.toList());
    }
}
