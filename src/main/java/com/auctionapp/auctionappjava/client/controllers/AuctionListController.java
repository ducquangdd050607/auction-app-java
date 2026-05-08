package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.client.network.Client;
import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class AuctionListController implements Initializable {

    private ObservableList<AuctionSummaryResponse> auctionData = FXCollections.observableArrayList();

    @FXML
    private HBox box;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnAdmin;
    @FXML
    private Button btnConfirm;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnRemove;
    @FXML
    private Button btnTest;
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
    private TableColumn<AuctionSummaryResponse, /*Integer*/String> clmTime; // Thời gian còn lại
    @FXML
    private TableColumn<AuctionSummaryResponse, ?> clmBiddingMoney;
    @FXML
    private TableColumn<AuctionSummaryResponse, ?> clmBiddedTime; // Thời điểm đặt
    @FXML
    private TableColumn<AuctionSummaryResponse, ?> clmChoose;
    @FXML
    private TextField txtSearch;
    @FXML
    private Label txtVersatile;


    @FXML
    void handleSearch(ActionEvent event) {
        // TODO: Tìm kiếm
    }

    @FXML
    void handleOpenAdminScreen(ActionEvent event) {
        // Optional
    }

    @FXML
    void handleRemove(ActionEvent event) throws IOException {
        // bật lên btn checkbox và xác nhận, chọn và xóa (admin)
        removeBehaviour(true);
    }

    @FXML
    void handleCancel(ActionEvent event) throws IOException {
        // hủy và khôi phục trạng thái ban đầu sau khi xóa (admin)
        removeBehaviour(false);
    }

    @FXML
    void handleConfirm(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Chắc chưa?");
        alert.setHeaderText("Bạn có muốn xóa sản phẩm không?");

        alert.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {
                alert.close();

                //xử lý xóa...

                removeBehaviour(false);

            } else {
                //xử lý hủy(chắc chỉ thế này)
                alert.close();

            }
        });
    }

    @FXML
    void handleTest(ActionEvent event) throws IOException {
        if (LoginController.bidderRoute) {
            SceneSwitcherUtils.PopupController(event, "/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml", "Thông tin sản phẩm");
        } else {
            SceneSwitcherUtils.PopupController(event, "/com/auctionapp/auctionappjava/views/InsideItemScreen.fxml", "BXH");
        }
    }

    @FXML
    void handleAdd(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auctionapp/auctionappjava/views/AddItemScreen.fxml"));
        Parent root = loader.load();

//        AddItemController addCtrl = loader.getController();
//        addCtrl.setOnItemAdded(newItem -> {
//            loadAuctionsFromServer();
//        });

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //lọc và kiểm tra kiểu người dùng - đưa ra các btn tương ứng
        String[] statuses = {"MỞ", "ĐANG DIỄN RA", "KẾT THÚC", "ĐÃ TRẢ TIỀN/HỦY"}; //trạng thái
        cbFilterStatus.getItems().addAll(statuses);

        String[] type = {};//manual-added

        cbType.getItems().addAll(type);

        try {
            show();// kiểm tra kiểu người dùng
            setMode(NavigatorController.modeName);// thay đổi trong AutionListScreen
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Khởi tạo các cột và set data vào bảng
        setupColumns();
        listAuctions.setItems(auctionData);

        // Tự động kéo dữ liệu từ mạng khi mở màn hình
        loadAuctionsFromServer();

        // Cho phép Double-click truy cập sản phẩm
        setupRowDoubleClick();
    }

    // Luồng xử lý ngầm gọi Server
    private void loadAuctionsFromServer() {
        Request req = new Request("GET_ALL_AUCTIONS", null);

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
                    // Ép kiểu lấy danh sách từ Response
                    List<AuctionSummaryResponse> listFromServer = (List<AuctionSummaryResponse>) response.data();

                    // Xóa dữ liệu cũ và cập nhật dữ liệu mới vào bảng
                    auctionData.setAll(listFromServer);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, response.message());
                    alert.show();
                }
            });
        });
    }

    private void setupColumns() {
        // Dùng SimpleStringProperty cho các cột chứa chuỗi (String)
        clmName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().itemName()));
        clmType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().category()));

        // Nếu bạn có khai báo các cột này trong FXML, hãy map dữ liệu tương tự
        if (clmStatus != null) {
            clmStatus.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().status())));
        }
        if (clmTime != null) {
            clmTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().timeLeft()));
        }

        // Dùng SimpleObjectProperty cho các cột chứa Số (BigDecimal, int...)
        clmStartPrice.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().startPrice()));

        if (clmCurrentPrice != null) {
            clmCurrentPrice.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().currentPrice()));
        }

        if (clmMinIncrement != null) {
            clmMinIncrement.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().minimumIncrement()));
        }

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

        //nút admin
        btnAdmin.setVisible(false);
        btnAdmin.setManaged(false);

        // nút xác nhận-hủy-khung chọn chỉ khi bấm remove
        btnConfirm.setVisible(false);
        btnConfirm.setManaged(false);
        clmChoose.setVisible(false);
        btnCancel.setVisible(false);
        btnCancel.setManaged(false);

        //nút admin
        if (LoginController.adminRoute) {
            btnAdmin.setVisible(true);
            btnAdmin.setManaged(true);
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
        clmChoose.setVisible(admin);
        btnAdmin.setVisible(!admin);
        btnAdmin.setManaged(!admin);
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

        } else if (Objects.equals(mode, "Lịch sử đấu giá")) {
            txtVersatile.setText("Bét88 History");
            clmBiddedTime.setVisible(true);
            clmBiddingMoney.setVisible(true);
            clmCurrentPrice.setVisible(false);
            clmBidders.setVisible(false);
        }
    }

    private void setupRowDoubleClick() {
        listAuctions.setRowFactory(tv -> {
            TableRow<AuctionSummaryResponse> row = new TableRow<>();

            row.setOnMouseClicked(event -> {

                if (event.getClickCount() == 2 && !row.isEmpty()) {

                    try {
                        openAuctionDetail(row.getItem());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
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

            // Truyền dữ liệu sang màn hình con
            AuctionDetailController ctrl = loader.getController();
            ctrl.setAuction(auction);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chi tiết: " + auction.itemName());
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.showAndWait();

        } else {
            // Seller/Admin
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/auctionapp/auctionappjava/views/InsideItemScreen.fxml"));
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
}