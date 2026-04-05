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
    private Button btnRemove;
    @FXML
    private ComboBox<String> cbFilterStatus;
    @FXML
    private ComboBox<String> cbSort;
    @FXML
    private ComboBox<String> cbType;
    @FXML
    private TableView<Item> listAuctions;
    @FXML
    private TextField txtSearch;


    @FXML
    void handleSearch(ActionEvent event) {

    }

    @FXML
    void handleOpenAdminScreen(ActionEvent event) {

    }

    @FXML
    void handleRemove(ActionEvent event) {

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
    public void initialize(URL location, ResourceBundle resources) {
        String[] statuses = {"MỞ", "ĐANG DIỄN RA", "KẾT THÚC", "ĐÃ TRẢ TIỀN/HỦY"};
        cbFilterStatus.getItems().addAll(statuses);

        String[] sorts = {"TÊN", "GÍA TIỀN", "THỜI GIAN", "XU HƯỚNG(?)"};
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
        btnAdd.setVisible(false);
        btnAdd.setManaged(false);
        btnRemove.setVisible(false);
        btnRemove.setManaged(false);
        btnAdmin.setVisible(false);
        btnAdmin.setManaged(false);

        if (Route.adminRoute) {
            btnAdmin.setVisible(true);
            btnAdmin.setManaged(true);

        } else if (Route.sellerRoute) {
            btnAdd.setVisible(true);
            btnAdd.setManaged(true);
            btnRemove.setVisible(true);
            btnRemove.setManaged(true);

        } else if (Route.bidderRoute) {
            box.setVisible(false);
            box.setManaged(false);
        }
    }
}