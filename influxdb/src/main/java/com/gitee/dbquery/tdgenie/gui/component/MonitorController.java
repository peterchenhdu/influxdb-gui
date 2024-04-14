package com.gitee.dbquery.tdgenie.gui.component;

import com.gitee.dbquery.tdgenie.common.enums.NodeTypeEnum;
import com.gitee.dbquery.tdgenie.model.ConnectionModel;
import com.gitee.dbquery.tdgenie.model.DatabaseModel;
import com.gitee.dbquery.tdgenie.model.StableModel;
import com.gitee.dbquery.tdgenie.sdk.dto.QueryRstDTO;
import com.gitee.dbquery.tdgenie.sdk.util.RestConnectionUtils;
import com.gitee.dbquery.tdgenie.sdk.util.VersionUtils;
import com.gitee.dbquery.tdgenie.store.ApplicationStore;
import com.gitee.dbquery.tdgenie.util.AlertUtils;
import com.gitee.dbquery.tdgenie.util.DateTimeUtils;
import com.gitee.dbquery.tdgenie.util.ObjectUtils;
import com.gitee.dbquery.tdgenie.util.TsdbConnectionUtils;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import io.datafx.controller.ViewController;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.Map;

@ViewController(value = "/fxml/component/monitor.fxml", title = "资源监控", iconPath = "")
public class MonitorController {

    @FXML
    private StackPane rootPane;

    public static final Color BACKGROUND_DARK = Color.rgb(39, 49, 66); // #2a2a2a
    public static final Color BACKGROUND_LIGHT = Color.rgb(255, 255, 255); // #2a2a2a
    public static final Color FOREGROUND_DARK = Color.rgb(223, 223, 223); // #2a2a2a
    public static final Color FOREGROUND_LIGHT = Color.rgb(52, 52, 52); // #2a2a2a
    public static final Color BORDERCOLOR_DARK = Color.rgb(49, 61, 79); // #2a2a2a
    public static final Color BORDERCOLOR_LIGHT = Color.rgb(185, 185, 185, 0.3f); // #2a2a2a
    @ActionHandler
    private FlowActionHandler actionHandler;
    private Tile donutChartTile;
    private Tile cpuProcessChart;
    private Tile memProcessChart;
    private Tile diskProcessChart;

    private Tile cpuChartTile;
    private Tile memChartTile;
    private Tile diskChartTile;

    private long lastTimerCall;
    private AnimationTimer timer;


    @FXML
    private GridPane centerPane;

    @PostConstruct
    private void init() {
        rootPane.setOnContextMenuRequested(event -> {
            event.consume(); // 标记事件已被处理，防止默认的上下文菜单显示
        });

        // LineChart Data
        XYChart.Series<String, Number> cpuSeries = new XYChart.Series();
        cpuSeries.setName("cpu_taosd");

        XYChart.Series<String, Number> memSeries = new XYChart.Series();
        memSeries.setName("mem_taosd");

        XYChart.Series<String, Number> diskSeries = new XYChart.Series();
        diskSeries.setName("disk_used");

        double usedCpu = 0;
        double totalCpu = 1;
        double usedMen = 0;
        double totalMen = 1;
        double usedDisk = 0;
        double totalDisk = 1;


        ConnectionModel connectionModel;
        if (ApplicationStore.getCurrentNode().getType().equals(NodeTypeEnum.CONNECTION)) {
            connectionModel = (ConnectionModel) ApplicationStore.getCurrentNode().getData();
        } else if (ApplicationStore.getCurrentNode().getType().equals(NodeTypeEnum.DB)) {
            connectionModel = ((DatabaseModel) ApplicationStore.getCurrentNode().getData()).getConnectionModel();
        } else if (ApplicationStore.getCurrentNode().getType().equals(NodeTypeEnum.STB)) {
            connectionModel = ((StableModel) ApplicationStore.getCurrentNode().getData()).getDb().getConnectionModel();
        } else {
            return;
        }
        String sql;
        if (VersionUtils.compareVersion(connectionModel.getVersion(), "3.0") > 0) {
            sql = "SELECT _wstart as ts , " +
                    "avg( cpu_engine ) AS avg_cpu_taosd," +
                    "avg( cpu_system ) AS avg_cpu_system," +
                    "avg( cpu_cores ) AS avg_cpu_cores," +
                    "avg( mem_engine ) AS avg_mem_taosd," +
                    "avg( mem_system ) AS avg_mem_system," +
                    "avg( mem_total ) AS avg_mem_total," +
                    "avg( disk_used ) AS avg_disk_used," +
                    "avg( disk_total ) AS avg_disk_total " +
                    "FROM " +
                    "log.dnodes_info " +
                    "WHERE " +
                    "ts > '" + DateTimeUtils.format(LocalDateTime.now().minusDays(1)) + "' INTERVAL ( 10m );";
        } else {
            sql = "SELECT " +
                    "avg( cpu_taosd ) AS avg_cpu_taosd," +
                    "avg( cpu_system ) AS avg_cpu_system," +
                    "avg( cpu_cores ) AS avg_cpu_cores," +
                    "avg( mem_taosd ) AS avg_mem_taosd," +
                    "avg( mem_system ) AS avg_mem_system," +
                    "avg( mem_total ) AS avg_mem_total," +
                    "avg( disk_used ) AS avg_disk_used," +
                    "avg( disk_total ) AS avg_disk_total " +
                    "FROM " +
                    "log.dn " +
                    "WHERE " +
                    "ts > '" + DateTimeUtils.format(LocalDateTime.now().minusDays(1)) + "' INTERVAL ( 10m );";
        }
        QueryRstDTO rst;
        try {
            rst = RestConnectionUtils.executeQuery(TsdbConnectionUtils.getConnection(connectionModel), sql);
        } catch (Exception e) {
            if (e.getMessage().contains("Database not exist")) {
                AlertUtils.showExceptionMsg("log数据库不存在，请检查taosKeeper是否开启(未开启，则不能获取到监控数据)");
            } else {
                AlertUtils.showException(e);
            }
            return;
        }

        for (Map<String, Object> map : rst.getDataList()) {
            cpuSeries.getData().add(new XYChart.Data(map.get("ts").toString().substring(11, 16), map.get("avg_cpu_taosd")));
            memSeries.getData().add(new XYChart.Data(map.get("ts").toString().substring(11, 16), map.get("avg_mem_taosd")));
            diskSeries.getData().add(new XYChart.Data(map.get("ts").toString().substring(11, 16), map.get("avg_disk_used")));

        }

        if (ObjectUtils.isNotEmpty(rst.getDataList())) {
            Map<String, Object> lastObj = rst.getDataList().get(rst.getDataList().size() - 1);
            usedCpu = Double.parseDouble(lastObj.get("avg_cpu_system").toString());
            totalCpu = 100 * (Double.parseDouble(lastObj.get("avg_cpu_cores").toString()));
            usedMen = Double.parseDouble(lastObj.get("avg_mem_system").toString());
            totalMen = Double.parseDouble(lastObj.get("avg_mem_total").toString());
            usedDisk = Double.parseDouble(lastObj.get("avg_disk_used").toString());
            totalDisk = Double.parseDouble(lastObj.get("avg_disk_total").toString());
        }


        cpuChartTile = TileBuilder.create()
                .skinType(Tile.SkinType.SMOOTHED_CHART)
                .foregroundBaseColor(FOREGROUND_LIGHT)
                .backgroundColor(BACKGROUND_LIGHT)
                .borderColor(BORDERCOLOR_LIGHT)
                .borderWidth(0.8d)
                .title("CPU使用趋势")
                .animated(true)
                .smoothing(false)
                .series(cpuSeries)
                .build();

        memChartTile = TileBuilder.create()
                .skinType(Tile.SkinType.SMOOTHED_CHART)
                .foregroundBaseColor(FOREGROUND_LIGHT)
                .backgroundColor(BACKGROUND_LIGHT)
                .borderColor(BORDERCOLOR_LIGHT)
                .borderWidth(0.8d)
                .title("内存使用趋势")
                .animated(true)
                .smoothing(false)
                .series(memSeries)
                .build();

        diskChartTile = TileBuilder.create()
                .skinType(Tile.SkinType.SMOOTHED_CHART)
                .foregroundBaseColor(FOREGROUND_LIGHT)
                .backgroundColor(BACKGROUND_LIGHT)
                .borderColor(BORDERCOLOR_LIGHT)
                .borderWidth(0.8d)
                .title("磁盘使用趋势")
                .animated(true)
                .smoothing(false)
                .series(diskSeries)
                .build();


        cpuProcessChart = TileBuilder.create()
                .skinType(Tile.SkinType.CIRCULAR_PROGRESS)
                .title("CPU使用率").foregroundBaseColor(FOREGROUND_LIGHT).backgroundColor(BACKGROUND_LIGHT).borderColor(BORDERCOLOR_LIGHT).borderWidth(0.8d)
//                .text("Some text")
//                .unit("\u0025")
                //.graphic(new WeatherSymbol(ConditionAndIcon.CLEAR_DAY, 48, Color.WHITE))
                .build();

        cpuProcessChart.setValue(100 * usedCpu / totalCpu);

        memProcessChart = TileBuilder.create()
                .skinType(Tile.SkinType.CIRCULAR_PROGRESS)
                .title("内存使用率").foregroundBaseColor(FOREGROUND_LIGHT).backgroundColor(BACKGROUND_LIGHT).borderColor(BORDERCOLOR_LIGHT).borderWidth(0.8d)
//                .text("Some text")
//                .unit("\u0025")
                //.graphic(new WeatherSymbol(ConditionAndIcon.CLEAR_DAY, 48, Color.WHITE))
                .build();

        memProcessChart.setValue(100 * usedMen / totalMen);


        diskProcessChart = TileBuilder.create()
                .skinType(Tile.SkinType.CIRCULAR_PROGRESS)
                .title("磁盘使用率").foregroundBaseColor(FOREGROUND_LIGHT).backgroundColor(BACKGROUND_LIGHT).borderColor(BORDERCOLOR_LIGHT).borderWidth(0.8d)
//                .text("Some text")
//                .unit("\u0025")
                //.graphic(new WeatherSymbol(ConditionAndIcon.CLEAR_DAY, 48, Color.WHITE))
                .build();

        diskProcessChart.setValue(100 * usedDisk / totalDisk);


        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now > lastTimerCall + 3_500_000_000L) {
//                    for (XYChart.Data<String, Number> stringNumberData : cpuSeries.getData()) {
//                        stringNumberData.setYValue(RND.nextInt(30));
//                    }
//                    for (XYChart.Data<String, Number> data : series3.getData()) {
//                        data.setYValue(RND.nextInt(10));
//                    }
//                    circularProgressTile.setValue(RND.nextDouble() * 120);
                    lastTimerCall = now;
                }
            }
        };


        centerPane.add(cpuProcessChart, 0, 0);
        centerPane.add(memProcessChart, 1, 0);
        centerPane.add(diskProcessChart, 2, 0);
        centerPane.add(cpuChartTile, 0, 1, 3, 1);
        centerPane.add(memChartTile, 0, 2, 3, 1);
        centerPane.add(diskChartTile, 0, 3, 3, 1);

        timer.start();

    }

    @PreDestroy
    private void destroy() {
        // useful for jpro
        timer.stop();
        System.err.println("destroy " + this + actionHandler.getExceptionHandler());
    }


}
