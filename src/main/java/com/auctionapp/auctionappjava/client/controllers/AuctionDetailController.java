package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.formatMoney;

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

    @FXML
    public void initialize() {
        // Tự động kéo dữ liệu từ session ra mỗi khi mở màn hình
        AuctionSummaryResponse currentAuction = AuctionSession.getInstance().getCurrentAuction();
        if (currentAuction != null) {
            loadAuctionData(currentAuction);
        }
    }

    void loadAuctionData(AuctionSummaryResponse auction) {
        lblItemName.setText(auction.itemName());
        lblCategory.setText(auction.category());
        lblStartingPrice.setText(formatMoney(auction.startPrice()));
        lblMinIncrement.setText(formatMoney(auction.minimumIncrement()));
        lblCurrentPrice.setText(formatMoney(auction.currentPrice()));
        lblStatus.setText(String.valueOf(auction.status()));
        //lblEndDate.setText(String.valueOf()); Cái này đang thuộc về AddItem(?), tương tự 2 lbl còn lại.
    }

    @FXML
    void handleBack(ActionEvent event) {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleBidding(ActionEvent event) throws IOException {
        if (AuctionStatus.RUNNING.equals(AuctionStatus.valueOf(lblStatus.getText()))) {
            SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/ConfirmBiddingScreen.fxml", "Đặt cược");
        } else {
            Runnable unableToGamble = () -> {
                btnGamble.setDisable(true);
            };
            AlertUtils.AnnouncementController("oops", "Phiên đấu giá hiện không thể tham gia", unableToGamble, null);
        }
    }

    @FXML
    void handleRanking(ActionEvent event) throws IOException {
        SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/InsideItemScreen.fxml", "Bảng xếp hạng");
    }
}