package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.common.model.ArtItem;
import com.auctionapp.auctionappjava.common.model.ElectronicsItem;
import com.auctionapp.auctionappjava.common.model.Item;
import com.auctionapp.auctionappjava.common.model.VehicleItem;
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
import java.util.UUID;

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

        // --- Tạo Item theo loại ---
        // UUID dùng random
        UUID sellerId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Item newItem;
        switch (category) {
            case "Nghệ thuật":
                newItem = new ArtItem(id, now, now, sellerId,
                        name, description, startingPrice,
                        extraInfo,   // artist
                        "");         // medium – có thể thêm field sau
                break;
            case "Điện tử":
                newItem = new ElectronicsItem(id, now, now, sellerId,
                        name, description, startingPrice,
                        extraInfo,   // brand
                        "");         // model
                break;
            case "Phương tiện":
                newItem = new VehicleItem(id, now, now, sellerId,
                        name, description, startingPrice,
                        extraInfo,   // brand
                        "");         // regist-cáijđấy
                break;


            default:
                // Tạo lỗi Item lạ
                lblError.setText("Loại sản phẩm chưa được hỗ trợ: " + category);
                return;
        }

        if (onItemAdded != null) {
            onItemAdded.accept(newItem);
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
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
