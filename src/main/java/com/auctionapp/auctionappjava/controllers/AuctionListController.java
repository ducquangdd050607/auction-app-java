package com.auctionapp.auctionappjava.controllers;

import com.auctionapp.auctionappjava.models.Item;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuctionListController implements Initializable {
    private Stage stage;
    private Parent root;
    private Scene scene;

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
    private TableColumn<Item, Double> clmStartPrice;
    @FXML
    private TableColumn<Item, String> clmStatus;
    @FXML
    private TableColumn<Item, Integer> clmTime;
    @FXML
    private TableColumn<Item, String> clmType;
    @FXML
    private TableView<Item> listAuctions;
    @FXML
    private TableColumn<?, ?> clmChoose;
    @FXML
    private TextField txtSearch;


    @FXML
    void handleSearch(ActionEvent event) {

    }

    @FXML
    void handleOpenAdminScreen(ActionEvent event) {

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
    void handleConfirm(ActionEvent event) throws IOException{
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
    void handleAdd(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/auctionapp/auctionappjava/views/AddItemScreen.fxml"));
        stage = new Stage();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void handleSelectAuction(MouseEvent event) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) { //lọc và kiểm tra kiểu người dùng - đưa ra các btn tương ứng
        String[] statuses = {"MỞ", "ĐANG DIỄN RA", "KẾT THÚC", "ĐÃ TRẢ TIỀN/HỦY"}; //trạng thái
        cbFilterStatus.getItems().addAll(statuses);

        String[] sorts = {"TÊN", "GÍA TIỀN", "THỜI GIAN", "XU HƯỚNG(?)"};// sort
        cbSort.getItems().addAll(sorts);

        String[] type = {};//manual-added
        cbType.getItems().addAll(type);

        try {
            show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

        } else if (Route.bidderRoute) {
            box.setVisible(false);
            box.setManaged(false);
        }
    }
}