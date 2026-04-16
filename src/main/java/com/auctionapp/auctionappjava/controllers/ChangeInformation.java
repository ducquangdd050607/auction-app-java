package com.auctionapp.auctionappjava.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class ChangeInformation {

    private Stage stage;
    private Parent root;
    private Scene scene;

    @FXML
    private Label lblError;

    @FXML
    private Label lblExtraInfo;

    @FXML
    private TextField txtBirthday;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtUsername;

    @FXML
    void handleConfirm(ActionEvent event) throws IOException {
        // TODO: Thay đổi thông tin trong database

        if (txtEmail.getText().trim().isEmpty() || txtName.getText().trim().isEmpty() || txtUsername.getText().trim().isEmpty() || txtBirthday.getText().trim().isEmpty()) {
            lblError.setText("Hãy điền tất cả thông tin");
            lblError.setVisible(true);
            lblError.setTextFill(Color.web("#FF8A80"));
        } else {
            root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/WalletScreen.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        }
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/WalletScreen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

}
