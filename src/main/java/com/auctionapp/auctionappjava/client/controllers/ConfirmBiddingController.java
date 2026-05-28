package com.auctionapp.auctionappjava.client.controllers;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.formatMoney;
import static com.auctionapp.auctionappjava.common.util.MoneyUtils.purifyingText;
import static com.auctionapp.auctionappjava.common.util.MoneyUtils.settingMoneyFormat;
import static com.auctionapp.auctionappjava.common.util.ValidationUtils.*;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.exception.ValidationException;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import com.auctionapp.auctionappjava.server.dao.BidDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcBidDao;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ConfirmBiddingController {

  public static boolean isAutoBidding = false;

  private final BigDecimal best = AuctionSession.getInstance().getCurrentAuction().currentPrice();
  private final BigDecimal minIncrement =
      AuctionSession.getInstance().getCurrentAuction().minimumIncrement();
  private final BigDecimal balance = UserSession.getInstance().getCurrentUser().walletBalance();
  private final String userId = UserSession.getInstance().getCurrentUser().id();
  private final String currentAuctionId =
      AuctionSession.getInstance().getCurrentAuction().auctionId();

  @FXML private Button btnMore;

  @FXML private CheckBox chboxAutoBidding;

  @FXML private Label lblBalance;

  @FXML private Label lblError;

  @FXML private VBox boxAutoBidding;

  @FXML private Label lblBest;

  @FXML private Label lblMinIncrement;

  @FXML private TextField txtSetAuto;

  @FXML private TextField txtSetMaxAuto;

  @FXML private TextField txtSetPrice;

  @FXML private Button btnConfirm;

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
    // THÊM AUTO-BID OPTIONAL: mỗi lần mở màn confirm thì mặc định tắt auto-bid.
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
    // THÊM AUTO-BID UI: format cả ô max auto-bid để parse tiền giống các ô khác.
    settingMoneyFormat(txtSetMaxAuto);
  }

  @FXML
  void handleBack(ActionEvent event) throws IOException {
    SceneSwitcherUtils.NewSceneController(
        event,
        "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml",
        "Thông tin sản phẩm");
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
    try {
      // 1. Kiểm tra các trường văn bản trống trước
      requireText(txtSetPrice.getText(), "Giá tiền cược");

      if (isAutoBidding) {
        requireText(txtSetAuto.getText(), "Giá tiền tự đặt cược");
        requireText(txtSetMaxAuto.getText(), "Giá tiền tự đặt cược");
      }

      // 2. Chuyển đổi dữ liệu chuỗi sang BigDecimal sau khi đã chắc chắn không trống
      BigDecimal bidPrice = purifyingText(txtSetPrice.getText());

      // 3. Thực hiện các logic kiểm tra Auto Bidding chuyên sâu bằng Utils mới
      if (isAutoBidding) {
        BigDecimal autoStep = purifyingText(txtSetAuto.getText());
        BigDecimal maxAuto = purifyingText(txtSetMaxAuto.getText());

        // Kiểm tra bước tiền tự đặt cược >= bước giá
        requireValidAutoStep(autoStep, minIncrement);

        // Kiểm tra tiền tự đặt tối đa >= giá hiện tại nhập vào
        requireValidAutoMax(maxAuto, bidPrice);
      }

      // 4. Kiểm tra logic đặt cược thông thường
      requireGreaterThanBest(bidPrice, best);

      // Kiểm tra số tiền lệch ra phải đạt tối thiểu bước giá (minIncrement)
      requireValidIncrement(bidPrice, best, minIncrement);

    } catch (ValidationException e) {
      String errorMessage = e.getMessage();
      // Custom
      if ("Vui lòng nhiều hơn mức quy định.".equals(errorMessage)) {
        errorMessage = "Vui lòng nhiều hơn mức " + lblMinIncrement.getText() + ".";
      }

      lblError.setText(errorMessage);
      lblError.setVisible(true);
      lblError.setTextFill(Color.web("#FF8A80"));
      return;
    }

    // Khóa nút đặt giá
    btnConfirm.setDisable(true);
    lblError.setVisible(false);
    lblError.setManaged(false);
    // Lấy số tiền người dùng chốt đặt
    BigDecimal finalBidAmount = purifyingText(txtSetPrice.getText());
    // THÊM AUTO-BID UI: chụp lại trạng thái checkbox để request async không bị lệch nếu UI thay
    // đổi.
    boolean autoBidEnabled = isAutoBidding;

    // Gói hàng gửi đi
    PlaceBidRequest payload =
        new PlaceBidRequest(
            UUID.fromString(currentAuctionId), // ID phiên đấu giá lấy từ biến ở trên
            UUID.fromString(userId), // ID người dùng hiện tại
            finalBidAmount // Số tiền cược
            );
    Request bidReq = new Request("PLACE_BID", payload);

    // THÊM AUTO-BID UI: nếu user bật checkbox thì gửi cấu hình auto-bid trước khi đặt giá.
    Request autoBidReq = null;
    if (autoBidEnabled) {
      ConfigureAutoBidRequest autoBidPayload =
          new ConfigureAutoBidRequest(
              UUID.fromString(currentAuctionId),
              UUID.fromString(userId),
              purifyingText(txtSetMaxAuto.getText()),
              purifyingText(txtSetAuto.getText()),
              true);
      autoBidReq = new Request("CONFIGURE_AUTO_BID", autoBidPayload);
    }
    Request finalAutoBidReq = autoBidReq;

    // Chuẩn bị hình ảnh cho Alert
    Image image =
        new Image(
            Objects.requireNonNull(
                getClass().getResourceAsStream("/com/auctionapp/auctionappjava/images/Mari.jpg")));
    ImageView imageView = new ImageView(image);
    imageView.setPreserveRatio(true);
    imageView.setFitWidth(500);

    CompletableFuture.supplyAsync(
            () -> {
              try {
                // THÊM AUTO-BID UI: lưu cấu hình auto-bid trước, nếu fail thì dừng và báo lỗi.
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
            })
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    if (response.success()) {
                      // Cập nhật lại UserSession
                      LoginResponse oldUser = UserSession.getInstance().getCurrentUser();
                      BigDecimal latestBalance = oldUser.walletBalance().subtract(finalBidAmount);
                      BigDecimal latestAuctionPrice = finalBidAmount;

                      // Khởi tạo biến lấy số lượng Bidder (mặc định lấy số cũ nếu lỗi)
                      AuctionSummaryResponse oldData =
                          AuctionSession.getInstance().getCurrentAuction();
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
                      LoginResponse updatedUser =
                          new LoginResponse(
                              oldUser.id(),
                              oldUser.username(),
                              oldUser.fullName(),
                              oldUser.role(),
                              oldUser.email(),
                              latestBalance,
                              oldUser.accStatus());
                      UserSession.getInstance().setCurrentUser(updatedUser);

                      // Cập nhật lại AuctionSession
                      AuctionSummaryResponse updatedData =
                          new AuctionSummaryResponse(
                              oldData.auctionId(),
                              oldData.category(),
                              oldData.itemName(),
                              oldData.sellerName(),
                              oldData.description(),
                              oldData.startPrice(),
                              latestAuctionPrice, // THÊM AUTO-BID RESULT: dùng giá cuối cùng sau
                              // auto-bid.
                              oldData.minimumIncrement(),
                              oldData.startDateTime(),
                              oldData.endDateTime(),
                              oldData.timeLeft(),
                              oldData.status(),
                              latestBidderCount,
                              null,
                              oldData.bots());
                      AuctionSession.getInstance().setCurrentAuction(updatedData);

                      // Định nghĩa hành động sẽ xảy ra khi người dùng bấm nút OK trên Alert
                      Runnable goBackToDetail =
                          () -> {
                            try {
                              SceneSwitcherUtils.NewSceneController(
                                  event,
                                  "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml",
                                  "Thông tin sản phẩm");
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

  @FXML
  void handleWallet(ActionEvent event) throws IOException {
    SceneSwitcherUtils.PopupController(
        event, "/com/auctionapp/auctionappjava/views/DepositScreen.fxml", "moneymoneymoney");
  }

  public void showOutbidWarning() {
    Platform.runLater(
        () -> {
          // Khóa cứng nút bấm ngay lập tức
          btnConfirm.setDisable(true);

          Image image =
              new Image(
                  Objects.requireNonNull(
                      getClass()
                          .getResourceAsStream(
                              "/com/auctionapp/auctionappjava/images/ErrorMari.jpg")));
          ImageView imageView = new ImageView(image);
          imageView.setPreserveRatio(true);
          imageView.setFitWidth(500);

          Runnable goBackToDetail =
              () -> {
                try {
                  // Mẹo: Tạo một sự kiện fake click từ nút btnConfirm để mượn xài hàm
                  // SceneSwitcherUtils
                  ActionEvent dummyEvent = new ActionEvent(btnConfirm, null);
                  SceneSwitcherUtils.NewSceneController(
                      dummyEvent,
                      "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml",
                      "Thông tin sản phẩm");
                } catch (Exception e) {
                  e.printStackTrace();
                }
              };

          AlertUtils.AnnouncementController(
              "Thông báo!",
              "Có thay đổi trong phiên đấu giá, hãy quay về màn hình chi tiết để tìm hiểu",
              goBackToDetail,
              imageView);
        });
  }
}
