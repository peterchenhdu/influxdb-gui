package com.gitee.dbquery.influxdbgui.util;

import com.gitee.dbquery.influxdbgui.store.ApplicationStore;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * @author chenpi
 * @since 1.0.0 2024/3/22 21:44
 **/
public class ContextMenuUtils {

    public static ContextMenu generateTabPaneContextMenu(TabPane tabPane) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem closeAllMenuItem = new MenuItem("全部关闭");
        closeAllMenuItem.setOnAction(event -> {
            tabPane.getTabs().clear();
            ApplicationStore.getTabsMap().clear();
        });
        MenuItem closeOtherMenuItem = new MenuItem("关闭未选中");
        closeOtherMenuItem.setOnAction(event -> {
            for (Tab tab : tabPane.getTabs()) {
                if (tab.selectedProperty().getValue()) {
                    tabPane.getTabs().clear();
                    ApplicationStore.getTabsMap().clear();
                    tabPane.getTabs().add(tab);
                    ApplicationStore.getTabsMap().put(tab.getText(), tab);
                    break;
                }
            }
        });
        MenuItem closeCurrentMenuItem = new MenuItem("关闭选中");
        closeCurrentMenuItem.setOnAction(event -> {
            for (Tab tab : tabPane.getTabs()) {
                if (tab.selectedProperty().getValue()) {
                    tabPane.getTabs().remove(tab);
                    ApplicationStore.getTabsMap().remove(tab.getText());
                    break;
                }
            }
        });
        contextMenu.getItems().addAll(closeCurrentMenuItem, closeOtherMenuItem, closeAllMenuItem);

        return contextMenu;
    }
}
