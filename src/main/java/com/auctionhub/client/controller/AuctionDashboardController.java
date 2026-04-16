package com.auctionhub.client.controller;

import com.auctionhub.client.core.ClientContext;
import com.auctionhub.client.service.AlertService;
import com.auctionhub.client.util.FxUtils;
import com.auctionhub.common.dto.ApiEnvelope;
import com.auctionhub.common.dto.AuctionDetailDto;
import com.auctionhub.common.dto.AuctionSummaryDto;
import com.auctionhub.common.dto.AutoBidRequest;
import com.auctionhub.common.dto.BidDto;
import com.auctionhub.common.dto.PlaceBidRequest;
import com.auctionhub.common.enums.AuctionStatus;
import com.auctionhub.common.enums.EventType;
import com.auctionhub.common.util.JacksonSupport;
import com.auctionhub.common.util.MoneyUtils;
import com.auctionhub.common.util.TimeUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AuctionDashboardController {
    @FXML
    private TableView<AuctionSummaryDto> auctionTable;
    @FXML
    private TableColumn<AuctionSummaryDto, String> titleColumn;
    @FXML
    private TableColumn<AuctionSummaryDto, String> typeColumn;
    @FXML
    private TableColumn<AuctionSummaryDto, String> currentPriceColumn;
    @FXML
    private TableColumn<AuctionSummaryDto, String> statusColumn;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilterCombo;
    @FXML
    private ComboBox<String> sortCombo;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Label detailTitleLabel;
    @FXML
    private Label detailMetaLabel;
    @FXML
    private Label sellerLabel;
    @FXML
    private Label leaderLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label winnerLabel;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField bidAmountField;
    @FXML
    private TextField autoMaxBidField;
    @FXML
    private TextField autoIncrementField;
    @FXML
    private Button placeBidButton;
    @FXML
    private Button autoBidButton;
    @FXML
    private TableView<BidDto> bidHistoryTable;
    @FXML
    private TableColumn<BidDto, String> bidTimeColumn;
    @FXML
    private TableColumn<BidDto, String> bidderColumn;
    @FXML
    private TableColumn<BidDto, String> bidAmountColumn;
    @FXML
    private TableColumn<BidDto, String> bidTypeColumn;
    @FXML
    private LineChart<String, Number> priceChart;

    private final ObservableList<AuctionSummaryDto> masterAuctions = FXCollections.observableArrayList();
    private UUID subscribedAuctionId;

    @FXML
    public void initialize() {
        setupTable();
        registerRealtimeListeners();
        statusFilterCombo.setItems(FXCollections.observableArrayList("ALL", "OPEN", "RUNNING", "FINISHED", "PAID", "CANCELED"));
        statusFilterCombo.getSelectionModel().select("ALL");
        sortCombo.setItems(FXCollections.observableArrayList("Sắp hết giờ", "Giá cao nhất", "Tên A-Z"));
        sortCombo.getSelectionModel().selectFirst();
        auctionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                loadAuctionDetail(newValue.auctionId());
            }
        });
        refreshAuctions();
    }

    @FXML
    private void refreshAuctions() {
        setLoading(true);
        try {
            masterAuctions.setAll(ClientContext.getInstance().api().listAuctions());
            applyFilters();
            if (!masterAuctions.isEmpty() && auctionTable.getSelectionModel().getSelectedItem() == null) {
                auctionTable.getSelectionModel().selectFirst();
            }
        } catch (Exception ex) {
            AlertService.error("Load auctions failed", ex.getMessage());
        } finally {
            setLoading(false);
        }
    }

    @FXML
    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String statusFilter = statusFilterCombo.getValue();
        Comparator<AuctionSummaryDto> comparator = switch (sortCombo.getValue()) {
            case "Giá cao nhất" -> Comparator.comparing(AuctionSummaryDto::currentPrice).reversed();
            case "Tên A-Z" -> Comparator.comparing(AuctionSummaryDto::title);
            default -> Comparator.comparingLong(AuctionSummaryDto::secondsRemaining);
        };

        List<AuctionSummaryDto> filtered = masterAuctions.stream()
                .filter(auction -> keyword.isBlank() || auction.title().toLowerCase().contains(keyword) || auction.description().toLowerCase().contains(keyword))
                .filter(auction -> "ALL".equals(statusFilter) || auction.status().name().equals(statusFilter))
                .sorted(comparator)
                .toList();
        auctionTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handlePlaceBid() {
        AuctionSummaryDto selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.error("No auction selected", "Hãy chọn một phiên đấu giá trước.");
            return;
        }
        try {
            AuctionDetailDto detail = ClientContext.getInstance().api().placeBid(
                    new PlaceBidRequest(selected.auctionId(), new BigDecimal(bidAmountField.getText().trim())));
            updateDetail(detail);
            refreshAuctions();
            bidAmountField.clear();
            AlertService.info("Bid thành công", "Giá của bạn đã được ghi nhận.");
        } catch (Exception ex) {
            AlertService.error("Bid thất bại", ex.getMessage());
        }
    }

    @FXML
    private void handleAutoBid() {
        AuctionSummaryDto selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.error("No auction selected", "Hãy chọn một phiên đấu giá trước.");
            return;
        }
        try {
            AuctionDetailDto detail = ClientContext.getInstance().api().configureAutoBid(new AutoBidRequest(
                    selected.auctionId(),
                    new BigDecimal(autoMaxBidField.getText().trim()),
                    new BigDecimal(autoIncrementField.getText().trim())));
            updateDetail(detail);
            refreshAuctions();
            AlertService.info("Auto-bid", "Đã cập nhật auto-bid cho phiên đang chọn.");
        } catch (Exception ex) {
            AlertService.error("Auto-bid thất bại", ex.getMessage());
        }
    }

    private void loadAuctionDetail(UUID auctionId) {
        try {
            if (subscribedAuctionId != null && !subscribedAuctionId.equals(auctionId)) {
                ClientContext.getInstance().api().unsubscribeAuction(subscribedAuctionId);
            }
            AuctionDetailDto detail = ClientContext.getInstance().api().subscribeAuction(auctionId);
            subscribedAuctionId = auctionId;
            updateDetail(detail);
        } catch (Exception ex) {
            AlertService.error("Load auction detail failed", ex.getMessage());
        }
    }

    private void updateDetail(AuctionDetailDto detail) {
        AuctionSummaryDto summary = detail.summary();
        detailTitleLabel.setText(summary.title());
        detailMetaLabel.setText(summary.itemType() + " • " + detail.attributeOne() + " • " + detail.attributeTwo());
        sellerLabel.setText(summary.sellerName());
        leaderLabel.setText(summary.leadingBidderName());
        statusLabel.setText(summary.status().name());
        timeLabel.setText(TimeUtils.formatDisplay(summary.endTime()));
        winnerLabel.setText(detail.winnerName());
        descriptionArea.setText(summary.description() + "/n/n" + detail.statusExplanation());
        placeBidButton.setDisable(!detail.canCurrentUserBid());
        autoBidButton.setDisable(!detail.canCurrentUserBid());
        bidHistoryTable.setItems(FXCollections.observableArrayList(detail.bidHistory()));
        updateChart(detail.bidHistory());
    }

    private void updateChart(List<BidDto> bids) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (BidDto bid : bids) {
            series.getData().add(new XYChart.Data<>(TimeUtils.formatDisplay(bid.createdAt()), bid.amount()));
        }
        priceChart.getData().setAll(series);
    }

    private void registerRealtimeListeners() {
        ClientContext.getInstance().api().onEvent(EventType.BID_PLACED, this::handleRealtimeDetailUpdate);
        ClientContext.getInstance().api().onEvent(EventType.AUCTION_STATUS_CHANGED, this::handleRealtimeDetailUpdate);
        ClientContext.getInstance().api().onEvent(EventType.AUCTION_UPDATED, this::handleRealtimeDetailUpdate);
    }

    private void handleRealtimeDetailUpdate(ApiEnvelope event) {
        AuctionDetailDto detail = JacksonSupport.convert(event.getPayload(), AuctionDetailDto.class);
        FxUtils.runOnUiThread(() -> {
            if (subscribedAuctionId != null && subscribedAuctionId.equals(detail.summary().auctionId())) {
                updateDetail(detail);
            }
            refreshAuctions();
        });
    }

    private void setupTable() {
        titleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().title()));
        typeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().itemType().name()));
        currentPriceColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(MoneyUtils.format(cell.getValue().currentPrice())));
        statusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().status().name()));

        bidTimeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(TimeUtils.formatDisplay(cell.getValue().createdAt())));
        bidderColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().bidderName()));
        bidAmountColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(MoneyUtils.format(cell.getValue().amount())));
        bidTypeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().autoGenerated() ? "AUTO" : "MANUAL"));

        auctionTable.setPlaceholder(new Label("Chưa có phiên đấu giá nào để hiển thị."));
        bidHistoryTable.setPlaceholder(new Label("Chưa có lịch sử bid."));
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
    }
}
