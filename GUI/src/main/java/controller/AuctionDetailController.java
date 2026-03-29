package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class AuctionDetailController {

    private Stage stage;
    private Parent root;
    private Scene scene;

    @FXML
    private HBox boxBidInput;

    @FXML
    private VBox boxCurrentBid;

    @FXML
    private HBox boxOpenDate;

    @FXML
    private HBox boxSellerActions;

    @FXML
    private Label lblCategory;

    @FXML
    private Label lblCurrentBid;

    @FXML
    private Label lblEndDate;

    @FXML
    private Label lblExtraInfo;

    @FXML
    private Label lblItemName;

    @FXML
    private Label lblNotice;

    @FXML
    private Label lblOpenDate;

    @FXML
    private Label lblStartingPrice;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblTopBidder;

    @FXML
    private TextField txtBidAmount;

    @FXML
    void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/AuctionListScreen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void handlePlaceBid(ActionEvent event) {

    }

    @FXML
    void handleViewBidders(ActionEvent event) {

    }

}
