package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.core.ClientContext;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

public class ConfirmBiddingController {

    private long defaultAmount = 0;
    private static UUID selectedAuctionId;

    public static void setSelectedAuctionId(UUID auctionId) {
        selectedAuctionId = auctionId;
    }


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
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###");

        txtSetPrice.textProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue == null || newValue.isEmpty()) {
                return;
            }

            String cleanInput = newValue.replaceAll("[^\\d]", "");

            try {
                if (cleanInput.isEmpty()) {
                    txtSetPrice.setText("");
                    return;
                }

                long amount = Long.parseLong(cleanInput);
                String formattedStr = formatter.format(amount);

                formattedStr = formattedStr.replaceAll(",", ".");

                if (!newValue.equals(formattedStr)) {

                    int cursorPosition = txtSetPrice.getCaretPosition();
                    int lengthDiff = formattedStr.length() - newValue.length();

                    txtSetPrice.setText(formattedStr);

                    txtSetPrice.positionCaret(cursorPosition + lengthDiff);
                }

            } catch (NumberFormatException e) {
                txtSetPrice.setText(oldValue);
            }
        });
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
            btnMore.setManaged(false);
            btnMore.setVisible(false);
            return;
        }

        long bidAmount = purifyingText(txtSetPrice.getText());
        if (lblBalance.getText() != null && !lblBalance.getText().isBlank() && purifyingText(lblBalance.getText()) < bidAmount) {
            lblError.setText("Không đủ tiền trong số dư!");
            lblError.setTextFill(Color.web("#FF8A80"));
            btnMore.setManaged(true);
            btnMore.setVisible(true);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Chắc chưa?");
        alert.setHeaderText("Bạn CHẮC muốn ĐặC KưỢk không?");

        alert.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                alert.close();
                try {
                    if (selectedAuctionId == null) {
                        throw new IllegalStateException("Chưa chọn phiên đấu giá từ danh sách");
                    }
                    ClientContext.getInstance().getApi().placeBid(selectedAuctionId, BigDecimal.valueOf(bidAmount));
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Thông báo");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Đã đặt cược xong!");
                    successAlert.showAndWait();

                    Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    currentStage.close();
                } catch (RuntimeException ex) {
                    lblError.setText(ex.getMessage() == null ? "Không thể đặt giá" : ex.getMessage());
                    lblError.setTextFill(Color.web("#FF8A80"));
                    lblError.setVisible(true);
                }

            } else {
                alert.close();
            }
        });
    }

    @FXML
    void handleWallet(ActionEvent event) throws IOException {

        SceneSwitcherController.PopupController(event,"/com/auctionapp/auctionappjava/views/DepositScreen.fxml", "moneymoneymoney");

    }
    long purifyingText(String price) {
        return Long.parseLong(price.replaceAll("[^\\d]", ""));
    }
}
