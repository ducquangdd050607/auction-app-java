package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private Label lblError;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtUsername;

    @FXML
    void handleConfirm(ActionEvent event) throws IOException {
        if (txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()) {
            lblError.setText("Hãy điền đủ thông tin");
            lblError.setVisible(true);
            lblError.setTextFill(Color.web("#FF8A80"));
        } else {

            //TODO: Kiểm tra DB cho tài khoản

            SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/RouteScreen.fxml", "Vai trò");
        }
    }

    @FXML
    void handleRegister(ActionEvent event) throws IOException {
        SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/RegisterScreen.fxml", "Đăng kí tài khoản");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateRegisterStatus(RegisterController.isRegister);
    }

    @FXML
    public void updateRegisterStatus(boolean isRegister) {
        if (isRegister) {
            lblError.setText("Đăng kí thành công, hãy nhập lại tài khoản.");
            lblError.setVisible(true);
            lblError.setTextFill(Color.web("#d5ffda"));
        }
    }
}
