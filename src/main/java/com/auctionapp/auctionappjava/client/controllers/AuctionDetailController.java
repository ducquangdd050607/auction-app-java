package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class AuctionDetailController {

    private Stage stage;

    @FXML
    private Button btnBack;

    @FXML
    private Button btnGamble;

    @FXML
    private Button btnRanking;

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
    private Label lblMinIncrement;

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
        SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/ConfirmBiddingScreen.fxml", "Đặt cược");
    }

    @FXML
    void handleRanking(ActionEvent event) throws IOException {
        SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/InsideItemScreen.fxml", "Bảng xếp hạng:))");
    }
}
