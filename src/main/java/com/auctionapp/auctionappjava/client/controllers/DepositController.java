package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.DepositRequest;
import com.auctionapp.auctionappjava.common.dto.LoginResponse;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.purifyingText;
import static com.auctionapp.auctionappjava.common.util.MoneyUtils.settingMoneyFormat;

public class DepositController {

    @FXML
    private Label lblCurrentBalance;

    @FXML
    private Label lblMessage;

    @FXML
    private TextField txtAmount;


    @FXML
    public void initialize() {
        settingMoneyFormat(txtAmount);
        lblCurrentBalance.setText(UserSession.getInstance().getCurrentUser().walletBalance().toPlainString() + " VND");
    }

    @FXML
    void handleDeposit(ActionEvent event) {
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
                Runnable depositMethod = () -> {
                    DepositRequest payload = new DepositRequest(UserSession.getInstance().getCurrentUser().id(), purifyingText(txtAmount.getText()));
                    Request depositRequest = new Request("DEPOSIT", payload);
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return Client.getInstance().sendRequest(depositRequest);
                        } catch (Exception e) {
                            return new Response(false, "Lỗi kết nối máy chủ!", null);
                        }
                    }).thenAccept(response -> {
                        Platform.runLater(() -> {
                            if (response.success()) {
                                // Cập nhật lại UserSession
                                LoginResponse oldUser = UserSession.getInstance().getCurrentUser();
                                LoginResponse updatedUser = new LoginResponse(
                                        oldUser.id(),
                                        oldUser.username(),
                                        oldUser.fullName(),
                                        oldUser.role(),
                                        oldUser.email(),
                                        oldUser.walletBalance().add(amount)   // Cộng thêm tiền nạp vào
                                );
                                UserSession.getInstance().setCurrentUser(updatedUser);
                            } else {
                                lblMessage.setText(response.message());
                                lblMessage.setVisible(true);
                                lblMessage.setTextFill(Color.web("#FF8A80"));
                            }
                        });
                    });;
                };

                AlertUtils.SceneOffAlertController(event,
                        "Chắc chưa?",
                        "Bạn CHẮC muốn NẠP TIỀN không?",
                        "Một khi vào, không thể rứt ra:>>",
                        "Thông báo",
                        "Đã nạp thành công!",
                        "Happy Gambling!",
                        depositMethod,
                        null);
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
    void handleBack(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }
}
