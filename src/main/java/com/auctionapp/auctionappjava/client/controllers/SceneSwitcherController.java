package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcherController {

    public static void PopupController(ActionEvent event, String address, String title) throws IOException {
        Parent modalRoot = FXMLLoader.load(SceneSwitcherController.class.getResource(address));
        Stage modalStage = new Stage();
        modalStage.setTitle(title);

        Stage parentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        modalStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        modalStage.initOwner(parentStage);

        Scene modalScene = new Scene(modalRoot);
        modalStage.setScene(modalScene);
        modalStage.setResizable(false);

        modalStage.showAndWait();
    }

    public static void NewSceneController(ActionEvent event, String address, String title) throws IOException {
        Parent root = FXMLLoader.load(SceneSwitcherController.class.getResource(address));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    public static void NavMainController(ActionEvent event, String address, String title) throws IOException {
        Parent scene1 = FXMLLoader.load(SceneSwitcherController.class.getResource("/com/auctionapp/auctionappjava/views/UsersManager.fxml"));
        //Navigator.mainBorderPane.setCenter(scene1);
    }
}
