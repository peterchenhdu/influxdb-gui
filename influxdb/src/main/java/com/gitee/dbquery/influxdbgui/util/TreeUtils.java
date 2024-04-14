package com.gitee.dbquery.influxdbgui.util;

import com.gitee.dbquery.influxdbgui.common.enums.NodeTypeEnum;
import com.gitee.dbquery.influxdbgui.model.CommonNode;
import com.gitee.dbquery.influxdbgui.model.ConnectionModel;
import com.gitee.dbquery.influxdbgui.model.DatabaseModel;
import com.gitee.dbquery.influxdbgui.model.StableModel;
import com.gitee.dbquery.influxdbgui.sdk.dto.ConnectionDTO;
import com.gitee.dbquery.influxdbgui.sdk.dto.QueryRstDTO;
import com.gitee.dbquery.influxdbgui.sdk.util.DataBaseUtils;
import com.gitee.dbquery.influxdbgui.sdk.util.SuperTableUtils;
import javafx.scene.control.TreeItem;

import java.util.Map;

/**
 * @author chenpi
 * @since 1.0.0 2024/3/22 21:41
 **/
public class TreeUtils {
    public static TreeItem<CommonNode> generateConnectionTree(ConnectionModel connectionModel) {
        TreeItem<CommonNode> connectionItem = new TreeItem<>(new CommonNode(connectionModel.getName(), NodeTypeEnum.CONNECTION, connectionModel), ImageViewUtils.getImageViewByType(NodeTypeEnum.CONNECTION));


        ConnectionDTO connection = TsdbConnectionUtils.getConnection(connectionModel);

        QueryRstDTO dbList ;
        try {
            dbList = DataBaseUtils.getAllDatabase(connection);
        } catch (Exception e) {
            return connectionItem;
        }


        for (Map<String, Object> db : dbList.getDataList()) {
            QueryRstDTO tbList = SuperTableUtils.getAllStable(connection, db.get("name").toString());
            DatabaseModel databaseModel = new DatabaseModel(db.get("name").toString(), db, connectionModel);
            TreeItem<CommonNode> dbNode = new TreeItem<>(new CommonNode(db.get("name").toString(), NodeTypeEnum.DB, databaseModel), ImageViewUtils.getImageViewByType(NodeTypeEnum.DB));
            connectionItem.getChildren().add(dbNode);

            for (Map<String, Object> tb : tbList.getDataList()) {
                TreeItem<CommonNode> tbNode = new TreeItem<>(new CommonNode(tb.get("name").toString(), NodeTypeEnum.STB, new StableModel(tb, databaseModel)), ImageViewUtils.getImageViewByType(NodeTypeEnum.STB));
                dbNode.getChildren().add(tbNode);
            }
        }

        return connectionItem;
    }
}
