package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.math.BigDecimal;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.purifyingText;
import static com.auctionapp.auctionappjava.common.util.MoneyUtils.settingMoneyFormat;


public class ConfirmBiddingController {

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
        SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml", "pretendtobeatitle");

    }

    @FXML
    void handlePreset(ActionEvent event) {

        Button clickedButton = (Button) event.getSource();
        String presetValue = clickedButton.getText();

        BigDecimal amount = purifyingText(presetValue);

        if (txtSetPrice.getText().isEmpty()) {
            txtSetPrice.setText(String.valueOf(amount));

        } else {
            txtSetPrice.setText(String.valueOf(purifyingText(txtSetPrice.getText()).add(amount)));

        }
    }

    @FXML
    void handleTrueConfirm(ActionEvent event) {
        if (txtSetPrice.getText().isEmpty()) {
            lblError.setText("Hãy nhập giá tiền cược.");
            lblError.setTextFill(Color.web("#FF8A80"));

        } else if (((purifyingText(lblBalance.getText()).subtract(purifyingText(txtSetPrice.getText()))).compareTo(new BigDecimal(0))) < 0) {
            //:((((((

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

            // TODO: Nơi bạn gọi Service/DAO để trừ tiền vào Database
            // TODO: Cập nhật sàn đấu giá cho người đặt cược mới

            Runnable pseudoMethod = () -> { //Test
                System.out.println("PseudoMethod");
            };


            AlertUtils.SceneOffAlertController(event,
                    "Chắc chưa?",
                    "Bạn CHẮC muốn ĐặK CưỢk không?",
                    "",
                    "Thông báo",
                    "",
                    "Đã thay đổi ĐặK CưỢk thành công!",
                    pseudoMethod);

        }
    }


    @FXML
    void handleWallet(ActionEvent event) throws IOException {

        SceneSwitcherUtils.PopupController(event,"/com/auctionapp/auctionappjava/views/DepositScreen.fxml", "moneymoneymoney");

    }
}
