package com.auctionapp.auctionappjava.client.controllers;

<<<<<<< HEAD
import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.common.dto.AuctionRealtimeEvent;
import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
import com.auctionapp.auctionappjava.common.dto.BidHistoryChartResponse;
import com.auctionapp.auctionappjava.common.dto.BidHistoryPointDto;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
=======
import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
<<<<<<< HEAD
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.formatMoney;

public class RankingListController {

    @FXML
    private Button btnBack;
    @FXML
    private Button btnExportWinner;
    @FXML
    private TableColumn<BidRankingRow, String> colBidAmount;
    @FXML
    private TableColumn<BidRankingRow, String> colBidTime;
    @FXML
    private TableColumn<BidRankingRow, String> colRank;
    @FXML
    private TableColumn<BidRankingRow, String> colUsername;
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
    private Label lblDescription;
    @FXML
    private TableView<BidRankingRow> tableBidders;
    @FXML
    private LineChart<String, Number> bidLineChart;

    private final ObservableList<BidRankingRow> bidRows = FXCollections.observableArrayList();
    private final XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
    private final Set<String> renderedBidKeys = new HashSet<>();
    private final DateTimeFormatter chartTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter tableTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private AuctionSummaryResponse currentAuction;
    private Consumer<AuctionRealtimeEvent> realtimeListener;
    private boolean realtimeSubscribed;

    @FXML
    void initialize() {
        currentAuction = AuctionSession.getInstance().getCurrentAuction();
        setupTable();
        setupPriceChart();
        installCloseHook();

        if (currentAuction == null) {
            showEmptyAuctionState();
            return;
        }

        renderAuctionHeader(currentAuction);
        loadHistoryThenSubscribe(currentAuction);
    }

    private void setupTable() {
        colRank.setCellValueFactory(cellData -> cellData.getValue().rankProperty());
        colUsername.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        colBidAmount.setCellValueFactory(cellData -> cellData.getValue().amountProperty());
        colBidTime.setCellValueFactory(cellData -> cellData.getValue().bidTimeProperty());
        tableBidders.setItems(bidRows);
        tableBidders.setPlaceholder(new Label("Chưa có lượt đấu giá"));
    }

    private void setupPriceChart() {
        if (bidLineChart == null) return;
        priceSeries.setName("Giá cao nhất");
        bidLineChart.setAnimated(false);
        bidLineChart.setCreateSymbols(true);
        bidLineChart.getData().clear();
        bidLineChart.getData().add(priceSeries);
    }

    private void installCloseHook() {
        Platform.runLater(() -> {
            if (btnBack != null && btnBack.getScene() != null && btnBack.getScene().getWindow() instanceof Stage stage) {
                stage.setOnCloseRequest(event -> unsubscribeRealtime());
            }
        });
    }

    private void showEmptyAuctionState() {
        lblStartingPrice.setText("0");
        lblTopBid.setText("0");
        lblMinIncrement.setText("0");
        lblCategory.setText("Không có dữ liệu");
        lblItemName.setText("Không có auction đang chọn");
        lblStatus.setText("-");
        lblTopBidder.setText("Chưa có");
        if (lblDescription != null) {
            lblDescription.setText("");
        }
    }

    private void renderAuctionHeader(AuctionSummaryResponse auction) {
        lblStartingPrice.setText(formatMoney(auction.startPrice()));
        lblTopBid.setText(formatMoney(auction.currentPrice()));
        lblMinIncrement.setText(formatMoney(auction.minimumIncrement()));
        lblCategory.setText(auction.category());
        lblItemName.setText(auction.itemName());
        lblStatus.setText(String.valueOf(auction.status()));
        lblTopBidder.setText("Chưa có");
        if (lblDescription != null) {
            lblDescription.setText(auction.description() == null ? "" : auction.description());
        }
    }

    /**
     * Không dùng sample/fake data: màn BXH luôn gọi GET_BID_HISTORY để lấy bid thật từ DB.
     * Gửi GET_BID_HISTORY và SUBSCRIBE_AUCTION tuần tự để tránh lẫn response trên socket.
     */
    private void loadHistoryThenSubscribe(AuctionSummaryResponse auction) {
        CompletableFuture.supplyAsync(() -> {
            try {
                UUID auctionId = UUID.fromString(auction.auctionId());
                Response historyResponse = Client.getInstance().sendRequest(new Request("GET_BID_HISTORY", auctionId));

                if (historyResponse.success()) {
                    registerRealtimeListener();
                    Response subscribeResponse = Client.getInstance().sendRequest(new Request("SUBSCRIBE_AUCTION", auctionId));
                    realtimeSubscribed = subscribeResponse.success();
                }

                return historyResponse;
            } catch (Exception e) {
                return new Response(false, "Không tải được BXH: " + e.getMessage(), null);
            }
        }).thenAccept(response -> Platform.runLater(() -> {
            if (!response.success() || !(response.data() instanceof BidHistoryChartResponse history)) {
                tableBidders.setPlaceholder(new Label(response.message() == null ? "Không tải được lịch sử bid" : response.message()));
                return;
            }
            renderBidHistory(history);
        }));
    }

    private void renderBidHistory(BidHistoryChartResponse history) {
        bidRows.clear();
        priceSeries.getData().clear();
        renderedBidKeys.clear();

        if (history.points() == null) {
            return;
        }

        history.points().stream()
                .sorted(Comparator.comparing(point -> point.bidTime() == null ? LocalDateTime.MIN : point.bidTime()))
                .forEach(this::addBidHistoryPoint);
    }

    private void addBidHistoryPoint(BidHistoryPointDto point) {
        if (point == null || point.amount() == null) return;
        String key = buildBidKey(point.bidId(), point.auctionId(), point.bidderId(), point.amount(), point.bidTime());
        if (!renderedBidKeys.add(key)) return;

        String bidderName = point.bidderName() == null || point.bidderName().isBlank()
                ? "Không rõ"
                : point.bidderName();
        if (point.autoBid()) {
            bidderName += " (Auto)";
        }

        String tableTime = formatTableTime(point.bidTime());
        String chartTime = formatChartTime(point.bidTime());
        String amountText = formatMoney(point.amount());

        bidRows.add(new BidRankingRow(
                String.valueOf(bidRows.size() + 1),
                bidderName,
                amountText,
                tableTime
        ));
        priceSeries.getData().add(new XYChart.Data<>(chartTime, point.amount()));

        lblTopBid.setText(amountText);
        lblTopBidder.setText(bidderName);
    }

    private void registerRealtimeListener() {
        if (realtimeListener != null) return;
        realtimeListener = this::handleRealtimeEvent;
        Client.getInstance().addRealtimeListener(realtimeListener);
    }

    private void handleRealtimeEvent(AuctionRealtimeEvent event) {
        if (event == null || currentAuction == null || event.auctionId() == null) return;
        if (!event.auctionId().toString().equals(currentAuction.auctionId())) return;

        Platform.runLater(() -> {
            switch (event.type()) {
                case AuctionRealtimeEvent.BID_PLACED -> handleBidPlaced(event);
                case AuctionRealtimeEvent.AUCTION_EXTENDED -> handleAuctionExtended(event);
                case AuctionRealtimeEvent.AUCTION_FINISHED -> handleAuctionFinished(event);
                default -> { }
            }
        });
    }

    private void handleBidPlaced(AuctionRealtimeEvent event) {
        BigDecimal amount = event.bidAmount() != null ? event.bidAmount() : event.currentPrice();
        if (amount == null) return;

        LocalDateTime bidTime = event.bidTime() == null ? LocalDateTime.now() : event.bidTime();
        BidHistoryPointDto point = new BidHistoryPointDto(
                event.bidId(),
                event.auctionId(),
                event.bidderId(),
                event.bidderName(),
                amount,
                bidTime,
                event.autoBid()
        );
        addBidHistoryPoint(point);

        if (event.currentPrice() != null) {
            lblTopBid.setText(formatMoney(event.currentPrice()));
        }
        if (event.bidderName() != null && !event.bidderName().isBlank()) {
            lblTopBidder.setText(event.bidderName() + (event.autoBid() ? " (Auto)" : ""));
        }
        updateAuctionSession(event.currentPrice(), event.newEndTime(), null);
    }

    private void handleAuctionExtended(AuctionRealtimeEvent event) {
        updateAuctionSession(null, event.newEndTime(), null);
    }

    private void handleAuctionFinished(AuctionRealtimeEvent event) {
        lblStatus.setText(String.valueOf(AuctionStatus.FINISHED));
        updateAuctionSession(event.currentPrice(), event.newEndTime(), AuctionStatus.FINISHED);
    }

    private void updateAuctionSession(BigDecimal newPrice, LocalDateTime newEndTime, AuctionStatus newStatus) {
        AuctionSummaryResponse old = AuctionSession.getInstance().getCurrentAuction();
        if (old == null) return;
        AuctionSummaryResponse updated = new AuctionSummaryResponse(
                old.auctionId(),
                old.category(),
                old.itemName(),
                old.sellerName(),
                old.description(),
                old.startPrice(),
                newPrice != null ? newPrice : old.currentPrice(),
                old.minimumIncrement(),
                old.startDateTime(),
                newEndTime != null ? newEndTime : old.endDateTime(),
                old.timeLeft(),
                newStatus != null ? newStatus : old.status(),
                old.bidderCount(),
                old.imageData()
        );
        AuctionSession.getInstance().setCurrentAuction(updated);
        currentAuction = updated;
    }

    private void unsubscribeRealtime() {
        if (realtimeListener != null) {
            Client.getInstance().removeRealtimeListener(realtimeListener);
            realtimeListener = null;
        }

        AuctionSummaryResponse auction = currentAuction;
        if (realtimeSubscribed && auction != null) {
            realtimeSubscribed = false;
            CompletableFuture.runAsync(() -> {
                try {
                    Client.getInstance().sendRequest(new Request("UNSUBSCRIBE_AUCTION", UUID.fromString(auction.auctionId())));
                } catch (Exception ignored) {
                }
            });
        }
    }

    private String buildBidKey(UUID bidId, UUID auctionId, UUID bidderId, BigDecimal amount, LocalDateTime bidTime) {
        if (bidId != null) {
            return bidId.toString();
        }
        return String.valueOf(auctionId) + "|" + String.valueOf(bidderId) + "|" + amount + "|" + String.valueOf(bidTime);
    }

    private String formatChartTime(LocalDateTime time) {
        return time == null ? "?" : time.format(chartTimeFormatter);
    }

    private String formatTableTime(LocalDateTime time) {
        return time == null ? "?" : time.format(tableTimeFormatter);
=======

public class RankingListController {

    private BigDecimal best = AuctionSession.getInstance().getCurrentAuction().currentPrice();
    private BigDecimal minIncrement = AuctionSession.getInstance().getCurrentAuction().minimumIncrement();
    private BigDecimal start = AuctionSession.getInstance().getCurrentAuction().startPrice();
    //private String bestBidder =

    @FXML
    private Button btnBack;

    @FXML
    private Button btnExportWinner;

    @FXML
    private TableColumn<?, ?> colBidAmount;

    @FXML
    private TableColumn<?, ?> colBidTime;

    @FXML
    private TableColumn<?, ?> colRank;

    @FXML
    private TableColumn<?, ?> colUsername;

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
    private TableView<?> tableBidders;

    @FXML
    void initialize() {
        lblStartingPrice.setText(start.toPlainString());
        lblTopBid.setText(best.toPlainString());
        lblMinIncrement.setText(minIncrement.toPlainString());
        lblCategory.setText(AuctionSession.getInstance().getCurrentAuction().category());
        lblItemName.setText(AuctionSession.getInstance().getCurrentAuction().itemName());
        lblStatus.setText(AuctionSession.getInstance().getCurrentAuction().status().toString());
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
    }

    @FXML
    void handleRemove(ActionEvent event) {
        if (LoginController.adminRoute) {
            // force-remove
        } else if (LoginController.sellerRoute) {
<<<<<<< HEAD
            // xử lý xóa theo quyền seller nếu cần
=======
            // if (Item.status == "OPEN") {
            // remove
            // } else {
            // { báo lỗi: Không đủ thẩm quyền xóa sản phẩm }
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
        }
    }

    @FXML
    void handleBack(ActionEvent event) throws IOException {
<<<<<<< HEAD
        unsubscribeRealtime();
        if (LoginController.bidderRoute) {
            SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml", "Thông tin sản phẩm");
=======
        if (LoginController.bidderRoute) {
            SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml", "Thông tin sản phẩm");

>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
        } else {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    void handleBidders(ActionEvent event) {
<<<<<<< HEAD
        // Có thể bổ sung màn quản lý bidder sau nếu cần.
=======
        // TODO: Quản lý Bidders trong sản phẩm
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
    }

    @FXML
    void handleExportWinner(ActionEvent event) {
<<<<<<< HEAD
        // Có thể bổ sung chức năng xuất thông báo người thắng sau nếu cần.
    }

    public static final class BidRankingRow {
        private final StringProperty rank;
        private final StringProperty username;
        private final StringProperty amount;
        private final StringProperty bidTime;

        private BidRankingRow(String rank, String username, String amount, String bidTime) {
            this.rank = new SimpleStringProperty(rank);
            this.username = new SimpleStringProperty(username);
            this.amount = new SimpleStringProperty(amount);
            this.bidTime = new SimpleStringProperty(bidTime);
        }

        public StringProperty rankProperty() {
            return rank;
        }

        public StringProperty usernameProperty() {
            return username;
        }

        public StringProperty amountProperty() {
            return amount;
        }

        public StringProperty bidTimeProperty() {
            return bidTime;
        }
    }
=======
        // TODO: Xuất ra danh sách người thắng (Optional)
    }

>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
}
