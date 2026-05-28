package com.auctionapp.auctionappjava.client.controllers;

import static com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils.NavSceneController;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.exception.AppException;
import com.auctionapp.auctionappjava.common.util.CompressionUtils;
import com.auctionapp.auctionappjava.common.util.MoneyUtils;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DashboardController implements Initializable {
  // Lưu thông tin các response để lấy dữ liệu làm timeline
  private AuctionSummaryResponse mostBiddedAuction;
  private AuctionSummaryResponse mostTreadingAuction;
  private AuctionSummaryResponse mostExpiredAuction;
  private Timeline dashboardTimer;

  @FXML private Button btnGo1;
  @FXML private Button btnGo2;
  @FXML private Button btnGo3;
  @FXML private Button btnHistory;
  @FXML private Button btnSellerItemManager;
  @FXML private Button btnWallet;
  @FXML private Button btnShowActiveUsers;
  @FXML private Label endTime1;
  @FXML private Label endTime2;
  @FXML private Label endTime3;
  @FXML private VBox itemCard1;
  @FXML private VBox itemCard2;
  @FXML private Label lblBalance;
  @FXML private Label lblBidders;
  @FXML private Label lblCompleted;
  @FXML private Label lblGreeting;
  @FXML private Label lblGreetingSub;
  @FXML private Label lblHistory;
  @FXML private Label lblItemDesc1;
  @FXML private Label lblItemDesc2;
  @FXML private Label lblItemDesc3;
  @FXML private Label lblItemName1;
  @FXML private Label lblItemName2;
  @FXML private Label lblItemName3;
  @FXML private Label lblItemPrice1;
  @FXML private Label lblItemPrice2;
  @FXML private Label lblItemPrice3;
  @FXML private Label lblRUNNINGs;
  @FXML private Label lblTimer1;
  @FXML private Label lblTimer2;
  @FXML private Label lblTimer3;
  @FXML private Label lblLatestOrMostExpiredAuction;
  @FXML private Label lblTitle;
  @FXML private HBox boxItems;
  @FXML private ListView<?> boxLoading;
  @FXML private ImageView imgProduct1;
  @FXML private ImageView imgProduct2;
  @FXML private ImageView imgProduct3;


  @Override
  public void initialize(URL location, ResourceBundle resources) {

    Random rand = new Random();
    String greetingSub;
    int randomInt = rand.nextInt(3);

    if (randomInt == 0) {
      greetingSub = "Have a nice day :D";
    } else if (randomInt == 1) {
      greetingSub = "Good day ni99a >:C";
    }  else {
      greetingSub = "Oi, gitgud son :D";
    }

    try {
      lblGreeting.setText(
          "Xin chào, " + UserSession.getInstance().getCurrentUser().fullName() + "!");

      lblGreetingSub.setText(greetingSub);

      lblBalance.setText(
          MoneyUtils.formatMoney(UserSession.getInstance().getCurrentUser().walletBalance())
              + " VND");
      loadStatDataFromServer();
      show();
    } catch (IOException e) {
      throw new AppException("Không thể khởi tạo dashboard", e);
    }

    loadFeaturedAuctionsFromServer();

    // Khởi tạo bộ đếm
    dashboardTimer =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                event -> {
                  updateTimersUI();
                }));
    dashboardTimer.setCycleCount(Animation.INDEFINITE);
    dashboardTimer.play();
  }

  // Hàm tắt bộ đếm (gọi hàm này khi thoát app hoặc chuyển scene)
  public void stopTimer() {
    if (dashboardTimer != null) dashboardTimer.stop();
  }

  // Hàm quét qua các thẻ và cập nhật Label
  private void updateTimersUI() {
    if (mostBiddedAuction != null) lblTimer1.setText(calculateRemainingTime(mostBiddedAuction));
    if (mostTreadingAuction != null) lblTimer2.setText(calculateRemainingTime(mostTreadingAuction));
    if (mostExpiredAuction != null) lblTimer3.setText(calculateRemainingTime(mostExpiredAuction));
  }

  // Logic tính toán chuỗi thời gian
  private String calculateRemainingTime(AuctionSummaryResponse auction) {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime startTime = LocalDateTime.parse(auction.startDateTime(), formatter);
      LocalDateTime endTime = LocalDateTime.parse(auction.endDateTime(), formatter);

      if (now.isBefore(startTime)) {
        return "Mở sau " + formatDuration(java.time.Duration.between(now, startTime));
      } else if (now.isBefore(endTime)) {
        return formatDuration(java.time.Duration.between(now, endTime));
      } else {
        return "Đã kết thúc";
      }
    } catch (Exception e) {
      return "00:00:00";
    }
  }

  // Hàm format giây sang formatter chuẩn
  private String formatDuration(java.time.Duration duration) {
    if (duration.isNegative() || duration.isZero()) return "00:00:00";
    long hours = duration.toHours();
    long minutes = duration.toMinutesPart();
    long seconds = duration.toSecondsPart();
    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
  }

  public void loadStatDataFromServer() throws IOException {
    Request req = new Request("GET_STAT", UserSession.getInstance().getCurrentUser().id());
    CompletableFuture.supplyAsync(
            () -> {
              try {
                return Client.getInstance().sendRequest(req);
              } catch (Exception e) {
                e.printStackTrace();
                return new Response(false, "Lỗi kết nối Server", null);
              }
            })
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    if (response.success()) {

                      List<Object> statFromServer = (List<Object>) response.data();

                      Object stat = statFromServer.get(0);

                      if (LoginController.bidderRoute) {
                        lblHistory.setText(stat.toString());

                      } else if (LoginController.sellerRoute) {
                        lblCompleted.setText(stat.toString());

                      } else if (LoginController.adminRoute) lblBidders.setText(stat.toString());

                      Object stat2 = statFromServer.get(1);
                      lblRUNNINGs.setText(stat2.toString());

                    } else {
                      Alert alert = new Alert(Alert.AlertType.ERROR, response.message());
                      alert.show();
                    }
                  });
            });
  }

  public void loadFeaturedAuctionsFromServer() {
    boxItems.setVisible(false);
    boxItems.setManaged(false);

    boxLoading.setVisible(true);
    boxLoading.setManaged(true);

    ProgressIndicator loadingSpinner = new ProgressIndicator();
    loadingSpinner.setMaxSize(50, 50);
    boxLoading.setPlaceholder(loadingSpinner);

    Request req = new Request("GET_ALL_FEATURED_AUCTIONS", null);
    CompletableFuture.supplyAsync(
            () -> {
              try {
                return Client.getInstance().sendRequest(req);
              } catch (Exception e) {
                e.printStackTrace();
                return new Response(false, "Lỗi kết nối Server", null);
              }
            })
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    if (response.success()) {
                      boxLoading.setVisible(false);
                      boxLoading.setManaged(false);

                      boxItems.setVisible(true);
                      boxItems.setManaged(true);

                      // Ép kiểu lấy danh sách từ Response
                      List<AuctionSummaryResponse> auctionsFromServer =
                          (List<AuctionSummaryResponse>) response.data();

                      // Điền dữ liệu mới vào bảng
                      mostBiddedAuction = auctionsFromServer.get(0);
                      mostExpiredAuction = auctionsFromServer.get(1);

                      lblItemName3.setText(mostExpiredAuction.itemName());
                      endTime3.setText(mostExpiredAuction.endDateTime());
                      lblItemPrice3.setText(
                              MoneyUtils.formatMoney(mostExpiredAuction.currentPrice()));
                      lblItemDesc3.setText(isDescriptionEmpty(mostExpiredAuction.description()));
                      loadImageForCard(mostExpiredAuction.auctionId(), imgProduct3);

                      btnGo3.setOnAction(
                              event -> {
                                try {
                                  AuctionSession.getInstance().setCurrentAuction(mostExpiredAuction);
                                  handleDetail(mostExpiredAuction);
                                } catch (IOException e) {
                                  e.printStackTrace();
                                }
                              });

                      if (auctionsFromServer.size() < 3) {
                        // Ẩn đi itemCard 1 (Đông nhất) và itemCard 2 (Xu hướng)
                        itemCard1.setVisible(false);
                        itemCard2.setVisible(false);
                        itemCard1.setManaged(false);
                        itemCard2.setManaged(false);

                        lblLatestOrMostExpiredAuction.setText("Phiên đấu giá mới:");

                      } else {
                        mostTreadingAuction = auctionsFromServer.get(2);

                        lblItemName1.setText(mostBiddedAuction.itemName());
                        endTime1.setText(mostBiddedAuction.endDateTime());
                        lblItemPrice1.setText(
                                MoneyUtils.formatMoney(mostBiddedAuction.currentPrice()));
                        lblItemDesc1.setText(isDescriptionEmpty(mostBiddedAuction.description()));
                        loadImageForCard(mostBiddedAuction.auctionId(), imgProduct1);

                        lblItemName2.setText(mostTreadingAuction.itemName());
                        endTime2.setText(mostTreadingAuction.endDateTime());
                        lblItemPrice2.setText(
                                MoneyUtils.formatMoney(mostTreadingAuction.currentPrice()));
                        lblItemDesc2.setText(isDescriptionEmpty(mostTreadingAuction.description()));
                        loadImageForCard(mostTreadingAuction.auctionId(), imgProduct2);

                        btnGo1.setOnAction(
                                event -> {
                                  try {
                                    AuctionSession.getInstance().setCurrentAuction(mostBiddedAuction);
                                    handleDetail(mostBiddedAuction);
                                  } catch (IOException e) {
                                    e.printStackTrace();
                                  }
                                });

                        btnGo2.setOnAction(
                                event -> {
                                  try {
                                    AuctionSession.getInstance().setCurrentAuction(mostTreadingAuction);
                                    handleDetail(mostTreadingAuction);
                                  } catch (IOException e) {
                                    e.printStackTrace();
                                  }
                                });

                      }
                      updateTimersUI();
                    } else {
                      Label noDataLabel = new Label("Không tìm thấy phiên đấu giá nào hoạt động.");
                      noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray; ");
                      boxLoading.setPlaceholder(noDataLabel);
                    }

                    // Luôn luôn cất vòng xoay đi và thay bằng nhãn chữ này

                  });
            });
  }

  @FXML
  void handleDetail(AuctionSummaryResponse auction) throws IOException {
    if (LoginController.bidderRoute) {
      // Bidder
      FXMLLoader loader =
          new FXMLLoader(
              getClass()
                  .getResource("/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml"));
      Parent root = loader.load();

      Stage stage = new Stage();
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.setTitle("Chi tiết: " + auction.itemName());
      stage.setScene(new Scene(root));
      stage.sizeToScene();
      stage.centerOnScreen();
      stage.showAndWait();

      AuctionSession.getInstance().cleanAuctionSession();
    } else {
      // Seller/Admin
      FXMLLoader loader =
          new FXMLLoader(
              getClass()
                  .getResource("/com/auctionapp/auctionappjava/views/RankingListScreen.fxml"));
      Parent root = loader.load();

      Stage stage = new Stage();
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.setTitle("BXH – " + auction.itemName());
      stage.setScene(new Scene(root));
      stage.sizeToScene();
      stage.centerOnScreen();
      stage.showAndWait();
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
    NavSceneController(
        event,
        NavigatorController.getMainBorderPane(),
        "/com/auctionapp/auctionappjava/views/HistoryScreen.fxml");
    NavigatorController.activateHistory();
  }

  @FXML
  void handleManager(ActionEvent event) throws IOException {
    NavigatorController.modeName = "Quản lý vật phẩm";
    NavigatorController.activateAccountButton();
    NavSceneController(
        event,
        NavigatorController.getMainBorderPane(),
        "/com/auctionapp/auctionappjava/views/AuctionListScreen.fxml");
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

    NavSceneController(
        event,
        NavigatorController.getMainBorderPane(),
        "/com/auctionapp/auctionappjava/views/AuctionListScreen.fxml");
  }

  @FXML
  void handleOpenUsersList(ActionEvent event) throws IOException {
    NavigatorController.activateUserManager();
    NavSceneController(
        event,
        NavigatorController.getMainBorderPane(),
        "/com/auctionapp/auctionappjava/views/UsersManagerScreen.fxml");
  }

  @FXML
  void handleWallet(ActionEvent event) throws IOException {
    NavigatorController.activateAccountButton();
    NavSceneController(
        event,
        NavigatorController.getMainBorderPane(),
        "/com/auctionapp/auctionappjava/views/AccountScreen.fxml");
  }

  // Hàm tải ảnh ngầm cho từng khung
  private void loadImageForCard(String auctionId, ImageView targetImageView) {
    Request imgReq = new Request("GET_IMAGE", new ImageRequest(auctionId));

    CompletableFuture.supplyAsync(
            () -> {
              try {
                return Client.getInstance().sendRequest(imgReq);
              } catch (Exception e) {
                return null;
              }
            })
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    if (response != null && response.success() && response.data() != null) {
                      ImageResponse imgRes = (ImageResponse) response.data();
                      byte[] compressedBytes = imgRes.imageData();

                      if (compressedBytes != null && compressedBytes.length > 0) {
                        try {
                          // Giải nén và vẽ ảnh lên UI
                          byte[] originalBytes = CompressionUtils.decompress(compressedBytes);
                          Image realImage =
                              new Image(new java.io.ByteArrayInputStream(originalBytes));
                          targetImageView.setImage(realImage);

                          // Bật tính năng click để zoom ngay sau khi ảnh load xong
                          setupZoomImage(targetImageView);
                        } catch (Exception e) {
                          e.printStackTrace();
                        }
                      }
                    }
                  });
            });
  }

  // Hàm phóng to ảnh khi click vào
  private void setupZoomImage(ImageView imageView) {
    imageView.setOnMouseClicked(
        (javafx.scene.input.MouseEvent event) -> {
          if (imageView.getImage() == null) {
            imageView.setCursor(javafx.scene.Cursor.NONE);
            return;
          }

          Stage zoomStage = new Stage();
          zoomStage.initModality(Modality.APPLICATION_MODAL);
          zoomStage.setTitle("Xem chi tiết ảnh");

          ImageView zoomedImageView = new ImageView(imageView.getImage());
          zoomedImageView.setPreserveRatio(true);
          zoomedImageView.setFitWidth(800);
          zoomedImageView.setFitHeight(600);

          javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane(zoomedImageView);
          root.setOnMouseClicked(e -> zoomStage.close()); // Click ra ngoài để đóng

          Scene scene = new Scene(root, 900, 700);
          zoomStage.setScene(scene);
          zoomStage.centerOnScreen();
          zoomStage.showAndWait();
        });
  }

  private String isDescriptionEmpty(String description) {
    return (description == null || description.trim().isEmpty()) ? "Không có mô tả" : description;
  }
}
