package com.gitee.dbquery.tdgenie.util;

import com.jfoenix.controls.JFXButton;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import java.util.HashSet;
import java.util.Set;

/**
 * @author chenpi
 * @since 1.0.0 2024/3/22 21:38
 **/
public class GridPaneUtils {
    public static void deleteFieldRow(GridPane grid, final JFXButton delButton) {
        Set<Node> deleteNodes = new HashSet<>();
        boolean matchFlag = false;
        for (Node child : grid.getChildren()) {
            // get index from child
            Integer rowIndex = GridPane.getRowIndex(child);

            // handle null values for index=0
            int r = rowIndex == null ? 0 : rowIndex;


            if (matchFlag && !child.getId().contains(delButton.getId().split("_")[0] + "_")) {//you bug TODO
                // decrement rows for rows after the deleted row
                GridPane.setRowIndex(child, r - 1);
            }
            if (child.getId().contains(delButton.getId().split("_")[0] + "_")) {
                // collect matching rows for deletion
                deleteNodes.add(child);
                matchFlag = true;
            }
        }

        // remove nodes from row
        grid.getChildren().removeAll(deleteNodes);
    }
}
