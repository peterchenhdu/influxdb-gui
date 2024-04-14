package com.gitee.dbquery.influxdbgui.util;

import com.gitee.dbquery.influxdbgui.model.ConnectionModel;
import com.gitee.dbquery.influxdbgui.sdk.dto.ConnectionDTO;

/**
 * @author 风一样的码农
 * @since 1.0.0 2024/2/1 21:18
 **/
public class TsdbConnectionUtils {
    public static ConnectionDTO getConnection(ConnectionModel connectionModel) {
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setIp(connectionModel.getIp());
        connectionDTO.setRestfulPort(connectionModel.getPort());
        connectionDTO.setUsername(connectionModel.getUsername());
        connectionDTO.setPassword(connectionModel.getPassword());
        connectionDTO.setVersion(connectionModel.getVersion());
        return connectionDTO;
    }

    public static ConnectionDTO getConnectionWithDB(ConnectionModel connectionModel, String db) {
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setIp(connectionModel.getIp());
        connectionDTO.setRestfulPort(connectionModel.getPort());
        connectionDTO.setUsername(connectionModel.getUsername());
        connectionDTO.setPassword(connectionModel.getPassword());
        connectionDTO.setVersion(connectionModel.getVersion());
        connectionDTO.setDb(db);
        return connectionDTO;
    }
}
