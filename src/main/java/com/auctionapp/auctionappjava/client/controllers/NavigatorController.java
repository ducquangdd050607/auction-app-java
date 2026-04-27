package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NavigatorController implements Initializable {

    private Stage stage;
    protected static String modeName;
    private static NavigatorController instance;

    @FXML
    private BorderPane mainBorderPane;
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
    private Button identity;
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnExit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        try {
            show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Hàm fire() có tác dụng sẽ bấm thẳng vào nút được fire ngay khi load (initialize) scene hiện tại
            btnDashboard.fire();
        }
    }

    public static BorderPane getMainBorderPane() {
        return instance.mainBorderPane;
    }

    public void show() throws IOException {
        // Ẩn tất cả đi trước
            groupAdmin.setVisible(false);
            groupAdmin.setManaged(false);
            groupSeller.setVisible(false);
            groupSeller.setManaged(false);
            groupBidder.setVisible(false);
            groupBidder.setManaged(false);

        // Lấy giá trị boolean từ class RouteController và LoginController ra kiểm tra
        if (LoginController.adminRoute) {
            groupAdmin.setVisible(true);
            groupAdmin.setManaged(true);
            identity.setText("ADMIN");

        } else if (RouteController.sellerRoute) {
            groupSeller.setVisible(true);
            groupSeller.setManaged(true);
            identity.setText("SELLER");

        } else if (RouteController.bidderRoute) {
            groupBidder.setVisible(true);
            groupBidder.setManaged(true);
            identity.setText("BIDDER");
        }
    }

    @FXML
    void handleAccount(ActionEvent event) throws IOException {
        SceneSwitcherUtils.NavSceneController(event, mainBorderPane, "/com/auctionapp/auctionappjava/views/AccountScreen.fxml");
    }

    @FXML
    void handleItemsList(ActionEvent event) throws IOException {
        modeName = ((Button) event.getSource()).getText();
        SceneSwitcherUtils.NavSceneController(event, mainBorderPane, "/com/auctionapp/auctionappjava/views/AuctionListScreen.fxml");
    }

    @FXML
    void handleBackToDash(ActionEvent event) throws IOException {
        SceneSwitcherUtils.NavSceneController(event, mainBorderPane, "/com/auctionapp/auctionappjava/views/DashboardScreen.fxml");
    }

    @FXML
    void handleGotoUsersManager(ActionEvent event) throws IOException {
        SceneSwitcherUtils.NavSceneController(event, mainBorderPane, "/com/auctionapp/auctionappjava/views/UsersManagerScreen.fxml");
    }

    @FXML
    void handleSignOut(ActionEvent event) throws IOException {

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        Runnable switchScene = () -> {
            try {
                SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/MainScreen.fxml", "Bíd88");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        AlertUtils.ConfirmExitController(event,
                "Chắc chưa?",
                "Bạn có chắc muốn đăng xuất không?",
                switchScene);
        }

    @FXML
    // Đổi Route
    void handleSwitcher(ActionEvent event) throws IOException {

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        Runnable switchScene = () -> {
            try {
                SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/RouteScreen.fxml", "Vai trò");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        AlertUtils.ConfirmExitController(event,
                "Chắc chưa?",
                "Bạn có chắc muốn đổi vai trò không?",
                switchScene);
    }

    @FXML
    void handleExit (ActionEvent event) throws IOException {

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();

        Runnable closeStage = () -> {
            stage.close();
        };

        AlertUtils.ConfirmExitController(event,
                "Xác nhận thoát",
                "Bạn có chắc chắn muốn thoát ứng dụng không?",
                closeStage);
    }

}