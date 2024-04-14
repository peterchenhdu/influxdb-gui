package com.gitee.dbquery.influxdbgui.util;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import io.datafx.controller.context.ApplicationContext;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 风一样的码农
 * @since 2024/2/8
 **/
@Slf4j
public class AlertUtils {

    public static void show(String msg) {
        JFXAlert<Void> alert = new JFXAlert<>(ApplicationContext.getInstance().getRegisteredObject(Stage.class));
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setOverlayClose(true);
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label("提示"));
        layout.setBody(new Label(msg));
        JFXButton closeButton = new JFXButton("确定");
        closeButton.setOnAction(event -> alert.hideWithAnimation());
        layout.setActions(closeButton);
        alert.setContent(layout);
        alert.show();
    }

    public static void showException(Throwable t) {
        log.error(t.toString(), t);
        show("程序异常：" + t.getMessage());
    }

    public static void showExceptionMsg(String msg) {
        show("程序异常：" + msg);
    }
}
