package com.auctionapp.auctionappjava.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class BidderListController {

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
    void handleAdminRemove(ActionEvent event) {

    }

    @FXML
    void handleBack(ActionEvent event) {

    }

    @FXML
    void handleBidders(ActionEvent event) {

    }

    @FXML
    void handleExportWinner(ActionEvent event) {

    }

}
