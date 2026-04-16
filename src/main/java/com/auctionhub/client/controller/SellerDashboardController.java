package com.auctionhub.client.controller;

import com.auctionhub.client.core.ClientContext;
import com.auctionhub.client.service.AlertService;
import com.auctionhub.common.dto.AuctionSummaryDto;
import com.auctionhub.common.dto.CreateAuctionRequest;
import com.auctionhub.common.dto.UpdateAuctionRequest;
import com.auctionhub.common.enums.ItemType;
import com.auctionhub.common.util.MoneyUtils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SellerDashboardController {
    @FXML
    private TableView<AuctionSummaryDto> sellerAuctionTable;
    @FXML
    private TableColumn<AuctionSummaryDto, String> sellerTitleColumn;
    @FXML
    private TableColumn<AuctionSummaryDto, String> sellerPriceColumn;
    @FXML
    private TableColumn<AuctionSummaryDto, String> sellerStatusColumn;
    @FXML
    private ComboBox<ItemType> itemTypeCombo;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField startPriceField;
    @FXML
    private TextField minIncrementField;
    @FXML
    private TextField attributeOneField;
    @FXML
    private TextField attributeTwoField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextField startTimeField;
    @FXML
    private TextField endTimeField;

    @FXML
    public void initialize() {
        itemTypeCombo.setItems(FXCollections.observableArrayList(ItemType.values()));
        itemTypeCombo.getSelectionModel().select(ItemType.ELECTRONICS);
        sellerTitleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().title()));
        sellerPriceColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(MoneyUtils.format(cell.getValue().currentPrice())));
        sellerStatusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().status().name()));
        sellerAuctionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> fillForm(newValue));
        sellerAuctionTable.setPlaceholder(new Label("Seller chưa có phiên đấu giá nào."));
        loadMyAuctions();
    }

    @FXML
    private void loadMyAuctions() {
        try {
            sellerAuctionTable.setItems(FXCollections.observableArrayList(ClientContext.getInstance().api().listMyAuctions()));
        } catch (Exception ex) {
            AlertService.error("Load seller auctions failed", ex.getMessage());
        }
    }

    @FXML
    private void handleCreateAuction() {
        try {
            ClientContext.getInstance().api().createAuction(new CreateAuctionRequest(
                    itemTypeCombo.getValue(),
                    titleField.getText(),
                    descriptionArea.getText(),
                    new BigDecimal(startPriceField.getText().trim()),
                    new BigDecimal(minIncrementField.getText().trim()),
                    parseDateTime(startDatePicker.getValue().toString(), startTimeField.getText()),
                    parseDateTime(endDatePicker.getValue().toString(), endTimeField.getText()),
                    attributeOneField.getText(),
                    attributeTwoField.getText()));
            AlertService.info("Tạo phiên", "Đã tạo phiên đấu giá mới.");
            clearForm();
            loadMyAuctions();
        } catch (Exception ex) {
            AlertService.error("Create auction failed", ex.getMessage());
        }
    }

    @FXML
    private void handleUpdateAuction() {
        AuctionSummaryDto selected = sellerAuctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.error("No auction selected", "Hãy chọn một phiên trước khi cập nhật.");
            return;
        }
        try {
            ClientContext.getInstance().api().updateAuction(new UpdateAuctionRequest(
                    selected.auctionId(),
                    titleField.getText(),
                    descriptionArea.getText(),
                    new BigDecimal(startPriceField.getText().trim()),
                    new BigDecimal(minIncrementField.getText().trim()),
                    parseDateTime(startDatePicker.getValue().toString(), startTimeField.getText()),
                    parseDateTime(endDatePicker.getValue().toString(), endTimeField.getText()),
                    attributeOneField.getText(),
                    attributeTwoField.getText()));
            AlertService.info("Cập nhật phiên", "Thông tin phiên đấu giá đã được cập nhật.");
            loadMyAuctions();
        } catch (Exception ex) {
            AlertService.error("Update auction failed", ex.getMessage());
        }
    }

    @FXML
    private void handleDeleteAuction() {
        AuctionSummaryDto selected = sellerAuctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.error("No auction selected", "Hãy chọn một phiên trước khi xóa.");
            return;
        }
        try {
            ClientContext.getInstance().api().deleteAuction(selected.auctionId());
            AlertService.info("Xóa phiên", "Phiên đấu giá đã bị xóa.");
            clearForm();
            loadMyAuctions();
        } catch (Exception ex) {
            AlertService.error("Delete auction failed", ex.getMessage());
        }
    }

    @FXML
    private void handleCancelAuction() {
        AuctionSummaryDto selected = sellerAuctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.error("No auction selected", "Hãy chọn một phiên trước khi hủy.");
            return;
        }
        try {
            ClientContext.getInstance().api().cancelAuction(selected.auctionId());
            AlertService.info("Hủy phiên", "Phiên đấu giá đã chuyển sang CANCELED.");
            loadMyAuctions();
        } catch (Exception ex) {
            AlertService.error("Cancel auction failed", ex.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        titleField.clear();
        descriptionArea.clear();
        startPriceField.clear();
        minIncrementField.clear();
        attributeOneField.clear();
        attributeTwoField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        startTimeField.clear();
        endTimeField.clear();
        itemTypeCombo.getSelectionModel().select(ItemType.ELECTRONICS);
    }

    private void fillForm(AuctionSummaryDto auction) {
        if (auction == null) {
            return;
        }
        titleField.setText(auction.title());
        descriptionArea.setText(auction.description());
        startPriceField.setText(auction.startingPrice().toPlainString());
        minIncrementField.setText(auction.minimumIncrement().toPlainString());
        startDatePicker.setValue(auction.startTime().toLocalDate());
        endDatePicker.setValue(auction.endTime().toLocalDate());
        startTimeField.setText(auction.startTime().toLocalTime().toString());
        endTimeField.setText(auction.endTime().toLocalTime().toString());
    }

    private LocalDateTime parseDateTime(String date, String time) {
        return LocalDateTime.of(java.time.LocalDate.parse(date), LocalTime.parse(time));
    }
}
