package com.auctionhub.client.controller;

import com.auctionhub.client.core.ClientContext;
import com.auctionhub.client.service.AlertService;
import com.auctionhub.client.service.SceneNavigator;
import com.auctionhub.client.session.ClientSession;
import com.auctionhub.common.dto.AuthUserDto;
import com.auctionhub.common.dto.LoginRequest;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {
        loadingIndicator.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        setLoading(true, "Đang đăng nhập...");
        Task<AuthUserDto> task = new Task<>() {
            @Override
            protected AuthUserDto call() {
                return ClientContext.getInstance().api().login(new LoginRequest(usernameField.getText(), passwordField.getText()));
            }
        };
        task.setOnSucceeded(event -> {
            ClientSession.getInstance().login(task.getValue());
            setLoading(false, "Đăng nhập thành công.");
            SceneNavigator.getInstance().showShell();
        });
        task.setOnFailed(event -> {
            setLoading(false, "Đăng nhập thất bại.");
            AlertService.error("Login failed", task.getException().getCause() == null ? task.getException().getMessage() : task.getException().getCause().getMessage());
        });
        new Thread(task, "login-task").start();
    }

    @FXML
    private void openRegister() {
        SceneNavigator.getInstance().showRegister();
    }

    private void setLoading(boolean loading, String message) {
        loadingIndicator.setVisible(loading);
        statusLabel.setText(message);
    }
}
