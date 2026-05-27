package com.auctionapp.auctionappjava.client.controllers;

import static com.auctionapp.auctionappjava.common.util.ValidationUtils.*;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.common.dto.RegisterRequest;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.exception.AppException;
import com.auctionapp.auctionappjava.common.exception.ValidationException;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

  static boolean isRegister = false;
  static String roles = null;

  @FXML private Button btnBid;

  @FXML private Button btnConfirm;

  @FXML private Button btnSell;

  @FXML private Label confirmRoute;

  @FXML private Label lblError;

  @FXML private PasswordField txtConfirmPassword;

  @FXML private TextField txtEmail;

  @FXML private TextField txtFullname;

  @FXML private PasswordField txtPassword;

  @FXML private TextField txtUsername;

  @FXML private Hyperlink hplLogin;

  @FXML
  void handleSeller() {
    roles = "SELLER";
    confirmRoute.setText("Bạn chọn là Seller");
    System.out.println(roles); // Debug
  }

  @FXML
  void handleBidder() {
    roles = "BIDDER";
    confirmRoute.setText("Bạn chọn là Bidder");
    System.out.println(roles);
  }

  @FXML
  void handleConfirm(ActionEvent event) {

    try {
      requireText(txtFullname.getText(), "Họ và tên");
      requireText(txtUsername.getText(), "Tên người dùng");
      requireText(txtPassword.getText(), "Mật khẩu");
      requireText(txtConfirmPassword.getText(), "Xác nhận mật khẩu");
      requireText(txtEmail.getText(), "Email");

      requireConfirmPassword(txtPassword.getText(), txtConfirmPassword.getText());

      requireEmail(txtEmail.getText());

      requireRole(roles);

    } catch (ValidationException ve) {
      lblError.setText(ve.getMessage());
      lblError.setVisible(true);
      return; // Mẹ thiếu return ạ:((
    }

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

    // Đóng gói Request
    RegisterRequest payload = new RegisterRequest(user, pass, name, mail, role);
    Request request = new Request("REGISTER", payload);

    // Gửi qua mạng ngầm
    CompletableFuture.supplyAsync(
            () -> {
              try {
                return Client.getInstance().sendRequest(request);
              } catch (Exception e) {
                return new Response(false, "Lỗi kết nối", null);
              }
            })
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    if (response.success()) {
                      // yêu cầu nhập lại thông tin
                      try {
                        isRegister = true;
                        SceneSwitcherUtils.NewSceneController(
                            event,
                            "/com/auctionapp/auctionappjava/views/LoginScreen.fxml",
                            "Đăng nhập");
                      } catch (IOException e) {
                        throw new AppException("Không thể chuyển sang màn hình đăng nhập", e);
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

  @FXML
  void handleLogIn(ActionEvent event) throws IOException {

    SceneSwitcherUtils.NewSceneController(
        event, "/com/auctionapp/auctionappjava/views/LoginScreen.fxml", "Đăng nhập");
  }
}
