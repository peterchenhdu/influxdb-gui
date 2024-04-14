package com.gitee.dbquery.tdgenie.util;

import com.gitee.dbquery.tdgenie.common.enums.NodeTypeEnum;
import com.gitee.dbquery.tdgenie.gui.component.DbTabController;
import com.gitee.dbquery.tdgenie.gui.component.QueryTabController;
import com.gitee.dbquery.tdgenie.gui.component.RecordTabController;
import com.gitee.dbquery.tdgenie.gui.component.StbTabController;
import com.gitee.dbquery.tdgenie.store.ApplicationStore;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.FlowHandler;
import io.datafx.controller.flow.container.AnimatedFlowContainer;
import io.datafx.controller.flow.container.ContainerAnimations;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

/**
 * @author chenpi
 * @since 2024/3/26
 **/
@Slf4j
public class TabUtils {
    public static <T> void addConnectionTab(TabPane tabPane, String title) {
        try {
            addTab(tabPane, title, ImageViewUtils.getImageViewByType(NodeTypeEnum.CONNECTION), DbTabController.class, null);
        } catch (Exception e) {
            AlertUtils.showException(e);
        }
    }

    public static <T> void addDbTab(TabPane tabPane, String title) {
        try {
            addTab(tabPane, title, ImageViewUtils.getImageViewByType(NodeTypeEnum.DB), StbTabController.class, null);
        } catch (Exception e) {
            AlertUtils.showException(e);
        }
    }

    public static <T> void addQueryTab(TabPane tabPane) {
        try {
            addTab(tabPane, "数据查询", new ImageView("/images/query.png"), QueryTabController.class, null);
        } catch (Exception e) {
            AlertUtils.showException(e);
        }
    }

    public static <T> void addStbTab(TabPane tabPane, String title) {
        try {
            addTab(tabPane, title, ImageViewUtils.getImageViewByType(NodeTypeEnum.STB), RecordTabController.class, null);
        } catch (Exception e) {
            AlertUtils.showException(e);
        }
    }

    public static <T> void addTab(TabPane tabPane, String title, Node icon, Class<T> controllerClass) {
        try {
            addTab(tabPane, title, icon, controllerClass, null);
        } catch (Exception e) {
            AlertUtils.showException(e);
        }
    }
    public static <T> void addTab(TabPane tabPane, String title, Node icon, Class<T> controllerClass, Object userData) throws FlowException {

        FlowHandler flowHandler = new Flow(controllerClass).createHandler();
        Tab tab = ApplicationStore.getTabsMap().get(title);

        if (tab == null) {

            tab = new Tab(title);
            tab.setUserData(userData);
            tab.setGraphic(icon);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);

            StackPane node = flowHandler.start(new AnimatedFlowContainer(Duration.millis(320), ContainerAnimations.SWIPE_LEFT));
            node.getStyleClass().addAll("tab-content");
            tab.setContent(node);

            ApplicationStore.getTabsMap().put(title, tab);
            tab.setOnClosed(event -> {
                ApplicationStore.getTabsMap().remove(title);
                try {
                    flowHandler.getCurrentViewContext().destroy();
                } catch (IllegalAccessException | InvocationTargetException e) {
                    AlertUtils.showException(e);
                }

            });
        }

        tabPane.getSelectionModel().select(tab);
    }
}
