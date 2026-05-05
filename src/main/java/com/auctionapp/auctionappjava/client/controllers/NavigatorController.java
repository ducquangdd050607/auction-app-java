package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.session.UserSession;
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
    private Button btnDashboard;
    @FXML
    private Button btnExit;
    @FXML
    private Button btnGotoUsersManager;
    @FXML
    private Button btnHistory;
    @FXML
    private Button btnItemListAdmin;
    @FXML
    private Button btnItemListBidder;
    @FXML
    private Button btnItemListSeller;
    @FXML
    private Button btnItemManager;
    @FXML
    private Button btnSignout;
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
    private Button identity;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Button setting;

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

        // Lấy giá trị boolean từ class LoginController ra kiểm tra
        if (LoginController.adminRoute) {
            groupAdmin.setVisible(true);
            groupAdmin.setManaged(true);
            identity.setText("ADMIN");
            identity.setDisable(true);

        } else if (LoginController.sellerRoute) {
            groupSeller.setVisible(true);
            groupSeller.setManaged(true);
            identity.setText("SELLER");
            identity.setDisable(false);

        } else if (LoginController.bidderRoute) {
            groupBidder.setVisible(true);
            groupBidder.setManaged(true);
            identity.setText("BIDDER");
            identity.setDisable(false);
        }
    }

    @FXML
    void handleAccount(ActionEvent event) throws IOException {
        SceneSwitcherUtils.NavSceneController(event, mainBorderPane, "/com/auctionapp/auctionappjava/views/AccountScreen.fxml");
        activateAccountButton();
    }

    @FXML
    void handleItemsList(ActionEvent event) throws IOException {
        modeName = ((Button) event.getSource()).getText();
        setActiveButton((Button) event.getSource());
        SceneSwitcherUtils.NavSceneController(event, mainBorderPane, "/com/auctionapp/auctionappjava/views/AuctionListScreen.fxml");
    }

    @FXML
    void handleBackToDash(ActionEvent event) throws IOException {
        activateDashboardButton();
        SceneSwitcherUtils.NavSceneController(event, mainBorderPane, "/com/auctionapp/auctionappjava/views/DashboardScreen.fxml");
    }

    @FXML
    void handleGotoUsersManager(ActionEvent event) throws IOException {
        NavigatorController.activateUserManager();
        SceneSwitcherUtils.NavSceneController(event, mainBorderPane, "/com/auctionapp/auctionappjava/views/UsersManagerScreen.fxml");
    }

    @FXML
    void handleSignOut(ActionEvent event) throws IOException {

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        Runnable switchScene = () -> {
            try {
                // Xóa thông tin user trong session này
                UserSession.getInstance().cleanUserSession();
                RegisterController.isRegister = false;
                SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/MainScreen.fxml", "Bíd88");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        AlertUtils.ConfirmExitController(
                "Chắc chưa?",
                "Bạn có chắc muốn đăng xuất không?",
                switchScene);
    }


    @FXML
    void handleExit (ActionEvent event) throws IOException {

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();

        Runnable closeStage = () -> {
            stage.close();
        };

        AlertUtils.ConfirmExitController(
                "Xác nhận thoát",
                "Bạn có chắc chắn muốn thoát ứng dụng không?",
                closeStage);
    }

    private Button currentActiveButton = null;

    // Gọi nội bộ mỗi khi điều hướng trong Navigator
    private void setActiveButton(Button btn) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-menu-btn-active");
        }
        if (btn != null) {
            btn.getStyleClass().add("nav-menu-btn-active");
        }
        currentActiveButton = btn;
    }

    // Expose tĩnh để Dashboard gọi được
    public static void activateAccountButton() {
        if (instance != null) {
            instance.setActiveButton(instance.setting);
        }
    }

    public static void activateHistory() {
        if (instance != null) {
            instance.setActiveButton(instance.btnHistory);
        }
    }

    public static void activateItemListBidder() {
        if (instance != null) {
            instance.setActiveButton(instance.btnItemListBidder);
        }
    }

    public static void activateItemListSeller() {
        if (instance != null) {
            instance.setActiveButton(instance.btnItemListSeller);
        }
    }

    public static void activateItemListAdmin() {
        if (instance != null) {
            instance.setActiveButton(instance.btnItemListAdmin);
        }
    }

    public static void activateDashboardButton() {
        if (instance != null) {
            instance.setActiveButton(instance.btnDashboard);
        }
    }

    public static void activateUserManager() {
        if (instance != null) {
            instance.setActiveButton(instance.btnGotoUsersManager);
        }
    }
}