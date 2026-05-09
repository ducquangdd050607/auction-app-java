package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;

public class InsideItemController {

    private BigDecimal best = AuctionSession.getInstance().getCurrentAuction().currentPrice();
    private BigDecimal minIncrement = AuctionSession.getInstance().getCurrentAuction().minimumIncrement();
    private BigDecimal start = AuctionSession.getInstance().getCurrentAuction().startPrice();
    //private String bestBidder =

    @FXML
    private Button btnBack;

    @FXML
    private Button btnExportWinner;

    @FXML
    private TableColumn<?, ?> colBidAmount;

    @FXML
    private TableColumn<?, ?> colBidTime;

    @FXML
    private TableColumn<?, ?> colRank;

    @FXML
    private TableColumn<?, ?> colUsername;

    @FXML
    private Label lblMinIncrement;

    @FXML
    private Label lblCategory;

    @FXML
    private Label lblItemName;

    @FXML
    private Label lblStartingPrice;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblTopBid;

    @FXML
    private Label lblTopBidder;

    @FXML
    private TableView<?> tableBidders;

    @FXML
    void initialize() {
        lblStartingPrice.setText(start.toPlainString());
        lblTopBid.setText(best.toPlainString());
        lblMinIncrement.setText(minIncrement.toPlainString());
        lblCategory.setText(AuctionSession.getInstance().getCurrentAuction().category());
        lblItemName.setText(AuctionSession.getInstance().getCurrentAuction().itemName());
        lblStatus.setText(AuctionSession.getInstance().getCurrentAuction().status().toString());
    }

    @FXML
    void handleRemove(ActionEvent event) {
        if (LoginController.adminRoute) {
            // force-remove
        } else if (LoginController.sellerRoute) {
            // if (Item.status == "OPEN") {
            // remove
            // } else {
            // { báo lỗi: Không đủ thẩm quyền xóa sản phẩm }
        }
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        if (LoginController.bidderRoute) {
            SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml", "Thông tin sản phẩm");

        } else {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    void handleBidders(ActionEvent event) {
        // TODO: Quản lý Bidders trong sản phẩm
    }

    @FXML
    void handleExportWinner(ActionEvent event) {
        // TODO: Xuất ra danh sách người thắng (Optional)
    }

}
