package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.common.dto.RegisterRequest;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class RegisterController {

    static boolean isRegister = false;
    static String roles = "";

    @FXML
    private Button btnBid;

    @FXML
    private Button btnConfirm;

    @FXML
    private Button btnSell;

    @FXML
    private Label confirmRoute;

    @FXML
    private Label lblError;

    @FXML
    private PasswordField txtConfirmPassword;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtFullname;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtUsername;

    @FXML
    private Hyperlink hplLogin;

    @FXML
    void handleSeller(ActionEvent event) {
        roles = "SELLER";
        confirmRoute.setText("Bạn chọn là Seller");
        System.out.println(roles); //Debug
    }

    @FXML
    void handleBidder(ActionEvent event) {
        roles = "BIDDER";
        confirmRoute.setText("Bạn chọn là Bidder");
        System.out.println(roles);
    }

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

        else if (roles.isEmpty()) {
            lblError.setText("Vui lòng chọn vai trò");
            lblError.setVisible(true);
        }

        else {
            btnConfirm.setDisable(true);
            txtFullname.setDisable(true);
            txtUsername.setDisable(true);
            txtPassword.setDisable(true);
            txtConfirmPassword.setDisable(true);
            txtEmail.setDisable(true);
            btnBid.setDisable(true);
            btnSell.setDisable(true);
            hplLogin.setDisable(true);

            String user = txtUsername.getText();
            String pass = txtPassword.getText();
            String name = txtFullname.getText();
            String mail = txtEmail.getText();
            String role = roles;

             // TODO: Thêm 1 ComboBox chọn vai trò - nah good ol' buttons'll work.

            // Đóng gói Request
            RegisterRequest payload = new RegisterRequest(user, pass, name, mail, role);
            Request request = new Request("REGISTER", payload);

            // Gửi qua mạng ngầm
            CompletableFuture.supplyAsync(() -> {
                try {
                    return Client.getInstance().sendRequest(request);
                } catch (Exception e) {
                    return new Response(false, "Lỗi kết nối", null);
                }
            }).thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response.success()) {
                        // yêu cầu nhập lại thông tin
                        try {
                            isRegister = true;
                            SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/LoginScreen.fxml", "Đăng nhập");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        btnConfirm.setDisable(false);
                        txtFullname.setDisable(false);
                        txtUsername.setDisable(false);
                        txtPassword.setDisable(false);
                        txtConfirmPassword.setDisable(false);
                        txtEmail.setDisable(false);
                        btnBid.setDisable(false);
                        btnSell.setDisable(false);
                        hplLogin.setDisable(false);

                        lblError.setText(response.message());
                        lblError.setVisible(true);
                    }
                });
            });
        }
    }

    @FXML
    void handleLogIn(ActionEvent event) throws IOException {

        SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/LoginScreen.fxml", "Đăng nhập");

    }
}
