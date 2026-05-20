package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
import com.auctionapp.auctionappjava.common.dto.BidRankingResponse;
import com.auctionapp.auctionappjava.common.dto.ImageRequest;
import com.auctionapp.auctionappjava.common.dto.ImageResponse;
import com.auctionapp.auctionappjava.common.dto.ManagerAndHistoryRequest;
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.CompressionUtils;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.formatMoney;

public class AuctionDetailController {

    @FXML
    private Button btnBack;

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
    private ImageView imgProduct;

    @FXML
    private ProgressIndicator imgSpinner;

    @FXML
    private Label lblNoImage;

    public static AuctionDetailController instance;

    @FXML
    public void initialize() {
        instance = this;

        // Tự động kéo dữ liệu từ session ra mỗi khi mở màn hình
        AuctionSummaryResponse currentAuction = AuctionSession.getInstance().getCurrentAuction();
        if (currentAuction != null) {
            loadAuctionData(currentAuction);
        }

        // Đổi con trỏ chuột thành hình bàn tay khi di chuột vào ảnh cho giống Web
        if (imgProduct != null) {
            imgProduct.setCursor(Cursor.HAND);

            // Bắt sự kiện Click chuột vào ảnh
            imgProduct.setOnMouseClicked((MouseEvent event) -> {
                // Nếu chưa chọn ảnh thì không làm gì cả
                if (imgProduct.getImage() == null) return;

                // 3. Tạo một cửa sổ (Stage) mới để phóng to ảnh
                Stage zoomStage = new Stage();
                zoomStage.initModality(Modality.APPLICATION_MODAL); // Khóa form ở dưới, bắt buộc xem xong mới được quay lại
                zoomStage.setTitle("Xem chi tiết ảnh");

                // 4. Tạo một ImageView mới chứa cùng bức ảnh đó nhưng to hơn
                ImageView zoomedImageView = new ImageView(imgProduct.getImage());
                zoomedImageView.setPreserveRatio(true);

                // Set kích thước tối đa để ảnh không bị tràn màn hình
                zoomedImageView.setFitWidth(800);
                zoomedImageView.setFitHeight(600);

                // 5. Bọc ảnh vào một StackPane để căn giữa
                StackPane root = new StackPane(zoomedImageView);

                // Click vào bất kỳ đâu trên cửa sổ phóng to này sẽ tự động đóng nó lại
                root.setOnMouseClicked(e -> zoomStage.close());

                // Hiển thị lên giữa màn hình
                Scene scene = new Scene(root, 900, 700);
                zoomStage.setScene(scene);
                zoomStage.centerOnScreen();
                zoomStage.showAndWait();
            });
        }
    }

    void loadAuctionData(AuctionSummaryResponse auction) {
        lblItemName.setText(auction.itemName());
        lblCategory.setText(auction.category());
        lblStartingPrice.setText(formatMoney(auction.startPrice()) + " VND");
        lblMinIncrement.setText(formatMoney(auction.minimumIncrement()) + " VND");
        lblCurrentPrice.setText(formatMoney(auction.currentPrice()) + " VND");
        lblStatus.setText(String.valueOf(auction.status()));
        lblEndDate.setText(auction.endDateTime());
        txtDescription.setText(auction.description());
        lblCurrentLeader.setText("Dang tai...");
        loadCurrentLeaderFromRanking(auction.auctionId());

        // Hiện vòng xoay, giấu khung ảnh và chữ đi
        imgSpinner.setVisible(true);
        imgProduct.setVisible(false);
        lblNoImage.setVisible(false);

        // Tạo request lấy ảnh
        Request imgReq = new Request("GET_IMAGE", new ImageRequest(auction.auctionId()));

        CompletableFuture.supplyAsync(() -> {
            try {
                return Client.getInstance().sendRequest(imgReq);
            } catch (Exception e) {
                return null;
            }
        }).thenAccept(response -> {
            Platform.runLater(() -> {
                // Tắt vòng xoay đi
                imgSpinner.setVisible(false);

                if (response != null && response.success() && response.data() != null) {
                    ImageResponse imgRes = (ImageResponse) response.data();
                    byte[] compressedBytes = imgRes.imageData();

                    if (compressedBytes != null && compressedBytes.length > 0) {
                        try {
                            // Giải nén lại ảnh
                            byte[] originalBytes = CompressionUtils.decompress(compressedBytes);

                            // Vẽ lại ảnh lên UI
                            Image realImage = new Image(new ByteArrayInputStream(originalBytes));
                            imgProduct.setImage(realImage);
                            imgProduct.setVisible(true);
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Không có ảnh thì hiện chữ
                imgProduct.setVisible(false);
                lblNoImage.setVisible(true);
            });
        });
    }

    private void loadCurrentLeaderFromRanking(String auctionId) {
        Request rankingReq = new Request("GET_BID_RANKING", new ManagerAndHistoryRequest(auctionId));

        CompletableFuture.supplyAsync(() -> {
            try {
                return Client.getInstance().sendRequest(rankingReq);
            } catch (Exception e) {
                return new Response(false, "Loi ket noi Server", null);
            }
        }).thenAccept(response -> Platform.runLater(() -> {
            if (response == null || !response.success() || !(response.data() instanceof List<?> rows) || rows.isEmpty()) {
                lblCurrentLeader.setText("Chua co");
                return;
            }

            Object firstRow = rows.get(0);
            if (firstRow instanceof BidRankingResponse winner) {
                lblCurrentLeader.setText(winner.bidderName());
            } else {
                lblCurrentLeader.setText("Chua co");
            }
        }));
    }

    @FXML
    void handleBack(ActionEvent event) {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleBidding(ActionEvent event) throws IOException {
        if (AuctionStatus.RUNNING.equals(AuctionStatus.valueOf(lblStatus.getText()))) {
            SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/ConfirmBiddingScreen.fxml", "Đặt cược");
        } else {
            Runnable unableToGamble = () -> {
                btnGamble.setDisable(true);
            };
            AlertUtils.AnnouncementController("oops", "Phiên đấu giá hiện không thể tham gia", unableToGamble, null);
        }
    }

    @FXML
    void handleRanking(ActionEvent event) throws IOException {
        SceneSwitcherUtils.NewSceneController(event, "/com/auctionapp/auctionappjava/views/RankingListScreen.fxml", "Bảng xếp hạng");
    }

    // Thêm hàm này để Router gọi
    public void updatePriceRealtime(BigDecimal newPrice) {
        Platform.runLater(() -> {
            lblCurrentPrice.setText(formatMoney(newPrice) + " VND");
        });
        AuctionSummaryResponse currentAuction = AuctionSession.getInstance().getCurrentAuction();
        if (currentAuction != null) {
            loadCurrentLeaderFromRanking(currentAuction.auctionId());
        }
    }

    // Thêm hàm update status theo thời gian thực
    public void updateStatusRealtime(com.auctionapp.auctionappjava.common.enums.AuctionStatus newStatus) {
        Platform.runLater(() -> {
            lblStatus.setText(newStatus.name());
        });
    }
}
