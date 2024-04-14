package com.gitee.dbquery.tdgenie.gui;

import cn.hutool.core.io.FileUtil;
import com.gitee.dbquery.tdgenie.common.enums.NodeTypeEnum;
import com.gitee.dbquery.tdgenie.gui.component.*;
import com.gitee.dbquery.tdgenie.model.CommonNode;
import com.gitee.dbquery.tdgenie.model.ConnectionModel;
import com.gitee.dbquery.tdgenie.model.DatabaseModel;
import com.gitee.dbquery.tdgenie.model.StableModel;
import com.gitee.dbquery.tdgenie.sdk.dto.ConnectionDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.db.DbConfigAddDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.db.DbConfigUpdateDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.field.TableFieldDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.stb.StableAddDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.stb.StableUpdateDTO;
import com.gitee.dbquery.tdgenie.sdk.util.DataBaseUtils;
import com.gitee.dbquery.tdgenie.sdk.util.RestConnectionUtils;
import com.gitee.dbquery.tdgenie.sdk.util.SuperTableUtils;
import com.gitee.dbquery.tdgenie.store.ApplicationStore;
import com.gitee.dbquery.tdgenie.store.H2DbUtils;
import com.gitee.dbquery.tdgenie.util.*;
import com.jfoenix.controls.*;
import io.datafx.controller.ViewController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MainController
 *
 * @author pc
 * @since 2024/01/31
 **/
@Slf4j
@ViewController("/fxml/main.fxml")
public class MainController {
    private static int createDbNextRowIndex = 2;
    /**
     * 页面主元素
     */
    @FXML
    private StackPane rootPane;
    @FXML
    private SplitPane splitPane;
    @FXML
    private JFXTreeView<CommonNode> leftTreeView;
    @FXML
    private JFXTabPane tabPane;

    /**
     * 菜单
     */
    @FXML
    @ActionTrigger("createConnectionAction")
    private MenuItem createConnectionMenuItem;
    @FXML
    @ActionTrigger("exitAction")
    private MenuItem exitMenuItem;
    @FXML
    @ActionTrigger("createQueryAction")
    private MenuItem createQueryMenuItem;
//    @FXML
//    @ActionTrigger("resourceMonitorAction")
//    private MenuItem resourceMonitorMenuItem;
//    @FXML
//    @ActionTrigger("connectionMonitorAction")
//    private MenuItem connectionMonitorMenuItem;
    @FXML
    @ActionTrigger("queryMonitorAction")
    private MenuItem queryMonitorMenuItem;
    @FXML
    @ActionTrigger("clusterQueryAction")
    private MenuItem clusterQueryMenuItem;
    @FXML
    @ActionTrigger("userQueryAction")
    private MenuItem userQueryMenuItem;
    @FXML
    @ActionTrigger("aboutAction")
    private MenuItem aboutMenuItem;
    @FXML
    private JFXDialog aboutDialog;
    /**
     * 快捷工具栏
     */
    @FXML
    private VBox createConnectionBox;
    @FXML
    private VBox createQueryBox;
//    @FXML
//    private VBox clusterBox;
    @FXML
    private VBox userQueryBox;
//    @FXML
//    private VBox tableQueryBox;
//    @FXML
//    private VBox connectionsBox;
    @FXML
    private VBox queryMonitorBox;
//    @FXML
//    private VBox resourceMonitorBox;
    /**
     * 新建表
     */
    @FXML
    private JFXDialog createTbDialog;
    @FXML
    private GridPane createTbPane;
    @FXML
    private Label createTbDialogTitle;
    @FXML
    private JFXTextField tableName0_TextField;
    @FXML
    @ActionTrigger("addField")
    private JFXButton addFieldButton;
    @FXML
    @ActionTrigger("closeCreateTbDialog")
    private JFXButton createTbCancelButton;
    @FXML
    @ActionTrigger("createTB")
    private JFXButton createTbSaveButton;
    /**
     * 新建连接
     */
    @FXML
    private JFXDialog dialog;
    @FXML
    private Label dialogTitle;
    @FXML
    private JFXTextField nameTextField;
    @FXML
    private JFXTextField ipTextField;
    @FXML
    private JFXTextField portTextField;
    @FXML
    private JFXTextField usernameTextField;
    @FXML
    private JFXTextField passwordTextField;
    @FXML
    @ActionTrigger("saveConnection")
    private JFXButton saveButton;
    @FXML
    @ActionTrigger("closeDialog")
    private JFXButton cancelButton;
    /**
     * 新建数据库
     */
    @FXML
    private JFXDialog createDbDialog;
    @FXML
    private Label createDbDialogTitle;
    @FXML
    private JFXTextField createDbName;
//    @FXML
//    private JFXTextField createDbReplica;
//    @FXML
//    private JFXTextField createDbDays;
//    @FXML
//    private JFXTextField createDbBlocks;
    @FXML
    @ActionTrigger("closeCreateDbDialog")
    private JFXButton createDBCancelButton;
    @FXML
    @ActionTrigger("createDB")
    private JFXButton createDBSaveButton;

    @PostConstruct
    public void init() throws SQLException {
        //注册MainController
        ApplicationContext.getInstance().register(this, MainController.class);

        //检查connection表
        ConnectionDAO.connectionTbExistCheck();
        //设置Tab菜单

        ApplicationContext.getInstance().register(tabPane, JFXTabPane.class);
        tabPane.setContextMenu(ContextMenuUtils.generateTabPaneContextMenu(tabPane));
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.setPrefSize(200, 200); // 设置首选大小

        //记录Main pane最后一次的dividerPositions
        splitPane.getDividers().get(0).positionProperty()
                .addListener((o, oldPos, newPos) -> ApplicationStore.setMainPaneLastDividerPositions(newPos.doubleValue()));
        //添加快捷工具栏Event事件
        addToolBarEvent();
        //获取树根节点
        TreeItem<CommonNode> root = getRootTreeItem();
        //添加树节点菜单
        addTreeNodeContextMenu(root);
        //存储根节点
        ApplicationStore.setConnectionTree(root);
        leftTreeView.setRoot(root);
        leftTreeView.setShowRoot(false);
        // 监听节点选择
        leftTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> onSelectTreeItem(newValue));

    }

    private TreeItem<CommonNode> getRootTreeItem() throws SQLException {
        TreeItem<CommonNode> root = new TreeItem<>(new CommonNode("influxdb-gui", NodeTypeEnum.ROOT, null), ImageViewUtils.getImageViewByType(NodeTypeEnum.ROOT));
        root.setExpanded(true);
        List<ConnectionModel> connectionNodeList = ConnectionDAO.getConnectionList();
        for (ConnectionModel connectionModel : connectionNodeList) {
            root.getChildren().add(TreeUtils.generateConnectionTree(connectionModel));
        }
        return root;
    }

    private void addTreeNodeContextMenu(TreeItem<CommonNode> root) {
        MenuItem createConnectionMenuItem = new MenuItem("新建连接");
        createConnectionMenuItem.setOnAction((ActionEvent t) -> {
            dialogTitle.setText("新建连接");
            showAddConnectionDialog();
        });

        MenuItem updateConnectionMenuItem = new MenuItem("编辑连接");
        updateConnectionMenuItem.setOnAction((ActionEvent t) -> {
            dialogTitle.setText("编辑连接");
            showAddConnectionDialog();
        });

        MenuItem deleteConnectionMenuItem = new MenuItem("删除连接");
        deleteConnectionMenuItem.setOnAction((ActionEvent t) -> {
            CommonNode currentNodeInfo = ApplicationStore.getCurrentNode();
            if (null == currentNodeInfo) {
                return;
            }
            ConnectionDAO.deleteConnection(currentNodeInfo.getName());
            for (TreeItem<CommonNode> connectionTreeItem : root.getChildren()) {
                if (connectionTreeItem.getValue().getName().equals(currentNodeInfo.getName())) {
                    root.getChildren().remove(connectionTreeItem);
                    break;
                }
            }
        });

        MenuItem exportSQLMenuItem = new MenuItem("导出SQL");
        exportSQLMenuItem.setOnAction(this::handExportDateAction);

        MenuItem createQueryMenuItem = new MenuItem("新建查询");
        createQueryMenuItem.setOnAction((ActionEvent t) -> {
            CommonNode currentNodeInfo = ApplicationStore.getCurrentNode();
            if (null == currentNodeInfo) {
                return;
            }
            TabUtils.addQueryTab(tabPane);
        });

        MenuItem createDbMenuItem = new MenuItem("新建数据库");
        createDbMenuItem.setOnAction((ActionEvent t) -> {
            createDbName.clear();
//            createDbReplica.clear();
//            createDbDays.clear();
//            createDbBlocks.clear();
            System.out.println("新建数据库 - 菜单点击");
            createDbDialogTitle.setText("新建数据库");
            createDbName.setDisable(false);
            createDbDialog.setTransitionType(JFXDialog.DialogTransition.TOP);
            createDbDialog.show(rootPane);
        });

        MenuItem updateDbMenuItem = new MenuItem("编辑数据库");
        updateDbMenuItem.setOnAction((ActionEvent t) -> {
            CommonNode commonNode = ApplicationStore.getCurrentNode();
            if (null == commonNode) {
                return;
            }

            createDbName.clear();
//            createDbReplica.clear();
//            createDbDays.clear();
//            createDbBlocks.clear();

            createDbDialogTitle.setText("编辑数据库");
            createDbName.setDisable(true);

            DatabaseModel databaseModel = (DatabaseModel) commonNode.getData();
            Map<String, Object> databaseResDTO = databaseModel.getDatabaseResDTO();
            createDbName.setText(databaseResDTO.get("name").toString());
//            createDbBlocks.setText(databaseResDTO.get(DataBaseUtils.getBufferParamCode(databaseModel.getConnectionModel().getVersion())).toString());
//            createDbDays.setText(databaseResDTO.get("keep").toString());
//            createDbReplica.setText(databaseResDTO.get("replica").toString());

            createDbDialog.setTransitionType(JFXDialog.DialogTransition.TOP);
            createDbDialog.show(rootPane);
        });


        MenuItem delDbMenuItem = new MenuItem("删除数据库");
        delDbMenuItem.setOnAction((ActionEvent t) -> {
            CommonNode commonNode = ApplicationStore.getCurrentNode();
            if (null == commonNode) {
                return;
            }

            DatabaseModel databaseModel = (DatabaseModel) commonNode.getData();
            try {
                DataBaseUtils.deleteDatabase(TsdbConnectionUtils.getConnection(databaseModel.getConnectionModel()), databaseModel.getName());
            } catch (Exception e) {
                AlertUtils.showException(e);
            }

            for (TreeItem<CommonNode> connectionTreeItem : root.getChildren()) {
                if (connectionTreeItem.getValue().getName().equals(databaseModel.getConnectionModel().getName())) {
                    for (TreeItem<CommonNode> dbTreeItem : connectionTreeItem.getChildren()) {
                        if (dbTreeItem.getValue().getName().equals(databaseModel.getName())) {
                            connectionTreeItem.getChildren().remove(dbTreeItem);
                            break;
                        }
                    }
                }
            }
        });


        MenuItem updateTbMenuItem = new MenuItem("编辑Measurement");
        updateTbMenuItem.setOnAction((ActionEvent t) -> {
            CommonNode commonNode = ApplicationStore.getCurrentNode();
            if (null == commonNode) {
                return;
            }

            createTbDialogTitle.setText("编辑Measurement");

            createTbPane.getChildren().remove(6, createTbPane.getChildren().size());
            StableModel stableModel = (StableModel) commonNode.getData();
            tableName0_TextField.setText(stableModel.getStb().get("name").toString());
            tableName0_TextField.setDisable(true);
            List<TableFieldDTO> fields = SuperTableUtils.getStableField(TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel()), stableModel.getDb().getName(), stableModel.getStb().get("name").toString());

            createDbNextRowIndex = 2;
            for (TableFieldDTO field : fields) {
                JFXTextField nameTextField = new JFXTextField();
                nameTextField.setId(createDbNextRowIndex + "_NameField");
                nameTextField.setPadding(new Insets(0, 6, 0, 0));
                nameTextField.setText(field.getName());
                nameTextField.setMinWidth(120);
                nameTextField.setDisable(true);
                createTbPane.add(nameTextField, 0, createDbNextRowIndex);

                JFXComboBox<String> typeJFXComboBox = new JFXComboBox<>();
                typeJFXComboBox.setId(createDbNextRowIndex + "_TypeField");
                typeJFXComboBox.setPadding(new Insets(0, 6, 0, 0));
                typeJFXComboBox.setItems(JavaFxBeanUtils.getDataTypeObservableList());
                typeJFXComboBox.setValue(field.getDataType());
                typeJFXComboBox.setMinWidth(120);
                createTbPane.add(typeJFXComboBox, 1, createDbNextRowIndex);

                JFXTextField lengthTextField = new JFXTextField();
                lengthTextField.setId(createDbNextRowIndex + "_LengthField");
                lengthTextField.setPadding(new Insets(0, 6, 0, 0));
                lengthTextField.setText(field.getLength().toString());
                lengthTextField.setMinWidth(100);
                createTbPane.add(lengthTextField, 2, createDbNextRowIndex);

                JFXComboBox<String> isTagJFXComboBox = new JFXComboBox<>();
                isTagJFXComboBox.setId(createDbNextRowIndex + "_isTagField");
                isTagJFXComboBox.setPadding(new Insets(0, 6, 0, 0));
                isTagJFXComboBox.setItems(JavaFxBeanUtils.getTrueFalseObservableList());
                isTagJFXComboBox.setValue(field.getIsTag().toString());
                isTagJFXComboBox.setMinWidth(100);
                createTbPane.add(isTagJFXComboBox, 3, createDbNextRowIndex);

                HBox hBox = new HBox();
                hBox.setId(createDbNextRowIndex + "_hBox");
                hBox.setMinWidth(300);
                createTbPane.add(hBox, 4, createDbNextRowIndex);
                generateCreateTbOptButton(hBox);

                createDbNextRowIndex++;
            }


            createTbDialog.setTransitionType(JFXDialog.DialogTransition.TOP);
            createTbDialog.show(rootPane);
        });

        MenuItem createTbMenuItem = new MenuItem("新建Measurement");
        createTbMenuItem.setOnAction((ActionEvent t) -> {
            createDbNextRowIndex = 2;
            createTbDialogTitle.setText("新建Measurement");
            createTbPane.getChildren().remove(6, createTbPane.getChildren().size());
            createTbDialog.setTransitionType(JFXDialog.DialogTransition.TOP);
            createTbDialog.show(rootPane);
        });

        MenuItem delTbMenuItem = new MenuItem("删除Measurement");
        delTbMenuItem.setOnAction((ActionEvent t) -> {
            CommonNode commonNode = ApplicationStore.getCurrentNode();
            if (null == commonNode) {
                return;
            }

            StableModel stableModel = (StableModel) commonNode.getData();
            try {
                SuperTableUtils.deleteStable(TsdbConnectionUtils.getConnection(stableModel.getDb().getConnectionModel()),
                        stableModel.getDb().getName(), stableModel.getStb().get("name").toString());
            } catch (Exception e) {
                AlertUtils.showException(e);
            }
            for (TreeItem<CommonNode> connectionTreeItem : root.getChildren()) {
                if (connectionTreeItem.getValue().getName().equals(stableModel.getDb().getConnectionModel().getName())) {
                    for (TreeItem<CommonNode> dbTreeItem : connectionTreeItem.getChildren()) {
                        if (dbTreeItem.getValue().getName().equals(stableModel.getDb().getName())) {
                            for (TreeItem<CommonNode> stbTreeItem : dbTreeItem.getChildren()) {
                                if (stbTreeItem.getValue().getName().equals(stableModel.getStb().get("name").toString())) {
                                    dbTreeItem.getChildren().remove(stbTreeItem);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });


        // 创建右键菜单(连接、库、表)
        // 注册鼠标右击事件处理程序
        ContextMenu nodeClickMenu = new ContextMenu();
        leftTreeView.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            nodeClickMenu.getItems().clear();
            if (!event.getButton().equals(MouseButton.SECONDARY)) {
                return;
            }

            CommonNode clickNodeData = (leftTreeView.getSelectionModel().getSelectedItem()).getValue();
            if (clickNodeData.getType().equals(NodeTypeEnum.CONNECTION)) {
                nodeClickMenu.getItems().addAll(createQueryMenuItem, createConnectionMenuItem, updateConnectionMenuItem, deleteConnectionMenuItem, createDbMenuItem);
            } else if (clickNodeData.getType().equals(NodeTypeEnum.DB)) {
                nodeClickMenu.getItems().addAll( createQueryMenuItem, createDbMenuItem, delDbMenuItem);
            } else {
                nodeClickMenu.getItems().addAll(createQueryMenuItem,  delTbMenuItem);
            }
            nodeClickMenu.show(leftTreeView, event.getScreenX(), event.getScreenY());
        });
    }

    private void addToolBarEvent() {
        createConnectionBox.setOnMouseClicked((MouseEvent t) -> {
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }
            createConnectionAction();
        });
        createQueryBox.setOnMouseClicked((MouseEvent t) -> {
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }
            createQueryAction();
        });
//        clusterBox.setOnMouseClicked((MouseEvent t) -> {
//            System.out.println("clusterBox点击");
//            if (!t.getButton().equals(MouseButton.PRIMARY)) {
//                return;
//            }
//            clusterQueryAction();
//        });
        userQueryBox.setOnMouseClicked((MouseEvent t) -> {
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }
            userQueryAction();

        });
//        tableQueryBox.setOnMouseClicked((MouseEvent t) -> {
//            if (!t.getButton().equals(MouseButton.PRIMARY)) {
//                return;
//            }
//            tableQueryAction();
//
//        });
//        connectionsBox.setOnMouseClicked((MouseEvent t) -> {
//            if (!t.getButton().equals(MouseButton.PRIMARY)) {
//                return;
//            }
//            connectionMonitorAction();
//        });
        queryMonitorBox.setOnMouseClicked((MouseEvent t) -> {
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }
            queryMonitorAction();
        });
//        resourceMonitorBox.setOnMouseClicked((MouseEvent t) -> {
//            if (!t.getButton().equals(MouseButton.PRIMARY)) {
//                return;
//            }
//            resourceMonitorAction();
//        });
    }

    private void onSelectTreeItem(TreeItem<CommonNode> treeItem) {
        ApplicationStore.setCurrentTreeItem(treeItem);
        CommonNode item = treeItem.getValue();
        switch (item.getType()) {
            case CONNECTION:
                TabUtils.addConnectionTab(tabPane, item.getData().toString());
                break;
            case DB:
                TabUtils.addDbTab(tabPane, item.getData().toString());
                break;
            case STB:
                TabUtils.addStbTab(tabPane, item.getData().toString());
                break;
            default:
                log.info("do nothing...");
        }
    }

    private void showAddConnectionDialog() {
        nameTextField.clear();
        ipTextField.clear();
        portTextField.clear();
        usernameTextField.clear();
        passwordTextField.clear();
        if (!"新建连接".equals(dialogTitle.getText())) {
            CommonNode currentNode = ApplicationStore.getCurrentNode();
            if (null == currentNode) {
                return;
            }
            ConnectionModel connectionModel = (ConnectionModel) currentNode.getData();
            List<Map<String, Object>> rst = ConnectionDAO.queryByName(connectionModel.getName());
            if (ObjectUtils.isNotEmpty(rst)) {
                nameTextField.setText(rst.get(0).get("NAME").toString());
                ipTextField.setText(rst.get(0).get("IP").toString());
                portTextField.setText(rst.get(0).get("PORT").toString());
                usernameTextField.setText(rst.get(0).get("USERNAME").toString());
                passwordTextField.setText(rst.get(0).get("PASSWORD").toString());
            }
        }
        dialog.setTransitionType(JFXDialog.DialogTransition.TOP);
        dialog.show(rootPane);
    }


    protected void handExportDateAction(ActionEvent event) {
        CommonNode currentNode = ApplicationStore.getCurrentNode();
        if(null == currentNode){
            return;
        }

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.sql)", "*.sql");
        fileChooser.getExtensionFilters().add(extFilter);
        Stage s = new Stage();
        File file = fileChooser.showSaveDialog(s);
        if (file == null) {
            return;
        }

        //文件已存在，则删除覆盖文件
        if (file.exists()) {
            boolean deleteFlag = file.delete();
            if(!deleteFlag) {
                log.warn("delete file error.");
            }
        }
        String exportFilePath = file.getAbsolutePath();
        System.out.println("导出文件的路径" + exportFilePath);

        StringBuilder sql = new StringBuilder();
        if (currentNode.getType().equals(NodeTypeEnum.DB)) {
            DatabaseModel databaseModel = (DatabaseModel) ApplicationStore.getCurrentNode().getData();
            String dbSql = DataBaseUtils.getDatabaseCreateSql(TsdbConnectionUtils.getConnection(databaseModel.getConnectionModel()), databaseModel.getName());
            sql.append(dbSql).append(";\n");
        } else if (ApplicationStore.getCurrentNode().getType().equals(NodeTypeEnum.STB)) {
            StableModel databaseModel = (StableModel) ApplicationStore.getCurrentNode().getData();
            String dbSql = SuperTableUtils.getStableSql(TsdbConnectionUtils.getConnection(databaseModel.getDb().getConnectionModel()), databaseModel.getDb().getName(), databaseModel.getStb().get("name").toString());
            sql.append(dbSql).append(";\n");
        }


        FileUtil.writeString(sql.toString(), file, "utf-8");

        AlertUtils.show("导出成功!保存路径:\n" + exportFilePath);


    }

    @ActionMethod("saveConnection")
    private void saveConnection() throws SQLException {
        System.out.println("save Connection...");

        if ("新建连接".equals(dialogTitle.getText())) {


            ConnectionModel connectionModel = new ConnectionModel();
            connectionModel.setName(nameTextField.getText());
            connectionModel.setIp(ipTextField.getText());
            connectionModel.setPort(portTextField.getText());
            connectionModel.setUsername(usernameTextField.getText());
            connectionModel.setPassword(passwordTextField.getText());
//            connectionModel.setVersion(RestConnectionUtils.getServerVersion(connectionModel));

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("name", nameTextField.getText());
            dataMap.put("ip", ipTextField.getText());
            dataMap.put("port", portTextField.getText());
            dataMap.put("username", usernameTextField.getText());
            dataMap.put("password", passwordTextField.getText());
            dataMap.put("version", "1.8.10");

            H2DbUtils.insertByHashMap("t_connection", Collections.singletonList(dataMap));
            ApplicationStore.getConnectionTree().getChildren().add(TreeUtils.generateConnectionTree(connectionModel));

        } else {
            CommonNode currentNode = ApplicationStore.getCurrentNode();
            if(null == currentNode) {
                return;
            }
            ConnectionModel selectedConnectionModel = (ConnectionModel) currentNode.getData();
            H2DbUtils.executeUpdate("update t_connection set name='" + nameTextField.getText() + "' where name='" + selectedConnectionModel.getName() + "';");
            H2DbUtils.executeUpdate("update t_connection set ip='" + ipTextField.getText() + "' where name='" + selectedConnectionModel.getName() + "';");
            H2DbUtils.executeUpdate("update t_connection set port='" + portTextField.getText() + "' where name='" + selectedConnectionModel.getName() + "';");
            H2DbUtils.executeUpdate("update t_connection set username='" + usernameTextField.getText() + "' where name='" + selectedConnectionModel.getName() + "';");
            H2DbUtils.executeUpdate("update t_connection set password='" + passwordTextField.getText() + "' where name='" + selectedConnectionModel.getName() + "';");
            currentNode.setName(nameTextField.getText());
            Event.fireEvent(ApplicationStore.getCurrentTreeItem(), new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), ApplicationStore.getCurrentTreeItem(), ApplicationStore.getCurrentTreeItem().getValue()));
            selectedConnectionModel.setIp(ipTextField.getText());
            selectedConnectionModel.setPort(portTextField.getText());
            selectedConnectionModel.setUsername(usernameTextField.getText());
            selectedConnectionModel.setPassword(passwordTextField.getText());
//            selectedConnectionModel.setVersion(RestConnectionUtils.getServerVersion(selectedConnectionModel));
        }
        dialog.close();
    }

    @ActionMethod("closeDialog")
    private void closeDialog() {
        dialog.close();
    }

    @ActionMethod("createDB")
    private void createDBSaveButton() {
        CommonNode currentNode = ApplicationStore.getCurrentNode();
        if(null == currentNode) {
            return;
        }

        ConnectionModel connectionModel;
        if (currentNode.getType().equals(NodeTypeEnum.CONNECTION)) {
            connectionModel = (ConnectionModel) ApplicationStore.getCurrentNode().getData();
        } else {
            connectionModel = ((DatabaseModel) ApplicationStore.getCurrentNode().getData()).getConnectionModel();
        }
        ConnectionDTO connection = TsdbConnectionUtils.getConnection(connectionModel);
        if ("新建数据库".equals(createDbDialogTitle.getText())) {
            DbConfigAddDTO dbConfigAddDTO = new DbConfigAddDTO();
            dbConfigAddDTO.setDbName(createDbName.getText());
//            dbConfigAddDTO.setKeep(createDbDays.getText());
//            dbConfigAddDTO.setBlocks(createDbBlocks.getText());
//            dbConfigAddDTO.setReplica(createDbReplica.getText());
            DataBaseUtils.createDatabase(connection, dbConfigAddDTO);

            for (TreeItem<CommonNode> treeItem : ApplicationStore.getConnectionTree().getChildren()) {
                if (treeItem.getValue().getName().equals(connectionModel.getName())) {

                    DatabaseModel newDatabaseModel = new DatabaseModel(dbConfigAddDTO.getDbName(), DataBaseUtils.getDatabase(connection, dbConfigAddDTO.getDbName()), connectionModel);

                    TreeItem<CommonNode> dbNode = new TreeItem<>(new CommonNode(dbConfigAddDTO.getDbName(), NodeTypeEnum.DB, newDatabaseModel), ImageViewUtils.getImageViewByType(NodeTypeEnum.DB));

                    treeItem.getChildren().add(dbNode);
                }
            }

        } else {
            DbConfigUpdateDTO dbConfigAddDTO = new DbConfigUpdateDTO();
            dbConfigAddDTO.setDbName(createDbName.getText());
//            dbConfigAddDTO.setBlocks(createDbBlocks.getText());
//            dbConfigAddDTO.setReplica(createDbReplica.getText());
            DataBaseUtils.updateDatabase(connection, dbConfigAddDTO);

            ApplicationStore.getCurrentNode().setName(createDbName.getText());
            Event.fireEvent(ApplicationStore.getCurrentTreeItem(), new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), ApplicationStore.getCurrentTreeItem(), ApplicationStore.getCurrentTreeItem().getValue()));

        }
        createDbDialog.close();
    }

    @ActionMethod("closeCreateDbDialog")
    private void closeCreateDbDialog() {
        createDbDialog.close();
    }

    @ActionMethod("closeCreateTbDialog")
    private void closeCreateTbDialog() {
        createTbDialog.close();
    }

    @SuppressWarnings("rawtypes")
    private List<TableFieldDTO> getFieldListFromCreateTbPane() {
        List<TableFieldDTO> fieldList = new ArrayList<>();
        for (int i = 6; i < createTbPane.getChildren().size(); i = i + 5) {
            TableFieldDTO f = new TableFieldDTO();
            f.setName(((JFXTextField) createTbPane.getChildren().get(i)).getText());
            f.setDataType(((JFXComboBox) createTbPane.getChildren().get(i + 1)).getValue().toString());
            if (ObjectUtils.isNotEmpty(((JFXTextField) createTbPane.getChildren().get(i + 2)).getText())) {
                f.setLength(Integer.valueOf(((JFXTextField) createTbPane.getChildren().get(i + 2)).getText()));
            }
            f.setIsTag(Boolean.valueOf(((JFXComboBox) createTbPane.getChildren().get(i + 3)).getValue().toString()));
            fieldList.add(f);
        }
        return fieldList;
    }

    @ActionMethod("createTB")
    private void createTB() {
        CommonNode currentNode = ApplicationStore.getCurrentNode();
        if(null == currentNode) {
            return;
        }

        DatabaseModel databaseModel;

        if (currentNode.getType().equals(NodeTypeEnum.DB)) {
            databaseModel = (DatabaseModel) currentNode.getData();
        } else {
            databaseModel = ((StableModel) currentNode.getData()).getDb();
        }
        ConnectionDTO connection = TsdbConnectionUtils.getConnection(databaseModel.getConnectionModel());
        if ("新建Measurement".equals(createTbDialogTitle.getText())) {


            StableAddDTO stableAddDTO = new StableAddDTO();
            stableAddDTO.setDb(databaseModel.getName());
            stableAddDTO.setTb(tableName0_TextField.getText());
            stableAddDTO.setFieldList(getFieldListFromCreateTbPane());


            SuperTableUtils.createStable(connection, stableAddDTO);

            for (TreeItem<CommonNode> treeItem : ApplicationStore.getConnectionTree().getChildren()) {
                if (treeItem.getValue().getName().equals(databaseModel.getConnectionModel().getName())) {


                    for (TreeItem<CommonNode> treeTbItem : treeItem.getChildren()) {
                        if (treeTbItem.getValue().getName().equals(databaseModel.getName())) {


                            StableModel newDatabaseModel = new StableModel(SuperTableUtils.getStable(connection, stableAddDTO.getDb(), stableAddDTO.getTb()), databaseModel);

                            TreeItem<CommonNode> dbNode = new TreeItem<>(new CommonNode(stableAddDTO.getTb(), NodeTypeEnum.STB, newDatabaseModel), ImageViewUtils.getImageViewByType(NodeTypeEnum.STB));

                            treeTbItem.getChildren().add(dbNode);

                        }
                    }


                }
            }

        } else {

            List<TableFieldDTO> uiFieldList = getFieldListFromCreateTbPane();
            List<String> uiFieldNameList = uiFieldList.stream().map(TableFieldDTO::getName).collect(Collectors.toList());


            List<TableFieldDTO> tdFieldList = SuperTableUtils.getStableField(connection, databaseModel.getName(), tableName0_TextField.getText());
            List<String> tdFieldNameList = tdFieldList.stream().map(TableFieldDTO::getName).collect(Collectors.toList());


            List<TableFieldDTO> addList = uiFieldList.stream().filter(f -> !tdFieldNameList.contains(f.getName())).collect(Collectors.toList());
            List<TableFieldDTO> deleteList = tdFieldList.stream().filter(f -> !uiFieldNameList.contains(f.getName())).collect(Collectors.toList());
            List<TableFieldDTO> updateList = uiFieldList.stream().filter(
                    f -> {
                        for (TableFieldDTO fieldDTO : tdFieldList) {
                            if (fieldDTO.getName().equals(f.getName())) {
                                //对字段新增TAG
                                if (Boolean.TRUE.equals(f.getIsTag()) && Boolean.FALSE.equals(fieldDTO.getIsTag())) {
                                    addList.add(f);//新增TAG
                                    deleteList.add(fieldDTO);//删除字段
                                    return false;
                                }

                                if (!fieldDTO.getDataType().equals(f.getDataType())) {
                                    //类型改变
                                    return true;
                                }
                                if (("NCHAR".equals(f.getDataType()) || "BINARY".equals(f.getDataType())) && !fieldDTO.getLength().equals(f.getLength())) {
                                    //长度变更
                                    return true;
                                }
                                //无变化
                                break;
                            }
                        }
                        return false;
                    }
            ).collect(Collectors.toList());


            StableUpdateDTO stableUpdateDTO = new StableUpdateDTO();
            stableUpdateDTO.setDb(databaseModel.getName());
            stableUpdateDTO.setTb(tableName0_TextField.getText());
            stableUpdateDTO.setAddList(addList);
            stableUpdateDTO.setDeleteList(deleteList);
            stableUpdateDTO.setUpdateList(updateList);
            SuperTableUtils.updateStable(connection, stableUpdateDTO);

//            ApplicationStore.getCurrentNode().setName(createDbName.getText());
//            Event.fireEvent(ApplicationStore.getCurrentTreeItem(), new TreeItem.TreeModificationEvent<CommonNode>(TreeItem.valueChangedEvent(), ApplicationStore.getCurrentTreeItem(), ApplicationStore.getCurrentTreeItem().getValue()));
//            databaseModel.getDatabaseResDTO().setName(createDbName.getText());
//            databaseModel.getDatabaseResDTO().setComp(createDbComp.getText());
//            databaseModel.getDatabaseResDTO().setBlocks(createDbBlocks.getText());
//            databaseModel.getDatabaseResDTO().setReplica(createDbReplica.getText());
        }
        createTbDialog.close();
    }

    private void generateCreateTbOptButton(HBox hBox) {
        JFXButton upButton = new JFXButton();
        upButton.setText("上移");
        upButton.setDisable(true);
        upButton.setId(createDbNextRowIndex + "_upButton");

        JFXButton downButton = new JFXButton();
        downButton.setText("下移");
        downButton.setDisable(true);
        downButton.setId(createDbNextRowIndex + "_downButton");

        JFXButton delButton = new JFXButton();
        delButton.setText("删除");
        delButton.setId(createDbNextRowIndex + "_delButton");
        delButton.setOnAction((ActionEvent tt) -> {
            System.out.println("删除 - 点击");
            GridPaneUtils.deleteFieldRow(createTbPane, ((JFXButton) tt.getTarget()));
            createDbNextRowIndex--;
        });

        hBox.getChildren().addAll(upButton, downButton, delButton);
    }

    @ActionMethod("addField")
    private void addField() {
        JFXTextField nameTextField = new JFXTextField();
        nameTextField.setId(createDbNextRowIndex + "_NameField");
        nameTextField.setPadding(new Insets(0, 6, 0, 0));
        nameTextField.setMinWidth(120);

        JFXComboBox<String> typeJFXComboBox = new JFXComboBox<>();
        typeJFXComboBox.setId(createDbNextRowIndex + "_TypeField");
        typeJFXComboBox.setPadding(new Insets(0, 6, 0, 0));
        typeJFXComboBox.setItems(JavaFxBeanUtils.getDataTypeObservableList());
        typeJFXComboBox.setMinWidth(120);
        typeJFXComboBox.setValue("TIMESTAMP");

        JFXTextField lengthTextField = new JFXTextField();
        lengthTextField.setId(createDbNextRowIndex + "_LengthField");
        lengthTextField.setPadding(new Insets(0, 6, 0, 0));
        lengthTextField.setMinWidth(100);

        JFXComboBox<String> isTagJFXComboBox = new JFXComboBox<>();
        isTagJFXComboBox.setId(createDbNextRowIndex + "_isTagField");
        isTagJFXComboBox.setPadding(new Insets(0, 6, 0, 0));
        isTagJFXComboBox.setItems(JavaFxBeanUtils.getTrueFalseObservableList());
        isTagJFXComboBox.setMinWidth(100);
        isTagJFXComboBox.setValue("false");

        HBox hBox = new HBox();
        hBox.setId(createDbNextRowIndex + "_hBox");
        hBox.setMinWidth(300);

        generateCreateTbOptButton(hBox);

        createTbPane.addRow(createDbNextRowIndex, nameTextField, typeJFXComboBox, lengthTextField, isTagJFXComboBox, hBox);
        createDbNextRowIndex++;
    }

    @ActionMethod("createQueryAction")
    public void createQueryAction() {
        TabUtils.addQueryTab(tabPane);
    }

    @ActionMethod("aboutAction")
    public void aboutAction() {
        aboutDialog.setTransitionType(JFXDialog.DialogTransition.TOP);
        aboutDialog.show(rootPane);
    }

    @ActionMethod("createConnectionAction")
    private void createConnectionAction() {
        dialogTitle.setText("新建连接");
        showAddConnectionDialog();
    }

    @ActionMethod("exitAction")
    public void exitAction() {
        System.exit(0);
    }

//    @ActionMethod("clusterQueryAction")
//    public void clusterQueryAction() {
//        TabUtils.addTab(tabPane, "集群查看", new ImageView("/images/cluster.png"), ClusterQueryController.class);
//    }

    @ActionMethod("userQueryAction")
    public void userQueryAction() {
        TabUtils.addTab(tabPane, "用户查看", new ImageView("/images/user_query.png"), UserQueryController.class);
    }

    @ActionMethod("tableQueryAction")
    public void tableQueryAction() {
        TabUtils.addTab(tabPane, "普通表查看", new ImageView("/images/tb.png"), TableQueryController.class);
    }

    @ActionMethod("resourceMonitorAction")
    public void resourceMonitorAction() {
        if (ApplicationStore.getCurrentNode() == null) {
            AlertUtils.show("请先选择一个连接节点！");
            return;
        }
        TabUtils.addTab(tabPane, "监控" + ApplicationStore.getCurrentNode().getData().toString(), new ImageView("/images/monitor.png"), MonitorController.class);
    }

    @ActionMethod("connectionMonitorAction")
    public void connectionMonitorAction() {
        TabUtils.addTab(tabPane, "连接监控", new ImageView("/images/connections.png"), ConnectionMonitorController.class);
    }

    @ActionMethod("queryMonitorAction")
    public void queryMonitorAction() {
        TabUtils.addTab(tabPane, "查询监控", new ImageView("/images/query_monitor.png"), QueryMonitorController.class);
    }

    @PreDestroy
    public void destroy() {
    }


}



