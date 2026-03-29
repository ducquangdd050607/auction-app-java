package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
public class AddItemController {
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

    }

    @FXML
    void handleCancel(ActionEvent event) {

    }
    @FXML
    void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainScreen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
