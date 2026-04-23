package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
            AlertController.SceneOffAlertController(event,
                    "Chắc chưa?",
                    "Bạn có muốn đổi mật khẩu không?",
                    "",
                    "Thông báo",
                    "",
                    "Đã thay đổi mật khẩu thành công!");

        }
    }
}
