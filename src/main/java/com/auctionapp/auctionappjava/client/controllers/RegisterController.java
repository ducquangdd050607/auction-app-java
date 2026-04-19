package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    private Stage stage;
    private Parent root;
    private Scene scene;
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
    private TextField txtPrivateKey;//? --- :D?

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
            root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/LoginScreen.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        }
    }

    @FXML
    void handleLogIn(ActionEvent event) throws IOException {

        root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/LoginScreen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

}
