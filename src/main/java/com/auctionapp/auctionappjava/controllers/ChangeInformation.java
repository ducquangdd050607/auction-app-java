package com.auctionapp.auctionappjava.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
    void handleConfirm(ActionEvent event) {

    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/WalletScreen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
