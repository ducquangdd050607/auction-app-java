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

    private Stage stage;
    private Parent root;
    private Scene scene;
    static boolean isLoggedIn = false;

    @FXML
    private Label lblError;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtUsername;
    @FXML
    private Button btnAdConfirm;

    @FXML
    void handleConfirm(ActionEvent event) throws IOException {
        if (txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()) {
            lblError.setText("Hãy điền đủ thông tin");
            lblError.setVisible(true);
            lblError.setTextFill(Color.web("#FF8A80"));
        } else {
            isLoggedIn = true;
            root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/Route.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        }
    }

    @FXML
    void handleRegister(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/RegisterScreen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
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
