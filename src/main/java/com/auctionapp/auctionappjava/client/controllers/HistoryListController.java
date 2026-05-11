package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class HistoryListController implements Initializable {
    private ObservableList<BidHistoryResponse> historyDataList = FXCollections.observableArrayList();
    // Everything that happened in this shit is due to THIS mtfking Response.
    // Bản chất là HLC này == ULC, nhưng khác chí mạng ở việc khác Response.
    @FXML
    private ComboBox<String> cbFilterStatus;
    @FXML
    private ComboBox<String> cbType;
    @FXML
    private TableView<BidHistoryResponse> historyTable;
    @FXML
    private TableColumn<BidHistoryResponse, String> clmAuctionName;
    @FXML
    private TableColumn<BidHistoryResponse, String> clmBidderName;
    @FXML
    private TableColumn<BidHistoryResponse, BigDecimal> clmStartingPrice;
    @FXML
    private TableColumn<BidHistoryResponse, String> clmStatus;
    @FXML
    private TableColumn<BidHistoryResponse, BigDecimal> clmBiddingMoney;
    @FXML
    private TableColumn<BidHistoryResponse, String> clmBiddedTime; // Thời điểm đặt
    @FXML
    private TableColumn<BidHistoryResponse, ?> clmChoose;
    @FXML
    private Label txtHistory;
    @FXML
    private TextField txtSearch;

    @FXML
    void handleSearch(ActionEvent event) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //lọc và kiểm tra kiểu người dùng - đưa ra các btn tương ứng
        String[] statuses = {"MỞ", "ĐANG DIỄN RA", "KẾT THÚC", "ĐÃ TRẢ TIỀN/HỦY"}; //trạng thái
        cbFilterStatus.getItems().addAll(statuses);

        String[] type = {};//manual-added

        cbType.getItems().addAll(type);

        show();

        // Khởi tạo các cột và set data vào bảng
        setupColumns();
        historyTable.setItems(historyDataList);

        // Tự động kéo dữ liệu từ mạng khi mở màn hình
        loadAuctionsFromServer();

    }

    private void loadAuctionsFromServer() {
        ProgressIndicator loadingSpinner = new ProgressIndicator();
        loadingSpinner.setMaxSize(50, 50);
        historyTable.setPlaceholder(loadingSpinner);
        historyDataList.clear(); // Xóa sạch dữ liệu cũ trong lúc chờ tải mới

        // Triển khai 2 Requests riêng biệt (giống AuctionListController)

        Request req = new Request("GET_HISTORY", null);
        if (LoginController.bidderRoute) {
            BidManagerAndHistoryRequest bidReq = new BidManagerAndHistoryRequest(UserSession.getInstance().getCurrentUser().id().toString());
            req = new Request("GET_HISTORY", bidReq);
        // Trước mắt là GET_HISTORY trước

        } else if (LoginController.adminRoute) {
            req = new Request("GET_TRANSACTIONS", null);

        }

        Request finalReq = req;
        CompletableFuture.supplyAsync(() -> {
            try {
                return Client.getInstance().sendRequest(finalReq);
            } catch (Exception e) {
                e.printStackTrace();
                return new Response(false, "Lỗi kết nối Server", null);
            }
        }).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.success()) {
                    // Ép kiểu lấy danh sách từ Response
                    List<BidHistoryResponse> listFromServer = (List<BidHistoryResponse>) response.data();

                    // Xóa dữ liệu cũ và cập nhật dữ liệu mới vào bảng
                    historyDataList.setAll(listFromServer);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, response.message());
                    alert.show();
                }

                // Nếu tải xong mà danh sách vẫn trống trơn, đổi vòng xoay thành dòng chữ
                if (historyDataList.isEmpty()) {
                    Label noDataLabel = new Label("Hiện tại chưa tham gia phiên đấu giá nào.");
                    noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
                    historyTable.setPlaceholder(noDataLabel);
                }
            });
        });
    } private void setupColumns() {
        // Dùng SimpleStringProperty cho các cột chứa chuỗi (String)
        clmAuctionName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().auctionName()));

        clmBidderName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().bidderName()));

        // Nếu bạn có khai báo các cột này trong FXML, hãy map dữ liệu tương tự
        clmStatus.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().auctionStatus())));

        clmBiddedTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().biddedTime()));

        clmStartingPrice.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().auctionStartPrice()));

        clmBiddingMoney.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().amount()));

    }

    void show() {
        if (LoginController.bidderRoute) {
            txtHistory.setVisible(true);
            txtHistory.setText("Lịch sử đặt cược");
        }

        else if  (LoginController.adminRoute) {
            txtHistory.setVisible(true);
            txtHistory.setText("Danh sách đặt cược");
        }
    }
}
