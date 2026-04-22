package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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
            SceneSwitcherController.NewSceneController(event, "/com/auctionapp/auctionappjava/views/Route.fxml", "Vai trò");
        }
    }

    @FXML
    void handleRegister(ActionEvent event) throws IOException {
        SceneSwitcherController.NewSceneController(event, "/com/auctionapp/auctionappjava/views/RegisterScreen.fxml", "Đăng kí tài khoản");
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
