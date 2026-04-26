package com.auctionapp.auctionappjava.common.util;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class AlertUtil {
    // Runnable cho phép hàm có thể chạy sau khi bấm nút OK
    public static void SceneOffAlertController(ActionEvent event, String alertTitle, String alertHeader, String alertContent,
                                               String announcementTitle, String announcementHeader, String announcementContent, Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(alertTitle);
        alert.setHeaderText(alertHeader);
        alert.setContentText(alertContent);

        alert.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                alert.close();

                if (onConfirm != null) {
                    onConfirm.run();
                }

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle(announcementTitle);
                successAlert.setHeaderText(announcementHeader);
                successAlert.setContentText(announcementContent);
                successAlert.showAndWait();

                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.close();
            } else {
                alert.close();
            }
        });
    }
}
