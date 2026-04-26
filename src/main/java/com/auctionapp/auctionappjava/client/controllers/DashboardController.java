package com.auctionapp.auctionappjava.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardController {

    private Stage stage;
    private Parent root;
    private Scene scene;

    @FXML
    private Button btnQuickManage;
    @FXML
    private Button btnQuickUsers;
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
    private Label identity;
    @FXML
    private Label lblGreeting;
    @FXML
    private Label lblGreetingSub;
    @FXML
    private Label lblStat1Label;
    @FXML
    private Label lblStat1Value;
    @FXML
    private Label lblStat2Label;
    @FXML
    private Label lblStat2Value;
    @FXML
    private Label lblStat3Label;
    @FXML
    private Label lblStat3Label1;
    @FXML
    private Label lblStat3Value;
    @FXML
    private Label lblStat3Value1;
    @FXML
    private Label lblStat4Icon;
    @FXML
    private Label lblStat4Label;
    @FXML
    private Label lblStat4Value;
    @FXML
    private HBox quickActions;
    @FXML
    private VBox statCard1;
    @FXML
    private VBox statCard2;
    @FXML
    private VBox statCard3;
    @FXML
    private VBox statCard4;
}
