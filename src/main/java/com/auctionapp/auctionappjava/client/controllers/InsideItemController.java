package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;

public class InsideItemController {

    private Stage stage;

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
    void handleRemove(ActionEvent event) {
        if (Route.adminRoute) {
            // force-remove
        } else if (Route.sellerRoute) {
            // if (Item.status == "OPEN") {
            // remove
            // } else {
            // { báo lỗi: Không đủ thẩm quyền xóa sản phẩm }
        }
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        if (Route.bidderRoute) {
            SceneSwitcherController.NewSceneController(event, "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml", "pretendtobeatitle");

        } else {
            stage = (Stage) btnBack.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    void handleBidders(ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION, "Danh sách bidder được đồng bộ qua màn hình chi tiết/auction realtime.").showAndWait();
    }

    @FXML
    void handleExportWinner(ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION, "Winner được xác định tự động khi AuctionMonitor đóng phiên.").showAndWait();
    }

}
