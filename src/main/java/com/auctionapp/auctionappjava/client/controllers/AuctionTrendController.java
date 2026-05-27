package com.auctionapp.auctionappjava.client.controllers;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.formatPriceColumn;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.common.dto.AuctionTrendResponse;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AuctionTrendController implements Initializable {
  private final ObservableList<AuctionTrendResponse> trendData =
      FXCollections.observableArrayList();

  public static AuctionTrendController instance;

  @FXML private Label lblSummary;
  @FXML private TableView<AuctionTrendResponse> tblTrends;
  @FXML private TableColumn<AuctionTrendResponse, String> clmItemName;
  @FXML private TableColumn<AuctionTrendResponse, BigDecimal> clmCurrentPrice;
  @FXML private TableColumn<AuctionTrendResponse, BigDecimal> clmScore;
  @FXML private TableColumn<AuctionTrendResponse, String> clmLabel;
  @FXML private TableColumn<AuctionTrendResponse, String> clmReason;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instance = this;

    setupColumns();
    tblTrends.setItems(trendData);
    loadTrends(true);
  }

  private void setupColumns() {
    clmItemName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().itemName()));
    clmCurrentPrice.setCellValueFactory(
        cell -> new SimpleObjectProperty<>(cell.getValue().currentPrice()));
    clmScore.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().trendScore()));
    clmLabel.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().trendLabel()));
    clmReason.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().reason()));
    formatPriceColumn(clmCurrentPrice);
  }

  private void loadTrends(boolean isInitialLoad) {
    // Chỉ khóa nút và hiện vòng xoay khi lần đầu load list
    if (isInitialLoad) {
      ProgressIndicator loadingSpinner = new ProgressIndicator();
      loadingSpinner.setMaxSize(50, 50);
      lblSummary.setText("Đang tải xu hướng...");
      tblTrends.setPlaceholder(loadingSpinner);

      // Xóa sạch list cũ để màn hình trống trong lúc hiện vòng xoay
      trendData.clear();
    }

    Request request = new Request("GET_AUCTION_TRENDS", null);
    CompletableFuture.supplyAsync(
            () -> {
              try {
                return Client.getInstance().sendRequest(request);
              } catch (Exception e) {
                e.printStackTrace();
                return new Response(false, "Lỗi kết nối Server", null);
              }
            })
        .thenAccept(
            response ->
                Platform.runLater(
                    () -> {
                      if (response.success()) {
                        List<AuctionTrendResponse> trends =
                            (List<AuctionTrendResponse>) response.data();
                        trendData.setAll(trends);
                        lblSummary.setText("Tổng " + trendData.size() + " phiên | ");
                      } else {
                        lblSummary.setText(response.message());
                      }

                      Label noDataLabel = new Label("Chưa có dữ liệu xu hướng.");
                      noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
                      tblTrends.setPlaceholder(noDataLabel);
                    }));
  }
}
