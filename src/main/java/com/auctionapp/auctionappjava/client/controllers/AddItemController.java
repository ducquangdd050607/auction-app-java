package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.AddItemRequest;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.MoneyUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class AddItemController implements Initializable {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private RowConstraints extra1;
    @FXML
    private RowConstraints extra2;
    @FXML
    private ComboBox<String> cbCategory;
    @FXML
    private Label lblError;
    @FXML
    private Label lblExtraInfo1;
    @FXML
    private Label lblExtraInfo2;
    @FXML
    private TextField txtDescription;
    @FXML
    private TextField txtEndDate;
    @FXML
    private TextField txtExtraInfo1;
    @FXML
    private TextField txtExtraInfo2;
    @FXML
    private TextField txtItemName;
    @FXML
    private TextField txtOpenDate;
    @FXML
    private TextField txtStartingPrice;
    @FXML
    private TextField txtMinIncrement;
    @FXML
    private Button btnAddItem;
    @FXML
    private Button btnCancel;

    /*private void toggleExtraRows(boolean show) {
        lblExtraInfo1.setVisible(show);
        lblExtraInfo1.setManaged(show);
        txtExtraInfo1.setVisible(show);
        txtExtraInfo1.setManaged(show);

        lblExtraInfo2.setVisible(show);
        lblExtraInfo2.setManaged(show);
        txtExtraInfo2.setVisible(show);
        txtExtraInfo2.setManaged(show);
    }*/

    private Image alertImage;

    @FXML
    void handleAddItem(ActionEvent event) {
        lblError.setText("");

        String name = txtItemName.getText().trim();
        String description = txtDescription.getText().trim();
        String priceText = txtStartingPrice.getText().trim();
        String openDateText = txtOpenDate.getText().trim();
        String endDateText = txtEndDate.getText().trim();
        String category = cbCategory.getValue();
        String attribute1 = txtExtraInfo1.getText().trim();
        String attribute2 = txtExtraInfo2.getText().trim();
        String minIncrement = txtMinIncrement.getText().trim();

        if (name.isEmpty() || priceText.isEmpty() || openDateText.isEmpty()
                || endDateText.isEmpty() || minIncrement.isEmpty() || category == null) {
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

        LocalDateTime openTime, endTime;
        try {
            openTime = LocalDateTime.parse(openDateText, DATE_FORMATTER);
            endTime = LocalDateTime.parse(endDateText, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            lblError.setText("Định dạng ngày phải là: dd/MM/yyyy HH:mm");
            return;
        }

        if (!endTime.isAfter(openTime)) {
            lblError.setText("Ngày kết thúc phải sau ngày mở.");
            return;
        }

        // Các thuộc tính
        String type = null;
        long duration = ChronoUnit.MINUTES.between(openTime, endTime);

        switch (category) {
            case "Nghệ thuật":
                type = ItemType.ART.name();
                break;
            case "Điện tử":
                type = ItemType.ELECTRONICS.name();
                break;
            case "Phương tiện":
                type = ItemType.VEHICLE.name();
                break;
        }

        AddItemRequest payload = new AddItemRequest(UserSession.getInstance().getCurrentUser().id(), name,
                description, startingPrice, MoneyUtils.purifyingText(minIncrement), type, openTime, endTime, attribute1, attribute2);
        Request addItemReq = new Request("ADD_ITEM", payload);

        ImageView imageView = new ImageView(alertImage);
        imageView.setPreserveRatio(true); // Giữ nguyên tỉ lệ ảnh gốc
        imageView.setFitWidth(500);       // Chỉ cần set chiều rộng, chiều cao sẽ tự nhảy theo

        Runnable mainMethod = () -> { //Test
            // Khóa nút ngay lập tức trước khi gọi mạng
            Platform.runLater(() -> {
                btnAddItem.setDisable(true);
                btnCancel.setDisable(true);
            });
            CompletableFuture.supplyAsync(() -> {
                try {
                    return Client.getInstance().sendRequest(addItemReq);
                } catch (Exception e) {
                    return new Response(false, "Lỗi kết nối máy chủ!", null);
                }
            }).thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response.success()) {
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.close();
                    } else {
                        lblError.setText(response.message());
                        lblError.setVisible(true);
                        lblError.setTextFill(Color.web("#FF8A80"));

                        btnAddItem.setDisable(true);
                        btnCancel.setDisable(true);
                    }
                    btnAddItem.setDisable(false);
                    btnCancel.setDisable(false);
                });
            });
        };


        AlertUtils.SceneOffAlertController(event,
                "Chắc chưa?",
                "Bạn CHẮC muốn Thêm phiên đấu giá này không?",
                "",
                "Thông báo",
                "",
                "Đã thêm thành công!",
                mainMethod,
                imageView);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /*toggleExtraRows(false);*/

        alertImage = new Image(getClass().getResourceAsStream("/com/auctionapp/auctionappjava/images/Mari.jpg"));

        // Populate category dropdown từ ItemType hoặc danh sách cố định
        cbCategory.getItems().addAll("Nghệ thuật", "Điện tử", "Phương tiện"); // mở rộng theo ItemType

        // Cập nhật nhãn extra info khi đổi loại
        cbCategory.setOnAction(e -> {
            String selected = cbCategory.getValue();
            if (selected == null) return;
            switch (selected) {
                case "Nghệ thuật":
                    /*toggleExtraRows(true);*/
                    lblExtraInfo1.setText("Tên nghệ sĩ:");
                    txtExtraInfo1.setPromptText("Ví dụ: Nguyễn Văn A");
                    lblExtraInfo2.setText("Thể loại:");
                    txtExtraInfo2.setPromptText("Ví dụ: tranh vẽ, ảnh chụp,...");
                    break;
                case "Điện tử":
                    lblExtraInfo1.setText("Thương hiệu:");
                    txtExtraInfo1.setPromptText("Ví dụ: Samsung,...");
                    lblExtraInfo2.setText("Loại sản phẩm:");
                    txtExtraInfo2.setPromptText("Ví dụ: smartphone, laptop,...");
                    break;
                case "Phương tiện":
                    lblExtraInfo1.setText("Nhà sản xuất:");
                    txtExtraInfo1.setPromptText("Ví dụ: Nissan,...");
                    lblExtraInfo2.setText("Registration Hint:");  // TODO: chỉnh lại tên
                    txtExtraInfo2.setPromptText("Ví dụ: ...");
                    break;

                default:
                    lblExtraInfo1.setText("Thông tin thêm:");
            }
        });
    }

    @FXML
    void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
