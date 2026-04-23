package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.core.ClientContext;
import com.auctionapp.auctionappjava.common.enums.Role;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class Route {

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
        } else if (selectServerRole(Role.ADMIN, txtPrivateKey.getText().trim())) {
            bidderRoute = false;
            sellerRoute = false;
            adminRoute = true;

            SceneSwitcherController.NewSceneController(event, "/com/auctionapp/auctionappjava/views/Navigator.fxml", "Bíd88");
        }
    }

    @FXML
    void handleConfirm(ActionEvent event) throws IOException {
        if (user == null) {
            lblError.setText("Vui lòng chọn vai trò");
            lblError.setVisible(true);
            return;
        }
        Role selectedRole = user.equals("1") ? Role.SELLER : Role.BIDDER;
        if (!selectServerRole(selectedRole, null)) {
            return;
        }
        if (selectedRole == Role.SELLER) {
            bidderRoute = false;
            sellerRoute = true;
            adminRoute = false;
        } else {
            bidderRoute = true;
            sellerRoute = false;
            adminRoute = false;
        }
        SceneSwitcherController.NewSceneController(event, "/com/auctionapp/auctionappjava/views/Navigator.fxml", "Bíd88");
    }

    private boolean selectServerRole(Role role, String adminKey) {
        try {
            ClientContext context = ClientContext.getInstance();
            context.getApi().selectRole(context.getSession().getUserId(), role, adminKey);
            return true;
        } catch (RuntimeException ex) {
            lblError.setText(ex.getMessage() == null ? "Không thể chọn vai trò" : ex.getMessage());
            lblError.setVisible(true);
            return false;
        }
    }
}
