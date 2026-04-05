package com.auctionapp.auctionappjava.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Navigator implements Initializable {

    private Stage stage;
    private Parent root;
    private Scene scene;

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Button changePassword;
    @FXML
    private VBox groupAccount;
    @FXML
    private VBox groupAdmin;
    @FXML
    private VBox groupBidder;
    @FXML
    private VBox groupHome;
    @FXML
    private VBox groupSeller;
    @FXML
    private Button setting;
    @FXML
    private Button wallet;
    @FXML
    private Label identity;


    @FXML
    void history(ActionEvent event) {

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
        // 1. Ẩn tất cả đi trước
        groupAdmin.setVisible(false);
        groupAdmin.setManaged(false);
        groupSeller.setVisible(false);
        groupSeller.setManaged(false);
        groupBidder.setVisible(false);
        groupBidder.setManaged(false);

        // 2. Lấy giá trị boolean từ class Route và kiểm tra
        if (Route.adminRoute) {
            groupAdmin.setVisible(true);
            groupAdmin.setManaged(true);
            identity.setText("ADMIN");

        } else if (Route.sellerRoute) {
            groupSeller.setVisible(true);
            groupSeller.setManaged(true);
            identity.setText("SELLER");

        } else if (Route.bidderRoute) {
            groupBidder.setVisible(true);
            groupBidder.setManaged(true);
            identity.setText("BIDDER");
        }
        Parent scene1 = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/Dashboard.fxml"));
        mainBorderPane.setCenter(scene1);
    }

    @FXML
    void handleChangePassword(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/ChangePasswordScreen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void handleWallet(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/WalletScreen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void itemsList(ActionEvent event) throws IOException {
        Parent scene1 = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/AuctionListScreen.fxml"));
        mainBorderPane.setCenter(scene1);

    }

    @FXML
    void handleBackToDash(ActionEvent event) throws IOException {
        Parent scene1 = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/Dashboard.fxml"));
        mainBorderPane.setCenter(scene1);
    }


}
