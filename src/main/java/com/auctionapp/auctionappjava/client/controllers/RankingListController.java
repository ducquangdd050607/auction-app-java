package com.auctionapp.auctionappjava.client.controllers;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.formatMoney;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class RankingListController {

  private final ObservableList<BidRankingResponse> bidderData = FXCollections.observableArrayList();
  private static final DateTimeFormatter BID_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
  public static RankingListController instance;

  @FXML private Button btnBack;

  @FXML private Button btnExportWinner;

  @FXML private Button btnRemove;

  @FXML private TableColumn<BidRankingResponse, BigDecimal> colBidAmount;

  @FXML private TableColumn<BidRankingResponse, String> colBidTime;

  @FXML private TableColumn<BidRankingResponse, Integer> colRank;

  @FXML private TableColumn<BidRankingResponse, String> colUsername;

  @FXML private Label lblDescription;

  @FXML private Label lblMinIncrement;

  @FXML private Label lblCategory;

  @FXML private Label lblItemName;

  @FXML private Label lblStartingPrice;

  @FXML private Label lblStatus;

  @FXML private Label lblTopBid;

  @FXML private Label lblTopBidder;

  @FXML private TableView<BidRankingResponse> tableBidders;

  @FXML private LineChart<String, Number> bidLineChart;

  @FXML
  void initialize() {
    instance = this;
    AuctionSummaryResponse currentAuction = AuctionSession.getInstance().getCurrentAuction();

    lblStartingPrice.setText(formatMoney(currentAuction.startPrice()) + " VND");
    lblTopBid.setText(formatMoney(currentAuction.currentPrice()) + " VND");
    lblMinIncrement.setText(formatMoney(currentAuction.minimumIncrement()) + " VND");
    lblCategory.setText(currentAuction.category());
    lblItemName.setText(currentAuction.itemName());
    lblStatus.setText(currentAuction.status().toString());
    lblDescription.setText(currentAuction.description());
    lblTopBidder.setText("Đang tải...");

    show();

    setupColumns();
    setupChart();

    // Bọc dữ liệu để hỗ trợ sort khi click vào Header cột (Giống bên AuctionList)
    SortedList<BidRankingResponse> sortedData = new SortedList<>(bidderData);
    sortedData.comparatorProperty().bind(tableBidders.comparatorProperty());

    tableBidders.setItems(sortedData);

    loadBiddersFromServer(true);
  }

  private void show() {
    if (LoginController.bidderRoute) {
      btnRemove.setVisible(false);
      btnRemove.setManaged(false);
    } else {
      btnRemove.setVisible(true);
      btnRemove.setManaged(true);
    }
  }

  private void setupColumns() {
    colRank.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().rank()));
    colUsername.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().bidderName()));
    colBidAmount.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().amount()));
    colBidAmount.setCellFactory(
        column ->
            new TableCell<>() {
              @Override
              protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : formatMoney(amount) + " VND");
              }
            });
    colBidTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().bidTime()));
  }

  private void setupChart() {
    bidLineChart.setTitle("Biểu đồ biến động giá");
    bidLineChart.setLegendVisible(false);
    bidLineChart.setAnimated(false);
  }

  private void loadBiddersFromServer(boolean isInitialLoad) {
    btnExportWinner.setDisable(true);

    // Chỉ hiện vòng xoay và dọn dẹp bảng nếu là lần đầu tiên mở màn hình
    if (isInitialLoad) {
      ProgressIndicator loadingSpinner = new ProgressIndicator();
      loadingSpinner.setMaxSize(50, 50);
      tableBidders.setPlaceholder(loadingSpinner);

      // Xóa data cũ ngay lập tức để màn hình trống trong lúc xoay vòng vòng
      bidderData.clear();
      bidLineChart.getData().clear();
    }

    String auctionId = AuctionSession.getInstance().getCurrentAuction().auctionId();
    Request req = new Request("GET_BID_RANKING", new ManagerAndHistoryRequest(auctionId));

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
            response ->
                Platform.runLater(
                    () -> {
                      btnExportWinner.setDisable(false);

                      if (response.success()) {
                        List<BidRankingResponse> rows = (List<BidRankingResponse>) response.data();

                        // Nếu là tải Realtime ngầm, đợi có data mới mang về rỗi mới xóa data cũ
                        if (!isInitialLoad) {
                          bidderData.clear();
                          bidLineChart.getData().clear();
                        }

                        bidderData.setAll(rows);
                        updateWinner(rows);
                        updateChart(rows);

                        // Dữ liệu trống thì hiện thông báo
                        if (rows.isEmpty()) {
                          Label noDataLabel = new Label("Chưa có lịch sử bid cho phiên này");
                          noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffffff;");
                          tableBidders.setPlaceholder(noDataLabel);
                        }
                      } else {
                        new Alert(Alert.AlertType.ERROR, response.message()).show();

                        // Bị lỗi mạng thì cũng hiện chữ báo lỗi
                        Label errorLabel = new Label("Lỗi tải dữ liệu");
                        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #E74C3C;");
                        tableBidders.setPlaceholder(errorLabel);
                      }
                    }));
  }

  private void updateWinner(List<BidRankingResponse> rows) {
    if (rows == null || rows.isEmpty()) {
      lblTopBidder.setText("Chưa có ai đặt bid");
      return;
    }

    BidRankingResponse winner = rows.get(0);
    lblTopBidder.setText(winner.bidderName());
    lblTopBid.setText(formatMoney(winner.amount()) + " VND");
  }

  private void updateChart(List<BidRankingResponse> rows) {
    bidLineChart.getData().clear();
    if (rows == null || rows.isEmpty()) {
      return;
    }

    XYChart.Series<String, Number> series = new XYChart.Series<>();
    rows.stream()
        .sorted(Comparator.comparing(row -> LocalDateTime.parse(row.bidTime(), BID_TIME_FORMATTER)))
        .forEach(row -> series.getData().add(new XYChart.Data<>(row.bidTime(), row.amount())));
    bidLineChart.getData().add(series);
  }

  @FXML
  void handleRemove(ActionEvent event) {
    if (LoginController.adminRoute) {
      removeAuction();
    } else if (LoginController.sellerRoute) {

      if (AuctionSession.getInstance().getCurrentAuction().status().equals(AuctionStatus.OPEN)) {
        removeAuction();

      } else {
        AlertUtils.AnnouncementController(
            "Không đủ tư cách xóa",
            "SELLER không thể xóa các phiên đang chạy hoặc đã kết thúc",
            null,
            null);
      }
    }
  }

  private void removeAuction() {

    Runnable remove =
        () -> {
          RemoveAuctionRequest removeReq =
              new RemoveAuctionRequest(
                  UserSession.getInstance().getCurrentUser().id(),
                  AuctionSession.getInstance().getCurrentAuction().auctionId());
          Request req = new Request("REMOVE_AUCTION", removeReq);

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
                            AlertUtils.AnnouncementController(
                                "Đã xong", "Phiên đấu đã bay màu", null, null);
                            Stage stage = (Stage) btnRemove.getScene().getWindow();
                            stage.close();

                          } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR, response.message());
                            alert.show();
                          }
                        });
                  });
        };

    AlertUtils.AnnouncementController("Chắc chắn chưa?", "Phiên đấu này sẽ bay màu", remove, null);
  }

  @FXML
  void handleBack(ActionEvent event) throws IOException {
    if (LoginController.bidderRoute) {
      SceneSwitcherUtils.NewSceneController(
          event,
          "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml",
          "Thong tin san pham");
    } else {
      Stage stage = (Stage) btnBack.getScene().getWindow();
      stage.close();
    }
  }

  @FXML
  void handleBidders(ActionEvent event) {
    loadBiddersFromServer(true);
  }

  @FXML
  void handleExportWinner(ActionEvent event) {
    if (bidderData.isEmpty()) {
      new Alert(Alert.AlertType.INFORMATION, "Chua co nguoi thang vi phien nay chua co bid.")
          .show();
      return;
    }

    BidRankingResponse winner = bidderData.get(0);

    new Alert(
            Alert.AlertType.INFORMATION,
            "Người thắng: "
                + winner.bidderName()
                + "\nGiá thắng: "
                + formatMoney(winner.amount())
                + " VND"
                + "\nThời gian đặt: "
                + winner.bidTime())
        .show();
  }

  public void refreshRankingRealtime() {
    // Đơn giản là gọi lại hàm tải dữ liệu
    Platform.runLater(() -> loadBiddersFromServer(false));
  }
}
