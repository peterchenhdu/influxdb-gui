package com.gitee.dbquery.influxdbgui.gui.component;

import com.gitee.dbquery.influxdbgui.sdk.util.RestConnectionUtils;
import com.gitee.dbquery.influxdbgui.util.ObjectUtils;
import com.gitee.dbquery.influxdbgui.util.TsdbConnectionUtils;
import com.gitee.dbquery.influxdbgui.common.enums.NodeTypeEnum;
import com.gitee.dbquery.influxdbgui.model.ConnectionModel;
import com.gitee.dbquery.influxdbgui.model.DatabaseModel;
import com.gitee.dbquery.influxdbgui.model.StableModel;
import com.gitee.dbquery.influxdbgui.sdk.dto.ConnectionDTO;
import com.gitee.dbquery.influxdbgui.sdk.dto.QueryRstDTO;
import com.gitee.dbquery.influxdbgui.sdk.util.DataBaseUtils;
import com.gitee.dbquery.influxdbgui.sdk.util.SuperTableUtils;
import com.gitee.dbquery.influxdbgui.store.ApplicationStore;
import com.gitee.dbquery.influxdbgui.util.TableUtils;
import io.datafx.controller.ViewController;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

/**
 * CommonTabController
 *
 * @author pc
 * @since 2024/01/31
 **/
@ViewController(value = "/fxml/component/db_tab.fxml")
public class DbTabController {
    @FXML
    private VBox centPane;
    @FXML
    private HBox queryBox;
    @FXML
    private Pagination pagination;

    @FXML
    private TableView<Map<String, Object>> tableView;


    private IntegerProperty pageCount = new SimpleIntegerProperty();
    private ListProperty<Map<String, Object>> dataModelMapList = new SimpleListProperty<>(FXCollections.observableArrayList());

    @PostConstruct
    public void init() {
// enable copy/paste
        TableUtils.installCopyPasteHandler(tableView);
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        MenuItem item = new MenuItem("复制");
        item.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ObservableList<TablePosition> posList = tableView.getSelectionModel().getSelectedCells();
                int old_r = -1;
                StringBuilder clipboardString = new StringBuilder();
                for (TablePosition p : posList) {
                    int r = p.getRow();
                    int c = p.getColumn();
                    Object cell = tableView.getColumns().get(c).getCellData(r);
                    if (cell == null) {
                        cell = "";
                    }
                    if (old_r == r) {
                        clipboardString.append('\t');
                    } else if (old_r != -1) {
                        clipboardString.append('\n');
                    }
                    clipboardString.append(cell);
                    old_r = r;
                }
                final ClipboardContent content = new ClipboardContent();
                content.putString(clipboardString.toString());
                Clipboard.getSystemClipboard().setContent(content);
            }
        });
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(item);
        tableView.setContextMenu(menu);

        FilteredList<Map<String, Object>> filteredData = new FilteredList<>(dataModelMapList, p -> true);
        tableView.setItems(filteredData);

        pagination.pageCountProperty().bind(pageCount);
        pagination.setPageFactory(param -> {
            showPage(param + 1);
            return tableView;
        });


        if (ApplicationStore.getCurrentNode().getType() == NodeTypeEnum.CONNECTION) {
            ConnectionModel connectionModel = (ConnectionModel) ApplicationStore.getCurrentNode().getData();
            QueryRstDTO dbList = DataBaseUtils.getAllDatabase(TsdbConnectionUtils.getConnection(connectionModel));

            for(String column : dbList.getColumnList()) {
                TableColumn<Map<String, Object>, String> dbNameColumn = new TableColumn<>();
                dbNameColumn.setId("column_" + column);
                dbNameColumn.setText(column);
                dbNameColumn.setCellValueFactory(new MapValueFactory(column));
                tableView.getColumns().add(dbNameColumn);
            }

            dataModelMapList.addAll(dbList.getDataList());

            centPane.getChildren().removeAll(queryBox);
        } else if (ApplicationStore.getCurrentNode().getType() == NodeTypeEnum.DB) {
            DatabaseModel databaseModel = (DatabaseModel) ApplicationStore.getCurrentNode().getData();
            ConnectionDTO connection = TsdbConnectionUtils.getConnection(databaseModel.getConnectionModel());

            QueryRstDTO stbList = SuperTableUtils.getAllStable(connection, ApplicationStore.getCurrentNode().getName());

            for(String column : stbList.getColumnList()) {
                TableColumn<Map<String, Object>, String> dbNameColumn = new TableColumn<>();
                dbNameColumn.setId("column_" + column);
                dbNameColumn.setText(column);
                dbNameColumn.setCellValueFactory(new MapValueFactory(column));
                tableView.getColumns().add(dbNameColumn);
            }

            dataModelMapList.addAll(stbList.getDataList());



            centPane.getChildren().removeAll(queryBox);
        } else if (ApplicationStore.getCurrentNode().getType() == NodeTypeEnum.STB) {
            StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();
            ConnectionDTO connection = TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel());
            QueryRstDTO queryRstDTO = RestConnectionUtils.executeQuery(connection, "select * from `" + stableModel.getDb().getName() + "`.`" +
                    stableModel.getStb().get("name") + "` limit 1, 10");

            queryRstDTO.getColumnList().forEach(column -> {
                TableColumn<Map<String, Object>, String> tmpColumn = new TableColumn<>();
                tmpColumn.setId(column + "Column");
                tmpColumn.setText(column);
                tmpColumn.setCellValueFactory(new MapValueFactory(column));
                tableView.getColumns().add(tmpColumn);
            });

            queryRstDTO.getDataList().forEach(db -> {
                db.forEach((k, v) -> {
                    if (v instanceof Byte[] || v instanceof byte[]) {
                        db.put(k, new String((byte[]) v));
                    }
                });
                dataModelMapList.add(db);
            });


        }


    }


    private void showPage(Integer page) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("page", page);
        query(queryMap);

    }


    private void query(Map<String, Object> queryMap) {

        if (ApplicationStore.getCurrentNode().getType() == NodeTypeEnum.STB) {
            Integer page = (Integer) queryMap.get("page");
            int start = (page - 1) * 1000;
            StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();
            ConnectionDTO connection = TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel());
            QueryRstDTO queryRstDTO = RestConnectionUtils.executeQuery(connection, "select * from `" + stableModel.getDb().getName() + "`.`" +
                    stableModel.getStb().get("name") + "` limit " + start + ", " + 1000);

            tableView.getColumns().clear();
            queryRstDTO.getColumnList().forEach(column -> {
                TableColumn<Map<String, Object>, String> tmpColumn = new TableColumn<>();
                tmpColumn.setId(column + "Column");
                tmpColumn.setText(column);
                tmpColumn.setCellValueFactory(new MapValueFactory(column));
                tableView.getColumns().add(tmpColumn);
            });

            dataModelMapList.clear();
            queryRstDTO.getDataList().forEach(db -> {
                db.forEach((k, v) -> {
                    if (v instanceof Byte[] || v instanceof byte[]) {
                        db.put(k, new String((byte[]) v));
                    }
                });
                dataModelMapList.add(db);
            });


            QueryRstDTO countRstDTO = RestConnectionUtils.executeQuery(connection, "select count(*) from `" + stableModel.getDb().getName() + "`.`" +
                    stableModel.getStb().get("name") + "`") ;
            long total = ObjectUtils.isEmpty(countRstDTO.getDataList()) ? 0 : (long) countRstDTO.getDataList().get(0).get("count(*)");
            pageCount.setValue((total / 1000) + 1);
        }

    }


    @PreDestroy
    private void destroy() {
        System.err.println("destroy " + this);
    }


}
