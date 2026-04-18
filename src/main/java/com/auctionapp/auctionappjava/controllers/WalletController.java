package com.auctionapp.auctionappjava.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;
public class WalletController {

    private Stage stage;
    private Parent root;
    private Scene scene;

    @FXML
    private Label lblBalance;

    @FXML
    private Label lblEmail;

    @FXML
    private Label lblFullName;

    @FXML
    private Label lblRole;

    @FXML
    private Label lblUsername;

    @FXML
    void handleChangingInformation(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/ChangeInformation.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();

    }

    @FXML
    void handleDeposit(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/DepositScreen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }
    
    @FXML
    void handleChangePassword(ActionEvent event) throws IOException {
        Parent modalRoot = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/ChangePasswordScreen.fxml"));
        Stage modalStage = new Stage();
        modalStage.setTitle("Đổi mật khẩu");

        Stage parentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        modalStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        modalStage.initOwner(parentStage);

        Scene modalScene = new Scene(modalRoot);
        modalStage.setScene(modalScene);
        modalStage.setResizable(false);

        modalStage.showAndWait();
    }
}
