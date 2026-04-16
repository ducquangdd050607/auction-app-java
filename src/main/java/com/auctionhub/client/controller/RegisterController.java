package com.auctionhub.client.controller;

import com.auctionhub.client.core.ClientContext;
import com.auctionhub.client.service.AlertService;
import com.auctionhub.client.service.SceneNavigator;
import com.auctionhub.common.dto.RegisterRequest;
import com.auctionhub.common.enums.Role;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

public class RegisterController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private ComboBox<Role> roleComboBox;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(Role.BIDDER, Role.SELLER));
        roleComboBox.getSelectionModel().select(Role.BIDDER);
        loadingIndicator.setVisible(false);
    }

    @FXML
    private void handleRegister() {
        setLoading(true, "Đang tạo tài khoản...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                ClientContext.getInstance().api().register(new RegisterRequest(
                        usernameField.getText(),
                        passwordField.getText(),
                        confirmPasswordField.getText(),
                        fullNameField.getText(),
                        emailField.getText(),
                        roleComboBox.getValue()));
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            setLoading(false, "Đăng ký thành công.");
            AlertService.info("Đăng ký thành công", "Tài khoản đã được tạo. Hãy dùng tài khoản mới để đăng nhập.");
            SceneNavigator.getInstance().showLogin();
        });
        task.setOnFailed(event -> {
            setLoading(false, "Đăng ký thất bại.");
            AlertService.error("Register failed", task.getException().getCause() == null ? task.getException().getMessage() : task.getException().getCause().getMessage());
        });
        new Thread(task, "register-task").start();
    }

    @FXML
    private void backToLogin() {
        SceneNavigator.getInstance().showLogin();
    }

    private void setLoading(boolean loading, String message) {
        loadingIndicator.setVisible(loading);
        statusLabel.setText(message);
    }
}
