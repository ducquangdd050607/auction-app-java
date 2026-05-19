package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.LoginResponse;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.exception.AppException;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.formatMoney;

public class AccountController implements Initializable {

    @FXML
    private HBox boxBalance;

    @FXML
    private Button btnDeposit;

    @FXML
    private Label lblBalance;

    @FXML
    private Label lblEmail;

    @FXML
    private Label lblRoute;

    @FXML
    private Label lblFullname;

    public static AccountController instance;

    public void loadAccountData() {
        lblFullname.setText(UserSession.getInstance().getCurrentUser().fullName());
        lblRoute.setText(UserSession.getInstance().getCurrentUser().role());
        lblEmail.setText(UserSession.getInstance().getCurrentUser().email());
        lblBalance.setText(formatMoney(UserSession.getInstance().getCurrentUser().walletBalance()) + " VND");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        try {
            loadAccountData();
            getLatestBalanceFromServer();
            show();
        } catch (IOException e) {
            throw new AppException("Không thể khởi tạo màn hình tài khoản", e);
        }
    }

    @FXML
    void handleChangingInformation(ActionEvent event) throws IOException {
        SceneSwitcherUtils.PopupController(event, "/com/auctionapp/auctionappjava/views/ChangeInformationScreen.fxml", "Thay đổi thông tin");
        loadAccountData();
    }

    @FXML
    void handleDeposit(ActionEvent event) throws IOException {
        SceneSwitcherUtils.PopupController(event, "/com/auctionapp/auctionappjava/views/DepositScreen.fxml", "Nạp tiền");
        loadAccountData();
    }

    @FXML
    void handleChangePassword(ActionEvent event) throws IOException {
        SceneSwitcherUtils.PopupController(event, "/com/auctionapp/auctionappjava/views/ChangePasswordScreen.fxml", "Đổi mật khẩu");
    }

    void balanceAndDeposit(boolean isVisible) {
        boxBalance.setVisible(isVisible);
        btnDeposit.setVisible(isVisible);
        boxBalance.setManaged(isVisible);
        btnDeposit.setManaged(isVisible);
    }

    public void show() throws IOException {
        // Nếu là admin thì set ẩn (false) là được
        balanceAndDeposit(!LoginController.adminRoute);
    }

    public void updateBalanceRealtime(BigDecimal newBalance) {
        lblBalance.setText(formatMoney(newBalance) + " VND");
    }

    private void getLatestBalanceFromServer() {
        String currentUserId = UserSession.getInstance().getCurrentUser().id();
        Request req = new Request("GET_BALANCE", currentUserId);

        CompletableFuture.supplyAsync(() -> {
            try {
                return Client.getInstance().sendRequest(req);
            } catch (Exception e) {
                return null;
            }
        }).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response != null && response.success() && response.data() != null) {
                    // Ép kiểu dữ liệu trả về thành BigDecimal
                    BigDecimal latestBalance = (BigDecimal) response.data();

                    // 1. Cập nhật con số trên Giao diện ngay lập tức
                    lblBalance.setText(formatMoney(latestBalance) + " VND");

                    // 2. Cập nhật lại UserSession (Nhân bản Record cũ, chỉ thay đổi số tiền)
                    LoginResponse oldSession = UserSession.getInstance().getCurrentUser();
                    LoginResponse newSession = new LoginResponse(
                            oldSession.id(),
                            oldSession.username(),
                            oldSession.fullName(),
                            oldSession.role(),
                            oldSession.email(),
                            latestBalance, // Cập nhật đúng số dư mới vào đây
                            oldSession.accStatus()
                    );
                    UserSession.getInstance().setCurrentUser(newSession);
                }
            });
        });
    }
}
