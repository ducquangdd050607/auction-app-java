package com.auctionapp.auctionappjava.client.controllers;


// Guaranteed Po*p Controller:>>


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import java.io.IOException;

public class DepositController {

    @FXML
    private Label lblCurrentBalance;

    @FXML
    private Label lblMessage;

    @FXML
    private TextField txtAmount;


    @FXML
    public void initialize() {
        // Ép hệ thống dùng chuẩn Mỹ để chắc chắn hàng nghìn được ngăn cách bằng dấu phẩy (,)
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###");

        // Lắng nghe MỌI SỰ THAY ĐỔI từng phím gõ vào ô txtAmount
        txtAmount.textProperty().addListener((observable, oldValue, newValue) -> {

            // Nếu xóa trắng ô thì không làm gì cả
            if (newValue == null || newValue.isEmpty()) {
                return;
            }

            // 1. Tẩy sạch mọi ký tự không phải là số (Chặn luôn người dùng gõ chữ)
            String cleanInput = newValue.replaceAll("[^\\d]", "");

            try {
                if (cleanInput.isEmpty()) {
                    txtAmount.setText("");
                    return;
                }

                // 2. Ép sang số và định dạng (VD: 100000 -> "100,000")
                long amount = Long.parseLong(cleanInput);
                String formattedStr = formatter.format(amount);

                // 3. Chuẩn hóa sang kiểu Việt Nam (Đổi dấu phẩy thành dấu chấm -> "100.000")
                formattedStr = formattedStr.replaceAll(",", ".");

                // 4. CHỐNG VÒNG LẶP VÔ TẬN: Chỉ cập nhật khi chuỗi mới thực sự khác
                if (!newValue.equals(formattedStr)) {

                    // 5. CHỐNG NHẢY CON TRỎ CHUỘT: Tính toán độ chênh lệch chiều dài chuỗi
                    int cursorPosition = txtAmount.getCaretPosition();
                    int lengthDiff = formattedStr.length() - newValue.length();

                    // Ghi đè text mới lên giao diện
                    txtAmount.setText(formattedStr);

                    // Đẩy con trỏ chuột về đúng vị trí nó đang đứng
                    txtAmount.positionCaret(cursorPosition + lengthDiff);
                }

            } catch (NumberFormatException e) {
                // TODO: xử lí exception số quá lớn
                // Nếu người dùng cố tình dán (paste) một số quá lớn vượt ngưỡng long
                // Ép quay về giá trị cũ trước khi dán
                txtAmount.setText(oldValue);
            }
        });
    }

    @FXML
    void handleDeposit(ActionEvent event) throws IOException {
        // TODO: Viết logic nhận tiền vào database ở đây
        String rawInput = txtAmount.getText();

        // Kiểm tra rỗng
        if (rawInput == null || rawInput.trim().isEmpty()) {
            lblMessage.setText("Vui lòng nhập hoặc chọn số tiền cần nạp!");
            lblMessage.setVisible(true);
            lblMessage.setTextFill(Color.web("#FF8A80"));
        } else {
            // Người dùng có thể nhập "500.000" hoặc "500000". Ta phải xóa hết dấu chấm/phẩy đi
            String cleanInput = rawInput.replace(".", "").replace(",", "");

            // Ép kiểu sang số nguyên lớn (Dùng tiền tệ VNĐ thì nên dùng long thay vì int)
            long amount = Long.parseLong(cleanInput);

            // Kiểm tra logic nghiệp vụ
            if (amount < 10000) {
                lblMessage.setText("Vui lòng nạp tối thiểu 10.000 đ!");
                lblMessage.setVisible(true);
                lblMessage.setTextFill(Color.web("#FF8A80"));
            } else {
                // TODO: Nơi bạn gọi Service/DAO để cộng tiền vào Database

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Chắc chưa?");
                alert.setHeaderText("Bạn CHẮC muốn NẠP TIỀN không?");
                alert.setContentText("Một khi vào, không thể rứt ra:>>");

                alert.showAndWait().ifPresent(response -> {

                    if (response == ButtonType.OK) {

                        alert.close();
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Thông báo");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("Đã nạp thành công!");
                        successAlert.showAndWait();

                        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        currentStage.close();

                    } else {
                        alert.close();
                    }
                });
            }
        }
    }

    @FXML
    void handlePreset(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String presetValue = clickedButton.getText();
        txtAmount.setText(presetValue);
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }
}
