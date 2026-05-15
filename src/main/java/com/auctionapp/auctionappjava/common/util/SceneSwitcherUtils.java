package com.auctionapp.auctionappjava.common.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcherUtils {
    public static void PopupController(ActionEvent event, String address, String title) throws IOException {
        Parent modalRoot = FXMLLoader.load(SceneSwitcherUtils.class.getResource(address));
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
        Parent root = FXMLLoader.load(SceneSwitcherUtils.class.getResource(address));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    private static Button currentActiveButton;
    public static void NavSceneController(ActionEvent event, BorderPane borderPane, String address) throws IOException {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-menu-btn-active");
        }
        Button clickedButton = (Button) event.getSource();
        clickedButton.getStyleClass().add("nav-menu-btn-active");
        currentActiveButton = clickedButton;
        Parent view = FXMLLoader.load(SceneSwitcherUtils.class.getResource(address));
        borderPane.setCenter(view);
    }
}
