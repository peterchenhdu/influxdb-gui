package com.gitee.dbquery.tdgenie.gui.component;

import cn.hutool.core.io.FileUtil;
import com.gitee.dbquery.tdgenie.common.enums.NodeTypeEnum;
import com.gitee.dbquery.tdgenie.sdk.dto.ConnectionDTO;
import com.gitee.dbquery.tdgenie.sdk.dto.QueryRstDTO;
import com.gitee.dbquery.tdgenie.sdk.util.RestConnectionUtils;
import com.gitee.dbquery.tdgenie.util.AlertUtils;
import com.gitee.dbquery.tdgenie.util.ObjectUtils;
import com.gitee.dbquery.tdgenie.util.TableUtils;
import com.gitee.dbquery.tdgenie.util.TsdbConnectionUtils;
import com.gitee.dbquery.tdgenie.model.CommonNode;
import com.gitee.dbquery.tdgenie.model.ConnectionModel;
import com.gitee.dbquery.tdgenie.model.DatabaseModel;
import com.gitee.dbquery.tdgenie.model.StableModel;
import com.gitee.dbquery.tdgenie.store.ApplicationStore;
import com.github.vertical_blank.sqlformatter.SqlFormatter;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CommonTabController
 *
 * @author pc
 * @since 2024/01/31
 **/
@ViewController(value = "/fxml/component/query_tab.fxml")
public class QueryTabController {
    private static final String[] KEYWORDS = new String[]{"ABORT","ACCOUNT","ACCOUNTS","ADD","AFTER","AGGREGATE","ALIVE",
            "ALL","ALTER","ANALYZE","AND","APPS","AS","ASC","AT_ONCE","ATTACH","BALANCE","BEFORE","BEGIN","BETWEEN","BIGINT",
            "BINARY","BITAND","BITNOT","BITOR","BLOCKS","BNODE","BNODES","BOOL","BUFFER","BUFSIZE","BY","CACHE","CACHEMODEL",
            "CACHESIZE","CASCADE","CAST","CHANGE","CLIENT_VERSION","CLUSTER","COLON","COLUMN","COMMA","COMMENT","COMP","COMPACT",
            "CONCAT","CONFLICT","CONNECTION","CONNECTIONS","CONNS","CONSUMER","CONSUMERS","CONTAINS","COPY","COUNT","CREATE",
            "CURRENT_USER","DATABASE","DATABASES","DBS","DEFERRED","DELETE","DELIMITERS","DESC","DESCRIBE","DETACH","DISTINCT",
            "DISTRIBUTED","DIVIDE","DNODE","DNODES","DOT","DOUBLE","DROP","DURATION","EACH","ENABLE","END","EVERY","EXISTS",
            "EXPIRED","EXPLAIN","FAIL","FILE","FILL","FIRST","FLOAT","FLUSH","FOR","FROM","FUNCTION","FUNCTIONS","GLOB",
            "GRANT","GRANTS","GROUP","HAVING","ID","IF","IGNORE","IMMEDIATE","IMPORT","IN","INDEX","INDEXES","INITIALLY",
            "INNER","INSERT","INSTEAD","INT","INTEGER","INTERVAL","INTO","IS","ISNULL","JOIN","JSON","KEEP","KEY","KILL",
            "LAST","LAST_ROW","LICENCES","LIKE","LIMIT","LINEAR","LOCAL","MATCH","MAX_DELAY","BWLIMIT","MAXROWS","MAX_SPEED",
            "MERGE","META","MINROWS","MINUS","MNODE","MNODES","MODIFY","MODULES","NCHAR","NEXT","NMATCH","NONE","NOT","NOTNULL",
            "NOW","NULL","NULLS","OF","OFFSET","ON","OR","ORDER","OUTPUTTYPE","PAGES","PAGESIZE","PARTITIONS","PASS","PLUS",
            "PORT","PPS","PRECISION","PREV","PRIVILEGE","QNODE","QNODES","QTIME","QUERIES","QUERY","RAISE","RANGE","RATIO",
            "READ","REDISTRIBUTE","RENAME","REPLACE","REPLICA","RESET","RESTRICT","RETENTIONS","REVOKE","ROLLUP","ROW",
            "SCHEMALESS","SCORES","SELECT","SEMI","SERVER_STATUS","SERVER_VERSION","SESSION","SET","SHOW","SINGLE_STABLE",
            "SLIDING","SLIMIT","SMA","SMALLINT","SNODE","SNODES","SOFFSET","SPLIT","STABLE","STABLES","START","STATE",
            "STATE_WINDOW","STATEMENT","STORAGE","STREAM","STREAMS","STRICT","STRING","SUBSCRIPTIONS","SYNCDB","SYSINFO",
            "TABLE","TABLES","TAG","TAGS","TBNAME","TIMES","TIMESTAMP","TIMEZONE","TINYINT","TO","TODAY","TOPIC","TOPICS",
            "TRANSACTION","TRANSACTIONS","TRIGGER","TRIM","TSERIES","TTL","UNION","UNSIGNED","UPDATE","USE","USER","USERS",
            "USING","VALUE","VALUES","VARCHAR","VARIABLE","VARIABLES","VERBOSE","VGROUP","VGROUPS","VIEW","VNODES","WAL",
            "WAL_FSYNC_PERIOD","WAL_LEVEL","WAL_RETENTION_PERIOD","WAL_RETENTION_SIZE","WATERMARK","WHERE","WINDOW_CLOSE",
            "WITH","WRITE","_C0","_IROWTS","_QDURATION","_QEND","_QSTART","_ROWTS","_WDURATION","_WEND","_WSTART","abort","account","accounts","add","after","aggregate","alive","all","alter","analyze","and","apps","as","asc","at_once","attach","balance","before","begin","between","bigint","binary","bitand","bitnot","bitor","blocks","bnode","bnodes","bool","buffer","bufsize","by","cache","cachemodel","cachesize","cascade","cast","change","client_version","cluster","colon","column","comma","comment","comp","compact","concat","conflict","connection","connections","conns","consumer","consumers","contains","copy","count","create","current_user","database","databases","dbs","deferred","delete","delimiters","desc","describe","detach","distinct","distributed","divide","dnode","dnodes","dot","double","drop","duration","each","enable","end","every","exists","expired","explain","fail","file","fill","first","float","flush","for","from","function","functions","glob","grant","grants","group","having","id","if","ignore","immediate","import","in","index","indexes","initially","inner","insert","instead","int","integer","interval","into","is","isnull","join","json","keep","key","kill","last","last_row","licences","like","limit","linear","local","match","max_delay","bwlimit","maxrows","max_speed","merge","meta","minrows","minus","mnode","mnodes","modify","modules","nchar","next","nmatch","none","not","notnull","now","null","nulls","of","offset","on","or","order","outputtype","pages","pagesize","partitions","pass","plus","port","pps","precision","prev","privilege","qnode","qnodes","qtime","queries","query","raise","range","ratio","read","redistribute","rename","replace","replica","reset","restrict","retentions","revoke","rollup","row","schemaless","scores","select","semi","server_status","server_version","session","set","show","single_stable","sliding","slimit","sma","smallint","snode","snodes","soffset","split","stable","stables","start","state","state_window","statement","storage","stream","streams","strict","string","subscriptions","syncdb","sysinfo","table","tables","tag","tags","tbname","times","timestamp","timezone","tinyint","to","today","topic","topics","transaction","transactions","trigger","trim","tseries","ttl","union","unsigned","update","use","user","users","using","value","values","varchar","variable","variables","verbose","vgroup","vgroups","view","vnodes","wal","wal_fsync_period","wal_level","wal_retention_period","wal_retention_size","watermark","where","window_close","with","write","_c0","_irowts","_qduration","_qend","_qstart","_rowts","_wduration","_wend","_wstart"};
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'";
    private static final String COMMENT_PATTERN = "-- [^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

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
    private SplitPane querySplitPane;
    @FXML
    private JFXTabPane executeResultTabPane;
    @FXML
    private JFXComboBox<String> connectionComboBox;
    @FXML
    private JFXComboBox<String> dbComboBox;
    @FXML
    private CodeArea sqlEditArea;
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

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("BRACKET") != null ? "bracket" :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("STRING") != null ? "string" :
                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                            null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    @ActionMethod("executeQuery")
    private void executeQuery() {
        showPage(1);
    }

    private void prettySql() {
        String sql = sqlEditArea.getText();
        sqlEditArea.clear();
        sqlEditArea.replaceText(0, 0, SqlFormatter.format(sql));

    }

    protected void saveSql() {
        // ShowDialog.showConfirmDialog(FXRobotHelper.getStages().get(0),
        // "是否导出数据到txt？", "信息");
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.sql)", "*.sql");
        fileChooser.getExtensionFilters().add(extFilter);
        Stage s = new Stage();
        File file = fileChooser.showSaveDialog(s);
        if (file == null)
            return;
        if (file.exists()) {//文件已存在，则删除覆盖文件
            file.delete();
        }
        String exportFilePath = file.getAbsolutePath();
        System.out.println("保存文件的路径" + exportFilePath);



        FileUtil.writeString(sqlEditArea.getText(), file, "utf-8");

        AlertUtils.show("保存成功!保存路径:\n" + exportFilePath);


    }

    @PostConstruct
    public void init() {

        rootPane.setOnContextMenuRequested(event -> {
            event.consume(); // 标记事件已被处理，防止默认的上下文菜单显示
        });

        // add line numbers to the left of area
        sqlEditArea.setParagraphGraphicFactory(LineNumberFactory.get(sqlEditArea));
        // recompute the syntax highlighting 500 ms after user stops editing area
        Subscription cleanupWhenNoLongerNeedIt = sqlEditArea

                // plain changes = ignore style changes that are emitted when syntax highlighting is reapplied
                // multi plain changes = save computation by not rerunning the code multiple times
                //   when making multiple changes (e.g. renaming a method at multiple parts in file)
                .multiPlainChanges()

                // do not emit an event until 500 ms have passed since the last emission of previous stream
                .successionEnds(Duration.ofMillis(500))

                // run the following code block when previous stream emits an event
                .subscribe(ignore -> sqlEditArea.setStyleSpans(0, computeHighlighting(sqlEditArea.getText())));
        // when no longer need syntax highlighting and wish to clean up memory leaks
        // run: `cleanupWhenNoLongerNeedIt.unsubscribe();`



        prettySqlBox.setOnMouseClicked((MouseEvent t) -> {
            System.out.println("prettySqlBox点击");
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }

            prettySql();
        });

        saveSqlBox.setOnMouseClicked((MouseEvent t) -> {
            if (!t.getButton().equals(MouseButton.PRIMARY)) {
                return;
            }

            saveSql();
        });


        if (ApplicationStore.getConnectionTree() != null) {
            ApplicationStore.getConnectionTree().getChildren().forEach(d -> {
                connectionComboBox.getItems().add(d.getValue().getName());
            });
        }

        connectionComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    System.out.println(newValue);
                    dbComboBox.getItems().clear();
                    for (TreeItem<CommonNode> node : ApplicationStore.getConnectionTree().getChildren()) {
                        if (node.getValue().getName().equals(newValue)) {
                            node.getChildren().forEach(d -> {
                                dbComboBox.getItems().add(d.getValue().getName());
                            });
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
        }


        querySplitPane.setDividerPositions(0.7);

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
        if (connectionComboBox.getSelectionModel().isEmpty() || ObjectUtils.isEmpty(sqlEditArea.getText())) {
            return;
        }

        String connectionName = connectionComboBox.getSelectionModel().getSelectedItem();
        String dbName = dbComboBox.getSelectionModel().getSelectedItem();
        ConnectionModel connectionModel = getConnectionModel(connectionName);
        ConnectionDTO connection = null;
        if (null == dbName) {
            connection = TsdbConnectionUtils.getConnection(connectionModel);
        } else {
            connection = TsdbConnectionUtils.getConnectionWithDB(connectionModel, dbName);
        }


        executeSql.setText(sqlEditArea.getText());

        String[] sqlArr = sqlEditArea.getText().split(";");
        for(String sql:sqlArr) {
            processSingleSql(queryMap, sql.trim(), connection);
        }


    }

    private void processSingleSql(Map<String, Object> queryMap, String sql, ConnectionDTO connection) {
        if(ObjectUtils.isEmpty(sql)) {
            return;
        }

//        if (sql.toUpperCase().startsWith("SELECT")) {
            executeResultTabPane.getSelectionModel().select(1);


            Integer page = (Integer) queryMap.get("page");
            int start = (page - 1) * 1000;

            QueryRstDTO queryRstDTO = null;
            long queryStart = System.currentTimeMillis();
            try {
                if(sql.toUpperCase().startsWith("SELECT")) {
                    queryRstDTO = RestConnectionUtils.executeQuery(connection, "select * from (" + sql.replaceAll(";", "") + ") limit 1000 OFFSET " + start  );
                } else {
                    queryRstDTO = RestConnectionUtils.executeQuery(connection, sql.replaceAll(";", ""));
                }
                executeStatus.setText("OK");
            } catch (Exception e) {
                executeStatus.setText(e.getMessage());
                tableView.getColumns().clear();
                executeResultTabPane.getSelectionModel().select(0);
                return;
            }
            executeCost.setText((System.currentTimeMillis() - queryStart) + "ms");

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

//            QueryRstDTO countRstDTO = RestConnectionUtils.executeQuery(connection, "select count(*) from (" + sqlEditArea.getText().replaceAll(";", "") + ")");
//            long total = ObjectUtils.isEmpty(countRstDTO.getDataList()) ? 0 : (long) countRstDTO.getDataList().get(0).get("count(*)");
//            pageCount.setValue((total / 1000) + 1);
//        } else {
//            executeResultTabPane.getSelectionModel().select(0);
//            long start = System.currentTimeMillis();
//            try {
//                RestConnectionUtils.executeUpdate(connection, Collections.singletonList(sql));
//                executeStatus.setText("OK");
//            } catch (Exception e) {
//                executeStatus.setText(e.getMessage());
//            }
//            executeCost.setText((System.currentTimeMillis() - start) + "ms");
//
//        }
    }


    @PreDestroy
    private void destroy() {
        System.err.println("destroy " + this);
    }


}
