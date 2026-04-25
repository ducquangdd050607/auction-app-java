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
        } // else if Trùng username {
            // lblError.setText("Tên người dùng đã được dùng");
            // lblError.setVisible(true);

        try {

            ValidationUtils.requireEmail(txtEmail.getText());
            isRegister = true;

            User bidder = new Bidder();
            User seller = new Seller();

            seller.setUsername(txtUsername.getText());
            seller.setEmail(txtEmail.getText());
            seller.setPasswordHash(txtPassword.getText()); //placeholder


            bidder.setUsername(txtUsername.getText());
            bidder.setEmail(txtEmail.getText());
            seller.setPasswordHash(txtPassword.getText()); //placeholder

            //TODO: Cập nhật cách lấy Hash và Salt cho password

            //TODO: Nối lên DataBase lưu dữ liệu người dùng

            // yêu cầu nhập lại thông tin
            SceneSwitcherController.NewSceneController(event, "/com/auctionapp/auctionappjava/views/LoginScreen.fxml", "Đăng nhập");

        }
    }

    @FXML
    void handleLogIn(ActionEvent event) throws IOException {

        SceneSwitcherController.NewSceneController(event, "/com/auctionapp/auctionappjava/views/LoginScreen.fxml", "Đăng nhập");

    }
}
