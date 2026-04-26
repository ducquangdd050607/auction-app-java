package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
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

        SceneSwitcherUtils.PopupController(event, "/com/auctionapp/auctionappjava/views/ChangeInformationScreen.fxml", "Thay đổi thông tin");

    }

    @FXML
    void handleDeposit(ActionEvent event) throws IOException {

        SceneSwitcherUtils.PopupController(event, "/com/auctionapp/auctionappjava/views/DepositScreen.fxml", "Nạp tiền");

    }

    @FXML
    void handleChangePassword(ActionEvent event) throws IOException {

        SceneSwitcherUtils.PopupController(event, "/com/auctionapp/auctionappjava/views/ChangePasswordScreen.fxml", "Đổi mật khẩu");

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void balanceAndDeposit(boolean isVisible) {
        boxBalance.setVisible(isVisible);
        btnDeposit.setVisible(isVisible);
        boxBalance.setManaged(isVisible);
        btnDeposit.setManaged(isVisible);
    }

    public void show() throws IOException {
        // Nếu là admin thì set ẩn (false) là được
        balanceAndDeposit(!RouteController.adminRoute);
    }
}
