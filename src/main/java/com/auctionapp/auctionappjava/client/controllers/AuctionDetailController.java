package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class AuctionDetailController {

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

    //TODO: Truy cập thông tin vật phẩm

    void setAuction(AuctionSummaryResponse auction) {
        lblItemName.setText(auction.itemName());
        lblCategory.setText(auction.category());
        lblCurrentPrice.setText(auction.currentPrice().toString());
        lblMinIncrement.setText(auction.minimunIncrement().toString());
        lblStartingPrice.setText(auction.startPrice().toString());
        lblStatus.setText(auction.status());
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleBidding(ActionEvent event) throws IOException {

        if (lblStatus.getText().equals("RUNNING")) {
            SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/ConfirmBiddingScreen.fxml", "Đặt cược");
        } else {
            Runnable unableToGamble = () -> {
                btnGamble.setDisable(true);
            };

            AlertUtils.ConfirmExitController("oops", "Phiên đấu giá hiện không thể tham gia", unableToGamble);
        }
    }

    @FXML
    void handleRanking(ActionEvent event) throws IOException {
        SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/InsideItemScreen.fxml", "Bảng xếp hạng:))");
    }


}