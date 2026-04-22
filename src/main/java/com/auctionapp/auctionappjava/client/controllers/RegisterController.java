package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class RegisterController {

    static boolean isRegister = false;

    @FXML
    private Label lblError;

    @FXML
    private Label lblPrivateKey;

    @FXML
    private PasswordField txtConfirmPassword;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtUsername;

    @FXML
    void handleConfirm(ActionEvent event) throws IOException {

        if (txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty() ||
                txtConfirmPassword.getText().isEmpty() ||  txtEmail.getText().isEmpty()) {
            lblError.setText("Hãy điền đủ thông tin");
            lblError.setVisible(true);
        } else if  (!txtConfirmPassword.getText().equals(txtPassword.getText())) {
            lblError.setText("Mật khẩu không khớp");
            lblError.setVisible(true);
        }

        else {
            isRegister = true;
            // yêu cầu nhập lại thông tin
            SceneSwitcherController.NewSceneController(event, "/com/auctionapp/auctionappjava/views/LoginScreen.fxml", "Đăng nhập");

        }
    }

    @FXML
    void handleLogIn(ActionEvent event) throws IOException {

        SceneSwitcherController.NewSceneController(event, "/com/auctionapp/auctionappjava/views/LoginScreen.fxml", "Đăng nhập");

    }
}
