package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
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

    private static Button currentActiveButton;
    private static void setActiveButton(Button clickedButton) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-menu-btn-active");
        }
        clickedButton.getStyleClass().add("nav-menu-btn-active");
        currentActiveButton = clickedButton;
    }
    public static void NavSceneController(ActionEvent event, BorderPane borderPane, String address) throws IOException {
        Button clickedButton = (Button) event.getSource();
        setActiveButton(clickedButton);
        Parent view = FXMLLoader.load(SceneSwitcherController.class.getResource(address));
        borderPane.setCenter(view);
    }
    // Lưu ý có 1 hàm để ngay khi đi vào Navigator sẽ ở Dashboard -> Set targetButton là btnDashboard luôn
    // Cái này muốn xóa cái hàm này lắm nhưng không biết xóa đi thì xử lí sao :v
    public static void NavSceneController(Button targetButton, BorderPane borderPane, String address) throws IOException {
        setActiveButton(targetButton);
        Parent view = FXMLLoader.load(SceneSwitcherController.class.getResource(address));
        borderPane.setCenter(view);
    }
}
