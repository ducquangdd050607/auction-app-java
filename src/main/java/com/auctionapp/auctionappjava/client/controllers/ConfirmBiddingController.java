package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import com.auctionapp.auctionappjava.server.dao.BidDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcBidDao;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.purifyingText;
import static com.auctionapp.auctionappjava.common.util.MoneyUtils.settingMoneyFormat;
import static com.auctionapp.auctionappjava.common.util.MoneyUtils.formatMoney;

public class ConfirmBiddingController {

    public static boolean isAutoBidding = false;

    private final BigDecimal best = AuctionSession.getInstance().getCurrentAuction().currentPrice();
    private final BigDecimal minIncrement = AuctionSession.getInstance().getCurrentAuction().minimumIncrement();
    private final BigDecimal balance = UserSession.getInstance().getCurrentUser().walletBalance();
    private final String userId = UserSession.getInstance().getCurrentUser().id();
    private final String currentAuctionId = AuctionSession.getInstance().getCurrentAuction().auctionId();
    private final BidDao bidDao = new JdbcBidDao();

    @FXML
    private Button btnMore;

    @FXML
    private CheckBox chboxAutoBidding;

    @FXML
    private Label lblBalance;

    @FXML
    private Label lblError;

    @FXML
    private VBox boxAutoBidding;

    @FXML
    private Label lblBest;

    @FXML
    private Label lblMinIncrement;

    @FXML
    private TextField txtSetAuto;

    @FXML
    private TextField txtSetMaxAuto;

    @FXML
    private TextField txtSetPrice;

    @FXML
    private Button btnConfirm;

    public static ConfirmBiddingController instance;

    @FXML
    void handleAutoBidding(ActionEvent event) {
        boxAutoBidding.setVisible(chboxAutoBidding.isSelected());
        /*boxAutoBidding.setManaged(chboxAutoBidding.isSelected());*/
        isAutoBidding = chboxAutoBidding.isSelected();
    }

    @FXML
    public void initialize() {
        instance = this;
        // THEM AUTO-BID OPTIONAL: moi lan mo man confirm thi mac dinh tat auto-bid.
        isAutoBidding = false;
        chboxAutoBidding.setSelected(false);
        boxAutoBidding.setVisible(false);
        /*boxAutoBidding.setManaged(false);*/

        btnMore.setManaged(false);
        btnMore.setVisible(false);
        lblBalance.setText(formatMoney(balance) + " VND");
        lblBest.setText(formatMoney(best) + " VND");
        lblMinIncrement.setText(formatMoney(minIncrement) + " VND");
        settingMoneyFormat(txtSetPrice);
        settingMoneyFormat(txtSetAuto);
        // THEM AUTO-BID UI: format ca o max auto-bid de parse tien giong cac o khac.
        settingMoneyFormat(txtSetMaxAuto);
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml", "Thông tin sản phẩm");
        isAutoBidding = false;
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

        } else if (isAutoBidding && ((txtSetAuto.getText().isEmpty()) || (txtSetMaxAuto.getText().isEmpty()))) {
            lblError.setText("Hãy nhập giá tiền tự đặt cược.");
            lblError.setTextFill(Color.web("#FF8A80"));

        } else if (isAutoBidding && (((purifyingText(txtSetAuto.getText()).subtract(minIncrement))
                .compareTo(new BigDecimal(0))) < 0)) {
            lblError.setText("Bước tiền tự đặt cược đang nhỏ hơn bước đặt");
            lblError.setTextFill(Color.web("#FF8A80"));

        } else if (isAutoBidding && (((purifyingText(txtSetMaxAuto.getText()).subtract(purifyingText(txtSetPrice.getText())))
                    .compareTo(new BigDecimal(0))) < 0)) {
                lblError.setText("Tiền tự đặt cược tối đa đang nhỏ hơn giá hiện tại");
                lblError.setTextFill(Color.web("#FF8A80"));

        } else if ((best.subtract(purifyingText(txtSetPrice.getText())))
                .compareTo(new BigDecimal(0)) > 0) {
            lblError.setText("Tiền cược đang nhỏ hơn hiện tại!");
            lblError.setTextFill(Color.web("#FF8A80"));

        } else if ((((purifyingText(txtSetPrice.getText())).subtract(best)).compareTo(minIncrement)) < 0) {
            lblError.setText("Vui lòng nhiều hơn mức " + lblMinIncrement.getText() + ".");
            lblError.setVisible(true);
            lblError.setTextFill(Color.web("#FF8A80"));
        } else {
            // Khóa nút đặt giá
            btnConfirm.setDisable(true);

            // Lấy số tiền người dùng chốt đặt
            BigDecimal finalBidAmount = purifyingText(txtSetPrice.getText());
            // THEM AUTO-BID UI: chup lai trang thai checkbox de request async khong bi lech neu UI thay doi.
            boolean autoBidEnabled = isAutoBidding;

            // Gói hàng gửi đi
            PlaceBidRequest payload = new PlaceBidRequest(
                    UUID.fromString(currentAuctionId), // ID phiên đấu giá lấy từ biến ở trên
                    UUID.fromString(userId), // ID người dùng hiện tại
                    finalBidAmount // Số tiền cược
            );
            Request bidReq = new Request("PLACE_BID", payload);

            // THEM AUTO-BID UI: neu user bat checkbox thi gui cau hinh auto-bid truoc khi dat gia.
            Request autoBidReq = null;
            if (autoBidEnabled) {
                ConfigureAutoBidRequest autoBidPayload = new ConfigureAutoBidRequest(
                        UUID.fromString(currentAuctionId),
                        UUID.fromString(userId),
                        purifyingText(txtSetMaxAuto.getText()),
                        purifyingText(txtSetAuto.getText()),
                        true
                );
                autoBidReq = new Request("CONFIGURE_AUTO_BID", autoBidPayload);
            }
            Request finalAutoBidReq = autoBidReq;

            // Chuẩn bị hình ảnh cho Alert
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/auctionapp/auctionappjava/images/Mari.jpg")));
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(500);

            CompletableFuture.supplyAsync(() -> {
                try {
                    // THEM AUTO-BID UI: luu cau hinh auto-bid truoc, neu fail thi dung va bao loi.
                    if (finalAutoBidReq != null) {
                        Response autoBidResponse = Client.getInstance().sendRequest(finalAutoBidReq);
                        if (!autoBidResponse.success()) {
                            return autoBidResponse;
                        }
                    }
                    return Client.getInstance().sendRequest(bidReq);
                } catch (Exception e) {
                    return new Response(false, "Lỗi kết nối máy chủ!", null);
                }
            }).thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response.success()) {
                        // Cập nhật lại UserSession
                        LoginResponse oldUser = UserSession.getInstance().getCurrentUser();
                        BigDecimal latestBalance = oldUser.walletBalance().subtract(finalBidAmount);
                        BigDecimal latestAuctionPrice = finalBidAmount;

                        // Khởi tạo biến lấy số lượng Bidder (mặc định lấy số cũ nếu lỗi)
                        AuctionSummaryResponse oldData = AuctionSession.getInstance().getCurrentAuction();
                        int latestBidderCount = oldData.bidderCount();

                        if (response.data() instanceof Object[] resultData) {
                            if (resultData.length > 0 && resultData[0] instanceof BigDecimal) {
                                latestBalance = (BigDecimal) resultData[0];
                            }
                            if (resultData.length > 1 && resultData[1] instanceof BigDecimal) {
                                latestAuctionPrice = (BigDecimal) resultData[1];
                            }
                            if (resultData.length > 2 && resultData[2] instanceof Integer) {
                                latestBidderCount = (Integer) resultData[2];
                            }
                        } else if (response.data() instanceof BigDecimal) {
                            latestBalance = (BigDecimal) response.data();
                        }
                        LoginResponse updatedUser = new LoginResponse(
                                oldUser.id(),
                                oldUser.username(),
                                oldUser.fullName(),
                                oldUser.role(),
                                oldUser.email(),
                                latestBalance,
                                oldUser.accStatus()
                        );
                        UserSession.getInstance().setCurrentUser(updatedUser);

                        // Cập nhật lại AuctionSession
                        AuctionSummaryResponse updatedData = new AuctionSummaryResponse(
                                oldData.auctionId(),
                                oldData.category(),
                                oldData.itemName(),
                                oldData.sellerName(),
                                oldData.description(),
                                oldData.startPrice(),
                                latestAuctionPrice, // THEM AUTO-BID RESULT: dung gia cuoi cung sau auto-bid.
                                oldData.minimumIncrement(),
                                oldData.startDateTime(),
                                oldData.endDateTime(),
                                oldData.timeLeft(),
                                oldData.status(),
                                latestBidderCount,
                                null // Later
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
                        AlertUtils.AnnouncementController(
                                "Thông báo",
                                "Đã đặt giá thành công!",
                                goBackToDetail, // Gọi hàm chuyển trang
                                imageView);
                    } else {
                        lblError.setText(response.message());
                        lblError.setVisible(true);
                        lblError.setTextFill(Color.web("#FF8A80"));

                        // Nếu Server báo lỗi do thiếu tiền thì hiện nút nạp thêm
                        if (response.message().toLowerCase().contains("số dư")) {
                            btnMore.setManaged(true);
                            btnMore.setVisible(true);
                        }

                        // Hiện lại nút bấm để người dùng có thể thao tác lại
                        btnConfirm.setDisable(false);
                    }
                });
            });
        }
    }
    @FXML
    void handleWallet(ActionEvent event) throws IOException {
        SceneSwitcherUtils.PopupController(event,"/com/auctionapp/auctionappjava/views/DepositScreen.fxml", "moneymoneymoney");
    }

    public void showOutbidWarning() {
        Platform.runLater(() -> {
            // Khóa cứng nút bấm ngay lập tức
            btnConfirm.setDisable(true);

            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/auctionapp/auctionappjava/images/ErrorMari.jpg")));
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(500);

            Runnable goBackToDetail = () -> {
                try {
                    // Mẹo: Tạo một sự kiện fake click từ nút btnConfirm để mượn xài hàm SceneSwitcherUtils
                    ActionEvent dummyEvent = new ActionEvent(btnConfirm, null);
                    SceneSwitcherUtils.NewSceneController(
                            dummyEvent,
                            "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml",
                            "Thông tin sản phẩm"
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            AlertUtils.AnnouncementController(
                    "Thông báo!",
                    "Có thay đổi trong phiên đấu giá, hãy quay về màn hình chi tiết để tìm hiểu",
                    goBackToDetail,
                    imageView
            );
        });
    }
}
