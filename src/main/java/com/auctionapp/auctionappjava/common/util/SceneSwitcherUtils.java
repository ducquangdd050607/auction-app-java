package com.auctionapp.auctionappjava.common.util;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class SceneSwitcherUtils {

  public static Image icon =
      new Image(
          SceneSwitcherUtils.class.getResourceAsStream(
              "/com/auctionapp/auctionappjava/images/IconApp.png"));

  public static void PopupController(ActionEvent event, String address, String title)
      throws IOException {
    Parent modalRoot = FXMLLoader.load(SceneSwitcherUtils.class.getResource(address));
    Stage modalStage = new Stage();
    modalStage.setTitle(title);

    Stage parentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    modalStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
    modalStage.initOwner(parentStage);
    modalStage.getIcons().add(icon);

    Scene modalScene = new Scene(modalRoot);

    modalStage.setScene(modalScene);
    modalStage.setResizable(false);

    modalStage.showAndWait();
  }

  public static void NewSceneController(ActionEvent event, String address, String title)
      throws IOException {

    Parent root = FXMLLoader.load(SceneSwitcherUtils.class.getResource(address));
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.getIcons().add(icon);
    stage.setTitle(title);
    Scene scene = new Scene(root);
    stage.setResizable(false);
    stage.setScene(scene);
    stage.sizeToScene();
    stage.centerOnScreen();
    stage.show();
  }

  private static Button currentActiveButton;

  public static void NavSceneController(ActionEvent event, BorderPane borderPane, String address)
      throws IOException {
    if (currentActiveButton != null) {
      currentActiveButton.getStyleClass().remove("nav-menu-btn-active");
    }
    Button clickedButton = (Button) event.getSource();
    clickedButton.getStyleClass().add("nav-menu-btn-active");
    currentActiveButton = clickedButton;
    Parent view = FXMLLoader.load(SceneSwitcherUtils.class.getResource(address));
    borderPane.setCenter(view);
  }
}
