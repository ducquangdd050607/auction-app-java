package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class RouteController {

    static boolean bidderRoute = false;
    static boolean sellerRoute = false;
    static boolean adminRoute = false;
    private static String user;

    @FXML
    private Label confirmRoute;
    @FXML
    private Button btnAd;
    @FXML
    private Button btnBid;
    @FXML
    private Button btnSell;
    @FXML
    private Button btnConfirm;
    @FXML
    private Button btnConfirmAd;
    @FXML
    private Label lblError;
    @FXML
    private TextField txtPrivateKey;


    @FXML
    void handleAdmin(ActionEvent event) {
        lblError.setText(""); //clear
        txtPrivateKey.setVisible(true);
        btnConfirmAd.setVisible(true);
        confirmRoute.setText("Nhập mã: ");
        btnConfirm.setVisible(false);
    }

    @FXML
    void handleBidder(ActionEvent event) {
        lblError.setText(""); //clear
        btnConfirm.setVisible(true);
        confirmRoute.setText("Bạn chọn là Bidder");
        txtPrivateKey.setVisible(false);
        btnConfirmAd.setVisible(false);
        user = "0";
    }

    @FXML
    void handleSeller(ActionEvent event) {
        lblError.setText(""); //clear
        btnConfirm.setVisible(true);
        confirmRoute.setText("Bạn chọn là Seller");
        txtPrivateKey.setVisible(false);
        btnConfirmAd.setVisible(false);
        user = "1";
    }

    @FXML
    void showPrivateKey(MouseEvent event) {
        btnConfirmAd.setVisible(true);
    }

    @FXML
    void handleConfirmAd(ActionEvent event) throws IOException {
        if (txtPrivateKey.getText().isEmpty()) {
            lblError.setText("Vui lòng nhập mã");
            lblError.setVisible(true);
        } else {
            bidderRoute = false;
            sellerRoute = false;
            adminRoute = true;

            SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/NavigatorButtons.fxml", "Bíd88");
        }
    }

    @FXML
    void handleConfirm(ActionEvent event) throws IOException {
        try {
            if (user.equals("1")) {
                bidderRoute = false;
                sellerRoute = true;
                adminRoute = false;
                SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/NavigatorButtons.fxml", "Bíd88");

            } else if (user.equals("0")) {
                bidderRoute = true;
                sellerRoute = false;
                adminRoute = false;
                SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/NavigatorButtons.fxml", "Bíd88");

            }

        } catch (NullPointerException e) {
            lblError.setText("Vui lòng chọn vai trò");
            lblError.setVisible(true);
        }
    }
}
