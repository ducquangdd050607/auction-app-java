package com.auctionapp.auctionappjava.client.service;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class AlertService {
    public void info(String title, String message){ show(Alert.AlertType.INFORMATION, title, message); }
    public void error(String title, String message){ show(Alert.AlertType.ERROR, title, message); }
    private void show(Alert.AlertType type, String title, String message){ Runnable r=() -> { Alert a=new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait(); }; if(Platform.isFxApplicationThread()) r.run(); else Platform.runLater(r); }
}
