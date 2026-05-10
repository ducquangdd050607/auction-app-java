package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import com.mysql.cj.log.Log;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.purifyingText;
import static com.auctionapp.auctionappjava.common.util.MoneyUtils.settingMoneyFormat;

public class ConfirmBiddingController {

    public static boolean isAutoBidding = false;

    private final BigDecimal best = AuctionSession.getInstance().getCurrentAuction().currentPrice();
    private final BigDecimal minIncrement = AuctionSession.getInstance().getCurrentAuction().minimumIncrement();
    private final BigDecimal balance = UserSession.getInstance().getCurrentUser().walletBalance();
    private final String userId = UserSession.getInstance().getCurrentUser().id();
    private final String currentAuctionId = AuctionSession.getInstance().getCurrentAuction().auctionId();

    @FXML
    private Button btnMore;

    @FXML
    private CheckBox chboxAutoBidding;

    @FXML
    private Label lblBalance;

    @FXML
    private Label lblError;

    @FXML
    private HBox boxAutoBidding;

    @FXML
    private Label lblBest;

    @FXML
    private Label lblMinIncrement;

    @FXML
    private TextField txtSetAuto;

    @FXML
    private TextField txtSetPrice;

    @FXML
    private Button btnConfirm;

    @FXML
    void handleAutoBidding(ActionEvent event) {
        boxAutoBidding.setVisible(chboxAutoBidding.isSelected());
        boxAutoBidding.setManaged(chboxAutoBidding.isSelected());
        isAutoBidding = chboxAutoBidding.isSelected();
    }

    @FXML
    public void initialize() {
        btnMore.setManaged(false);
        btnMore.setVisible(false);
        lblBalance.setText(balance.toPlainString());
        lblBest.setText(best.toPlainString());
        lblMinIncrement.setText(minIncrement.toPlainString());
        btnMore.setManaged(false);
        settingMoneyFormat(txtSetPrice);
        settingMoneyFormat(txtSetAuto);
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml", "Thông tin sản phẩm");
        isAutoBidding = false;
        // TODO: SỬA LẠI CÁCH BACK HOẠT ĐỘNG

    }

    @FXML
    void handlePreset(ActionEvent event) {

        Button clickedButton = (Button) event.getSource();
        String presetValue = clickedButton.getText();

        BigDecimal amount = purifyingText(presetValue);

        if (txtSetPrice.getText().isEmpty()) {
            txtSetPrice.setText(String.valueOf(amount));

        } else {
            txtSetPrice.setText(String.valueOf(purifyingText(txtSetPrice.getText()).add(amount)));

        }
    }

    @FXML
    void handleTrueConfirm(ActionEvent event) {
        if (txtSetPrice.getText().isEmpty()) {
            lblError.setText("Hãy nhập giá tiền cược.");
            lblError.setTextFill(Color.web("#FF8A80"));

        } else if ((txtSetAuto.getText().isEmpty()) & (isAutoBidding)) {
            lblError.setText("Hãy nhập giá tiền tự đặt cược.");
            lblError.setTextFill(Color.web("#FF8A80"));

        } else if ((((purifyingText(txtSetAuto.getText()).subtract(minIncrement))
                .compareTo(new BigDecimal(0))) < 0) & (isAutoBidding)) {
            lblError.setText("Giá tiền tự đặt cược đang nhỏ hơn bước đặt");
            lblError.setTextFill(Color.web("#FF8A80"));

        } else if ((best.subtract(purifyingText(txtSetPrice.getText())))
                .compareTo(new BigDecimal(0)) > 0) {
            lblError.setText("Tiền cược đang nhỏ hơn hiện tại!");
            lblError.setTextFill(Color.web("#FF8A80"));

        } else if (balance.subtract(purifyingText(txtSetPrice.getText()))
                .compareTo(new BigDecimal(0)) < 0) {

            lblError.setText("Không đủ tiền trong số dư!");
            lblError.setTextFill(Color.web("#FF8A80"));
            btnMore.setManaged(true);
            btnMore.setVisible(true);

        } else if ((((purifyingText(txtSetPrice.getText())).subtract(best)).compareTo(minIncrement)) < 0) {
            lblError.setText("Vui lòng nhiều hơn mức " + lblMinIncrement.getText() + ".");
            lblError.setVisible(true);
            lblError.setTextFill(Color.web("#FF8A80"));
        } else {
            // Khóa nút đặt giá
            btnConfirm.setDisable(true);

            // Lấy số tiền người dùng chốt đặt
            BigDecimal finalBidAmount = purifyingText(txtSetPrice.getText());

            // Gói hàng gửi đi
            PlaceBidRequest payload = new PlaceBidRequest(
                    UUID.fromString(currentAuctionId), // ID phiên đấu giá lấy từ biến ở trên
                    UUID.fromString(userId), // ID người dùng hiện tại
                    finalBidAmount // Số tiền cược
            );
            Request bidReq = new Request("PLACE_BID", payload);

            // Chuẩn bị hình ảnh cho Alert
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/auctionapp/auctionappjava/images/Mari.jpg")));
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(500);

            CompletableFuture.supplyAsync(() -> {
                try {
                    return Client.getInstance().sendRequest(bidReq);
                } catch (Exception e) {
                    return new Response(false, "Lỗi kết nối máy chủ!", null);
                }
            }).thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response.success()) {
                        // Cập nhật lại UserSession
                        LoginResponse oldUser = UserSession.getInstance().getCurrentUser();
                        LoginResponse updatedUser = new LoginResponse(
                                oldUser.id(),
                                oldUser.username(),
                                oldUser.fullName(),
                                oldUser.role(),
                                oldUser.email(),
                                oldUser.walletBalance().subtract(finalBidAmount)
                        );
                        UserSession.getInstance().setCurrentUser(updatedUser);

                        // Cập nhật lại AuctionSession
                        AuctionSummaryResponse oldData = AuctionSession.getInstance().getCurrentAuction();
                        AuctionSummaryResponse updatedData = new AuctionSummaryResponse(
                                oldData.auctionId(),
                                oldData.category(),
                                oldData.itemName(),
                                oldData.startPrice(),
                                finalBidAmount, // Thay currentprice bằng giá vừa đặt thành công
                                oldData.minimumIncrement(),
                                oldData.timeLeft(),
                                oldData.status(),
                                oldData.bidderCount() + 1
                        );
                        AuctionSession.getInstance().setCurrentAuction(updatedData);

                        // Định nghĩa hành động sẽ xảy ra khi người dùng bấm nút OK trên Alert
                        Runnable goBackToDetail = () -> {
                            try {
                                SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml", "Thông tin sản phẩm");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        };

                        // Hiển thị Alert, truyền Runnable vào để khi tắt Alert nó tự nhảy trang
                        AlertUtils.ConfirmAlertController(
                                "Thông báo",
                                "Đã đặt giá thành công!",
                                goBackToDetail, // Gọi hàm chuyển trang
                                imageView);
                    } else {
                        lblError.setText(response.message());
                        lblError.setVisible(true);
                        lblError.setTextFill(Color.web("#FF8A80"));

                        // Hiện lại nút bấm để người dùng có thể thao tác lại
                        btnConfirm.setDisable(false);
                        /*btnMore.setManaged(true);
                        btnMore.setVisible(true);*/
                    }
                });
            });
        }
    }
    @FXML
    void handleWallet(ActionEvent event) throws IOException {

        SceneSwitcherUtils.PopupController(event,"/com/auctionapp/auctionappjava/views/DepositScreen.fxml", "moneymoneymoney");

    }
}
