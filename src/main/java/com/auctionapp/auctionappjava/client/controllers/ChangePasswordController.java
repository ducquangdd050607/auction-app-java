package com.auctionapp.auctionappjava.client.controllers;

import static com.auctionapp.auctionappjava.common.util.ValidationUtils.*;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.ChangePasswordRequest;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.exception.ValidationException;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class ChangePasswordController {

  @FXML private Label lblMessage;

  @FXML private PasswordField txtConfirmPassword;

  @FXML private PasswordField txtNewPassword;

  @FXML
  void handleBack(ActionEvent event) {
    // Đóng stage lại khi back về nav
    Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    currentStage.close();
  }

  @FXML
  void handleConfirm(ActionEvent event) {
    try {
      requireText(txtNewPassword.getText(), "Mật khẩu");
      requireText(txtConfirmPassword.getText(), "Xác nhận mật khẩu");

      requireConfirmPassword(txtNewPassword.getText(), txtConfirmPassword.getText());

    } catch (ValidationException ve) {
      lblMessage.setText(ve.getMessage());
      lblMessage.setVisible(true);
      return;
    }

    Runnable pseudoMethod =
        () -> { // Test
          System.out.println("PseudoMethod");

          ChangePasswordRequest payload =
              new ChangePasswordRequest(
                  UserSession.getInstance().getCurrentUser().id(), txtNewPassword.getText());
          Request changePasswordRequest = new Request("CHANGE_PASSWORD", payload);
          CompletableFuture.supplyAsync(
              () -> {
                try {
                  return Client.getInstance().sendRequest(changePasswordRequest);
                } catch (Exception e) {
                  return new Response(false, "Lỗi kết nối máy chủ!", null);
                }
              });
        };

    Image image =
        new Image(
            Objects.requireNonNull(
                getClass().getResourceAsStream("/com/auctionapp/auctionappjava/images/Mari.jpg")));
    ImageView mariDaCat = new ImageView(image);
    mariDaCat.setPreserveRatio(true);
    mariDaCat.setFitWidth(500);

    AlertUtils.ConfirmAlertController(
        event,
        "Chắc chưa?",
        "Bạn có muốn đổi mật khẩu không?",
        "",
        "Thông báo",
        "",
        "Đã thay đổi mật khẩu thành công!",
        pseudoMethod,
        mariDaCat);
  }
}
