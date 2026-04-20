package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class AuctionDetailController {

    private Stage stage;
    private Scene scene;


    @FXML
    private Button btnBack;

    @FXML
    private Button btnGamble;

    @FXML
    private Label lblCategory;

    @FXML
    private Label lblCurrentLeader;

    @FXML
    private Label lblCurrentPrice;

    @FXML
    private Label lblEndDate;

    @FXML
    private Label lblItemName;

    @FXML
    private Label lblStartingPrice;

    @FXML
    private Label lblStatus;

    @FXML
    private Label txtDescription;


    @FXML
    void handleBack(ActionEvent event) throws IOException {
        stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleBidding(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/ConfirmBiddingScreen.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void handleRanking(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/InsideItemScreen.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
