package com.auctionapp.auctionappjava.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class ChangePasswordController {

    private Stage stage;
    private Parent root;
    private Scene scene;

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
            // Xong việc thì tự động đóng cửa sổ modal này lại
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
        }
    }
}
