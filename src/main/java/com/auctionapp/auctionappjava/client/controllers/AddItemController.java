package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.core.ClientContext;
import com.auctionapp.auctionappjava.common.config.AppConstants;
import com.auctionapp.auctionappjava.common.dto.AuctionSummaryDto;
import com.auctionapp.auctionappjava.common.dto.CreateAuctionRequest;
import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.common.factory.AuctionItemFactory;

import com.auctionapp.auctionappjava.common.model.Item;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class AddItemController implements Initializable {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private ComboBox<String> cbCategory;
    @FXML
    private Label lblError;
    @FXML
    private Label lblExtraInfo;
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtEndDate;
    @FXML
    private TextField txtExtraInfo;
    @FXML
    private TextField txtItemName;
    @FXML
    private TextField txtOpenDate;
    @FXML
    private TextField txtStartingPrice;

    private java.util.function.Consumer<Item> onItemAdded;

    public void setOnItemAdded(java.util.function.Consumer<Item> callback) {
        this.onItemAdded = callback;
    }

    @FXML
    void handleAddItem(ActionEvent event) {
        lblError.setText("");

        String name = txtItemName.getText().trim();
        String description = txtDescription.getText().trim();
        String priceText = txtStartingPrice.getText().trim();
        String openDateText = txtOpenDate.getText().trim();
        String endDateText = txtEndDate.getText().trim();
        String category = cbCategory.getValue();
        String extraInfo = txtExtraInfo.getText().trim();

        if (name.isEmpty() || priceText.isEmpty() || openDateText.isEmpty()
                || endDateText.isEmpty() || category == null) {
            lblError.setText("Vui lòng điền đầy đủ thông tin bắt buộc.");
            return;
        }

        BigDecimal startingPrice;

        try {
            startingPrice = new BigDecimal(priceText);
            if (startingPrice.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            lblError.setText("Giá khởi điểm phải là số dương hợp lệ.");
            return;
        }

        LocalDateTime openDate, endDate;
        try {
            openDate = LocalDateTime.parse(openDateText, DATE_FORMATTER);
            endDate = LocalDateTime.parse(endDateText, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            lblError.setText("Định dạng ngày phải là: dd/MM/yyyy HH:mm");
            return;
        }

        if (!endDate.isAfter(openDate)) {
            lblError.setText("Ngày kết thúc phải sau ngày mở.");
            return;
        }

        ItemType itemType = toItemType(category);
        if (itemType == null) {
            lblError.setText("Loại sản phẩm chưa được hỗ trợ: " + category);
            return;
        }

        Item newItem;
        try {
            AuctionSummaryDto summary = ClientContext.getInstance().getApi().createAuction(new CreateAuctionRequest(
                    ClientContext.getInstance().getSession().getUserId(),
                    name,
                    description,
                    startingPrice,
                    AppConstants.DEFAULT_MIN_INCREMENT,
                    openDate,
                    endDate,
                    itemType,
                    extraInfo,
                    ""));
            newItem = AuctionItemFactory.create(
                    summary.itemType(),
                    summary.itemId(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    summary.sellerId(),
                    summary.title(),
                    summary.description(),
                    summary.startingPrice(),
                    extraInfo,
                    "");
        } catch (RuntimeException ex) {
            lblError.setText(ex.getMessage() == null ? "Không thể tạo auction" : ex.getMessage());
            return;
        }

        if (onItemAdded != null) {
            onItemAdded.accept(newItem);
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }


    private ItemType toItemType(String category) {
        return switch (category) {
            case "Nghệ thuật" -> ItemType.ART;
            case "Điện tử" -> ItemType.ELECTRONICS;
            case "Phương tiện" -> ItemType.VEHICLE;
            default -> null;
        };
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Populate category dropdown từ ItemType hoặc danh sách cố định
        cbCategory.getItems().addAll("Nghệ thuật", "Điện tử", "Phương tiện"); // mở rộng theo ItemType

        // Cập nhật nhãn extra info khi đổi loại
        cbCategory.setOnAction(e -> {
            String selected = cbCategory.getValue();
            if (selected == null) return;
            switch (selected) {
                case "Nghệ thuật":
                    lblExtraInfo.setText("Tên nghệ sĩ:");
                    txtExtraInfo.setPromptText("Ví dụ: Nguyễn Văn A");
                    break;
                case "Điện tử":
                    lblExtraInfo.setText("Thương hiệu:");
                    txtExtraInfo.setPromptText("Ví dụ: Samsung");
                    break;
                case "Phương tiện":
                    lblExtraInfo.setText("Thương hiệu:");
                    txtExtraInfo.setPromptText("Ví dụ: Nissan");
                    break;

                default:
                    lblExtraInfo.setText("Thông tin thêm:");
            }
        });
    }

    @FXML
    void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleBack(ActionEvent event) {
        handleCancel(event);
    }
}
