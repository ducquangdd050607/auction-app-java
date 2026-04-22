package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class AlertController {

    public static void SceneOffAlertController(ActionEvent event, String alertTitle, String alertHeader, String alertContent,
                                               String announcementTitle, String announcementHeader, String announcementContent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(alertTitle);
        alert.setHeaderText(alertHeader);
        alert.setContentText(alertContent);

        alert.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                alert.close();
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
