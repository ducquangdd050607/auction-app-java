package com.auctionapp.auctionappjava.client.controllers;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class AlertController {

    public void SceneOffAlertController(String alertTitle, String alertHeader, String alertContent,
                                String announcementTitle, String announcementHeader, String announcementContent ) {
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

            } else {
                alert.close();
            }
        });
    }
}
