package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.common.dto.AuctionTrendResponse;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.formatPriceColumn;

public class AuctionTrendController implements Initializable {
    private final ObservableList<AuctionTrendResponse> trendData = FXCollections.observableArrayList();

    @FXML
    private Button btnReload;
    @FXML
    private Label lblSummary;
    @FXML
    private TableView<AuctionTrendResponse> tblTrends;
    @FXML
    private TableColumn<AuctionTrendResponse, String> clmItemName;
    @FXML
    private TableColumn<AuctionTrendResponse, BigDecimal> clmCurrentPrice;
    @FXML
    private TableColumn<AuctionTrendResponse, BigDecimal> clmScore;
    @FXML
    private TableColumn<AuctionTrendResponse, String> clmLabel;
    @FXML
    private TableColumn<AuctionTrendResponse, String> clmReason;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        tblTrends.setItems(trendData);
        loadTrends();
    }

    @FXML
    void handleReload(ActionEvent event) {
        loadTrends();
    }

    private void setupColumns() {
        clmItemName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().itemName()));
        clmCurrentPrice.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().currentPrice()));
        clmScore.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().trendScore()));
        clmLabel.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().trendLabel()));
        clmReason.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().reason()));
        formatPriceColumn(clmCurrentPrice);
    }

    private void loadTrends() {
        btnReload.setDisable(true);
        lblSummary.setText("Đang tải xu hướng...");
        tblTrends.setPlaceholder(new ProgressIndicator());
        trendData.clear();

        Request request = new Request("GET_AUCTION_TRENDS", null);
        CompletableFuture.supplyAsync(() -> {
            try {
                return Client.getInstance().sendRequest(request);
            } catch (Exception e) {
                e.printStackTrace();
                return new Response(false, "Lỗi kết nối Server", null);
            }
        }).thenAccept(response -> Platform.runLater(() -> applyResponse(response)));
    }

    private void applyResponse(Response response) {
        btnReload.setDisable(false);
        if (response.success()) {
            List<AuctionTrendResponse> trends = (List<AuctionTrendResponse>) response.data();
            trendData.setAll(trends);
            lblSummary.setText("Tổng " + trendData.size() + " phiên | ");
        } else {
            lblSummary.setText(response.message());
        }

        Label noDataLabel = new Label("Chưa có dữ liệu xu hướng.");
        noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
        tblTrends.setPlaceholder(noDataLabel);
    }
}
