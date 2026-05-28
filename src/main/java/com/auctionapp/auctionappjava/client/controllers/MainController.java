package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

public class MainController {

  private Stage stage;

  @FXML
  void handleLogin(ActionEvent event) throws IOException {
    SceneSwitcherUtils.NewSceneController(
        event, "/com/auctionapp/auctionappjava/views/LoginScreen.fxml", "Đăng nhập");
  }

  @FXML
  void handleRegister(ActionEvent event) throws IOException {
    SceneSwitcherUtils.NewSceneController(
        event, "/com/auctionapp/auctionappjava/views/RegisterScreen.fxml", "Đăng kí tài khoản");
  }

  @FXML
  void handleExit(ActionEvent event) throws IOException {

    stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

    Runnable closeStage =
        () -> {
          stage.close();
        };

    AlertUtils.AnnouncementController(
        "Xác nhận thoát", "Bạn có chắc chắn muốn thoát ứng dụng không?", closeStage, null);
  }
}
