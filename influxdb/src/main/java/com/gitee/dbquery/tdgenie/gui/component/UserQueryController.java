package com.gitee.dbquery.tdgenie.gui.component;

import com.gitee.dbquery.tdgenie.common.enums.NodeTypeEnum;
import com.gitee.dbquery.tdgenie.model.CommonNode;
import com.gitee.dbquery.tdgenie.model.ConnectionModel;
import com.gitee.dbquery.tdgenie.model.DatabaseModel;
import com.gitee.dbquery.tdgenie.model.StableModel;
import com.gitee.dbquery.tdgenie.sdk.dto.ConnectionDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.QueryRstDTO;
import com.gitee.dbquery.tdgenie.sdk.util.RestConnectionUtils;
import com.gitee.dbquery.tdgenie.store.ApplicationStore;
import com.gitee.dbquery.tdgenie.util.ObjectUtils;
import com.gitee.dbquery.tdgenie.util.TableUtils;
import com.gitee.dbquery.tdgenie.util.TsdbConnectionUtils;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTabPane;
import io.datafx.controller.ViewController;
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
@ViewController(value = "/fxml/component/user_query_tab.fxml")
public class UserQueryController {


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


        if (null == ApplicationStore.getCurrentNode()) {
            if(connectionComboBox.getItems().size()>0)  {
                connectionComboBox.getSelectionModel().select(0);
            }
        } else if (ApplicationStore.getCurrentNode().getType() == NodeTypeEnum.CONNECTION) {
            ConnectionModel connectionModel = (ConnectionModel) ApplicationStore.getCurrentNode().getData();

            connectionComboBox.getSelectionModel().select(connectionModel.getName());
        } else if (ApplicationStore.getCurrentNode().getType() == NodeTypeEnum.DB) {
            DatabaseModel databaseModel = (DatabaseModel) ApplicationStore.getCurrentNode().getData();

            connectionComboBox.getSelectionModel().select(databaseModel.getConnectionModel().getName());
        } else if (ApplicationStore.getCurrentNode().getType() == NodeTypeEnum.STB) {
            StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();
            DatabaseModel databaseModel = stableModel.getDb();
            connectionComboBox.getSelectionModel().select(databaseModel.getConnectionModel().getName());
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
        ConnectionDTO connection = TsdbConnectionUtils.getConnection(connectionModel);

        String sql = "show users;";
        processSingleSql(queryMap, sql.trim(), connection);


    }

    private void processSingleSql(Map<String, Object> queryMap, String sql, ConnectionDTO connection) {
        if (ObjectUtils.isEmpty(sql)) {
            return;
        }

        QueryRstDTO queryRstDTO = null;
        try {
            queryRstDTO = RestConnectionUtils.executeQuery(connection, sql.replaceAll(";", ""));
        } catch (Exception e) {
            tableView.getColumns().clear();
            return;
        }

        if (null != queryRstDTO) {
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


        pageCount.setValue(1);
        pagination.setMaxPageIndicatorCount(1);

    }


    @PreDestroy
    private void destroy() {
        System.err.println("destroy " + this);
    }


}
