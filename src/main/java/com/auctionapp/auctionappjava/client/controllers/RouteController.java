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
    private String user = null;

    @FXML
    private Label confirmRoute;
    @FXML
    private Button btnBid;
    @FXML
    private Button btnSell;
    @FXML
    private Button btnConfirm;
    @FXML
    private Label lblError;

    @FXML
    void handleBidder(ActionEvent event) {
        lblError.setText("");
        btnConfirm.setVisible(true);
        confirmRoute.setText("Bạn chọn là Bidder");
        user = "0";
    }

    @FXML
    void handleSeller(ActionEvent event) {
        lblError.setText("");
        btnConfirm.setVisible(true);
        confirmRoute.setText("Bạn chọn là Seller");
        user = "1";
    }

    @FXML
    void handleConfirm(ActionEvent event) throws IOException {
        try {
            if (user.equals("1")) {
                bidderRoute = false;
                sellerRoute = true;
                SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/NavigatorButtons.fxml", "Bíd88");

            } else if (user.equals("0")) {
                bidderRoute = true;
                sellerRoute = false;
                SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/NavigatorButtons.fxml", "Bíd88");

            }

        } catch (NullPointerException e) {
            lblError.setText("Vui lòng chọn vai trò");
            lblError.setVisible(true);
        }
    }
}
