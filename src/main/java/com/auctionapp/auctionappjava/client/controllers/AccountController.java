package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AccountController implements Initializable {

    @FXML
    private HBox boxBalance;

    @FXML
    private Button btnDeposit;

    @FXML
    private Label lblBalance;

    @FXML
    private Label lblEmail;

    @FXML
    private Label lblRoute;

    @FXML
    private Label lblUsername;

    @FXML
    void handleChangingInformation(ActionEvent event) throws IOException {
        Parent modalRoot = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/ChangeInformation.fxml"));
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

    @FXML
    void handleDeposit(ActionEvent event) throws IOException {
        Parent modalRoot = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/DepositScreen.fxml"));
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void show() throws IOException {
        if (Route.adminRoute) {
            balanceAndDeposit(false);

        } else {
            balanceAndDeposit(true);
        }
    }

    void balanceAndDeposit(boolean admin) {
        boxBalance.setVisible(admin);
        btnDeposit.setVisible(admin);
        boxBalance.setManaged(admin);
        btnDeposit.setManaged(admin);
    }

}
