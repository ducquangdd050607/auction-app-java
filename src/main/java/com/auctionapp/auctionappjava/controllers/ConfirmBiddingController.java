package com.auctionapp.auctionappjava.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ConfirmBiddingController {

    private Stage stage;
    private Parent root;
    private Scene scene;

    @FXML
    private Button Plus100;

    @FXML
    private Button Plus1000;

    @FXML
    private Button Plus10000;

    @FXML
    private Button Plus100000;

    @FXML
    private Label lblBalance;

    @FXML
    private Label lblError;

    @FXML
    private Label lblRole;

    @FXML
    private TextField txtSetPrice;

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/InsideItemScreen.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void handlePlus100(ActionEvent event) {

    }

    @FXML
    void handlePlus1000(ActionEvent event) {

    }

    @FXML
    void handlePlus10000(ActionEvent event) {

    }

    @FXML
    void handlePlus100000(ActionEvent event) {

    }

    @FXML
    void handleTrueConfirm(ActionEvent event) {
        if (txtSetPrice.getText().isEmpty()) {
            lblError.setText("Hãy nhập giá tiền cược.");
            lblError.setTextFill(Color.web("#FF8A80"));
        }

    }

}
