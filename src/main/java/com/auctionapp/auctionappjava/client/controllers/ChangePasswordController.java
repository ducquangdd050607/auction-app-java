package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ChangePasswordController {

    @FXML
    private Label lblMessage;

    @FXML
    private PasswordField txtConfirmPassword;

    @FXML
    private PasswordField txtNewPassword;

    @FXML
    void handleBack(ActionEvent event) {
        // Đóng stage lại khi back về nav
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }

    @FXML
    void handleConfirm(ActionEvent event) {
        if (txtNewPassword.getText().isEmpty() || txtConfirmPassword.getText().isEmpty()) {
            lblMessage.setText("Hãy điền đủ mật khẩu mới và xác nhận");
            lblMessage.setVisible(true);
            lblMessage.setTextFill(Color.web("#FF8A80"));
        } else if (!txtConfirmPassword.getText().equals(txtNewPassword.getText())) {
            lblMessage.setText("Mật khẩu mới không khớp nhau");
            lblMessage.setVisible(true);
            lblMessage.setTextFill(Color.web("#FF8A80"));
        }
        else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Chắc chưa?");
            alert.setHeaderText("Bạn có muốn đổi mật khẩu không?");

            alert.showAndWait().ifPresent(response -> {

                if (response == ButtonType.OK) {

                    alert.close();
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Thông báo");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Đã thay đổi mật khẩu thành công!");
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
