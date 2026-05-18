package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.LoginRequest;
import com.auctionapp.auctionappjava.common.dto.LoginResponse;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class LoginController implements Initializable {

    static boolean bidderRoute = false;
    static boolean sellerRoute = false;
    static boolean adminRoute = false;

    @FXML
    private Label lblError;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtUsername;
    @FXML
    private Button btnLogin;
    @FXML
    private Hyperlink hplRegister;

    @FXML
    void handleConfirm(ActionEvent event) {
        if (txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()) {
            lblError.setText("Hãy điền đủ thông tin");
            lblError.setVisible(true);
            lblError.setTextFill(Color.web("#FF8A80"));
        } else {
            btnLogin.setDisable(true); // Khóa nút bấm, chống trường hợp nhồi c*t vào server
            txtUsername.setDisable(true);
            txtPassword.setDisable(true);
            hplRegister.setDisable(true);

            String user = txtUsername.getText();
            String pass = txtPassword.getText();

            // 1. Gói dữ liệu và tạo Request
            LoginRequest payload = new LoginRequest(user, pass);
            Request loginReq = new Request("LOGIN", payload);

            // 2. Gửi qua mạng ngầm
            CompletableFuture.supplyAsync(() -> {
                try {
                    // Gọi Singleton Client của bạn
                    return Client.getInstance().sendRequest(loginReq);
                } catch (Exception e) {
                    return new Response(false, "Lỗi kết nối máy chủ!", null);
                }
            }).thenAccept(response -> {
                // 3. Trở lại luồng UI để vẽ giao diện
                Platform.runLater(() -> {
                    if (response.success()) {
                        // Móc DTO ra xem Role là gì
                        LoginResponse authUser = (LoginResponse) response.data();
                        // Thêm thông tin user cho session đó
                        UserSession.getInstance().setCurrentUser(authUser);

                        if ("ADMIN".equals(authUser.role())) {
                            try {
                                adminRoute = true;
                                sellerRoute = false;
                                bidderRoute = false;
                                SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/NavigatorButtons.fxml", "Bíd88");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else if ("SELLER".equals(authUser.role())) {
                            try {
                                adminRoute = false;
                                sellerRoute = true;
                                bidderRoute = false;
                                SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/NavigatorButtons.fxml", "Bíd88");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else if ("BIDDER".equals(authUser.role())){
                            try {
                                adminRoute = false;
                                sellerRoute = false;
                                bidderRoute = true;
                                SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/NavigatorButtons.fxml", "Bíd88");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        btnLogin.setDisable(false);
                        txtUsername.setDisable(false);
                        txtPassword.setDisable(false);
                        hplRegister.setDisable(false);

                        // Báo lỗi
                        lblError.setText(response.message());
                        lblError.setVisible(true);
                        lblError.setTextFill(Color.web("#FF8A80"));
                    }
                });
            });
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
