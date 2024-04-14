package com.gitee.dbquery.tdgenie.store;

import com.gitee.dbquery.tdgenie.model.CommonNode;
import com.gitee.dbquery.tdgenie.model.ConnectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import lombok.Data;

import java.util.HashMap;

/**
 * ApplicationStore
 *
 * @author pc
 * @since 2024/01/31
 **/
@Data
public class ApplicationStore {
    private static TreeItem<CommonNode> connectionTree;
    private static CommonNode currentNode;
    private static TreeItem<CommonNode> currentTreeItem;
    private static HashMap<String, Tab> tabsMap = new HashMap<>();
    private static Double mainPaneLastDividerPositions;

    public static TreeItem<CommonNode> getConnectionTree() {
        return connectionTree;
    }

    public static void setConnectionTree(TreeItem<CommonNode> connectionTree) {
        ApplicationStore.connectionTree = connectionTree;
    }

    public static ConnectionModel getConnection(String name) {
        for(TreeItem<CommonNode> node : connectionTree.getChildren()) {
            if(node.getValue().getName().equals(name)) {
                return (ConnectionModel)(node.getValue().getData());
            }
        }
        return null;
    }

    public static CommonNode getCurrentNode() {
        return currentTreeItem == null ? null : currentTreeItem.getValue();
    }

    public static HashMap<String, Tab> getTabsMap() {
        return tabsMap;
    }

    public static void setTabsMap(HashMap<String, Tab> tabsMap) {
        ApplicationStore.tabsMap = tabsMap;
    }

    public static TreeItem<CommonNode> getCurrentTreeItem() {
        return currentTreeItem;
    }

    public static void setCurrentTreeItem(TreeItem<CommonNode> currentTreeItem) {
        ApplicationStore.currentTreeItem = currentTreeItem;
    }

    public static Double getMainPaneLastDividerPositions() {
        return mainPaneLastDividerPositions;
    }

    public static void setMainPaneLastDividerPositions(Double mainPaneLastDividerPositions) {
        ApplicationStore.mainPaneLastDividerPositions = mainPaneLastDividerPositions;
    }
}


