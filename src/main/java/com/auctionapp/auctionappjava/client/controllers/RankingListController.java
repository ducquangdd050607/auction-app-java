package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.common.dto.BidRankingResponse;
import com.auctionapp.auctionappjava.common.dto.ManagerAndHistoryRequest;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.formatMoney;

public class RankingListController {

    private final ObservableList<BidRankingResponse> bidderData = FXCollections.observableArrayList();
    private static final DateTimeFormatter BID_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML
    private Button btnBack;

    @FXML
    private Button btnExportWinner;

    @FXML
    private TableColumn<BidRankingResponse, BigDecimal> colBidAmount;

    @FXML
    private TableColumn<BidRankingResponse, String> colBidTime;

    @FXML
    private TableColumn<BidRankingResponse, Integer> colRank;

    @FXML
    private TableColumn<BidRankingResponse, String> colUsername;

    @FXML
    private Label lblMinIncrement;

    @FXML
    private Label lblCategory;

    @FXML
    private Label lblItemName;

    @FXML
    private Label lblStartingPrice;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblTopBid;

    @FXML
    private Label lblTopBidder;

    @FXML
    private TableView<BidRankingResponse> tableBidders;

    @FXML
    private LineChart<String, Number> bidLineChart;

    @FXML
    void initialize() {
        var currentAuction = AuctionSession.getInstance().getCurrentAuction();

        lblStartingPrice.setText(formatMoney(currentAuction.startPrice()) + " VND");
        lblTopBid.setText(formatMoney(currentAuction.currentPrice()) + " VND");
        lblMinIncrement.setText(formatMoney(currentAuction.minimumIncrement()) + " VND");
        lblCategory.setText(currentAuction.category());
        lblItemName.setText(currentAuction.itemName());
        lblStatus.setText(currentAuction.status().toString());
        lblTopBidder.setText("Dang tai...");

        setupColumns();
        setupChart();
        tableBidders.setItems(bidderData);
        loadBiddersFromServer();
    }

    private void setupColumns() {
        colRank.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().rank()));
        colUsername.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().bidderName()));
        colBidAmount.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().amount()));
        colBidAmount.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : formatMoney(amount) + " VND");
            }
        });
        colBidTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().bidTime()));
    }

    private void setupChart() {
        bidLineChart.setTitle("Bien dong gia bid");
        bidLineChart.setLegendVisible(false);
        bidLineChart.setAnimated(false);
    }

    private void loadBiddersFromServer() {
        btnExportWinner.setDisable(true);

        ProgressIndicator loadingSpinner = new ProgressIndicator();
        loadingSpinner.setMaxSize(50, 50);
        tableBidders.setPlaceholder(loadingSpinner);
        bidderData.clear();
        bidLineChart.getData().clear();

        String auctionId = AuctionSession.getInstance().getCurrentAuction().auctionId();
        Request req = new Request("GET_BID_RANKING", new ManagerAndHistoryRequest(auctionId));

        CompletableFuture.supplyAsync(() -> {
            try {
                return Client.getInstance().sendRequest(req);
            } catch (Exception e) {
                e.printStackTrace();
                return new Response(false, "Loi ket noi Server", null);
            }
        }).thenAccept(response -> Platform.runLater(() -> {
            btnExportWinner.setDisable(false);

            if (response.success()) {
                List<BidRankingResponse> rows = (List<BidRankingResponse>) response.data();
                bidderData.setAll(rows);
                updateWinner(rows);
                updateChart(rows);
            } else {
                new Alert(Alert.AlertType.ERROR, response.message()).show();
            }

            Label noDataLabel = new Label("Chua co lich su bid cho phien nay.");
            noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
            tableBidders.setPlaceholder(noDataLabel);
        }));
    }

    private void updateWinner(List<BidRankingResponse> rows) {
        if (rows == null || rows.isEmpty()) {
            lblTopBidder.setText("Chua co");
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
            // force-remove
        } else if (LoginController.sellerRoute) {
            // seller remove flow
        }
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        if (LoginController.bidderRoute) {
            SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml", "Thong tin san pham");
        } else {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    void handleBidders(ActionEvent event) {
        loadBiddersFromServer();
    }

    @FXML
    void handleExportWinner(ActionEvent event) {
        if (bidderData.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Chua co nguoi thang vi phien nay chua co bid.").show();
            return;
        }

        BidRankingResponse winner = bidderData.get(0);
        System.out.println("[RANKING_WINNER] Auction "
                + AuctionSession.getInstance().getCurrentAuction().auctionId()
                + " winner: " + winner.bidderName()
                + ", amount: " + winner.amount()
                + ", bidTime: " + winner.bidTime());
        new Alert(
                Alert.AlertType.INFORMATION,
                "Nguoi thang: " + winner.bidderName()
                        + "\nGia thang: " + formatMoney(winner.amount()) + " VND"
                        + "\nThoi gian dat: " + winner.bidTime()
        ).show();
    }
}
