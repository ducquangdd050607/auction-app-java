package com.auctionapp.auctionappjava.client.controllers;

import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.common.model.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class AuctionListController implements Initializable {
    private Stage stage;
    private Scene scene;

    private ObservableList<Item> auctionData = FXCollections.observableArrayList();

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
    private ComboBox<String> cbSort;
    @FXML
    private ComboBox<String> cbType;
    @FXML
    private TableColumn<Item, Integer> clmBidders;
    @FXML
    private TableColumn<Item, Double> clmCurrentPrice;
    @FXML
    private TableColumn<Item, String> clmName;
    @FXML
    private TableColumn<Item, BigDecimal> clmStartPrice;
    @FXML
    private TableColumn<Item, String> clmStatus;
    @FXML
    private TableColumn<Item, Integer> clmTime; // Thời gian còn lại
    @FXML
    private TableColumn<Item, ItemType> clmType;
    @FXML
    private TableColumn<?, ?> clmBiddingMoney;
    @FXML
    private TableColumn<?, ?> clmBiddedTime; // Thời điểm đặt
    @FXML
    private TableView<Item> listAuctions;
    @FXML
    private TableColumn<?, ?> clmChoose;
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
    void handleRemove(ActionEvent event) throws IOException{
        // bật lên btn checkbox và xác nhận, chọn và xóa (admin)
        btnConfirm.setVisible(true);
        btnConfirm.setManaged(true);
        clmChoose.setVisible(true);
        btnAdmin.setVisible(false);
        btnAdmin.setManaged(false);
        btnCancel.setVisible(true);
        btnCancel.setManaged(true);
    }

    @FXML
    void handleCancel(ActionEvent event) throws IOException{
        // hủy:)) (admin)
        btnConfirm.setVisible(false);
        btnConfirm.setManaged(false);
        clmChoose.setVisible(false);
        btnAdmin.setVisible(true);
        btnAdmin.setManaged(true);
        btnCancel.setVisible(false);
        btnCancel.setManaged(false);
    }

    @FXML
    void handleConfirm(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Chắc chưa?");
        alert.setHeaderText("Bạn có muốn xóa sản phẩm không?");

        alert.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {
                alert.close();

                //xử lý xóa

                btnConfirm.setVisible(false);
                btnConfirm.setManaged(false);
                clmChoose.setVisible(false);
            } else {//xử lý hủy(chắc chỉ thế này)
                alert.close();

            }
        });
    }

    @FXML
    // về sau khi bấm vào sản phẩm sẽ điều hướng đến 
    // chi tiết sản phẩm, nếu trạng thái sp là OPEN, FINISHED, PAID/CANCELLED, điều hướng sang AuctionDetail
    // nếu trạng thái là RUNNING, điều hướng sang InsideItemScreen
    void handleTest(ActionEvent event) throws IOException {
        if (Route.bidderRoute) {
            Parent root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/AuctionDetailScreen.fxml"));
            stage = new Stage(); // hiện tại là (demo), dưới dạng pop-up
            scene = new Scene(root);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        }

        else {
            Parent root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/InsideItemScreen.fxml"));
            stage = new Stage(); // hiện tại là (demo), dưới dạng pop-up
            scene = new Scene(root);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        }
    }

    @FXML
    void handleAdd(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auctionapp/auctionappjava/views/AddItemScreen.fxml"));
        Parent root = loader.load();

        AddItemController addCtrl = loader.getController();
        addCtrl.setOnItemAdded(newItem -> {
            auctionData.add(newItem);
            // auctionData là ObservableList đang bind vào TableView
        });

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }

    @FXML
    void handleSelectAuction(MouseEvent event) {
        // TODO later: Chỉ định sản phẩm/ Truy cập sản phẩm đó
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
            setMode(Navigator.modeName);// thay đổi trong AutionListScreen
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setupColumns();
        listAuctions.setItems(auctionData);

    }

    private void setupColumns() {
        // Các cột nhận giá trị từ các getter
        // Hiện tại chỉ có tên, giá khởi đầu, loại
        clmName.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getTitle()));

        clmStartPrice.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getStartingPrice()));

        clmType.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getItemType()));
    }

    public void show() throws IOException {
        box.setVisible(true);
        box.setManaged(true);
        // bidder không thêm bỏ sp
        btnAdd.setVisible(false);
        btnAdd.setManaged(false);

        //thêm sp
        btnRemove.setVisible(false);
        btnRemove.setManaged(false);

        //xóa sp
        btnAdmin.setVisible(false);
        btnAdmin.setManaged(false);

        // nút xác nhận-hủy-khung chọn chỉ khi bấm remove
        btnConfirm.setVisible(false);
        btnConfirm.setManaged(false);
        clmChoose.setVisible(false);
        btnCancel.setVisible(false);
        btnCancel.setManaged(false);

        //nút admin
        if (Route.adminRoute) {
            btnAdmin.setVisible(true);
            btnAdmin.setManaged(true);
            btnRemove.setVisible(true);
            btnRemove.setManaged(true);

        } else if (Route.sellerRoute) {
            btnAdd.setVisible(true);
            btnAdd.setManaged(true);

        }
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
}