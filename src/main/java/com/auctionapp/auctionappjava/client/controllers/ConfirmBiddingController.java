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


public class ConfirmBiddingController {

    private long defaultAmount = 0;


    @FXML
    private Label lblBalance;

    @FXML
    private Label lblError;

    @FXML
    private TextField txtSetPrice;

    @FXML
    private Button btnMore;

    @FXML
    public void initialize() {
        btnMore.setManaged(false);
        settingMoneyFormat(txtSetPrice);
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        SceneSwitcherController.NewSceneController(event, "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml", "pretendtobeatitle");

    }

    @FXML
    void handlePreset(ActionEvent event) {

        Button clickedButton = (Button) event.getSource();
        String presetValue = clickedButton.getText();

        long amount = purifyingText(presetValue);

        if (txtSetPrice.getText().isEmpty()) {
            txtSetPrice.setText(String.valueOf(amount));
            defaultAmount = amount;

        } else {
            defaultAmount += amount;
            txtSetPrice.setText(String.valueOf(defaultAmount));
        }
    }


    @FXML
    void handleTrueConfirm(ActionEvent event) {
        if (txtSetPrice.getText().isEmpty()) {
            lblError.setText("Hãy nhập giá tiền cược.");
            lblError.setTextFill(Color.web("#FF8A80"));


        } else if (purifyingText(lblBalance.getText()) < purifyingText(txtSetPrice.getText())) {
            lblError.setText("Không đủ tiền trong số dư!");
            lblError.setTextFill(Color.web("#FF8A80"));
            btnMore.setManaged(true);
            btnMore.setVisible(true);
        }

//            elif (amount < minIncrement) {
//                lblError.setText("Vui lòng nhiều hơn mức " + minIncrement + ".");
//                lblError.setVisible(true);
//                lblError.setTextFill(Color.web("#FF8A80"));
//            }
        else {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Chắc chưa?");
            alert.setHeaderText("Bạn CHẮC muốn NẠP TIỀN không?");
            alert.setContentText("Một khi vào, không thể rứt ra:>>");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {

                    // TODO: Nơi bạn gọi Service/DAO để trừ tiền vào Database
                    // TODO: Cập nhật sàn đấu giá cho người đặt cược mới

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


    @FXML
    void handleWallet(ActionEvent event) throws IOException {

        SceneSwitcherController.PopupController(event,"/com/auctionapp/auctionappjava/views/DepositScreen.fxml", "moneymoneymoney");

    }
}
