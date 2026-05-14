package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.util.MoneyUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils.NavSceneController;

public class DashboardController implements Initializable {

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
    private Button btnWallet;
    @FXML
    private Button btnShowActiveUsers;
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
    private HBox boxItems;

    @FXML
    private ListView<?> boxLoading;

    @FXML
    void handleDetail(ActionEvent event) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            lblGreeting.setText("Xin chào, " + UserSession.getInstance().getCurrentUser().fullName() + "!");
            lblBalance.setText(MoneyUtils.formatMoney(UserSession.getInstance().getCurrentUser().walletBalance()) + " VND");
            show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //loadUserDataFromServer();

        loadConditionalAuctionsFromServer();

//        setupAuctionCards();


    }

//    public void loadUserDataFromServer() {
//        Request req = new Request()
//    }
//

    public void loadConditionalAuctionsFromServer() {

        boxItems.setVisible(false);
        boxItems.setManaged(false);

        boxLoading.setVisible(true);
        boxLoading.setManaged(true);

        ProgressIndicator loadingSpinner = new ProgressIndicator();
        loadingSpinner.setMaxSize(50, 50);
        boxLoading.setPlaceholder(loadingSpinner);

        Request req = new Request("GET_ALL_FEATURED_AUCTIONS", null);
        CompletableFuture.supplyAsync(() -> {
            try {
                return Client.getInstance().sendRequest(req);
            } catch (Exception e) {
                e.printStackTrace();
                return new Response(false, "Lỗi kết nối Server", null);
            }
        }).thenAccept(response -> {
            Platform.runLater(() -> {

                if (response.success()) {

                    boxLoading.setVisible(false);
                    boxLoading.setManaged(false);

                    boxItems.setVisible(true);
                    boxItems.setManaged(true);

                    // Ép kiểu lấy danh sách từ Response
                    List<AuctionSummaryResponse> auctionsFromServer = (List<AuctionSummaryResponse>) response.data();

                    // Điền dữ liệu mới vào bảng
                    AuctionSummaryResponse mostBiddedAuction = auctionsFromServer.get(0);


                    lblItemName1.setText(mostBiddedAuction.itemName());
                    endTime1.setText(String.valueOf(mostBiddedAuction.endDateTime()));
                    lblItemPrice1.setText(MoneyUtils.formatMoney(mostBiddedAuction.currentPrice()));
                    lblItemDesc1.setText(mostBiddedAuction.description());



                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, response.message());
                    alert.show();
                    Label noDataLabel = new Label("Không tìm thấy phiên đấu giá nào khớp yêu cầu.");
                    noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray; ");
                    boxLoading.setPlaceholder(noDataLabel);
                }

                // Luôn luôn cất vòng xoay đi và thay bằng nhãn chữ này

            });
        });
    }



    public void show() throws IOException {

        // Mặc định theo Admin
        btnHistory.setVisible(false);
        btnHistory.setManaged(false);
        btnSellerItemManager.setVisible(false);
        btnSellerItemManager.setManaged(false);
        btnWallet.setVisible(false);
        btnWallet.setManaged(false);
        btnShowActiveUsers.setVisible(true);
        btnShowActiveUsers.setManaged(true);

        if (LoginController.sellerRoute) {
            btnBehaviour(false);
            btnWallet.setVisible(true);
            btnWallet.setManaged(true);
            btnShowActiveUsers.setVisible(false);
            btnShowActiveUsers.setManaged(false);


        } else if (LoginController.bidderRoute) {
            btnBehaviour(true);
            btnWallet.setVisible(true);
            btnWallet.setManaged(true);
            btnShowActiveUsers.setVisible(false);
            btnShowActiveUsers.setManaged(false);
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
        NavSceneController(event, NavigatorController.getMainBorderPane(), "/com/auctionapp/auctionappjava/views/HistoryScreen.fxml");
        NavigatorController.activateHistory();
    }

    @FXML
    void handleManager(ActionEvent event) throws IOException {
        NavigatorController.modeName = "Quản lý vật phẩm";
        NavigatorController.activateAccountButton();
        NavSceneController(event, NavigatorController.getMainBorderPane(), "/com/auctionapp/auctionappjava/views/AuctionListScreen.fxml");

    }

    @FXML
    void handleOpenList(ActionEvent event) throws IOException {
        if (LoginController.adminRoute) {
            NavigatorController.activateItemListAdmin();
            NavigatorController.modeName = "Quản lý phiên đấu giá";

        } else if (LoginController.bidderRoute) {
            NavigatorController.activateItemListBidder();
            NavigatorController.modeName = "Danh sách đấu giá";

        } else if (LoginController.sellerRoute) {
            NavigatorController.activateItemListSeller();
            NavigatorController.modeName = "Danh sách đấu giá";
        }

        NavSceneController(event, NavigatorController.getMainBorderPane(), "/com/auctionapp/auctionappjava/views/AuctionListScreen.fxml");
    }

    @FXML
    void handleOpenUsersList(ActionEvent event) throws IOException {
        NavigatorController.activateUserManager();
        NavSceneController(event, NavigatorController.getMainBorderPane(), "/com/auctionapp/auctionappjava/views/UsersManagerScreen.fxml");
    }

    @FXML
    void handleWallet(ActionEvent event) throws IOException{
        NavigatorController.activateAccountButton();
        NavSceneController(event, NavigatorController.getMainBorderPane(), "/com/auctionapp/auctionappjava/views/AccountScreen.fxml");
    }
}
