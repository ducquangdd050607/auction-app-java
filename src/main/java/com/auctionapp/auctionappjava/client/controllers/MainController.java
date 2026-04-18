package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    private Stage stage;
    private Parent root;
    private Scene scene;

    @FXML
    private Button btnUser;
    @FXML
    private Button btnLogin;
    @FXML
    private Button btnRegister;
    @FXML
    private HBox guestNavBox;
    @FXML
    private Label lblUserAvatar;
    @FXML
    private StackPane userNavBox;
    @FXML
    private VBox navigator;
    @FXML
    private Button btnExplore;


    @FXML
    void handleAboutUs(ActionEvent event) {

    }

    @FXML
    void handleLogin(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/LoginScreen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        // 1. Ép Stage cập nhật lại kích thước cho vừa khít với LoginScreen
        stage.sizeToScene();

        // 2. Lệnh quyết định: Đưa cửa sổ ra chính giữa màn hình máy tính
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    void handleRegister(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/RegisterScreen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        // 1. Ép Stage cập nhật lại kích thước cho vừa khít với LoginScreen
        stage.sizeToScene();

        // 2. Lệnh quyết định: Đưa cửa sổ ra chính giữa màn hình máy tính
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    void handleExit (ActionEvent event) throws IOException {

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận thoát");
        alert.setHeaderText("Bạn có chắc chắn muốn thoát ứng dụng không?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                stage.close();
            }
        });
    }
}
