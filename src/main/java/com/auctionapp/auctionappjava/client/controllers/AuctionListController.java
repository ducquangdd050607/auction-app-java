package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.exception.AppException;
import com.auctionapp.auctionappjava.common.util.AlertUtils;
import com.auctionapp.auctionappjava.common.util.SceneSwitcherUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.auctionapp.auctionappjava.client.controllers.NavigatorController.modeName;
import static com.auctionapp.auctionappjava.common.util.MoneyUtils.formatPriceColumn;

public class AuctionListController implements Initializable {

    private final ObservableList<AuctionSummaryResponse> auctionData = FXCollections.observableArrayList();
    private boolean removeAuction = false;

    @FXML
    private HBox box;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnConfirm;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnRemove;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnReload;
    @FXML
    private ComboBox<String> cbFilterStatus;
    @FXML
    private ComboBox<String> cbType;
    @FXML
    private TableView<AuctionSummaryResponse> listAuctions;
    @FXML
    private TableColumn<AuctionSummaryResponse, String> clmName;
    @FXML
    private TableColumn<AuctionSummaryResponse, String> clmType;
    @FXML
    private TableColumn<AuctionSummaryResponse, BigDecimal> clmStartPrice;
    @FXML
    private TableColumn<AuctionSummaryResponse, BigDecimal> clmCurrentPrice;
    @FXML
    private TableColumn<AuctionSummaryResponse, BigDecimal> clmMinIncrement;
    @FXML
    private TableColumn<AuctionSummaryResponse, Integer> clmBidders;
    @FXML
    private TableColumn<AuctionSummaryResponse, String> clmStatus;
    @FXML
    private TableColumn<AuctionSummaryResponse, String> clmTime; // Thời gian còn lại
    @FXML
    private TableColumn<AuctionSummaryResponse, ?> clmBiddingMoney;
    @FXML
    private TableColumn<AuctionSummaryResponse, ?> clmBiddedTime; // Thời điểm đặt
    @FXML
    private TextField txtSearch;
    @FXML
    private Label txtVersatile;

    public static AuctionListController instance;

    // Thêm danh sách bọc ngoài dùng để LỌC (FilteredList)
    private FilteredList<AuctionSummaryResponse> filteredData;

    // Khai báo thêm đồng hồ
    private Timeline countdownTimer;

    @FXML
    void handleSearch(ActionEvent event) {
        // Lấy điều kiện lọc
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        String selectedStatus = cbFilterStatus.getValue();
        String selectedCategory = cbType.getValue();

        // Cập nhật điều kiện lọc cho FilteredList
        filteredData.setPredicate(auction -> {
            // 1. Khớp Tên
            boolean matchName = keyword.isEmpty() ||
                    (auction.itemName() != null && auction.itemName().toLowerCase().contains(keyword));

            // 2. Khớp Trạng thái (Cần map tiếng Việt với Enum AuctionStatus của bạn)
            boolean matchStatus = false;
            if (selectedStatus == null || selectedStatus.equals("Tất cả trạng thái")) {
                matchStatus = true;
            } else {
                // Tùy biến chỗ này theo chữ bạn hiển thị. Ví dụ:
                String translatedStatus = "";
                if (auction.status() != null) {
                    translatedStatus = switch (auction.status()) {
                        case OPEN -> "Đã mở";
                        case RUNNING -> "Đang diễn ra";
                        case FINISHED -> "Đã kết thúc";
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
                String serverCategory = switch (selectedCategory) {
                    case "Nghệ thuật" -> "ART";
                    case "Điện tử" -> "ELECTRONICS";
                    case "Phương tiện" -> "VEHICLE";
                    default -> "";
                };
                matchCategory = auction.category() != null &&
                        auction.category().equalsIgnoreCase(serverCategory);
            }

            // Chỉ hiện những dòng thỏa mãn cả 3 điều kiện
            return matchName && matchStatus && matchCategory;
        });
    }

    @FXML
    void handleRemove(ActionEvent event) throws IOException {
        // bật lên btn checkbox và xác nhận, chọn và xóa (admin)
        btnRemove.setDisable(true);
        removeBehaviour(true);

        Runnable enableRemove = () -> {
            removeAuction = true;
        };
        AlertUtils.AnnouncementController(
                "CẢNH BÁO!",
                "PHIÊN ĐẤU SẼ BỊ XÓA - Chọn phiên muốn xóa bằng cách bấm đúp vào phiên.",
                enableRemove,
                null);
    }

    @FXML
    void handleCancel(ActionEvent event) throws IOException {
        // hủy và khôi phục trạng thái ban đầu sau khi xóa (admin)
        btnRemove.setDisable(false);
        removeBehaviour(false);
        removeAuction = false;
    }

    @FXML
    void handleConfirm(ActionEvent event) throws IOException { //WIP

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Chắc chưa?");
        alert.setHeaderText("Bạn có muốn xóa sản phẩm không?");

        alert.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {
                removeBehaviour(false);
                alert.close();
                btnRemove.setDisable(false);
            } else {
                //xử lý hủy(chắc chỉ thế này)
                alert.close();
                btnRemove.setDisable(false);
            }
        });
    }

    @FXML
    void handleReload(ActionEvent event) throws IOException {

    }


    @FXML
    void handleAdd(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auctionapp/auctionappjava/views/AddItemScreen.fxml"));
        Parent root = loader.load();
        AddItemController addItemController = loader.getController();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.showAndWait();

        if (addItemController.isAddedSuccess()) {
            loadAuctionsFromServer();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;

        //lọc và kiểm tra kiểu người dùng - đưa ra các btn tương ứng
        String[] statuses = {"Tất cả trạng thái", "Đã mở", "Đang diễn ra", "Kết thúc"};
        cbFilterStatus.getItems().addAll(statuses);
        cbFilterStatus.setValue("Tất cả trạng thái"); // Chọn mặc định

        String[] type = {"Tất cả thể loại", "Nghệ thuật", "Điện tử", "Phương tiện"};
        cbType.getItems().addAll(type);
        cbType.setValue("Tất cả thể loại"); // Chọn mặc định

        try {
            show();// kiểm tra kiểu người dùng
            setMode(modeName);// thay đổi trong AutionListScreen
        } catch (IOException e) {
            throw new AppException("Không thể khởi tạo danh sách đấu giá", e);
        }

        // Khởi tạo các cột và set data vào bảng
        setupColumns();

        // 2. BỌC DỮ LIỆU BẰNG FILTERED LIST VÀ SORTER LIST
        filteredData = new FilteredList<>(auctionData, p -> true); // Ban đầu hiển thị tất cả
        SortedList<AuctionSummaryResponse> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(listAuctions.comparatorProperty()); // Để bảng có thể bấm sort theo cột

        // Gắn dữ liệu đã được bọc vào bảng thay vì auctionData gốc
        listAuctions.setItems(sortedData);

        // Tự động kéo dữ liệu từ mạng khi mở màn hình
        loadAuctionsFromServer();

        // Cho phép Double-click truy cập sản phẩm
        setupRowDoubleClick();

        // Cứ mỗi 1 giây, đồng hồ sẽ yêu cầu cái bảng vẽ lại giao diện 1 lần
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            listAuctions.refresh();
        }));
        countdownTimer.setCycleCount(Animation.INDEFINITE);
        countdownTimer.play();
    }

    // Tắt đồng hồ khi tắt màn hình để giải phóng RAM
    public void stopTimer() {
        if (countdownTimer != null) countdownTimer.stop();
    }

    // Luồng xử lý ngầm gọi Server
    private void loadAuctionsFromServer() {
        // Khóa search trong lúc loading
        txtSearch.setDisable(true);
        cbFilterStatus.setDisable(true);
        cbType.setDisable(true);
        btnSearch.setDisable(true);
        btnReload.setDisable(true);

        // Thêm vòng tròn loading trong lúc đợi lấy data từ server
        ProgressIndicator loadingSpinner = new ProgressIndicator();
        loadingSpinner.setMaxSize(50, 50);
        listAuctions.setPlaceholder(loadingSpinner);
        auctionData.clear();

        Request req = new Request("GET_ALL_AUCTIONS", null);
        if ((modeName.equals("Danh sách đấu giá")) || (modeName.equals("Quản lý phiên đấu giá"))) {
            req = new Request("GET_ALL_AUCTIONS", null);
        } else if (modeName.equals("Quản lý vật phẩm")) {
            ManagerAndHistoryRequest bidReq = new ManagerAndHistoryRequest(UserSession.getInstance().getCurrentUser().id().toString());
            req = new Request("GET_ALL_UPLOADED_AUCTIONS", bidReq);
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
                // Mở laại search khi dữ liệu được trả về
                txtSearch.setDisable(false);
                cbFilterStatus.setDisable(false);
                cbType.setDisable(false);
                btnSearch.setDisable(false);
                btnReload.setDisable(false);

                if (response.success()) {
                    // Ép kiểu lấy danh sách từ Response
                    List<AuctionSummaryResponse> listFromServer = (List<AuctionSummaryResponse>) response.data();

                    // Xóa dữ liệu cũ và cập nhật dữ liệu mới vào bảng
                    auctionData.setAll(listFromServer);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, response.message());
                    alert.show();
                }

                // Luôn luôn cất vòng xoay đi và thay bằng nhãn chữ này
                Label noDataLabel = new Label("Không tìm thấy phiên đấu giá nào khớp yêu cầu.");
                noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray; ");
                listAuctions.setPlaceholder(noDataLabel);
            });
        });
    }

    private void handleRemoveAuction(AuctionSummaryResponse auction) throws IOException {

        RemoveAuctionRequest removeReq = new RemoveAuctionRequest(
                UserSession.getInstance().getCurrentUser().id(),
                auction.auctionId());
        Request req = new Request("REMOVE_AUCTION", removeReq);

        CompletableFuture.supplyAsync(() -> {
            try {
                return Client.getInstance().sendRequest(req);
            } catch (Exception e) {
                e.printStackTrace();
                return new Response(false, "Lỗi kết nối Server", null);
            }
        }).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.success()) {
                    // XÓA LUÔN TRÊN MÀN HÌNH
                    auctionData.remove(auction);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, response.message());
                    alert.show();
                }

                if (auctionData.isEmpty()) {
                    Label noDataLabel = new Label("Hiện tại chưa có phiên đấu giá nào.");
                    noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
                    listAuctions.setPlaceholder(noDataLabel);
                }
            });
        });
    }

    private void setupColumns() {
        // Dùng SimpleStringProperty cho các cột chứa chuỗi (String)
        clmName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().itemName()));
        clmName.setStyle("-fx-alignment: CENTER-LEFT;");

        clmType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().category()));

        // Tính lại thời gian để set status
        if (clmStatus != null) {
            clmStatus.setCellValueFactory(cell -> {
                var auction = cell.getValue();
                String displayStatus = auction.status() != null ? auction.status().name() : "UNKNOWN";

                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime startTime = LocalDateTime.parse(auction.startDateTime(), formatter);
                    LocalDateTime endTime = LocalDateTime.parse(auction.endDateTime(), formatter);

                    // Phán đoán trạng thái Lạc quan dựa trên mốc thời gian
                    if (now.isBefore(startTime)) {
                        displayStatus = "OPEN"; // Hoặc chữ "MỞ" tùy bạn hiển thị
                    } else if (!now.isBefore(startTime) && now.isBefore(endTime)) {
                        displayStatus = "RUNNING"; // Hoặc "ĐANG DIỄN RA"
                    } else {
                        displayStatus = "FINISHED"; // Hoặc "KẾT THÚC"
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    displayStatus = "ERROR";
                }

                return new SimpleStringProperty(displayStatus);
            });
        }

        // Tương tự status, nhưng thêm việc refresh mỗi 1s đếm giờ
        if (clmTime != null) {
            clmTime.setCellValueFactory(cell -> {
                var auction = cell.getValue();
                String displayTime = "Không xác định";

                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime startTime = LocalDateTime.parse(auction.startDateTime(), formatter);
                    LocalDateTime endTime = LocalDateTime.parse(auction.endDateTime(), formatter);

                    // Pha 1: Chưa tới giờ mở -> Đếm ngược đến lúc Mở
                    if (now.isBefore(startTime)) {
                        displayTime = "Diễn ra sau " + formatDuration(java.time.Duration.between(now, startTime));
                    }
                    // Pha 2: Đã mở nhưng chưa kết thúc -> Đếm ngược đến lúc Kết thúc
                    else if (!now.isBefore(startTime) && now.isBefore(endTime)) {
                        displayTime = formatDuration(java.time.Duration.between(now, endTime));
                    }
                    // Pha 3: Vượt qua giờ kết thúc -> Khóa sổ
                    else {
                        displayTime = "Đã kết thúc";
                    }
                } catch (Exception e) {
                    displayTime = "Lỗi hiển thị";
                }

                return new SimpleStringProperty(displayTime);
            });
        }

        // Dùng SimpleObjectProperty cho các cột chứa Số (BigDecimal, int...)
        clmStartPrice.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().startPrice()));

        if (clmCurrentPrice != null) {
            clmCurrentPrice.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().currentPrice()));
        }

        if (clmMinIncrement != null) {
            clmMinIncrement.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().minimumIncrement()));
        }

        // Hàm format lại giá tiền trong bảng
        formatPriceColumn(clmStartPrice);
        if (clmCurrentPrice != null) formatPriceColumn(clmCurrentPrice);
        if (clmMinIncrement != null) formatPriceColumn(clmMinIncrement);

        if (clmBidders != null) {
            // Lambda sẽ tự động autoboxing int thành Integer cho TableColumn
            clmBidders.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().bidderCount()));
        }
    }

    public void show() throws IOException {

        // bidder không thêm bỏ sp
        btnAdd.setVisible(false);
        btnAdd.setManaged(false);

        //thêm sp
        btnRemove.setVisible(false);
        btnRemove.setManaged(false);

        // nút xác nhận-hủy-khung chọn chỉ khi bấm remove
        btnConfirm.setVisible(false);
        btnConfirm.setManaged(false);
        btnCancel.setVisible(false);
        btnCancel.setManaged(false);

        //nút admin
        if (LoginController.adminRoute) {
            btnRemove.setVisible(true);
            btnRemove.setManaged(true);

        } else if (LoginController.sellerRoute) {
            btnAdd.setVisible(true);
            btnAdd.setManaged(true);
        }
    }

    public void removeBehaviour(boolean admin) {
        // Hành vi các nút khi thao tác xóa(admin)
        btnConfirm.setVisible(admin);
        btnConfirm.setManaged(admin);
        btnCancel.setVisible(admin);
        btnCancel.setManaged(admin);
    }

    public void setMode(String mode) {

        if (Objects.equals(mode, "Danh sách đấu giá")) {
            txtVersatile.setText("Bét88 Live Auction Services");
        } else if (Objects.equals(mode, "Quản lý vật phẩm")) {
            txtVersatile.setText("Bét88 Items Manager");
        } else if (Objects.equals(mode, "Quản lý phiên đấu giá")) {
            txtVersatile.setText("Bét88 Live Auction Manager");
        }
    }

    private void setupRowDoubleClick() {
        listAuctions.setRowFactory(tv -> {
            TableRow<AuctionSummaryResponse> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    try {
                        AuctionSession.getInstance().setCurrentAuction(row.getItem());
                        if (removeAuction) {
                            Runnable finalWarning = () -> {
                                try {
                                    handleRemoveAuction(row.getItem());
                                } catch (IOException e) {
                                    throw new AppException("Không thể xóa phiên đấu giá", e);
                                }
                            };

                            AlertUtils.ConfirmAlertController(
                                    null,
                                    "CẢNH BÁO!",
                                    "PHIÊN ĐẤU SẼ BỊ XÓA",
                                    "BẠN CÓ MUỐN XÓA PHIÊN ĐẤU NÀY KHÔNG?",
                                    "ĐÃ XONG",
                                    "PHIÊN ĐẤU ĐÃ BỊ XÓA",
                                    "",
                                    finalWarning,
                                    getWarnedView());
                        } else {
                            openAuctionDetail(row.getItem());
                        }
                    } catch (IOException e) {
                        throw new AppException("Không thể mở chi tiết phiên đấu giá", e);
                    }
                }
            });
            return row;
        });
    }

    private void openAuctionDetail(AuctionSummaryResponse auction) throws IOException {
        if (LoginController.bidderRoute) {
            // Bidder
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chi tiết: " + auction.itemName());
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.showAndWait();

            // Update lại danh sách bảng
            AuctionSummaryResponse updatedAuction = AuctionSession.getInstance().getCurrentAuction();
            // Tìm xem cái phiên đấu giá này đang nằm ở dòng thứ mấy trong bảng
            for (int i = 0; i < auctionData.size(); i++) {
                if (auctionData.get(i).auctionId().equals(updatedAuction.auctionId())) {
                    // Tráo đổi dòng cũ bằng dòng mới
                    auctionData.set(i, updatedAuction);
                    break;
                }
            }

            // Sau khi xài xong thì clean AuctionSession
            AuctionSession.getInstance().cleanAuctionSession();
        } else {
            // Seller/Admin
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/auctionapp/auctionappjava/views/RankingListScreen.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("BXH – " + auction.itemName());
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.showAndWait();

        }
    }

    private ImageView getWarnedView() throws IOException {
        Image warningImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/auctionapp/auctionappjava/images/Koconut.png")));
        ImageView warningView = new ImageView(warningImage);
        warningView.setPreserveRatio(true);
        warningView.setFitWidth(80);
        return warningView;
    }

    private String formatDuration(java.time.Duration duration) {
        if (duration.isNegative() || duration.isZero()) return "00:00:00";

        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (days > 0) {
            return String.format("%d ngày %02d:%02d:%02d", days, hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }

    public void refreshListRealtime() {
        Platform.runLater(this::loadAuctionsFromServer);
    }

    // Hàm nhận lệnh từ Router để chỉ cập nhật dòng bị thay đổi
    public void updateSingleRowStatus(UUID auctionId, AuctionStatus newStatus) {
        Platform.runLater(() -> {
            // Quét và cập nhật trên danh sách gốc
            for (int i = 0; i < auctionData.size(); i++) {
                AuctionSummaryResponse currentItem = auctionData.get(i);

                if (currentItem.auctionId().equals(auctionId.toString())) {
                    // Tạo một bản sao mới, chỉ thay đổi status cho phù hợp
                    AuctionSummaryResponse updatedItem = new AuctionSummaryResponse(
                            currentItem.auctionId(), currentItem.category(), currentItem.itemName(),
                            currentItem.sellerName(), currentItem.description(), currentItem.startPrice(),
                            currentItem.currentPrice(), currentItem.minimumIncrement(), currentItem.startDateTime(),
                            currentItem.endDateTime(), currentItem.timeLeft(),
                            newStatus,
                            currentItem.bidderCount(), currentItem.imageData()
                    );

                    // Tráo dòng cũ bằng dòng mới trên danh sách gốc
                    auctionData.set(i, updatedItem);
                    break;
                }
            }
        });
    }

    // Cập nhật giá Real-time cho 1 dòng trên bảng danh sách
    public void updateSingleRowPrice(UUID auctionId, BigDecimal newPrice) {
        Platform.runLater(() -> {
            for (int i = 0; i < auctionData.size(); i++) {
                AuctionSummaryResponse currentItem = auctionData.get(i);

                if (currentItem.auctionId().equals(auctionId.toString())) {
                    // Tạo bản sao mới và ÉP GIÁ MỚI (newPrice) vào
                    AuctionSummaryResponse updatedItem = new AuctionSummaryResponse(
                            currentItem.auctionId(), currentItem.category(), currentItem.itemName(),
                            currentItem.sellerName(), currentItem.description(), currentItem.startPrice(),
                            newPrice, // <--- GIÁ VỪA ĐƯỢC CẬP NHẬT TỪ SERVER
                            currentItem.minimumIncrement(), currentItem.startDateTime(),
                            currentItem.endDateTime(), currentItem.timeLeft(),
                            currentItem.status(), currentItem.bidderCount(), currentItem.imageData()
                    );

                    auctionData.set(i, updatedItem);
                    break;
                }
            }
        });
    }
}
