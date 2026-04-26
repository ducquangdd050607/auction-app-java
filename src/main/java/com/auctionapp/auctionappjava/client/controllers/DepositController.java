package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.common.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;

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
            BigDecimal amount = purifyingText(rawInput);

            // Kiểm tra logic nghiệp vụ
            // compareTo trả về -1, 0, 1 theo thứ tự: Bé, bằng, lớn(cần cải thiện)
            if (amount.compareTo(new BigDecimal("10000")) < 0) {
                lblMessage.setText("Vui lòng nạp tối thiểu 10.000 đ!");
                lblMessage.setVisible(true);
                lblMessage.setTextFill(Color.web("#FF8A80"));
            } else {
                // TODO: Nơi bạn gọi Service/DAO để cộng tiền vào Database

                Runnable pseudoMethod = () -> { //Test
                    System.out.println("PseudoMethod");
                };


                AlertUtil.SceneOffAlertController(event,
                        "Chắc chưa?",
                        "Bạn CHẮC muốn NẠP TIỀN không?",
                        "Một khi vào, không thể rứt ra:>>",
                        "Thông báo",
                        "Đã nạp thành công!",
                        "Happy Gambling!",
                        pseudoMethod);
            }
        }
    }

    @FXML
    void handlePreset(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String presetValue = clickedButton.getText();

        BigDecimal amount = purifyingText(presetValue);

        if (txtAmount.getText().isEmpty()) {
            txtAmount.setText(String.valueOf(amount));

        } else {
            txtAmount.setText(String.valueOf(purifyingText(txtAmount.getText()).add(amount)));

        }
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }
}
