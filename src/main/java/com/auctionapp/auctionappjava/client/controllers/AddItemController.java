package com.auctionapp.auctionappjava.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AddItemController implements Initializable {
    private Stage stage;
    private Parent root;
    private Scene scene;
    @FXML
    private ComboBox<?> cbCategory;

    @FXML
    private Label lblError;

    @FXML
    private Label lblExtraInfo;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtEndDate;

    @FXML
    private TextField txtExtraInfo;

    @FXML
    private TextField txtItemName;

    @FXML
    private TextField txtOpenDate;

    @FXML
    private TextField txtStartingPrice;

    @FXML
    void handleAddItem(ActionEvent event) {

        //logic

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();// Đóng cửa sổ
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }











    @FXML
    void handleCancel(ActionEvent event) throws IOException{
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();// Đóng cửa sổ

    }
    @FXML
    void handleBack(ActionEvent event)  {

    }
}
