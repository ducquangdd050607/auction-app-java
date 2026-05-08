package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.math.BigDecimal;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.purifyingText;
import static com.auctionapp.auctionappjava.common.util.MoneyUtils.settingMoneyFormat;

public class ConfirmBiddingController {

    public static boolean isAutoBidding = false;

    @FXML
    private Button btnMore;

    @FXML
    private CheckBox chboxAutoBidding;

    @FXML
    private Label lblBalance;

    @FXML
    private Label lblError;

    @FXML
    private HBox boxAutoBidding;

    @FXML
    private Label lblBest;

    @FXML
    private Label lblMinIncrement;

    @FXML
    private TextField txtSetAuto;

    @FXML
    private TextField txtSetPrice;

    @FXML
    void handleAutoBidding(ActionEvent event) {
        boxAutoBidding.setVisible(chboxAutoBidding.isSelected());
        boxAutoBidding.setManaged(chboxAutoBidding.isSelected());
        isAutoBidding = chboxAutoBidding.isSelected();
    }

    @FXML
    public void initialize() {
        btnMore.setManaged(false);
        settingMoneyFormat(txtSetPrice);
        settingMoneyFormat(txtSetAuto);
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml", "Thông tin sản phẩm");
        isAutoBidding = false;
        // TODO: SỬA LẠI CÁCH BACK HOẠT ĐỘNG

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

        btnMore.setManaged(false);
        btnMore.setVisible(false);

        if (txtSetPrice.getText().isEmpty()) {
            lblError.setText("Hãy nhập giá tiền cược.");
            lblError.setTextFill(Color.web("#FF8A80"));

        } else if ((txtSetAuto.getText().isEmpty()) & (isAutoBidding)) {
            lblError.setText("Hãy nhập giá tiền tự đặt cược.");
            lblError.setTextFill(Color.web("#FF8A80"));

        } else if ((((purifyingText(txtSetAuto.getText()).subtract(purifyingText(lblMinIncrement.getText())))
                .compareTo(new BigDecimal(0))) < 0) & (isAutoBidding)) {
            lblError.setText("Giá tiền tự đặt cược đang nhỏ hơn bước đặt");
            lblError.setTextFill(Color.web("#FF8A80"));

        } else if (((purifyingText(lblBest.getText()).subtract(purifyingText(txtSetPrice.getText())))
                .compareTo(new BigDecimal(0))) > 0) {
            lblError.setText("Tiền cược đang nhỏ hơn hiện tại!");
            lblError.setTextFill(Color.web("#FF8A80"));

        } else if (((purifyingText(lblBalance.getText()).subtract(purifyingText(txtSetPrice.getText())))
                .compareTo(new BigDecimal(0))) < 0) {

            lblError.setText("Không đủ tiền trong số dư!");
            lblError.setTextFill(Color.web("#FF8A80"));
            btnMore.setManaged(true);
            btnMore.setVisible(true);

        } else if ((((purifyingText(txtSetPrice.getText())).subtract(purifyingText(lblBest.getText())))
                // Lấy giá đặt trừ giá hiện tại
                .compareTo(purifyingText(lblMinIncrement.getText()))) < 0) {
                // So sánh MinIncre

            lblError.setText("Vui lòng nhiều hơn mức " + lblMinIncrement.getText() + ".");
            lblError.setVisible(true);
            lblError.setTextFill(Color.web("#FF8A80"));
            }

        else {

            // TODO: Nơi bạn gọi Service/DAO để trừ tiền vào Database
            // TODO: Cập nhật sàn đấu giá cho người đặt cược mới
            // TODO: Nếu có chọn Auto-Bidding, lưu lại lựa chọn và tiền trong box

            Image image = new Image(getClass().getResourceAsStream("/com/auctionapp/auctionappjava/images/Mari.jpg"));
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true); // Giữ nguyên tỉ lệ ảnh gốc
            imageView.setFitWidth(500);       // Chỉ cần set chiều rộng, chiều cao sẽ tự nhảy theo

            Runnable pseudoMethod = () -> { //Test
                System.out.println("PseudoMethod");
            };


            AlertUtils.SceneOffAlertController(event,
                    "Chắc chưa?",
                    "Bạn CHẮC muốn ĐặK CưỢk không?",
                    "",
                    "Thông báo",
                    "",
                    "Đã ĐặK CưỢk thành công!",
                    pseudoMethod,
                    imageView);

        }
    }


    @FXML
    void handleWallet(ActionEvent event) throws IOException {

        SceneSwitcherUtils.PopupController(event,"/com/auctionapp/auctionappjava/views/DepositScreen.fxml", "moneymoneymoney");

    }
}
