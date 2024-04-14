package com.gitee.dbquery.tdgenie.util;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * @author 风一样的码农
 * @since 2024/2/20
 **/
public class JavaFxBeanUtils {
    public static ObservableList<String> getTrueFalseObservableList() {
        ObservableList<String> innerList = FXCollections.observableArrayList();
        ListProperty<String> dataValues = new SimpleListProperty<>(innerList);
        dataValues.add("true");
        dataValues.add("false");
        return innerList;
    }

    public static ObservableList<String> getDataTypeObservableList() {
        ObservableList<String> innerList = FXCollections.observableArrayList();
        ListProperty<String> dataValues = new SimpleListProperty<>(innerList);
        dataValues.add("TINYINT");
        dataValues.add("BOOL");
        dataValues.add("BINARY");
        dataValues.add("INT");
        dataValues.add("FLOAT");
        dataValues.add("NCHAR");
        dataValues.add("DOUBLE");
        dataValues.add("TIMESTAMP");
        dataValues.add("BIGINT");
        dataValues.add("SMALLINT");
        return innerList;
    }
}
