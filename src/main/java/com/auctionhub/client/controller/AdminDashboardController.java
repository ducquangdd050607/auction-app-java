package com.auctionhub.client.controller;

import com.auctionhub.client.core.ClientContext;
import com.auctionhub.client.service.AlertService;
import com.auctionhub.common.dto.AdminOverviewDto;
import com.auctionhub.common.dto.AuctionSummaryDto;
import com.auctionhub.common.dto.UserSummaryDto;
import com.auctionhub.common.util.MoneyUtils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AdminDashboardController {
    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label totalAuctionsLabel;
    @FXML
    private Label runningAuctionsLabel;
    @FXML
    private Label finishedAuctionsLabel;
    @FXML
    private TableView<UserSummaryDto> userTable;
    @FXML
    private TableColumn<UserSummaryDto, String> usernameColumn;
    @FXML
    private TableColumn<UserSummaryDto, String> userRoleColumn;
    @FXML
    private TableColumn<UserSummaryDto, String> userEmailColumn;
    @FXML
    private TableView<AuctionSummaryDto> adminAuctionTable;
    @FXML
    private TableColumn<AuctionSummaryDto, String> adminAuctionTitleColumn;
    @FXML
    private TableColumn<AuctionSummaryDto, String> adminAuctionStatusColumn;
    @FXML
    private TableColumn<AuctionSummaryDto, String> adminAuctionPriceColumn;

    @FXML
    public void initialize() {
        usernameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().username()));
        userRoleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().role().name()));
        userEmailColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().email()));
        adminAuctionTitleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().title()));
        adminAuctionStatusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().status().name()));
        adminAuctionPriceColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(MoneyUtils.format(cell.getValue().currentPrice())));
        loadOverview();
    }

    @FXML
    private void loadOverview() {
        try {
            AdminOverviewDto overview = ClientContext.getInstance().api().adminOverview();
            totalUsersLabel.setText(String.valueOf(overview.totalUsers()));
            totalAuctionsLabel.setText(String.valueOf(overview.totalAuctions()));
            runningAuctionsLabel.setText(String.valueOf(overview.runningAuctions()));
            finishedAuctionsLabel.setText(String.valueOf(overview.finishedAuctions()));
            userTable.setItems(FXCollections.observableArrayList(overview.users()));
            adminAuctionTable.setItems(FXCollections.observableArrayList(overview.auctions()));
        } catch (Exception ex) {
            AlertService.error("Load admin overview failed", ex.getMessage());
        }
    }

    @FXML
    private void handleMarkPaid() {
        AuctionSummaryDto selected = adminAuctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.error("No auction selected", "Hãy chọn một phiên trước khi chuyển PAID.");
            return;
        }
        try {
            ClientContext.getInstance().api().markPaid(selected.auctionId());
            AlertService.info("Mark PAID", "Phiên đấu giá đã được đánh dấu PAID.");
            loadOverview();
        } catch (Exception ex) {
            AlertService.error("Mark PAID failed", ex.getMessage());
        }
    }

    @FXML
    private void handleCancelAuction() {
        AuctionSummaryDto selected = adminAuctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.error("No auction selected", "Hãy chọn một phiên trước khi hủy.");
            return;
        }
        try {
            ClientContext.getInstance().api().cancelAuction(selected.auctionId());
            AlertService.info("Cancel auction", "Admin đã hủy phiên đấu giá.");
            loadOverview();
        } catch (Exception ex) {
            AlertService.error("Cancel auction failed", ex.getMessage());
        }
    }
}
