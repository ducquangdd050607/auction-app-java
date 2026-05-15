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
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
=======
import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
<<<<<<< HEAD
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
=======
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.formatMoney;

public class AuctionDetailController {

    @FXML
    private Button btnBack;
<<<<<<< HEAD
    @FXML
    private Button btnGamble;
    @FXML
    private Button btnRanking;
    @FXML
    private Label lblCategory;
    @FXML
    private Label lblCurrentLeader;
    @FXML
    private Label lblCurrentPrice;
    @FXML
    private Label lblEndDate;
    @FXML
    private Label lblItemName;
    @FXML
    private Label lblMinIncrement;
    @FXML
    private Label lblStartingPrice;
    @FXML
    private Label lblStatus;
    @FXML
    private Label txtDescription;
    @FXML
    private LineChart<String, Number> priceLineChart;

    private final XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
    private final Set<UUID> renderedBidIds = new HashSet<>();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private AuctionSummaryResponse currentAuction;
    private Consumer<AuctionRealtimeEvent> realtimeListener;

    @FXML
    public void initialize() {
        currentAuction = AuctionSession.getInstance().getCurrentAuction();
        setupPriceChart();
        if (currentAuction != null) {
            loadAuctionData(currentAuction);
            loadBidHistoryFromDatabase(currentAuction);
            subscribeRealtime(currentAuction);
        }
    }

    private void setupPriceChart() {
        if (priceLineChart == null) return;
        priceSeries.setName("Giá cao nhất");
        priceLineChart.setAnimated(false);
        priceLineChart.setCreateSymbols(true);
        priceLineChart.getData().clear();
        priceLineChart.getData().add(priceSeries);
    }

=======

    @FXML
    private Button btnGamble;

    @FXML
    private Button btnRanking;

    @FXML
    private Label lblCategory;

    @FXML
    private Label lblCurrentLeader;

    @FXML
    private Label lblCurrentPrice;

    @FXML
    private Label lblEndDate;

    @FXML
    private Label lblItemName;

    @FXML
    private Label lblMinIncrement;

    @FXML
    private Label lblStartingPrice;

    @FXML
    private Label lblStatus;

    @FXML
    private Label txtDescription;

    @FXML
    public void initialize() {
        // Tự động kéo dữ liệu từ session ra mỗi khi mở màn hình
        AuctionSummaryResponse currentAuction = AuctionSession.getInstance().getCurrentAuction();
        if (currentAuction != null) {
            loadAuctionData(currentAuction);
        }
    }

>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
    void loadAuctionData(AuctionSummaryResponse auction) {
        lblItemName.setText(auction.itemName());
        lblCategory.setText(auction.category());
        lblStartingPrice.setText(formatMoney(auction.startPrice()) + " VND");
        lblMinIncrement.setText(formatMoney(auction.minimumIncrement()) + " VND");
        lblCurrentPrice.setText(formatMoney(auction.currentPrice()) + " VND");
        lblStatus.setText(String.valueOf(auction.status()));
<<<<<<< HEAD
        lblEndDate.setText(String.valueOf(auction.endDateTime()));
        lblCurrentLeader.setText("Chưa có dữ liệu realtime");
        txtDescription.setText(auction.description());
    }

    private void loadBidHistoryFromDatabase(AuctionSummaryResponse auction) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return Client.getInstance().sendRequest(new Request("GET_BID_HISTORY", UUID.fromString(auction.auctionId())));
            } catch (Exception e) {
                return new Response(false, "Không tải được lịch sử bid: " + e.getMessage(), null);
            }
        }).thenAccept(response -> Platform.runLater(() -> {
            if (!response.success() || !(response.data() instanceof BidHistoryChartResponse history)) {
                return;
            }
            priceSeries.getData().clear();
            renderedBidIds.clear();
            history.points().stream()
                    .sorted(Comparator.comparing(BidHistoryPointDto::bidTime))
                    .forEach(this::addHistoryPointToChart);
        }));
    }

    private void addHistoryPointToChart(BidHistoryPointDto point) {
        if (point.bidId() != null && !renderedBidIds.add(point.bidId())) return;
        LocalDateTime bidTime = point.bidTime();
        String x = bidTime == null ? "?" : bidTime.format(timeFormatter);
        priceSeries.getData().add(new XYChart.Data<>(x, point.amount()));
    }

    private void subscribeRealtime(AuctionSummaryResponse auction) {
        realtimeListener = this::handleRealtimeEvent;
        Client.getInstance().addRealtimeListener(realtimeListener);
        CompletableFuture.runAsync(() -> {
            try {
                Client.getInstance().sendRequest(new Request("SUBSCRIBE_AUCTION", UUID.fromString(auction.auctionId())));
            } catch (Exception ignored) {
            }
        });
    }

    private void unsubscribeRealtime() {
        AuctionSummaryResponse auction = currentAuction;
        if (auction != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    Client.getInstance().sendRequest(new Request("UNSUBSCRIBE_AUCTION", UUID.fromString(auction.auctionId())));
                } catch (Exception ignored) {
                }
            });
        }
        if (realtimeListener != null) {
            Client.getInstance().removeRealtimeListener(realtimeListener);
            realtimeListener = null;
        }
    }

    private void handleRealtimeEvent(AuctionRealtimeEvent event) {
        if (event == null || currentAuction == null || event.auctionId() == null) return;
        if (!event.auctionId().toString().equals(currentAuction.auctionId())) return;

        Platform.runLater(() -> {
            switch (event.type()) {
                case AuctionRealtimeEvent.BID_PLACED -> handleBidPlacedEvent(event);
                case AuctionRealtimeEvent.AUCTION_EXTENDED -> handleAuctionExtendedEvent(event);
                case AuctionRealtimeEvent.AUCTION_FINISHED -> handleAuctionFinishedEvent(event);
                default -> { }
            }
        });
    }

    private void handleBidPlacedEvent(AuctionRealtimeEvent event) {
        BigDecimal amount = event.bidAmount() != null ? event.bidAmount() : event.currentPrice();
        if (amount != null && (event.bidId() == null || renderedBidIds.add(event.bidId()))) {
            LocalDateTime bidTime = event.bidTime() == null ? LocalDateTime.now() : event.bidTime();
            String x = bidTime.format(timeFormatter);
            priceSeries.getData().add(new XYChart.Data<>(x, amount));
        }
        if (event.currentPrice() != null) {
            lblCurrentPrice.setText(formatMoney(event.currentPrice()) + " VND");
        }
        if (event.bidderName() != null) {
            lblCurrentLeader.setText(event.bidderName() + (event.autoBid() ? " (Auto)" : ""));
        }
        if (event.newEndTime() != null) {
            lblEndDate.setText(String.valueOf(event.newEndTime()));
        }
        updateAuctionSession(event.currentPrice(), event.newEndTime(), null);
    }

    private void handleAuctionExtendedEvent(AuctionRealtimeEvent event) {
        if (event.newEndTime() != null) {
            lblEndDate.setText(String.valueOf(event.newEndTime()));
            updateAuctionSession(null, event.newEndTime(), null);
        }
    }

    private void handleAuctionFinishedEvent(AuctionRealtimeEvent event) {
        lblStatus.setText(String.valueOf(AuctionStatus.FINISHED));
        btnGamble.setDisable(true);
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

    @FXML
    void handleBack(ActionEvent event) {
        unsubscribeRealtime();
=======
        lblEndDate.setText(auction.endDateTime());
        txtDescription.setText(auction.description());
    }

    @FXML
    void handleBack(ActionEvent event) {
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleBidding(ActionEvent event) throws IOException {
        if (AuctionStatus.RUNNING.equals(AuctionStatus.valueOf(lblStatus.getText()))) {
            SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/ConfirmBiddingScreen.fxml", "Đặt cược");
        } else {
<<<<<<< HEAD
            Runnable unableToGamble = () -> btnGamble.setDisable(true);
=======
            Runnable unableToGamble = () -> {
                btnGamble.setDisable(true);
            };
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
            AlertUtils.AnnouncementController("oops", "Phiên đấu giá hiện không thể tham gia", unableToGamble, null);
        }
    }

    @FXML
    void handleRanking(ActionEvent event) throws IOException {
<<<<<<< HEAD
        unsubscribeRealtime();
        SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/RankingListScreen.fxml", "Bảng xếp hạng");
    }
}
=======
        SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/RankingListScreen.fxml", "Bảng xếp hạng");
    }
}
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
