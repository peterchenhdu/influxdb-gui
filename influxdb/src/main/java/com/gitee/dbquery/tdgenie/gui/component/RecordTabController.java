package com.gitee.dbquery.tdgenie.gui.component;

import com.gitee.dbquery.tdgenie.model.StableModel;
import com.gitee.dbquery.tdgenie.sdk.dto.ConnectionDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.QueryRstDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.field.TableFieldDTO;
import com.gitee.dbquery.tdgenie.sdk.util.RestConnectionUtils;
import com.gitee.dbquery.tdgenie.sdk.util.SuperTableUtils;
import com.gitee.dbquery.tdgenie.sdk.util.TsDataUpdateUtils;
import com.gitee.dbquery.tdgenie.store.ApplicationStore;
import com.gitee.dbquery.tdgenie.util.*;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CommonTabController
 *
 * @author pc
 * @since 2024/01/31
 **/
@ViewController(value = "/fxml/component/record_tab.fxml")
public class RecordTabController {
    @FXML
    private StackPane root;
    @FXML
    private VBox centPane;
    @FXML
    private HBox queryBox;
    @FXML
    private Pagination pagination;
    @ActionTrigger("search")
    @FXML
    private JFXButton searchButton;
    @ActionTrigger("reset")
    @FXML
    private JFXButton resetButton;

    @FXML
    private JFXDatePicker beginDatePicker;
    @FXML
    private JFXDatePicker endDatePicker;
    @FXML
    private TableView<Map<String, Object>> tableView;
    @FXML
    private Label pageInformation;
    @FXML
    private Label selectLocInfo;
    @FXML
    private Label updateRecordDialogTitle;
    @FXML
    private JFXDialog updateRecordDialog;
    @FXML
    private VBox updateRecordPane;
    @FXML
    private String currentUpdateTbName;
    @FXML
    @ActionTrigger("updateRecordAction")
    private JFXButton updateRecordSaveButton;
    @FXML
    @ActionTrigger("closeUpdateRecordDialog")
    private JFXButton updateRecordCancelButton;

    private IntegerProperty pageCount = new SimpleIntegerProperty();
    private ListProperty<Map<String, Object>> dataModelMapList = new SimpleListProperty<>(FXCollections.observableArrayList());

    @ActionMethod("updateRecordAction")
    private void updateRecordSaveButton() {
        StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();

        if ("0".equals(stableModel.getDb().getDatabaseResDTO().get("update"))) {
            AlertUtils.show("当前数据库不允许修改数据(update=0)");
            return;
        }

        ConnectionDTO connection = TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel());

        List<List<Object>> dataList = new ArrayList<>();
        List<Object> data = new ArrayList<>();
        for(Node hbox: updateRecordPane.getChildren()) {
            for(Node node:((HBox)hbox).getChildren()) {
                if(node instanceof  JFXTextField && !node.isDisable()) {
                    if(node.getUserData() == null) {
                        data.add(((JFXTextField) node).getText());
                    } else {
                        data.add(ObjectUtils.stringTypeConvert(((TableFieldDTO)node.getUserData()).getDataType(), ((JFXTextField) node).getText()));
                    }
                }
            }
        }
        dataList.add(data);


        List<TableFieldDTO> fieldList = SuperTableUtils.getStableField(connection, stableModel.getDb().getName(),
                stableModel.getStb().get("name").toString() );



        if (updateRecordDialogTitle.getText().equals("新建数据")) {
            String tbName = dataList.get(0).remove(0).toString();
            List<String> fList = fieldList.stream().filter(d-> !d.getIsTag()).map(TableFieldDTO::getName).collect(Collectors.toList());
            TsDataUpdateUtils.batchInsertAutoCreateTable(connection, stableModel.getDb().getName(), tbName,
                    stableModel.getDb().getName(), stableModel.getStb().get("name").toString() ,
                    dataList.get(0).subList(fList.size(), dataList.get(0).size()),
                    Collections.singletonList(dataList.get(0).subList(0, fList.size())));
        } else {
            //更新
            TsDataUpdateUtils.batchInsertSpecifyColumn(connection, stableModel.getDb().getName(), currentUpdateTbName,
                    fieldList.stream().filter(f -> !f.getIsTag()).map(TableFieldDTO::getName).collect(Collectors.toList()), dataList);
        }



        showPage((pagination.currentPageIndexProperty().get() + 1));

        updateRecordDialog.close();
    }

    @ActionMethod("closeUpdateRecordDialog")
    private void closeUpdateRecordDialog() {
        updateRecordDialog.close();
    }

    @PostConstruct
    public void init() {
        root.setOnContextMenuRequested(event -> {
            event.consume(); // 标记事件已被处理，防止默认的上下文菜单显示
        });

// enable copy/paste
        TableUtils.installCopyPasteHandler(tableView);
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        MenuItem addItem = new MenuItem("新建记录");
        MenuItem editItem = new MenuItem("编辑该行记录");
        MenuItem copyItem = new MenuItem("复制单元格内容");
        MenuItem copyInsertItem = new MenuItem("复制为Insert语句");
        copyInsertItem.setOnAction(event -> {
            StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();
            ConnectionDTO connection = TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel());
            List<TableFieldDTO> fieldList = SuperTableUtils.getStableField(connection, stableModel.getDb().getName(), stableModel.getStb().get("name").toString() );

            Map<String, Object> recordMap = tableView.getSelectionModel().getSelectedItem();

            String tbName = recordMap.get("tbname").toString();


            StringBuilder sb = new StringBuilder("INSERT INTO `"+stableModel.getDb().getName()+"`.`"+tbName+"` (");

            for(TableFieldDTO field:fieldList) {
                if(!field.getIsTag()) {
                    sb.append("`").append(field.getName()).append("`,");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(") VALUES (");

            for(TableFieldDTO field:fieldList) {
                if(!field.getIsTag()) {
                    if(field.getDataType().equals("TIMESTAMP") || field.getDataType().equals("NCHAR")) {
                        sb.append("'" + recordMap.get(field.getName()) + "',");
                    } else {
                        sb.append( recordMap.get(field.getName()) + ",");
                    }
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(");");
            final ClipboardContent content = new ClipboardContent();
            content.putString(sb.toString());
            Clipboard.getSystemClipboard().setContent(content);
        });



        copyItem.setOnAction(event -> {
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
        });

        editItem.setOnAction(event -> {
            updateRecordPane.getChildren().clear();
            StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();
            ConnectionDTO connection = TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel());

            List<TableFieldDTO> fieldList = SuperTableUtils.getStableField(connection, stableModel.getDb().getName(), stableModel.getStb().get("name").toString() );



            Map<String, Object> recordMap = tableView.getSelectionModel().getSelectedItem();

            Label tbLabel = new Label();
            tbLabel.setText("子表名:");
            tbLabel.setPadding(new Insets(0,24,0,0));
            JFXTextField tbNameTextField = new JFXTextField();
            tbNameTextField.setText(recordMap.get("tbname").toString());
            tbNameTextField.setDisable(true);
            HBox tbNameHBox = new HBox(tbLabel, tbNameTextField);
            tbNameHBox.setPadding(new Insets(0,0,10,0));
            updateRecordPane.getChildren().addAll(tbNameHBox);

            for(TableFieldDTO field:fieldList) {
                Label label = new Label();
                label.setText(field.getName() + ":");
                label.setPadding(new Insets(0,24,0,0));
                JFXTextField textField = new JFXTextField();
                textField.setText((recordMap == null || recordMap.get(field.getName()) == null) ? "":recordMap.get(field.getName()).toString());
                textField.setUserData(field);
                textField.setDisable(field.getIsTag());
                HBox hBox = new HBox(label, textField);
                hBox.setPadding(new Insets(0,0,10,0));
                updateRecordPane.getChildren().addAll(hBox);
            }

            currentUpdateTbName = recordMap.get("tbname").toString();

            updateRecordDialogTitle.setText("编辑数据");
            updateRecordDialog.setTransitionType(JFXDialog.DialogTransition.TOP);
            updateRecordDialog.show(root);
        });

        addItem.setOnAction(event -> {
            updateRecordPane.getChildren().clear();
            StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();
            ConnectionDTO connection = TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel());

            List<TableFieldDTO> fieldList = SuperTableUtils.getStableField(connection, stableModel.getDb().getName(), stableModel.getStb().get("name").toString() );

            Map<String, Object> recordMap = tableView.getSelectionModel().getSelectedItem();
            Label tbLabel = new Label();
            tbLabel.setText("子表名:");
            tbLabel.setPadding(new Insets(0,24,0,0));
            JFXTextField tbNameTextField = new JFXTextField();
            tbNameTextField.setPromptText((recordMap == null || recordMap.get("tbname") == null) ? "":recordMap.get("tbname").toString());
            tbNameTextField.setDisable(false);
            HBox tbNameHBox = new HBox(tbLabel, tbNameTextField);
            tbNameHBox.setPadding(new Insets(0,0,10,0));
            updateRecordPane.getChildren().addAll(tbNameHBox);
            for(TableFieldDTO field:fieldList) {
                Label label = new Label();
                label.setText(field.getName() + ":");
                label.setPadding(new Insets(0,24,0,0));
                JFXTextField textField = new JFXTextField();
                textField.setPromptText((recordMap == null || recordMap.get(field.getName()) == null) ? "":recordMap.get(field.getName()).toString());
                textField.setUserData(field);
//                textField.setDisable(field.getIsTag());
                HBox hBox = new HBox(label, textField);
                hBox.setPadding(new Insets(0,0,10,0));
                updateRecordPane.getChildren().addAll(hBox);
            }

            currentUpdateTbName = (recordMap == null || recordMap.get("tbname") == null) ? "":recordMap.get("tbname").toString();
            updateRecordDialogTitle.setText("新建数据");
            updateRecordDialog.setTransitionType(JFXDialog.DialogTransition.TOP);
            updateRecordDialog.show(root);
        });

        ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(addItem, editItem, copyItem, copyInsertItem);
        tableView.setContextMenu(menu);

        FilteredList<Map<String, Object>> filteredData = new FilteredList<>(dataModelMapList, p -> true);
        tableView.setItems(filteredData);

        tableView.setRowFactory(new Callback<TableView<Map<String, Object>>, TableRow<Map<String, Object>>>() {
            @Override
            public TableRow<Map<String, Object>> call(TableView<Map<String, Object>> param) {
                TableRow<Map<String, Object>> tableRow = new TableRow<Map<String, Object>>();
                tableRow.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getButton().equals(MouseButton.PRIMARY)) {
                            ObservableList<TablePosition> posList = tableView.getSelectionModel().getSelectedCells();
                            for (TablePosition p : posList) {
                                int r = p.getRow();
                                int c = p.getColumn();
                                selectLocInfo.setText("行:" + (r + 1) + "   列:" + (c + 1));
                                break;
                            }
                        }
                    }
                });
                return tableRow;
            }
        });

        pagination.pageCountProperty().bind(pageCount);

        pagination.setPageFactory(param -> {
            showPage(param + 1);
            return tableView;
        });


        search();

    }

    @ActionMethod("search")
    private void search() {
        showPage(1);
    }


    @ActionMethod("reset")
    private void reset() {
        beginDatePicker.setValue(null);
        endDatePicker.setValue(null);
        showPage(1);
    }

    private void showPage(Integer page) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("page", page);
        query(queryMap);

    }


    private void query(Map<String, Object> queryMap) {


        Integer page = (Integer) queryMap.get("page");
        int start = (page - 1) * 1000;
        StableModel stableModel = (StableModel) ApplicationStore.getCurrentNode().getData();
        ConnectionDTO connection = TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel());
        connection.setDb(stableModel.getDb().getName());
//        List<TableFieldDTO> fieldList = SuperTableUtils.getStableField(connection, stableModel.getDb().getName(), stableModel.getStb().get("name").toString() );

        String whereSql = "";
        if (beginDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            whereSql = " where time > '" + TimeUtils.LocalDateToString(beginDatePicker.getValue()) +
                    "' and time < '" + TimeUtils.LocalDateToString(endDatePicker.getValue()) + "' ";
        } else if (beginDatePicker.getValue() != null && endDatePicker.getValue() == null) {
            whereSql = " where time > '" + TimeUtils.LocalDateToString(beginDatePicker.getValue()) + "' ";
        } else if (beginDatePicker.getValue() == null && endDatePicker.getValue() != null) {
            whereSql = " where time < '" + TimeUtils.LocalDateToString(endDatePicker.getValue()) + "' ";
        }

        QueryRstDTO queryRstDTO = RestConnectionUtils.executeQuery(connection, "select  * from \""  +
                stableModel.getStb().get("name").toString() +"\"" + whereSql + " limit 1000 offset " + start);

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
                    db.put(k, new java.lang.String((byte[]) v));
                }
                if("time".equals(k)) {
                    db.put(k, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(v));
                }
            });
            dataModelMapList.add(db);
        });

        pageCount.setValue(Integer.MAX_VALUE);
        pagination.setMaxPageIndicatorCount(3);
        pageInformation.setText("每页1000条，当前第" + (pagination.currentPageIndexProperty().get() + 1) + "页，当前页记录数（" + dataModelMapList.size() + "）");
//            QueryRstDTO countRstDTO = RestConnectionUtils.executeQuery(connection, "select count(*) from " + stableModel.getDb().getName() + "." +
//                    stableModel.getStb().getName() + whereSql);
//            long total = ObjectUtils.isEmpty(countRstDTO.getDataList()) ? 0 : (long) countRstDTO.getDataList().get(0).get("count(*)");
//            pageCount.setValue((total / 1000) + 1);


    }


    @PreDestroy
    private void destroy() {
        System.err.println("destroy " + this);
    }


}
