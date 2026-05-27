package com.auctionapp.auctionappjava.client.controllers;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.formatPriceColumn;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public class HistoryListController implements Initializable {
  private ObservableList<BidHistoryResponse> historyDataList = FXCollections.observableArrayList();

  public static HistoryListController instance;

  // Everything that happened in this shit is due to THIS mtfking Response.
  // Bản chất là HLC này == ULC, nhưng khác chí mạng ở việc khác Response.

  @FXML private ComboBox<String> cbFilterStatus;
  @FXML private ComboBox<String> cbType;
  @FXML private TableView<BidHistoryResponse> historyTable;
  @FXML private TableColumn<BidHistoryResponse, String> clmAuctionName;
  @FXML private TableColumn<BidHistoryResponse, String> clmCategory;
  @FXML private TableColumn<BidHistoryResponse, String> clmBidderName;
  @FXML private TableColumn<BidHistoryResponse, BigDecimal> clmStartingPrice;
  @FXML private TableColumn<BidHistoryResponse, String> clmStatus;
  @FXML private TableColumn<BidHistoryResponse, BigDecimal> clmBiddingMoney;
  @FXML private TableColumn<BidHistoryResponse, String> clmBiddedTime; // Thời điểm đặt
  @FXML private Label txtHistory;
  @FXML private TextField txtSearch;
  @FXML private Button btnSearch;

  // Thêm FilteredList để làm bộ lọc
  private FilteredList<BidHistoryResponse> filteredData;

  @FXML
  void handleSearch(ActionEvent event) {
    String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
    String selectedStatus = cbFilterStatus.getValue();
    String selectedCategory = cbType.getValue();

    filteredData.setPredicate(
        bid -> {
          // 1. Khớp Tên Sản phẩm HOẶC Tên Bidder (nếu là Admin)
          boolean matchKeyword =
              keyword.isEmpty()
                  || (bid.auctionName() != null
                      && bid.auctionName().toLowerCase().contains(keyword));

          // Nếu là Admin, cho phép tìm kiếm thêm theo tên người đặt
          if (LoginController.adminRoute && !matchKeyword) {
            matchKeyword =
                bid.bidderName() != null && bid.bidderName().toLowerCase().contains(keyword);
          }

          // 2. Khớp Trạng thái
          boolean matchStatus = false;
          if (selectedStatus == null || selectedStatus.equals("Tất cả trạng thái")) {
            matchStatus = true;
          } else {
            String translatedStatus = "";
            if (bid.auctionStatus() != null) {
              translatedStatus =
                  switch (bid.auctionStatus()) {
                    case OPEN -> "MỞ";
                    case RUNNING -> "ĐANG DIỄN RA";
                    case FINISHED -> "KẾT THÚC";
                    default -> translatedStatus;
                  };
            }
            matchStatus = translatedStatus.equals(selectedStatus);
          }

          // 3. Khớp Thể loại
          boolean matchCategory = false;
          if (selectedCategory == null || selectedCategory.equals("Tất cả thể loại")) {
            matchCategory = true;
          } else {
            String serverCategory =
                switch (selectedCategory) {
                  case "Nghệ thuật" -> "ART";
                  case "Điện tử" -> "ELECTRONICS";
                  case "Phương tiện" -> "VEHICLE";
                  default -> "";
                };
            matchCategory =
                bid.category() != null && bid.category().equalsIgnoreCase(serverCategory);
          }

          return matchKeyword && matchStatus && matchCategory;
        });
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instance = this;

    // 1. Kiểm tra vai trò để ẩn/hiện cột Tên Bidder
    clmBidderName.setVisible(LoginController.adminRoute);

    // 2. Thiết lập ComboBox và FilteredList
    String[] statuses = {"Tất cả trạng thái", "MỞ", "ĐANG DIỄN RA", "KẾT THÚC", "ĐÃ TRẢ TIỀN/HỦY"};
    cbFilterStatus.getItems().addAll(statuses);
    cbFilterStatus.setValue("Tất cả trạng thái");

    String[] type = {"Tất cả thể loại", "Nghệ thuật", "Điện tử", "Phương tiện"};
    cbType.getItems().addAll(type);
    cbType.setValue("Tất cả thể loại");

    show();

    // Khởi tạo các cột và set data vào bảng
    setupColumns();

    // Bọc dữ liệu bằng FilteredList
    filteredData = new FilteredList<>(historyDataList, p -> true);
    historyTable.setItems(filteredData); // Gắn filteredData vào bảng

    // Tự động kéo dữ liệu từ mạng khi mở màn hình
    loadAuctionsFromServer(true);
  }

  private void loadAuctionsFromServer(boolean isInitialLoad) {
    // Khóa giao diện search
    if (isInitialLoad) {
      txtSearch.setDisable(true);
      cbFilterStatus.setDisable(true);
      cbType.setDisable(true);
      btnSearch.setDisable(true);

      ProgressIndicator loadingSpinner = new ProgressIndicator();
      loadingSpinner.setMaxSize(50, 50);
      historyTable.setPlaceholder(loadingSpinner);

      // Xóa sạch list cũ để màn hình trống trong lúc hiện vòng xoay
      historyDataList.clear();
    }

    // Triển khai 2 Requests riêng biệt (giống AuctionListController)

    Request req = new Request("GET_HISTORY", null);
    if (LoginController.bidderRoute) {
      ManagerAndHistoryRequest bidReq =
          new ManagerAndHistoryRequest(UserSession.getInstance().getCurrentUser().id().toString());
      req = new Request("GET_HISTORY", bidReq);
      // Trước mắt là GET_HISTORY trước

    } else if (LoginController.adminRoute) {
      req = new Request("GET_TRANSACTIONS", null);
    }

    Request finalReq = req;
    CompletableFuture.supplyAsync(
            () -> {
              try {
                return Client.getInstance().sendRequest(finalReq);
              } catch (Exception e) {
                e.printStackTrace();
                return new Response(false, "Lỗi kết nối Server", null);
              }
            })
        .thenAccept(
            response -> {
              Platform.runLater(
                  () -> {
                    // Mở lại nút nếu đã khóa ở trên
                    if (isInitialLoad) {
                      txtSearch.setDisable(false);
                      cbFilterStatus.setDisable(false);
                      cbType.setDisable(false);
                      btnSearch.setDisable(false);
                    }

                    if (response.success()) {
                      // Ép kiểu lấy danh sách từ Response
                      List<BidHistoryResponse> listFromServer =
                          (List<BidHistoryResponse>) response.data();

                      // Xóa dữ liệu cũ và cập nhật dữ liệu mới vào bảng
                      historyDataList.setAll(listFromServer);
                    } else {
                      Alert alert = new Alert(Alert.AlertType.ERROR, response.message());
                      alert.show();
                    }

                    // Nếu tải xong mà danh sách vẫn trống trơn hoặc search không có bid nào phù
                    // hợp, đổi vòng xoay thành dòng chữ
                    Label noDataLabel =
                        new Label(
                            "Chưa tham gia phiên đấu giá nào hoặc không tìm thấy kết quả phù hợp.");
                    noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
                    historyTable.setPlaceholder(noDataLabel);
                  });
            });
  }

  private void setupColumns() {
    // Dùng SimpleStringProperty cho các cột chứa chuỗi (String)
    clmCategory.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().category()));

    clmAuctionName.setCellValueFactory(
        cell -> new SimpleStringProperty(cell.getValue().auctionName()));

    clmBidderName.setCellValueFactory(
        cell -> new SimpleStringProperty(cell.getValue().bidderName()));

    // Nếu bạn có khai báo các cột này trong FXML, hãy map dữ liệu tương tự
    clmStatus.setCellValueFactory(
        cell -> new SimpleStringProperty(String.valueOf(cell.getValue().auctionStatus())));

    clmBiddedTime.setCellValueFactory(
        cell -> new SimpleStringProperty(cell.getValue().biddedTime()));

    clmStartingPrice.setCellValueFactory(
        cell -> new SimpleObjectProperty<>(cell.getValue().auctionStartPrice()));
    clmBiddingMoney.setCellValueFactory(
        cell -> new SimpleObjectProperty<>(cell.getValue().amount()));
    formatPriceColumn(clmStartingPrice);
    formatPriceColumn(clmBiddingMoney);
  }

  void show() {
    if (LoginController.bidderRoute) {
      txtHistory.setVisible(true);
      txtHistory.setText("Lịch sử đặt cược");
    } else if (LoginController.adminRoute) {
      txtHistory.setVisible(true);
      txtHistory.setText("Danh sách đặt cược");
    }
  }

  // Hàm này sẽ được RealtimeHandler gọi
  public void refreshHistoryRealtime() {
    Platform.runLater(() -> loadAuctionsFromServer(false));
  }
}
