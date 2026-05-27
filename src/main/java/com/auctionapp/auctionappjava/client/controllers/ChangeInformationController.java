package com.auctionapp.auctionappjava.client.controllers;

import static com.auctionapp.auctionappjava.common.util.ValidationUtils.*;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.ChangeInformationRequest;
import com.auctionapp.auctionappjava.common.dto.LoginResponse;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.exception.ValidationException;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ChangeInformationController {

  @FXML private Label lblError;

  @FXML private TextField txtEmail;

  @FXML private TextField txtFullname;

  @FXML
  void handleConfirm(ActionEvent event) {

    try {
      requireText(txtFullname.getText(), "Họ và tên");
      requireText(txtEmail.getText(), "Email");

      requireEmail(txtEmail.getText());

    } catch (ValidationException ex) {
      lblError.setText(ex.getMessage());
      lblError.setVisible(true);
      lblError.setTextFill(Color.web("#FF8A80"));
      return;
    }
    Runnable changeInformationMethod =
        () -> {
          ChangeInformationRequest payload =
              new ChangeInformationRequest(
                  UserSession.getInstance().getCurrentUser().id(),
                  txtFullname.getText(),
                  txtEmail.getText());
          Request changeInformationRequest = new Request("CHANGE_INFORMATION", payload);

          Image image =
              new Image(
                  Objects.requireNonNull(
                      getClass()
                          .getResourceAsStream("/com/auctionapp/auctionappjava/images/Mari.jpg")));
          ImageView mariDaCat = new ImageView(image);
          mariDaCat.setPreserveRatio(true);
          mariDaCat.setFitWidth(500);

          CompletableFuture.supplyAsync(
                  () -> {
                    try {
                      return Client.getInstance().sendRequest(changeInformationRequest);
                    } catch (Exception e) {
                      return new Response(false, "Lỗi kết nối máy chủ!", null);
                    }
                  })
              .thenAccept(
                  response -> {
                    Platform.runLater(
                        () -> {
                          if (response.success()) {
                            // Cập nhật lại UserSession
                            LoginResponse oldUser = UserSession.getInstance().getCurrentUser();
                            LoginResponse updatedUser =
                                new LoginResponse(
                                    oldUser.id(),
                                    oldUser.username(),
                                    txtFullname.getText(), // Tên mới
                                    oldUser.role(),
                                    txtEmail.getText(), // Email mới
                                    oldUser.walletBalance(),
                                    oldUser.accStatus());
                            UserSession.getInstance().setCurrentUser(updatedUser);

                            Runnable closeForm =
                                () -> {
                                  Stage currentStage =
                                      (Stage) ((Node) event.getSource()).getScene().getWindow();
                                  currentStage.close();
                                };

                            AlertUtils.AnnouncementController(
                                "Thông báo", "Đã thay đổi thành công!", closeForm, mariDaCat);

                          } else {
                            lblError.setText(response.message());
                            lblError.setVisible(true);
                            lblError.setTextFill(Color.web("#FF8A80"));
                          }
                        });
                  });
        };

    AlertUtils.AnnouncementController(
        "Chắc chưa?", "Bạn có muốn đổi thông tin không?", changeInformationMethod, null);
  }

  @FXML
  void handleBack(ActionEvent event) throws IOException {
    Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    currentStage.close();
  }
}
