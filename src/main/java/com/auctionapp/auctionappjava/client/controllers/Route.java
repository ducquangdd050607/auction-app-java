package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class Route {

    private Stage stage;
    private Parent root;
    private Scene scene;
    static boolean bidderRoute = false;
    static boolean sellerRoute = false;
    static boolean adminRoute = false;
    private String user;

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
            adminRoute = true;
            root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/Navigator.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();
        }
    }

    @FXML
    void handleConfirm(ActionEvent event) throws IOException {
        try {
            if (user.equals("1")) {
                sellerRoute = true;
                root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/Navigator.fxml"));
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.sizeToScene();
                stage.centerOnScreen();
                stage.show();

            } else if (user.equals("0")) {
                bidderRoute = true;
                root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/Navigator.fxml"));
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.sizeToScene();
                stage.centerOnScreen();
                stage.show();
            }
        } catch (NullPointerException e) {
            lblError.setText("Vui lòng chọn vai trò");
            lblError.setVisible(true);
        }
    }
}
