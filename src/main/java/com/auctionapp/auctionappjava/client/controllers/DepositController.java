package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.purifyingText;
import static com.auctionapp.auctionappjava.common.util.MoneyUtils.settingMoneyFormat;

public class DepositController {

    private long defaultAmount = 0;

    @FXML
    private Label lblCurrentBalance;

    @FXML
    private Label lblMessage;

    @FXML
    private TextField txtAmount;


    @FXML
    public void initialize() {

        settingMoneyFormat(txtAmount);

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
            // Thay bằng purifyingText
            long amount = purifyingText(rawInput);

            // Kiểm tra logic nghiệp vụ
            if (amount < 10000) {
                lblMessage.setText("Vui lòng nạp tối thiểu 10.000 đ!");
                lblMessage.setVisible(true);
                lblMessage.setTextFill(Color.web("#FF8A80"));
            } else {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Chắc chưa?");
                alert.setHeaderText("Bạn CHẮC muốn NẠP TIỀN không?");
                alert.setContentText("Một khi vào, không thể rứt ra:>>");

                alert.showAndWait().ifPresent(response -> {

                    if (response == ButtonType.OK) {

                        // TODO: Nơi bạn gọi Service/DAO để cộng tiền vào Database

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

        long amount = purifyingText(presetValue);

        if (txtAmount.getText().isEmpty()) {
            txtAmount.setText(String.valueOf(amount));
            defaultAmount = amount;

        } else {
            defaultAmount += amount;
            txtAmount.setText(String.valueOf(defaultAmount));
        }
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }
}
