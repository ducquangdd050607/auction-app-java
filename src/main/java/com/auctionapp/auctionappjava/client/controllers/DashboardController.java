package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils.NavSceneController;

public class DashboardController implements Initializable {

    public static int mode = 0;

    @FXML
    private Button btnGo1;

    @FXML
    private Button btnGo2;

    @FXML
    private Button btnGo3;

    @FXML
    private Button btnHistory;

    @FXML
    private Button btnSellerItemManager;

    @FXML
    private Button btnShowList;

    @FXML
    private Button btnWallet;

    @FXML
    private Label endTime1;

    @FXML
    private Label endTime2;

    @FXML
    private Label endTime3;

    @FXML
    private VBox itemCard1;

    @FXML
    private VBox itemCard2;

    @FXML
    private VBox itemCard3;

    @FXML
    private Label lblBalance;

    @FXML
    private Label lblBidders;

    @FXML
    private Label lblCompleted;

    @FXML
    private Label lblGreeting;

    @FXML
    private Label lblGreetingSub;

    @FXML
    private Label lblHistory;

    @FXML
    private Label lblItemDesc1;

    @FXML
    private Label lblItemDesc2;

    @FXML
    private Label lblItemDesc3;

    @FXML
    private Label lblItemName1;

    @FXML
    private Label lblItemName2;

    @FXML
    private Label lblItemName3;

    @FXML
    private Label lblItemPrice1;

    @FXML
    private Label lblItemPrice2;

    @FXML
    private Label lblItemPrice3;

    @FXML
    private Label lblRUNNINGs;

    @FXML
    private Label lblStat1Label;

    @FXML
    private Label lblStat1Label1;

    @FXML
    private Label lblStat3Label;

    @FXML
    private Label lblStat3Label1;

    @FXML
    private Label lblStat3Label2;

    @FXML
    private Label lblTimer1;

    @FXML
    private Label lblTimer2;

    @FXML
    private Label lblTimer3;

    @FXML
    void handleDetail(ActionEvent event) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            lblGreeting.setText("Xin chào, " + UserSession.getInstance().getCurrentUser().fullName() + "!");
            show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void show() throws IOException {

        // Mặc định theo Admin
        btnHistory.setVisible(false);
        btnHistory.setManaged(false);
        btnSellerItemManager.setVisible(false);
        btnSellerItemManager.setManaged(false);
        btnWallet.setVisible(false);
        btnWallet.setManaged(false);

        if (RouteController.sellerRoute) {
            btnBehaviour(false);
            btnWallet.setVisible(true);
            btnWallet.setManaged(true);


        } else if (RouteController.bidderRoute) {
            btnBehaviour(true);
            btnWallet.setVisible(true);
            btnWallet.setManaged(true);
        }

    }
    public void btnBehaviour(boolean bool) {
        btnHistory.setVisible(bool);
        btnHistory.setManaged(bool);
        btnSellerItemManager.setVisible(!bool);
        btnSellerItemManager.setManaged(!bool);
    }

    @FXML
    void handleHistory(ActionEvent event) throws IOException {
        mode = 1;
        NavSceneController(event, NavigatorController.getMainBorderPane(), "/com/auctionapp/auctionappjava/views/AuctionListScreen.fxml");

    }

    @FXML
    void handleManager(ActionEvent event) throws IOException {
        mode = 2;
        NavSceneController(event, NavigatorController.getMainBorderPane(), "/com/auctionapp/auctionappjava/views/AuctionListScreen.fxml");

    }

    @FXML
    void handleOpenList(ActionEvent event) throws IOException {
        NavSceneController(event, NavigatorController.getMainBorderPane(), "/com/auctionapp/auctionappjava/views/AuctionListScreen.fxml");
    }

    @FXML
    void handleWallet(ActionEvent event) throws IOException{
        NavSceneController(event, NavigatorController.getMainBorderPane(), "/com/auctionapp/auctionappjava/views/AccountScreen.fxml");
    }
}
