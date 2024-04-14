package com.gitee.dbquery.tdgenie.gui.component;

import com.gitee.dbquery.tdgenie.common.enums.NodeTypeEnum;
import com.gitee.dbquery.tdgenie.model.CommonNode;
import com.gitee.dbquery.tdgenie.model.ConnectionModel;
import com.gitee.dbquery.tdgenie.model.DatabaseModel;
import com.gitee.dbquery.tdgenie.model.StableModel;
import com.gitee.dbquery.tdgenie.sdk.dto.ConnectionDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.QueryRstDTO;
import com.gitee.dbquery.tdgenie.sdk.util.RestConnectionUtils;
import com.gitee.dbquery.tdgenie.sdk.util.VersionUtils;
import com.gitee.dbquery.tdgenie.store.ApplicationStore;
import com.gitee.dbquery.tdgenie.util.*;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import io.datafx.controller.ViewController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
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
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

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
@ViewController(value = "/fxml/component/table_query_tab.fxml")
public class TableQueryController {


    @FXML
    private Label pageInformation;
    @FXML
    private Label selectLocInfo;
    @FXML
    private Pagination pagination;
    @FXML
    private StackPane rootPane;
    @FXML
    private TableView<Map<String, Object>> tableView;

    @FXML
    private JFXTabPane executeResultTabPane;
    @FXML
    private JFXComboBox<String> connectionComboBox;

    @FXML
    private JFXComboBox<String> dbComboBox;

    @FXML
    private JFXTextField keyWordTextField;

    @FXML
    private JFXComboBox<String> stbComboBox;
    @FXML
    private Text executeSql;
    @FXML
    private Text executeStatus;
    @FXML
    private Text executeCost;
    @FXML
    @ActionTrigger("executeQuery")
    private JFXButton executeButton;
    @FXML
    private HBox prettySqlBox;
    @FXML
    private HBox saveSqlBox;
    private IntegerProperty pageCount = new SimpleIntegerProperty();
    private ListProperty<Map<String, Object>> dataModelMapList = new SimpleListProperty<>(FXCollections.observableArrayList());


    @ActionMethod("executeQuery")
    private void executeQuery() {
        showPage(1);
    }


    @PostConstruct
    public void init() {

        rootPane.setOnContextMenuRequested(event -> {
            event.consume(); // 标记事件已被处理，防止默认的上下文菜单显示
        });


        if (ApplicationStore.getConnectionTree() != null) {
            ApplicationStore.getConnectionTree().getChildren().forEach(d -> {
                connectionComboBox.getItems().add(d.getValue().getName());
            });
        }

        connectionComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    System.out.println(newValue);
                    tableView.getColumns().clear();
                    dbComboBox.getItems().clear();
                    for (TreeItem<CommonNode> node : ApplicationStore.getConnectionTree().getChildren()) {
                        if (node.getValue().getName().equals(newValue)) {
                            node.getChildren().forEach(d -> {
                                dbComboBox.getItems().add(d.getValue().getName());
                            });
                        }
                    }

                });

        dbComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    System.out.println(newValue);
                    tableView.getColumns().clear();
                    stbComboBox.getItems().clear();
                    for (TreeItem<CommonNode> node : ApplicationStore.getConnectionTree().getChildren()) {
                        if (connectionComboBox.getValue().equals(node.getValue().getName())) {
                           for(TreeItem<CommonNode> db : node.getChildren()) {
                               if (dbComboBox.getValue().equals(db.getValue().getName())) {
                                   db.getChildren().forEach(d -> {
                                       stbComboBox.getItems().add(d.getValue().getName());
                                   });
                               }
                           }
                        }
                    }

                });


        if (null == ApplicationStore.getCurrentNode()) {

        } else if (ApplicationStore.getCurrentNode().getType() == NodeTypeEnum.CONNECTION) {
            ConnectionModel connectionModel = (ConnectionModel) ApplicationStore.getCurrentNode().getData();

            connectionComboBox.getSelectionModel().select(connectionModel.getName());
        } else if (ApplicationStore.getCurrentNode().getType() == NodeTypeEnum.DB) {
            DatabaseModel databaseModel = (DatabaseModel) ApplicationStore.getCurrentNode().getData();

            connectionComboBox.getSelectionModel().select(databaseModel.getConnectionModel().getName());
            dbComboBox.getSelectionModel().select(databaseModel.getName());
        } else if (ApplicationStore.getCurrentNode().getType() == NodeTypeEnum.STB) {
            StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();
            DatabaseModel databaseModel = stableModel.getDb();
            connectionComboBox.getSelectionModel().select(databaseModel.getConnectionModel().getName());
            dbComboBox.getSelectionModel().select(databaseModel.getName());
            stbComboBox.getSelectionModel().select(stableModel.getStb().get("name").toString());
        }



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

        MenuItem queryDataItem = new MenuItem("查看该普通表数据");
        queryDataItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                Map<String, Object> recordMap = tableView.getSelectionModel().getSelectedItem();

                String tbName = recordMap.get("table_name").toString();
                String dbName = recordMap.get("db_name") == null ? dbComboBox.getValue():recordMap.get("db_name").toString();
                String connectionName = connectionComboBox.getValue();
                Map<String, String> userDataMap = new HashMap<>();
                userDataMap.put("tbName", tbName);
                userDataMap.put("dbName", dbName);
                userDataMap.put("connectionName", connectionName);

                try {
                    TabUtils.addTab(ApplicationContext.getInstance().getRegisteredObject(JFXTabPane.class), tbName + "@" + dbName + "@" + connectionName, ImageViewUtils.getImageViewByType(NodeTypeEnum.STB), TableRecordTabController.class, userDataMap);
                } catch (Exception e) {
                    AlertUtils.showException(e);
                }
            }
        });
        ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(item, queryDataItem);
        tableView.setContextMenu(menu);

        FilteredList<Map<String, Object>> filteredData = new FilteredList<>(dataModelMapList, p -> true);
        tableView.setItems(filteredData);

        pagination.pageCountProperty().bind(pageCount);
        pagination.setPageFactory(param -> {
            showPage(param + 1);
            return tableView;
        });

        showPage(1);

    }


    private void showPage(Integer page) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("page", page);
        query(queryMap);

    }

    private ConnectionModel getConnectionModel(String connectionName) {

        for (TreeItem<CommonNode> item : ApplicationStore.getConnectionTree().getChildren()) {
            if (item.getValue().getName().equals(connectionName)) {
                return (ConnectionModel) item.getValue().getData();
            }
        }

        return null;
    }

    private void query(Map<String, Object> queryMap) {
        if (connectionComboBox.getSelectionModel().isEmpty() ) {
            return;
        }

        String connectionName = connectionComboBox.getSelectionModel().getSelectedItem();
        ConnectionModel connectionModel = getConnectionModel(connectionName);
        if(null == connectionModel) {
            return;
        }

        ConnectionDTO connection = TsdbConnectionUtils.getConnection(connectionModel);

        if(ObjectUtils.isEmpty(connectionComboBox.getValue())) {
            return;
        }

        String sql;
        if(VersionUtils.compareVersion(connectionModel.getVersion(), "3.0") > 0) {
            sql = "select * from INFORMATION_SCHEMA.INS_TABLES ";
            boolean flag = false;
            if(!dbComboBox.getSelectionModel().isEmpty()) {
                sql += " where db_name='" + dbComboBox.getValue() + "' ";
                flag = true;
            }
            if(!stbComboBox.getSelectionModel().isEmpty()) {

                sql += (flag ? " and ":" where ") + " stable_name='" + stbComboBox.getValue() + "' ";
                flag = true;
            }
            if(ObjectUtils.isNotEmpty(keyWordTextField.getText())) {
                sql += (flag ? " and ":" where ") + "  table_name like '%" + keyWordTextField.getText() + "%'";
            }
        } else {
            if(dbComboBox.getSelectionModel().isEmpty() || stbComboBox.getSelectionModel().isEmpty()) {
                return;
            }

            sql = "select tbname as table_name from `" + dbComboBox.getValue() + "`.`" + stbComboBox.getValue() + "`";

            if(ObjectUtils.isNotEmpty(keyWordTextField.getText())) {
                sql += " where tbname like '%" + keyWordTextField.getText() + "%'";
            }
        }



        processSingleSql(queryMap, sql.trim(), connection);


    }

    private void processSingleSql(Map<String, Object> queryMap, String sql, ConnectionDTO connection) {
        if(ObjectUtils.isEmpty(sql)) {
            return;
        }



        Integer page = (Integer) queryMap.get("page");
        int start = (page - 1) * 1000;

        QueryRstDTO queryRstDTO = null;
        long queryStart = System.currentTimeMillis();
        try {
            if(sql.toUpperCase().startsWith("SELECT")) {
                queryRstDTO = RestConnectionUtils.executeQuery(connection, "select * from (" + sql.replaceAll(";", "") + ") limit " + start + ", " + 1000);
            } else {
                queryRstDTO = RestConnectionUtils.executeQuery(connection, sql.replaceAll(";", ""));
            }
        } catch (Exception e) {
            tableView.getColumns().clear();
            return;
        }

        if(null != queryRstDTO) {
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
        }


        pageCount.setValue(Integer.MAX_VALUE);
        pagination.setMaxPageIndicatorCount(3);
        pageInformation.setText("每页1000条，当前第" + (pagination.currentPageIndexProperty().get() + 1) + "页，当前页记录数（" + dataModelMapList.size() + "）");


    }


    @PreDestroy
    private void destroy() {
        System.err.println("destroy " + this);
    }


}
